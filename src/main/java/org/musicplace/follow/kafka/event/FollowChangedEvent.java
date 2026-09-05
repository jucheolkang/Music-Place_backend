package org.musicplace.follow.kafka.event;

public record FollowChangedEvent(String targetId, String actorMemberId) {}
