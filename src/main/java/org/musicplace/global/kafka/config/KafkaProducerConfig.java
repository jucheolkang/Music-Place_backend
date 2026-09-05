package org.musicplace.global.kafka.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.musicplace.follow.kafka.event.FollowCountEvent;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, FollowCountEvent> followCountEventProducerFactory(KafkaProperties props) {
        Map<String, Object> configProps = props.buildProducerProperties(null);
        // application.yml에 뭐가 잘못 들어있든 상관없이 여기서 확실하게 JsonSerializer로 고정합니다.
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    // 빈 이름을 "kafkaTemplate"으로 두면 스프링 부트가 자동 생성하던 기본 KafkaTemplate을
    // 완전히 대체합니다(KafkaAutoConfiguration의 기본 빈은 @ConditionalOnMissingBean이라 자동으로 물러남).
    @Bean
    public KafkaTemplate<String, FollowCountEvent> kafkaTemplate(
            ProducerFactory<String, FollowCountEvent> followCountEventProducerFactory
    ) {
        return new KafkaTemplate<>(followCountEventProducerFactory);
    }
}
