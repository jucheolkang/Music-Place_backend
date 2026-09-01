package org.musicplace.follow.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.musicplace.follow.repository.FollowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("isolation-test")
class FollowIsolationExperimentTest {

    @Autowired
    private FollowIsolationExperimentService experimentService;

    @Autowired
    private FollowRepository followRepository;

    private static final String TARGET_ID = "testuser7";
    private static final String NEW_FOLLOWER_ID = "testuser8";

    @AfterEach
    void cleanup() {
        followRepository.findAllByMemberId(NEW_FOLLOWER_ID).stream()
                .filter(f -> f.getTargetId().equals(TARGET_ID))
                .forEach(followRepository::delete);
    }

    @Test
    void repeatableRead_두번째_읽기는_첫번째와_같아야_한다() throws Exception {
        CountDownLatch firstReadDone = new CountDownLatch(1);
        CountDownLatch writerCommitted = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<long[]> readerResult = executor.submit(() ->
                experimentService.readTwiceRepeatableRead(TARGET_ID, firstReadDone, writerCommitted));

        Future<?> writerResult = executor.submit(() -> {
            try {
                experimentService.insertFollowAfterSignal(NEW_FOLLOWER_ID, TARGET_ID, firstReadDone);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            writerCommitted.countDown();
            return null;
        });

        writerResult.get(10, TimeUnit.SECONDS);
        long[] result = readerResult.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        System.out.printf("[REPEATABLE READ] first=%d, second=%d%n", result[0], result[1]);

        // REPEATABLE READ: 트랜잭션 시작 시점 스냅샷을 유지하므로 두 값이 같아야 함
        assertThat(result[0]).isEqualTo(result[1]);
    }

    @Test
    void readCommitted_두번째_읽기는_커밋된_변경을_반영해야_한다() throws Exception {
        CountDownLatch firstReadDone = new CountDownLatch(1);
        CountDownLatch writerCommitted = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<long[]> readerResult = executor.submit(() ->
                experimentService.readTwiceReadCommitted(TARGET_ID, firstReadDone, writerCommitted));

        Future<?> writerResult = executor.submit(() -> {
            try {
                experimentService.insertFollowAfterSignal(NEW_FOLLOWER_ID, TARGET_ID, firstReadDone);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            writerCommitted.countDown();
            return null;
        });

        writerResult.get(10, TimeUnit.SECONDS);
        long[] result = readerResult.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        System.out.printf("[READ COMMITTED] first=%d, second=%d%n", result[0], result[1]);

        // READ COMMITTED: 두 번째 읽기는 그 사이 커밋된 변경을 봐야 함
        assertThat(result[1]).isEqualTo(result[0] + 1);
    }
}
