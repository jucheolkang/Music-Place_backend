// =========================================
// Music Place 워밍업 테스트
// =========================================

import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 5,
    duration: '2m',

    thresholds: {
        'http_req_duration': ['p(95)<500'],
        'http_req_failed': ['rate<0.1'],
    },
};

const BASE_URL = 'http://app:8080';

export default function () {
    // 1. Health Check
    const healthRes = http.get(`http://app:8081/actuator/health`);
    check(healthRes, {
        'health check status 200': (r) => r.status === 200,
    });
    sleep(1);

    // 2. Hello Test
    const helloRes = http.get(`${BASE_URL}/hello`);
    check(helloRes, {
        'hello status 200': (r) => r.status === 200,
    });
    sleep(1);

    // 3. Login Test (수정됨: member_id/pw)
    const loginPayload = JSON.stringify({
        member_id: 'testuser1',
        pw: 'Test1234!',
    });

    const loginRes = http.post(
        `${BASE_URL}/auth/login`,
        loginPayload,
        { headers: { 'Content-Type': 'application/json' } }
    );

    check(loginRes, {
        'login status 200': (r) => r.status === 200,
        'token received': (r) => {
            try {
                return r.json('token') !== undefined;
            } catch (e) {
                return false;
            }
        },
    });

    sleep(2);
}
