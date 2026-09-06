package org.musicplace.follow.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.musicplace.follow.kafka.event.FollowCountEvent;
import org.musicplace.follow.service.FollowCountTransactionalOps;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class FollowCountConsumer {

    private final FollowCountTransactionalOps followCountTransactionalOps; // 기존 코드 그대로 재사용

    @KafkaListener(
            topics = "follow-count-events",
            groupId = "follow-count-consumer",
            containerFactory = "batchKafkaListenerContainerFactory"
    )
    public void consume(List<ConsumerRecord<String, FollowCountEvent>> records, Acknowledgment ack) {

        // 같은 배치 안에 같은 targetId가 여러 번 나와도 재계산은 한 번만 하면 충분
        Set<String> targetIds = records.stream()
                .map(r -> r.value().targetId())
                .collect(Collectors.toSet());

        for (String targetId : targetIds) {
            // 기존 UserRepository.recalculateFollowerCount(memberId)를 그대로 호출
            followCountTransactionalOps.recalculateFollowerCount(targetId);
        }

        ack.acknowledge();
    }
}
