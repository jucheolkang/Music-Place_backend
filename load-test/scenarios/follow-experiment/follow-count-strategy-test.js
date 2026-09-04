// =========================================
// Music Place - Follow Count 전략 비교 (C단계)
// 목적: 비관적 락 / 낙관적 락 / Unique+배치 3가지 방식의
//       처리량(throughput)·지연시간(latency) 비교
// follow-count-strategy-test.js
// =========================================

import http from 'k6/http';
import { check } from 'k6';
import { Trend, Counter } from 'k6/metrics';

const BASE_URL = __ENV.TARGET_BASE_URL || 'http://app:8080';

// 전략별 타겟 계정 (users 테이블에 이미 존재하는 계정 재사용)
const STRATEGY_TARGETS = {
    pessimistic: 'testuser1',
    optimistic: 'testuser2',
    batch: 'testuser3',
};

// 전략별 커스텀 지연시간 지표 (각 시나리오 결과를 따로 집계)
const pessimisticDuration = new Trend('pessimistic_duration', true);
const optimisticDuration = new Trend('optimistic_duration', true);
const batchDuration = new Trend('batch_duration', true);
const failCount = new Counter('experiment_failures');

const VUS = 30;
const ITERATIONS_PER_VU = 1;

export const options = {
    scenarios: {
        pessimistic: {
            executor: 'shared-iterations',
            exec: 'runPessimistic',
            vus: VUS,
            iterations: VUS * ITERATIONS_PER_VU,
            maxDuration: '30s',
        },
        optimistic: {
            executor: 'shared-iterations',
            exec: 'runOptimistic',
            vus: VUS,
            iterations: VUS * ITERATIONS_PER_VU,
            maxDuration: '30s',
            startTime: '35s', // pessimistic 시나리오와 시간이 겹치지 않도록 순차 실행
        },
        batch: {
            executor: 'shared-iterations',
            exec: 'runBatch',
            vus: VUS,
            iterations: VUS * ITERATIONS_PER_VU,
            maxDuration: '30s',
            startTime: '70s',
        },
    },
    thresholds: {
        'experiment_failures': ['count==0'],
    },
};

// 시나리오 간 시간을 분리한 이유: 같은 서버에서 세 전략을 동시에 돌리면
// 서로 다른 target을 쓴다 해도 커넥션 풀/CPU 자원을 나눠 쓰게 되어
// "전략 자체의 성능 차이"가 "동시 부하로 인한 간섭"과 섞여버립니다.
// 순차 실행해야 각 전략을 동일한 조건에서 비교할 수 있습니다.

export function setup() {
    for (const [strategy, targetId] of Object.entries(STRATEGY_TARGETS)) {
        const res = http.del(`${BASE_URL}/experiment/follow/${targetId}/reset`);
        if (res.status !== 200) {
            throw new Error(`setup 리셋 실패: ${strategy}/${targetId} - status ${res.status}`);
        }
    }
}

function runStrategy(strategy, trend) {
    const targetId = STRATEGY_TARGETS[strategy];
    const memberId = `k6-${strategy}-${__VU}-${__ITER}`;

    const res = http.post(
        `${BASE_URL}/experiment/follow/${strategy}?memberId=${memberId}&targetId=${targetId}`
    );

    trend.add(res.timings.duration);

    const ok = check(res, {
        [`${strategy} 200 응답`]: (r) => r.status === 200,
    });

    if (!ok) {
        failCount.add(1);
    }
}

export function runPessimistic() {
    runStrategy('pessimistic', pessimisticDuration);
}

export function runOptimistic() {
    runStrategy('optimistic', optimisticDuration);
}

export function runBatch() {
    runStrategy('batch', batchDuration);
}

export function teardown() {
    console.log('\n========================================');
    console.log('   Follow Count 전략별 최종 정합성 확인');
    console.log('========================================');

    for (const [strategy, targetId] of Object.entries(STRATEGY_TARGETS)) {
        if (strategy === 'batch') {
            // batch 전략은 팔로우 시점엔 카운터를 안 건드리므로 재계산을 명시적으로 호출
            http.post(`${BASE_URL}/experiment/follow/${targetId}/recalculate`);
        }
        const res = http.get(`${BASE_URL}/experiment/follow/${targetId}/count`);
        const followerCount = res.json('followerCount');
        console.log(`[${strategy}] target=${targetId} followerCount=${followerCount} (기대값=${VUS})`);
    }
    console.log('========================================\n');
}

export function handleSummary(data) {
    const now = new Date().toISOString().replace(/:/g, '-');

    const summarize = (name) => {
        const m = data.metrics[name];
        if (!m) return null;
        return {
            avg: m.values.avg?.toFixed(2),
            p95: m.values['p(95)']?.toFixed(2),
            max: m.values.max?.toFixed(2),
        };
    };

    console.log('\n========================================');
    console.log('   Follow Count 전략별 지연시간(ms) 비교');
    console.log('========================================');
    console.log('비관적 락  :', JSON.stringify(summarize('pessimistic_duration')));
    console.log('낙관적 락  :', JSON.stringify(summarize('optimistic_duration')));
    console.log('배치 방식  :', JSON.stringify(summarize('batch_duration')));
    console.log('========================================\n');

    return {
        [`/results/follow-count-strategy-${now}.json`]: JSON.stringify(data, null, 2),
    };
}
