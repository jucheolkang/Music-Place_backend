package org.musicplace.global.kafka.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.musicplace.playList.kafka.event.PlaylistIndexEvent;
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

import java.util.Map;

@Slf4j
@Configuration
public class PlaylistIndexKafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, PlaylistIndexEvent> playlistIndexConsumerFactory(
            KafkaProperties props, MeterRegistry meterRegistry
    ) {
        Map<String, Object> configProps = props.buildConsumerProperties(null);
        // Follow 컨슈머 그룹과 분리 — application.yml의 공통 group-id를 명시적으로 덮어씀
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "search-index-consumer");

        JsonDeserializer<PlaylistIndexEvent> deserializer = new JsonDeserializer<>(PlaylistIndexEvent.class);
        deserializer.addTrustedPackages("org.musicplace.playList.kafka.event");
        deserializer.setUseTypeMapperForKey(false);
        deserializer.setRemoveTypeHeaders(true);
        deserializer.setUseTypeHeaders(false);

        DefaultKafkaConsumerFactory<String, PlaylistIndexEvent> factory = new DefaultKafkaConsumerFactory<>(
                configProps,
                new StringDeserializer(),
                deserializer
        );
        factory.addListener(new MicrometerConsumerListener<>(meterRegistry));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PlaylistIndexEvent> searchIndexKafkaListenerContainerFactory(
            ConsumerFactory<String, PlaylistIndexEvent> playlistIndexConsumerFactory,
            KafkaTemplate<String, PlaylistIndexEvent> playlistIndexKafkaTemplate,
            MeterRegistry meterRegistry
    ) {
        ConcurrentKafkaListenerContainerFactory<String, PlaylistIndexEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(playlistIndexConsumerFactory);
        factory.setBatchListener(true);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        ExponentialBackOff backOff = new ExponentialBackOff(1000L, 2.0);
        backOff.setMaxElapsedTime(10_000L);

        Counter dltCounter = Counter.builder("playlist_index_events_dlt_total")
                .description("playlist-index-events에서 최종 실패해 DLT로 전송된 메시지 수")
                .register(meterRegistry);

        DeadLetterPublishingRecoverer dltRecoverer = new DeadLetterPublishingRecoverer(playlistIndexKafkaTemplate);
        ConsumerRecordRecoverer countingRecoverer = (record, exception) -> {
            dltCounter.increment();
            dltRecoverer.accept(record, exception);
        };

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(countingRecoverer, backOff);
        errorHandler.setRetryListeners((record, ex, deliveryAttempt) ->
                log.warn("playlist-index-events 재시도 {}회째, offset={}, cause={}",
                        deliveryAttempt, record.offset(), ex.getMessage()));
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }
}
