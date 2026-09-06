package org.musicplace.follow.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.musicplace.follow.kafka.event.FollowChangedEvent;
import org.musicplace.follow.kafka.event.FollowCountEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class FollowCountEventProducer {

    private final KafkaTemplate<String, FollowCountEvent> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFollowChanged(FollowChangedEvent event) {
        FollowCountEvent kafkaEvent = new FollowCountEvent(
                UUID.randomUUID().toString(),
                event.targetId(),
                event.actorMemberId(),
                Instant.now()
        );

        kafkaTemplate.send("follow-count-events", event.targetId(), kafkaEvent)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        // AFTER_COMMIT 시점이라 예외를 던져도 이미 커밋된 HTTP 트랜잭션에는 영향이 없고
                        // 클라이언트는 이미 200을 받은 뒤라 조용히 묻힙니다. 반드시 로그를 남겨서
                        // "팔로우는 DB에 저장됐는데 count 갱신 신호가 안 나간" 상태를 나중에라도 추적 가능하게 합니다.
                        log.error("follow-count-events 발행 실패: targetId={}, eventId={}",
                                event.targetId(), kafkaEvent.eventId(), ex);
                    }
                });
    }
}
