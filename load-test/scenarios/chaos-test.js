// =========================================
// Music Place 장애 대응 측정 (k8s + LitmusChaos)
// 목표: MySQL Pod 장애 전/중/후 응답시간·오류율 비교
// chaos-test.js
// =========================================

import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate, Trend } from 'k6/metrics';
import { SharedArray } from 'k6/data';

// DB 의존 요청과 인프라(헬스체크) 요청을 분리 집계
// -> 오류율이 전체 API 평균으로 희석되는 문제 방지
const dbDependentErrorRate = new Rate('db_dependent_errors');
const dbDependentDuration = new Trend('db_dependent_duration');
const infraErrorRate = new Rate('infra_errors');

export const options = {
    scenarios: {
        chaos_test: {
            executor: 'constant-vus',
            vus: 15,           // TODO: 원하는 동시 사용자 수로 조정 (일단 02-load-test.js의 browsing 시나리오 최대치 참고)
            duration: '8m',    // baseline(~2분) + 장애 유발 + 복구 확인까지 여유
        },
    },
    thresholds: {
        // 장애 구간에서 깨지는 게 정상 — 실행 종료 코드보다 리포트 확인용
        'db_dependent_errors': ['rate<0.02'],
        'http_req_duration': ['p(95)<500'],
    },
};

// k8s 클러스터 내부 DNS. NodePort가 아닌 ClusterIP 기반 FQDN을 사용
const BASE_URL = __ENV.TARGET_BASE_URL || 'http://musicplace-app-svc.musicplace.svc.cluster.local:8080';
const MGMT_URL = __ENV.TARGET_MGMT_URL || 'http://musicplace-app-svc.musicplace.svc.cluster.local:8081';

// 02-load-test.js와 동일한 테스트 계정 재사용
const testUsers = new SharedArray('users', function () {
    return [
        { member_id: 'testuser1', pw: 'Test1234!' },
        { member_id: 'testuser2', pw: 'Test1234!' },
        { member_id: 'testuser3', pw: 'Test1234!' },
        { member_id: 'testuser4', pw: 'Test1234!' },
        { member_id: 'testuser5', pw: 'Test1234!' },
    ];
});

// 로그인 로직은 02-load-test.js를 그대로 재사용
function login(member_id, pw) {
    const loginRes = http.post(
        `${BASE_URL}/auth/login`,
        JSON.stringify({ member_id, pw }),
        {
            headers: { 'Content-Type': 'application/json' },
            tags: { name: 'Login' },
        }
    );

    if (loginRes.status !== 200) {
        return null;
    }

    try {
        return loginRes.json('token');
    } catch (e) {
        return null;
    }
}

function getAuthHeaders(token) {
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
    };
}

export default function () {
    const user = testUsers[__VU % testUsers.length];
    const token = login(user.member_id, user.pw);

    if (!token) {
        // 로그인 실패도 장애의 영향일 수 있으므로 DB 오류율에 포함해서 기록
        dbDependentErrorRate.add(true);
        sleep(3);
        return;
    }

    const headers = getAuthHeaders(token);

    // --- 인프라 레벨: DB를 안 타는 헬스체크 (Pod 자체 생존 여부만 확인) ---
    const health = http.get(`${MGMT_URL}/actuator/health`, {
        tags: { endpoint_type: 'infra', name: 'HealthCheck' },
    });
    infraErrorRate.add(health.status !== 200);

    // --- DB 의존 흐름: 실제 장애 영향을 관찰할 대상 ---
    group('Browse Playlist Flow', () => {
        const publicPL = http.get(`${BASE_URL}/playList/public`, {
            headers,
            tags: { endpoint_type: 'db_dependent', name: 'GetPublicPlaylists' },
        });

        const plOk = check(publicPL, { 'public playlists ok': (r) => r.status === 200 });
        dbDependentErrorRate.add(!plOk);
        dbDependentDuration.add(publicPL.timings.duration);
        sleep(1);

        if (plOk) {
            try {
                const playlists = publicPL.json();
                if (playlists && playlists.length > 0) {
                    const plId = playlists[0].playlistId;

                    const music = http.get(`${BASE_URL}/playList/music/${plId}`, {
                        headers,
                        tags: { endpoint_type: 'db_dependent', name: 'GetMusic' },
                    });
                    const musicOk = check(music, { 'music ok': (r) => r.status === 200 });
                    dbDependentErrorRate.add(!musicOk);
                    dbDependentDuration.add(music.timings.duration);
                    sleep(1);

                    const comments = http.get(`${BASE_URL}/playList/comment/${plId}`, {
                        headers,
                        tags: { endpoint_type: 'db_dependent', name: 'GetComments' },
                    });
                    const commentsOk = check(comments, { 'comments ok': (r) => r.status === 200 });
                    dbDependentErrorRate.add(!commentsOk);
                    dbDependentDuration.add(comments.timings.duration);
                }
            } catch (e) {
                dbDependentErrorRate.add(true);
            }
        }
    });

    sleep(1);
}

export function handleSummary(data) {
    const now = new Date().toISOString().replace(/:/g, '-');

    console.log('\n========================================');
    console.log('   Music Place 장애 대응 측정 결과');
    console.log('========================================');
    console.log(`DB 의존 요청 오류율 : ${((data.metrics.db_dependent_errors?.values?.rate ?? 0) * 100).toFixed(2)}%`);
    console.log(`DB 의존 요청 평균   : ${(data.metrics.db_dependent_duration?.values?.avg ?? 0).toFixed(2)} ms`);
    console.log(`DB 의존 요청 P95    : ${(data.metrics.db_dependent_duration?.values?.["p(95)"] ?? 0).toFixed(2)} ms`);
    console.log(`인프라(헬스체크) 오류율 : ${((data.metrics.infra_errors?.values?.rate ?? 0) * 100).toFixed(2)}%`);
    console.log('========================================\n');
    console.log(`테스트 시작 기준 wall-clock: ${now} (Litmus 실험 시각과 대조용)`);

    return {
        [`/results/chaos-test-${now}.json`]: JSON.stringify(data, null, 2),
    };
}
