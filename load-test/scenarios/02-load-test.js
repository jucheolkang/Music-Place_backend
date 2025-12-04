// =========================================
// Music Place 부하 테스트 (Docker 환경)
// 목표: 병목 지점 찾기, 최대 처리량 확인
//02-load-test.js
// =========================================

import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';
import { SharedArray } from 'k6/data';

// 커스텀 메트릭
const errorRate = new Rate('errors');
const successRate = new Rate('success');
const authFailures = new Counter('auth_failures');
const dbOperations = new Counter('db_operations');

// 테스트 설정
export const options = {
    scenarios: {
        browsing: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '1m', target: 10 },
                { duration: '2m', target: 10 },
                { duration: '2m', target: 20 },
                { duration: '3m', target: 20 },
                { duration: '2m', target: 30 },
                { duration: '2m', target: 30 },
                { duration: '2m', target: 0 },
            ],
            exec: 'browsingScenario',
        },

        searching: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '2m', target: 5 },
                { duration: '3m', target: 5 },
                { duration: '2m', target: 10 },
                { duration: '3m', target: 10 },
                { duration: '2m', target: 15 },
                { duration: '2m', target: 0 },
            ],
            exec: 'searchingScenario',
            startTime: '30s',
        },

        writing: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '2m', target: 3 },
                { duration: '3m', target: 3 },
                { duration: '2m', target: 5 },
                { duration: '3m', target: 5 },
                { duration: '2m', target: 8 },
                { duration: '2m', target: 0 },
            ],
            exec: 'writingScenario',
            startTime: '1m',
        },
    },

    thresholds: {
        'http_req_duration': ['p(95)<500', 'p(99)<1000'],
        'http_req_failed': ['rate<0.05'],
        'errors': ['rate<0.05'],
    },
};

const BASE_URL = 'http://app:8080';

// 테스트 사용자
const testUsers = new SharedArray('users', function () {
    return [
        { member_id: 'testuser1', pw: 'Test1234!' },
        { member_id: 'testuser2', pw: 'Test1234!' },
        { member_id: 'testuser3', pw: 'Test1234!' },
        { member_id: 'testuser4', pw: 'Test1234!' },
        { member_id: 'testuser5', pw: 'Test1234!' },
        { member_id: 'testuser6', pw: 'Test1234!' },
        { member_id: 'testuser7', pw: 'Test1234!' },
        { member_id: 'testuser8', pw: 'Test1234!' },
        { member_id: 'testuser9', pw: 'Test1234!' },
        { member_id: 'testuser10', pw: 'Test1234!' },
    ];
});

// 로그인 함수
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
        authFailures.add(1);
        return null;
    }

    try {
        return loginRes.json('token');
    } catch (e) {
        authFailures.add(1);
        return null;
    }
}

function getAuthHeaders(token) {
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
    };
}

function checkResponse(response, name) {
    const result = check(response, {
        [`${name}: status 2xx`]: (r) => r.status >= 200 && r.status < 300,
    });

    if (result) {
        successRate.add(1);
    } else {
        errorRate.add(1);
    }

    return result;
}

// 시나리오 1: 브라우징
export function browsingScenario() {
    const user = testUsers[__VU % testUsers.length];
    const token = login(user.member_id, user.pw);

    if (!token) {
        sleep(5);
        return;
    }

    const headers = getAuthHeaders(token);

    group('Browse Playlists', () => {
        const myPL = http.get(`${BASE_URL}/playList`, { headers, tags: { name: 'GetMyPlaylists' } });
        checkResponse(myPL, 'My Playlists');
        dbOperations.add(1);
        sleep(1);

        const publicPL = http.get(`${BASE_URL}/playList/public`, { headers, tags: { name: 'GetPublicPlaylists' } });
        const plSuccess = checkResponse(publicPL, 'Public Playlists');
        dbOperations.add(1);
        sleep(2);

        if (plSuccess && publicPL.status === 200) {
            try {
                const playlists = publicPL.json();
                if (playlists && playlists.length > 0) {
                    const plId = playlists[0].playlist_id;

                    const music = http.get(`${BASE_URL}/playList/music/${plId}`, { headers, tags: { name: 'GetMusic' } });
                    checkResponse(music, 'Music List');
                    dbOperations.add(1);
                    sleep(2);

                    const comments = http.get(`${BASE_URL}/playList/comment/${plId}`, { headers, tags: { name: 'GetComments' } });
                    checkResponse(comments, 'Comments');
                    dbOperations.add(1);
                    sleep(1);
                }
            } catch (e) {
                // Ignore parsing errors
            }
        }
    });

    group('User Profile', () => {
        const userData = http.get(`${BASE_URL}/sign_in/getuser`, { headers, tags: { name: 'GetUserData' } });
        checkResponse(userData, 'User Data');
        dbOperations.add(1);
        sleep(1);

        const plCount = http.get(`${BASE_URL}/playList/count`, { headers, tags: { name: 'GetPLCount' } });
        checkResponse(plCount, 'PL Count');
        dbOperations.add(1);
        sleep(1);

        const follows = http.get(`${BASE_URL}/follow`, { headers, tags: { name: 'GetFollows' } });
        checkResponse(follows, 'Follows');
        dbOperations.add(1);
    });

    sleep(Math.random() * 2 + 1);
}

