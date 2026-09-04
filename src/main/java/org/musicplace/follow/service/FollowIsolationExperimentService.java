package org.musicplace.follow.service;

import lombok.RequiredArgsConstructor;
import org.musicplace.follow.domain.FollowEntity;
import org.musicplace.follow.repository.FollowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;

@Service
@RequiredArgsConstructor
public class FollowIsolationExperimentService {

    private final FollowRepository followRepository;

    /**
     * 리더 스레드: 지정된 격리 수준으로 같은 트랜잭션 안에서 두 번 카운트를 읽는다.
     * firstReadDone → 라이터에게 "이제 insert해도 됨" 신호
     * writerCommitted → 라이터가 "커밋 끝났음" 신호를 줄 때까지 대기
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public long[] readTwiceRepeatableRead(String targetId,
                                          CountDownLatch firstReadDone,
                                          CountDownLatch writerCommitted) throws InterruptedException {
        long first = followRepository.countByTargetId(targetId);
        firstReadDone.countDown();
        writerCommitted.await();
        long second = followRepository.countByTargetId(targetId);
        return new long[]{first, second};
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public long[] readTwiceReadCommitted(String targetId,
                                         CountDownLatch firstReadDone,
                                         CountDownLatch writerCommitted) throws InterruptedException {
        long first = followRepository.countByTargetId(targetId);
        firstReadDone.countDown();
        writerCommitted.await();
        long second = followRepository.countByTargetId(targetId);
        return new long[]{first, second};
    }

    /**
     * 라이터: 리더의 첫 읽기가 끝날 때까지 기다렸다가 insert+commit.
     * REQUIRES_NEW로 명시해서 별도 트랜잭션임을 코드로도 확실히 함.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertFollowAfterSignal(String memberId, String targetId,
                                        CountDownLatch firstReadDone) throws InterruptedException {
        firstReadDone.await();
        followRepository.save(FollowEntity.builder()
                .memberId(memberId)
                .targetId(targetId)
                .targetNickname("IsolationTestNickname")
                .build());
    }
}
