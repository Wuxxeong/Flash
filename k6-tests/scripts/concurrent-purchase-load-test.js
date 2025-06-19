import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// 커스텀 메트릭 정의
const errorRate = new Rate('errors');
const purchaseTime = new Trend('purchase_time');

// 테스트 설정
export const options = {
  stages: [
    // 1단계: 30명의 사용자가 동시에 접속
    { duration: '10s', target: 30 },
    // 2단계: 30명 유지하면서 구매 시도
    { duration: '30s', target: 30 },
    // 3단계: 점진적으로 감소
    { duration: '10s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'], // 95%의 요청이 2초 이내 완료
    errors: ['rate<0.1'], // 에러율 10% 미만
    purchase_time: ['p(95)<3000'], // 구매 완료 시간 95%가 3초 이내
  },
};

// 테스트 데이터
const BASE_URL = 'http://localhost:8080';
const ITEM_ID = 3; // i번 상품
const USER_COUNT = 30; // 1~30번 사용자

// 헤더 설정
const HEADERS = {
  'Content-Type': 'application/json',
  'Accept': 'application/json',
};

export default function () {
  // 각 VU(가상 사용자)마다 고유한 사용자 ID 할당
  const userId = (__VU % USER_COUNT) + 1;
  
  console.log(`VU ${__VU}: 사용자 ${userId}가 구매 시도 시작`);
  
  // 구매 시작 시간 기록
  const startTime = Date.now();
  
  try {
    // 구매 요청 전송 (쿼리 파라미터 사용)
    const purchaseResponse = http.post(
      `${BASE_URL}/api/purchase?itemId=${ITEM_ID}&userId=${userId}&quantity=1`,
      null, // 본문 없음
      { headers: HEADERS }
    );
    
    // 구매 완료 시간 계산
    const endTime = Date.now();
    const purchaseDuration = endTime - startTime;
    
    // 구매 시간 메트릭 기록
    purchaseTime.add(purchaseDuration);
    
    // 응답 체크
    const success = check(purchaseResponse, {
      'purchase status is 200': (r) => r.status === 200,
      'purchase response has order id': (r) => {
        if (r.status === 200) {
          try {
            const responseBody = JSON.parse(r.body);
            return responseBody.id !== undefined;
          } catch (e) {
            return false;
          }
        }
        return false;
      },
      'purchase response time < 2000ms': (r) => r.timings.duration < 2000,
    });
    
    // 에러율 계산
    errorRate.add(!success);
    
    // 응답 로깅
    if (purchaseResponse.status === 200) {
      console.log(`✅ VU ${__VU} (사용자 ${userId}): 구매 성공 - ${purchaseDuration}ms`);
      try {
        const responseBody = JSON.parse(purchaseResponse.body);
        console.log(`   주문 ID: ${responseBody.id}, 상품 ID: ${responseBody.itemId}`);
      } catch (e) {
        console.log(`   응답 파싱 실패: ${purchaseResponse.body}`);
      }
    } else {
      console.log(`❌ VU ${__VU} (사용자 ${userId}): 구매 실패 - ${purchaseResponse.status}`);
      console.log(`   에러 메시지: ${purchaseResponse.body}`);
    }
    
    // 성공한 경우 결제 프로세스 진행
    if (purchaseResponse.status === 200) {
      try {
        const orderData = JSON.parse(purchaseResponse.body);
        
        // 1단계: 결제 생성
        const createPaymentResponse = http.post(
          `${BASE_URL}/api/payment?orderId=${orderData.id}`,
          null, // 본문 없음
          { headers: HEADERS }
        );
        
        if (createPaymentResponse.status === 200) {
          console.log(`💳 VU ${__VU} (사용자 ${userId}): 결제 생성 성공`);
          
          try {
            const paymentData = JSON.parse(createPaymentResponse.body);
            
            // 2단계: 결제 처리
            const processPaymentResponse = http.post(
              `${BASE_URL}/api/payment/process?paymentId=${paymentData.id}`,
              null, // 본문 없음
              { headers: HEADERS }
            );
            
            if (processPaymentResponse.status === 200) {
              console.log(`✅ VU ${__VU} (사용자 ${userId}): 결제 처리 완료`);
            } else {
              console.log(`❌ VU ${__VU} (사용자 ${userId}): 결제 처리 실패 - ${processPaymentResponse.status}`);
              console.log(`   에러 메시지: ${processPaymentResponse.body}`);
            }
          } catch (e) {
            console.log(`💳 VU ${__VU} (사용자 ${userId}): 결제 데이터 파싱 실패 - ${e.message}`);
          }
        } else {
          console.log(`❌ VU ${__VU} (사용자 ${userId}): 결제 생성 실패 - ${createPaymentResponse.status}`);
          console.log(`   에러 메시지: ${createPaymentResponse.body}`);
        }
      } catch (e) {
        console.log(`💳 VU ${__VU} (사용자 ${userId}): 결제 처리 중 오류 - ${e.message}`);
      }
    }
    
  } catch (error) {
    console.error(`🚨 VU ${__VU} (사용자 ${userId}): 예외 발생 - ${error.message}`);
    errorRate.add(true);
  }
  
  // 요청 간 간격 (0.1~0.5초 랜덤)
  sleep(Math.random() * 0.4 + 0.1);
}

// 테스트 시작 전 실행
export function setup() {
  console.log('=== 동시 구매 부하테스트 시작 ===');
  console.log(`테스트 대상: ${BASE_URL}`);
  console.log(`상품 ID: ${ITEM_ID}`);
  console.log(`사용자 수: ${USER_COUNT}`);
  console.log(`최대 동시 사용자: 30`);
  console.log('================================');
  
  // 상품 정보 확인
  const itemResponse = http.get(`${BASE_URL}/api/items/${ITEM_ID}`);
  if (itemResponse.status === 200) {
    try {
      const itemData = JSON.parse(itemResponse.body);
      console.log(`📦 상품 정보: ${itemData.name} (재고: ${itemData.stock}개, 가격: ${itemData.price}원)`);
    } catch (e) {
      console.log('상품 정보 파싱 실패');
    }
  } else {
    console.log(`⚠️  상품 정보 조회 실패: ${itemResponse.status}`);
  }
}

// 테스트 종료 후 실행
export function teardown(data) {
  console.log('=== 동시 구매 부하테스트 완료 ===');
  console.log(`총 요청 수: ${data.metrics.http_reqs.values.count}`);
  console.log(`성공률: ${((1 - data.metrics.errors.values.rate) * 100).toFixed(2)}%`);
  console.log(`평균 응답 시간: ${data.metrics.http_req_duration.values.avg.toFixed(2)}ms`);
  console.log(`95% 응답 시간: ${data.metrics.http_req_duration.values['p(95)'].toFixed(2)}ms`);
  console.log('================================');
} 