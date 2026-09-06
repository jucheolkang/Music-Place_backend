// =========================================
// follow-unfollow-mix-test.js
// 팔로워 역할: testuser1~testuser9 (9개 계정을 VU 수만큼 라운드로빈 재사용 — VU가 9보다 많으면 여러 VU가 같은 계정을 동시에 씀. 이건 버그가 아니라 오히려 좋은 부하 패턴입니다 — 같은 targetId에 이벤트가 몰려서 8번 항목에서 설계한 배치 dedup 로직을 제대로 검증할 수 있습니다.)
// 타겟: testuser10 고정
// 각 VU는 자신의 팔로우 상태(isFollowing, followId)를 기억하면서, 안 팔로우 중이면 팔로우, 팔로우 중이면 언팔로우를 반복 — 실제 사용자가 팔로우/언팔로우를 오가는 패턴에 가깝습니다
// VU/기간은 기존 C단계 k6 실험(30VU)과 같은 규모로 가정했습니다 — 다른 규모를 원하시면 stages 값만 바꾸면 됩니다
// =========================================

import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://app:8080';
const TEST_PASSWORD = __ENV.TEST_PASSWORD || 'Test1234!';
const TARGET_ID = 'testuser10';
const FOLLOWER_COUNT = 9; // testuser1 ~ testuser9

export const options = {
    stages: [
        { duration: '30s', target: 30 }, // 램프업
        { duration: '2m', target: 30 },  // 유지
        { duration: '30s', target: 0 },  // 램프다운
    ],
};

export function setup() {
    const tokens = {};
    for (let i = 1; i <= FOLLOWER_COUNT; i++) {
        const memberId = `testuser${i}`;
        const res = http.post(
            `${BASE_URL}/auth/login`,
            JSON.stringify({ member_id: memberId, pw: TEST_PASSWORD }),
            { headers: { 'Content-Type': 'application/json' } }
        );

        if (res.status !== 200) {
            throw new Error(`로그인 실패: ${memberId}, status=${res.status}, body=${res.body}`);
        }

        tokens[memberId] = JSON.parse(res.body).token;
    }
    return { tokens };
}

// VU마다 독립적으로 유지되는 상태 (k6는 VU별로 별도 JS 컨텍스트를 쓰므로 모듈 전역 변수로 충분함)
let isFollowing = false;
let followId = null;

export default function (data) {
    const memberId = `testuser${(__VU % FOLLOWER_COUNT) + 1}`;
    const token = data.tokens[memberId];
    const headers = {
        headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
    };

    if (!isFollowing) {
        const res = http.post(
            `${BASE_URL}/follow`,
            JSON.stringify({
                target_id: TARGET_ID,
                nickname: `${memberId}_nick`,
                profile_img_url: 'https://example.com/default.png',
            }),
            headers
        );

        // 이미 팔로우 중(409, FOLLOW_ALREADY_EXISTS)인 경우도 정상 케이스로 취급
        check(res, {
            '팔로우 성공 또는 이미 존재': (r) => r.status === 200 || r.status === 409,
        });

        if (res.status === 200) {
            isFollowing = true;
            followId = JSON.parse(res.body); // 컨트롤러가 Long을 그대로 반환하므로 body는 순수 숫자
        }
    } else {
        const res = http.del(`${BASE_URL}/follow/${followId}`, null, headers);
        check(res, { '언팔로우 성공': (r) => r.status === 200 });
        isFollowing = false;
        followId = null;
    }

    sleep(Math.random() * 2 + 1); // 1~3초 대기
}
