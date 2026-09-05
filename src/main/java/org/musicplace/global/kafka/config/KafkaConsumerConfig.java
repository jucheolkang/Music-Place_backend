package org.musicplace.global.kafka.config;

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
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.ExponentialBackOff;

@Slf4j
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, FollowCountEvent> consumerFactory(KafkaProperties props) {
        // application.yml의 spring.json.trusted.packages 프로퍼티는 지웠습니다 (6-2 참고).
        // 여기 코드에서만 trustedPackages를 설정하고, YAML에는 이 키를 절대 추가하지 않습니다
        // (둘 다 쓰면 "must be configured with property setters, or via configuration properties; not both" 예외).
        JsonDeserializer<FollowCountEvent> deserializer = new JsonDeserializer<>(FollowCountEvent.class);
        deserializer.addTrustedPackages("org.musicplace.follow.kafka.event");
        // 프로듀서가 메시지에 __TypeId__ 헤더를 같이 실어 보내는데, 기본 설정에서는 컨슈머가
        // 생성자로 지정한 타입보다 이 헤더값을 우선 신뢰합니다. 항상 FollowCountEvent로만 고정해서
        // 헤더 기반 타입 추론 자체를 끄는 게 더 안전합니다.
        deserializer.setUseTypeMapperForKey(false);
        deserializer.setRemoveTypeHeaders(true);
        deserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(
                props.buildConsumerProperties(null),
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, FollowCountEvent> batchKafkaListenerContainerFactory(
            ConsumerFactory<String, FollowCountEvent> consumerFactory,
            KafkaTemplate<String, FollowCountEvent> kafkaTemplate
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

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                new DeadLetterPublishingRecoverer(kafkaTemplate),
                backOff
        );
        errorHandler.setRetryListeners((record, ex, deliveryAttempt) ->
                log.warn("follow-count-events 재시도 {}회째, offset={}, cause={}",
                        deliveryAttempt, record.offset(), ex.getMessage()));
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }
}
