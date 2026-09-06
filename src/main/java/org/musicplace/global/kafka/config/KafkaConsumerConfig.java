package org.musicplace.global.kafka.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.musicplace.follow.kafka.event.FollowCountEvent;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.MicrometerConsumerListener;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.ExponentialBackOff;

@Slf4j
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, FollowCountEvent> consumerFactory(KafkaProperties props, MeterRegistry meterRegistry) {
        JsonDeserializer<FollowCountEvent> deserializer = new JsonDeserializer<>(FollowCountEvent.class);
        deserializer.addTrustedPackages("org.musicplace.follow.kafka.event");
        deserializer.setUseTypeMapperForKey(false);
        deserializer.setRemoveTypeHeaders(true);
        deserializer.setUseTypeHeaders(false);

        DefaultKafkaConsumerFactory<String, FollowCountEvent> factory = new DefaultKafkaConsumerFactory<>(
                props.buildConsumerProperties(null),
                new StringDeserializer(),
                deserializer
        );
        factory.addListener(new MicrometerConsumerListener<>(meterRegistry));  // ← 이 한 줄만 추가
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, FollowCountEvent> batchKafkaListenerContainerFactory(
            ConsumerFactory<String, FollowCountEvent> consumerFactory,
            KafkaTemplate<String, FollowCountEvent> kafkaTemplate,
            MeterRegistry meterRegistry
    ) {
        ConcurrentKafkaListenerContainerFactory<String, FollowCountEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setBatchListener(true);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        // 1초 → 2초 → 4초... 지수 백오프로 재시도하다가 누적 10초를 넘으면 포기하고 DLT로 전송.
        // maxElapsedTime을 안 정하면 기본값이 사실상 무제한이라 DLT로 영영 안 넘어가니 반드시 설정할 것.
        ExponentialBackOff backOff = new ExponentialBackOff(1000L, 2.0);
        backOff.setMaxElapsedTime(10_000L);

        // Kafka/Kafka client 자체에는 "DLT에 몇 건 쌓였다"는 메트릭이 없어서 직접 셉니다.
        // DeadLetterPublishingRecoverer를 감싸서, 실제로 DLT로 넘어가는 순간마다 카운터를 1 증가시킵니다.
        Counter dltCounter = Counter.builder("follow_count_events_dlt_total")
                .description("follow-count-events에서 최종 실패해 DLT로 전송된 메시지 수")
                .register(meterRegistry);

        DeadLetterPublishingRecoverer dltRecoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
        ConsumerRecordRecoverer countingRecoverer = (record, exception) -> {
            dltCounter.increment();
            dltRecoverer.accept(record, exception);
        };

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(countingRecoverer, backOff);
        errorHandler.setRetryListeners((record, ex, deliveryAttempt) ->
                log.warn("follow-count-events 재시도 {}회째, offset={}, cause={}",
                        deliveryAttempt, record.offset(), ex.getMessage()));
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }
}
