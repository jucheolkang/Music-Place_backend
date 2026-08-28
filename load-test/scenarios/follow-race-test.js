// =========================================
// Music Place 팔로우 동시성 재현 테스트
// 목표: check-then-act 경쟁 상태로 인한 미처리 500 에러 재현
// follow-race-test.js
// =========================================

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

const successCount = new Counter('follow_success');
const serverErrorCount = new Counter('follow_server_error'); // 500: 미처리 예외
const businessErrorCount = new Counter('follow_business_error'); // 4xx: 의도된 비즈니스 예외
const otherCount = new Counter('follow_other');

export const options = {
    scenarios: {
        follow_race: {
            executor: 'shared-iterations',
            vus: 30,
            iterations: 30,
            maxDuration: '30s',
        },
    },
};

const BASE_URL = __ENV.TARGET_BASE_URL || 'http://localhost:8080';

const REQUESTER = { member_id: 'testuser6', pw: 'Test1234!' };
const TARGET_ID = 'testuser7';
const TARGET_NICKNAME = 'Tester7';
const TARGET_PROFILE_IMG_URL = 'https://via.placeholder.com/50';

function login(member_id, pw) {
    const res = http.post(
        `${BASE_URL}/auth/login`,
        JSON.stringify({ member_id, pw }),
        { headers: { 'Content-Type': 'application/json' }, tags: { name: 'Login' } }
    );
    if (res.status !== 200) {
        console.error(`로그인 실패: ${res.status} ${res.body}`);
        return null;
    }
    try {
        return res.json('token');
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

// 여러 VU가 동시에 setup()을 실행하지 않도록,
// 로그인 + 기존 팔로우 관계 정리를 최초 1회만 수행
export function setup() {
    const token = login(REQUESTER.member_id, REQUESTER.pw);
    if (!token) {
        throw new Error('setup 단계 로그인 실패 — 계정/비밀번호를 확인하세요.');
    }
    const headers = getAuthHeaders(token);

    // 기존에 testuser6 -> testuser7 팔로우 관계가 있다면 삭제해서 초기화
    const listRes = http.get(`${BASE_URL}/follow`, { headers, tags: { name: 'ListFollow' } });
    if (listRes.status === 200) {
        try {
            const follows = listRes.json();
            const existing = follows.find((f) => f.target_id === TARGET_ID);
            if (existing) {
                http.del(`${BASE_URL}/follow/${existing.follow_id}`, null, {
                    headers,
                    tags: { name: 'CleanupFollow' },
                });
                console.log(`기존 팔로우 관계(follow_id=${existing.follow_id}) 정리 완료`);
            }
        } catch (e) {
            console.error('기존 팔로우 목록 파싱 실패:', e);
        }
    }

    return { token };
}

export default function (data) {
    const headers = getAuthHeaders(data.token);

    const res = http.post(
        `${BASE_URL}/follow`,
        JSON.stringify({
            target_id: TARGET_ID,
            nickname: TARGET_NICKNAME,
            profile_img_url: TARGET_PROFILE_IMG_URL,
        }),
        { headers, tags: { name: 'FollowSave' } }
    );


    if (res.status === 500 && serverErrorCount.value === 0) {
        console.log(`첫 500 응답 body: ${res.body}`);
    }



    if (res.status === 200) {
        successCount.add(1);
    } else if (res.status === 500) {
        serverErrorCount.add(1);
    } else if (res.status >= 400 && res.status < 500) {
        businessErrorCount.add(1);
    } else {
        otherCount.add(1);
    }



    check(res, {
        '미처리 500 아님': (r) => r.status !== 500,
    });
}

export function handleSummary(data) {
    console.log('\n========================================');
    console.log('   팔로우 동시성 재현 테스트 결과');
    console.log('========================================');
    console.log(`성공(200)        : ${data.metrics.follow_success?.values?.count ?? 0}`);
    console.log(`미처리 서버 에러(500) : ${data.metrics.follow_server_error?.values?.count ?? 0}`);
    console.log(`비즈니스 에러(4xx)   : ${data.metrics.follow_business_error?.values?.count ?? 0}`);
    console.log(`기타              : ${data.metrics.follow_other?.values?.count ?? 0}`);
    console.log('========================================\n');

    return {
        stdout: '',
    };
}