// 시나리오 2: 검색 & 추가 (Mock YouTube API 사용)
export function searchingScenario() {
    const user = testUsers[(__VU + 3) % testUsers.length];
    const token = login(user.member_id, user.pw);

    if (!token) {
        sleep(5);
        return;
    }

    const headers = getAuthHeaders(token);
    const keywords = ['love', 'happy', 'sad', 'rock', 'pop', 'jazz', 'kpop'];

    group('Search Music', () => {
        const keyword = keywords[Math.floor(Math.random() * keywords.length)];

        // Mock YouTube API 호출
        const search = http.get(`${BASE_URL}/youtube/${keyword}`, { headers, tags: { name: 'SearchYoutube' } });
        checkResponse(search, 'YouTube Search');
        sleep(2);

        const ytPlaylist = http.get(`${BASE_URL}/youtube/playlist`, { headers, tags: { name: 'GetYoutubePlaylist' } });
        checkResponse(ytPlaylist, 'YouTube Playlist');
    });

    sleep(Math.random() * 2 + 1);
}

// 시나리오 3: 쓰기 작업
export function writingScenario() {
    const user = testUsers[(__VU + 7) % testUsers.length];
    const token = login(user.member_id, user.pw);

    if (!token) {
        sleep(5);
        return;
    }

    const headers = getAuthHeaders(token);

    group('Create Playlist', () => {
        const plData = JSON.stringify({
            title: `Test PL ${Date.now()}`,
            onOff: Math.random() > 0.5 ? 'Public' : 'Private',
            cover_img: 'https://via.placeholder.com/300',
            comment: 'Created by k6',
        });

        const createPL = http.post(`${BASE_URL}/playList`, plData, { headers, tags: { name: 'CreatePlaylist' } });
        checkResponse(createPL, 'Create Playlist');
        dbOperations.add(1);
        sleep(2);
    });

    group('Comment', () => {
        const publicPL = http.get(`${BASE_URL}/playList/public`, { headers });

        if (publicPL.status === 200) {
            try {
                const playlists = publicPL.json();
                if (playlists && playlists.length > 0) {
                    const plId = playlists[0].playlist_id;

                    const commentData = JSON.stringify({
                        nickName: user.member_id,
                        comment: `Great! ${Date.now()}`,
                        profile_img_url: 'https://via.placeholder.com/50',
                    });

                    const comment = http.post(`${BASE_URL}/playList/comment/${plId}`, commentData, { headers, tags: { name: 'CreateComment' } });
                    checkResponse(comment, 'Create Comment');
                    dbOperations.add(1);
                }
            } catch (e) {
                // Ignore
            }
        }
        dbOperations.add(1);
    });

    sleep(Math.random() * 2 + 1);
}

// 결과 요약
export function handleSummary(data) {
    console.log('\n========================================');
    console.log('  Music Place Load Test Summary');
    console.log('========================================');
    console.log(`Total Requests: ${data.metrics.http_reqs.values.count}`);
    console.log(`Failed Requests: ${(data.metrics.http_req_failed.values.rate * 100).toFixed(2)}%`);
    console.log(`Avg Response Time: ${data.metrics.http_req_duration.values.avg.toFixed(2)}ms`);
    console.log(`P95 Response Time: ${data.metrics.http_req_duration.values['p(95)'].toFixed(2)}ms`);
    console.log(`P99 Response Time: ${data.metrics.http_req_duration.values['p(99)'].toFixed(2)}ms`);
    console.log(`DB Operations: ${data.metrics.db_operations.values.count}`);
    console.log(`Auth Failures: ${data.metrics.auth_failures.values.count}`);
    console.log('========================================\n');

    return {
        '/results/summary.json': JSON.stringify(data, null, 2),
    };
}
