package org.musicplace.follow.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FollowCountExperimentService {

    private final FollowCountTransactionalOps transactionalOps;

    private static final int MAX_RETRY = 30;
    private static final long MAX_BACKOFF_MS = 100L;

    public void followWithPessimisticLock(String memberId, String targetId) {
        transactionalOps.followWithPessimisticLock(memberId, targetId);
    }

    public void followWithOptimisticLock(String memberId, String targetId) {
        int attempt = 0;
        while (true) {
            try {
                transactionalOps.followWithOptimisticLockAttempt(memberId, targetId);
                return;
            } catch (OptimisticLockingFailureException e) {
                attempt++;
                if (attempt >= MAX_RETRY) {
                    throw e;
                }
                sleepWithExponentialBackoffAndJitter(attempt, e);
            }
        }
    }

    private void sleepWithExponentialBackoffAndJitter(int attempt, OptimisticLockingFailureException cause) {
        long exponential = (long) Math.pow(2, attempt);
        long jitter = (long) (Math.random() * 10);
        long backoffMs = Math.min(MAX_BACKOFF_MS, exponential + jitter);
        try {
            Thread.sleep(backoffMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw cause;
        }
    }

    public void followWithBatchStrategy(String memberId, String targetId) {
        transactionalOps.followWithBatchStrategy(memberId, targetId);
    }

    public void recalculateFollowerCount(String targetId) {
        transactionalOps.recalculateFollowerCount(targetId);
    }

    public void resetForTest(String targetId) {
        transactionalOps.resetForTest(targetId);
    }
}
