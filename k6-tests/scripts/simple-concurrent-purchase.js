import http from 'k6/http';
import { check } from 'k6';

// 테스트 설정 - 30명이 동시에 구매 시도
export const options = {
  vus: 30, // 30명의 가상 사용자
  duration: '30s', // 30초 동안 테스트
  thresholds: {
    http_req_duration: ['p(95)<3000'], // 95%의 요청이 3초 이내
    http_req_failed: ['rate<0.2'], // 에러율 20% 미만
  },
};

const BASE_URL = 'http://localhost:8080';
const ITEM_ID = 1;

export default function () {
  // 각 VU마다 고유한 사용자 ID (1~30)
  const userId = (__VU % 30) + 1;
  
  // 구매 요청 (쿼리 파라미터 사용)
  const response = http.post(
    `${BASE_URL}/api/purchase?itemId=${ITEM_ID}&userId=${userId}&quantity=1`,
    null, // 본문 없음
    { headers: { 'Content-Type': 'application/json' } }
  );
  
  // 결과 체크
  check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 2000ms': (r) => r.timings.duration < 2000,
  });
  
  // 결과 로깅
  if (response.status === 200) {
    console.log(`✅ 사용자 ${userId}: 구매 성공`);
  } else {
    console.log(`❌ 사용자 ${userId}: 구매 실패 (${response.status}) - ${response.body}`);
  }
}

export function setup() {
  console.log('=== 간단한 동시 구매 테스트 ===');
  console.log(`30명의 사용자가 상품 ID ${ITEM_ID}를 동시에 구매 시도`);
  console.log('==============================');
} 