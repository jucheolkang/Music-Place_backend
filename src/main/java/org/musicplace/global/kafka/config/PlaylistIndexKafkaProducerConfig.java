package org.musicplace.global.kafka.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.musicplace.playList.kafka.event.PlaylistIndexEvent;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

@Configuration
public class PlaylistIndexKafkaProducerConfig {

    @Bean
    public ProducerFactory<String, PlaylistIndexEvent> playlistIndexEventProducerFactory(KafkaProperties props) {
        Map<String, Object> configProps = props.buildProducerProperties(null);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, PlaylistIndexEvent> playlistIndexKafkaTemplate(
            ProducerFactory<String, PlaylistIndexEvent> playlistIndexEventProducerFactory
    ) {
        return new KafkaTemplate<>(playlistIndexEventProducerFactory);
    }
}
