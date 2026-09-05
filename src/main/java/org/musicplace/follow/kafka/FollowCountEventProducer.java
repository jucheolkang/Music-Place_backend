package org.musicplace.follow.kafka;

import lombok.RequiredArgsConstructor;
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

        kafkaTemplate.send("follow-count-events", event.targetId(), kafkaEvent);
    }
}
