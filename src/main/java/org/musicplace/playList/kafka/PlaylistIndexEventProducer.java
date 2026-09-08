package org.musicplace.playList.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.musicplace.playList.kafka.event.PlaylistChangedEvent;
import org.musicplace.playList.kafka.event.PlaylistIndexEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlaylistIndexEventProducer {

    private final KafkaTemplate<String, PlaylistIndexEvent> playlistIndexKafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPlaylistChanged(PlaylistChangedEvent event) {
        PlaylistIndexEvent kafkaEvent = new PlaylistIndexEvent(
                UUID.randomUUID().toString(),
                event.playlistId(),
                Instant.now()
        );

        kafkaTemplate().send("playlist-index-events", String.valueOf(event.playlistId()), kafkaEvent)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("playlist-index-events 발행 실패: playlistId={}, eventId={}",
                                event.playlistId(), kafkaEvent.eventId(), ex);
                    }
                });
    }

    private KafkaTemplate<String, PlaylistIndexEvent> kafkaTemplate() {
        return playlistIndexKafkaTemplate;
    }
}
