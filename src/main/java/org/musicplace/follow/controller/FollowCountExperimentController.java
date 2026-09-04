package org.musicplace.follow.controller;

import lombok.RequiredArgsConstructor;
import org.musicplace.follow.service.FollowCountExperimentService;
import org.musicplace.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class FollowCountExperimentController {

    private final FollowCountExperimentService experimentService;
    private final UserRepository userRepository;

    @PostMapping("/experiment/follow/{strategy}")
    public ResponseEntity<Void> follow(
            @PathVariable String strategy,
            @RequestParam String memberId,
            @RequestParam String targetId) {

        switch (strategy) {
            case "pessimistic" -> experimentService.followWithPessimisticLock(memberId, targetId);
            case "optimistic" -> experimentService.followWithOptimisticLock(memberId, targetId);
            case "batch" -> experimentService.followWithBatchStrategy(memberId, targetId);
            default -> throw new IllegalArgumentException("unknown strategy: " + strategy);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/experiment/follow/{targetId}/recalculate")
    public ResponseEntity<Void> recalculate(@PathVariable String targetId) {
        experimentService.recalculateFollowerCount(targetId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/experiment/follow/{targetId}/reset")
    public ResponseEntity<Void> reset(@PathVariable String targetId) {
        experimentService.resetForTest(targetId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/experiment/follow/{targetId}/count")
    public ResponseEntity<Map<String, Long>> count(@PathVariable String targetId) {
        Long followerCount = userRepository.findByMemberId(targetId)
                .orElseThrow(() -> new IllegalArgumentException("target not found: " + targetId))
                .getFollowerCount();
        return ResponseEntity.ok(Map.of("followerCount", followerCount));
    }
}
