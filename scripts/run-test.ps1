// =========================================
// Music Place 스트레스 테스트
// 목적: 시스템 한계점 찾기
// =========================================

import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '2m', target: 20 },
    { duration: '3m', target: 50 },
    { duration: '3m', target: 100 },  // 한계 테스트
    { duration: '2m', target: 150 },  // 과부하
    { duration: '2m', target: 0 },
  ],

  thresholds: {
    'http_req_duration': ['p(95)<1000'],
    'http_req_failed': ['rate<0.2'],  // 스트레스 테스트이므로 20%까지 허용
  },
};

const BASE_URL = 'http://app:8080';

export default function () {
  const loginRes = http.post(
    `${BASE_URL}/auth/login`,
    JSON.stringify({
      member_id: `testuser${(__VU % 10) + 1}`,
      pw: 'Test1234!',
    }),
    { headers: { 'Content-Type': 'application/json' } }
  );

  if (loginRes.status === 200) {
    try {
      const token = loginRes.json('token');
      const headers = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
      };

      http.get(`${BASE_URL}/playList/public`, { headers });
      sleep(0.5);

      http.get(`${BASE_URL}/follow`, { headers });
      sleep(0.5);
    } catch (e) {
      // Ignore
    }
  }

  sleep(1);
}
