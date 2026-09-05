package org.musicplace.follow.kafka.event;

import java.time.Instant;

public record FollowCountEvent(
        String eventId,
        String targetId,
        String actorMemberId,
        Instant occurredAt
) {}
