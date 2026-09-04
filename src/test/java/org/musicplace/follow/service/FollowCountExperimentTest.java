package org.musicplace.follow.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.musicplace.follow.repository.FollowRepository;
import org.musicplace.user.domain.Gender;
import org.musicplace.user.domain.UserEntity;
import org.musicplace.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("isolation-test")
class FollowCountExperimentTest {

    @Autowired
    private FollowCountExperimentService experimentService;
    @Autowired
    private FollowRepository followRepository;
    @Autowired
    private UserRepository userRepository;

    private static final String TARGET_ID = "counttest-target";
    private static final int CONCURRENT_FOLLOWERS = 30;

    @BeforeEach
    void setUp() {
        if (!userRepository.existsByMemberId(TARGET_ID)) {
            userRepository.save(UserEntity.builder()
                    .memberId(TARGET_ID)
                    .pw("dummy")
                    .gender(Gender.male)
                    .email("counttest-target@test.com")
                    .nickname("CountTestTarget")
                    .name("CountTestTarget")
                    .role("USER")
                    .build());
        }
    }

    @AfterEach
    void cleanUp() {
        experimentService.resetForTest(TARGET_ID);
    }

    @Test
    void pessimisticLock_동시_30명_팔로우_카운터_정확해야_한다() throws Exception {
        runConcurrentFollows(i -> experimentService.followWithPessimisticLock("pess-follower-" + i, TARGET_ID));

        Long finalCount = userRepository.findByMemberId(TARGET_ID).orElseThrow().getFollowerCount();
        System.out.printf("[비관적 락] 최종 followerCount=%d (기대값=%d)%n", finalCount, CONCURRENT_FOLLOWERS);
        assertThat(finalCount).isEqualTo((long) CONCURRENT_FOLLOWERS);
    }

    @Test
    void optimisticLock_동시_30명_팔로우_재시도로_카운터_정확해야_한다() throws Exception {
        runConcurrentFollows(i -> experimentService.followWithOptimisticLock("opt-follower-" + i, TARGET_ID));

        Long finalCount = userRepository.findByMemberId(TARGET_ID).orElseThrow().getFollowerCount();
        System.out.printf("[낙관적 락] 최종 followerCount=%d (기대값=%d)%n", finalCount, CONCURRENT_FOLLOWERS);
        assertThat(finalCount).isEqualTo((long) CONCURRENT_FOLLOWERS);
    }

    @Test
    void batchStrategy_팔로우_직후엔_카운터_0_재계산후_정확해야_한다() throws Exception {
        runConcurrentFollows(i -> experimentService.followWithBatchStrategy("batch-follower-" + i, TARGET_ID));

        Long beforeRecalc = userRepository.findByMemberId(TARGET_ID).orElseThrow().getFollowerCount();
        long actualFollowCount = followRepository.countByTargetId(TARGET_ID);
        System.out.printf("[배치] 재계산 전 followerCount=%d, 실제 follow row 수=%d%n", beforeRecalc, actualFollowCount);
        assertThat(beforeRecalc).isEqualTo(0L);
        assertThat(actualFollowCount).isEqualTo((long) CONCURRENT_FOLLOWERS);

        experimentService.recalculateFollowerCount(TARGET_ID);
        Long afterRecalc = userRepository.findByMemberId(TARGET_ID).orElseThrow().getFollowerCount();
        System.out.printf("[배치] 재계산 후 followerCount=%d%n", afterRecalc);
        assertThat(afterRecalc).isEqualTo((long) CONCURRENT_FOLLOWERS);
    }

    private void runConcurrentFollows(java.util.function.IntConsumer followAction) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_FOLLOWERS);
        CountDownLatch ready = new CountDownLatch(CONCURRENT_FOLLOWERS);
        CountDownLatch start = new CountDownLatch(1);

        List<? extends Future<?>> futures = IntStream.range(0, CONCURRENT_FOLLOWERS)
                .mapToObj(i -> executor.submit(() -> {
                    ready.countDown();
                    try {
                        start.await();
                        followAction.accept(i);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }))
                .toList();

        ready.await();
        start.countDown();

        for (Future<?> f : futures) {
            try {
                f.get(30, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                throw new RuntimeException(e.getCause());
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
        }
        executor.shutdown();
    }
}
