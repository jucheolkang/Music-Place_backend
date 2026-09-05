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
        JsonDeserializer<FollowCountEvent> deserializer = new JsonDeserializer<>(FollowCountEvent.class);
        deserializer.addTrustedPackages("org.musicplace.follow.kafka.event");
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
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }
}
