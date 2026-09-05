package org.musicplace.global.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic followCountEventsTopic() {
        return TopicBuilder.name("follow-count-events")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic followCountEventsDlt() {
        return TopicBuilder.name("follow-count-events.DLT")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
