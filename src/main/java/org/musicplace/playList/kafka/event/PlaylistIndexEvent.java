package org.musicplace.playList.kafka.event;

import java.time.Instant;

public record PlaylistIndexEvent(
        String eventId,
        Long playlistId,
        Instant occurredAt
) {}
