import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// 커스텀 메트릭 정의
const errorRate = new Rate('errors');
const completePurchaseTime = new Trend('complete_purchase_time');
const successfulPurchases = new Rate('successful_purchases');

// 테스트 설정
export const options = {
  stages: [
    // 1단계: 1만명의 사용자가 동시에 접속
    { duration: '30s', target: 10000 },
    // 2단계: 1만명 유지하면서 구매 시도
    { duration: '60s', target: 10000 },
    // 3단계: 점진적으로 감소
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<8000'], // 95%의 요청이 8초 이내 완료
    errors: ['rate<0.2'], // 에러율 20% 미만
    complete_purchase_time: ['p(95)<10000'], // 완전한 구매 완료 시간 95%가 10초 이내
    successful_purchases: ['rate>0.7'], // 성공률 70% 이상
  },
};

// 테스트 데이터
const BASE_URL = 'http://localhost:8080';
const ITEM_ID = 6; // 테스트 상품 ID
const USER_COUNT = 5000; // 1~5000번 사용자 (5만개 사용자 데이터)

// 헤더 설정
const HEADERS = {
  'Content-Type': 'application/json',
  'Accept': 'application/json',
};

export default function () {
  // 각 VU(가상 사용자)마다 고유한 사용자 ID 할당
  const userId = (__VU % USER_COUNT) + 1;
  
  // 전체 구매 프로세스 시작 시간 기록
  const purchaseStartTime = Date.now();
  
  try {
    console.log(`🔄 VU ${__VU} (사용자 ${userId}): 구매 프로세스 시작`);
    
    // 1단계: 주문 생성
    const purchaseResponse = http.post(
      `${BASE_URL}/api/purchase?itemId=${ITEM_ID}&userId=${userId}&quantity=1`,
      null,
      { headers: HEADERS }
    );
    
    if (purchaseResponse.status !== 200) {
      console.log(`❌ VU ${__VU} (사용자 ${userId}): 주문 생성 실패 - ${purchaseResponse.status}`);
      console.log(`   에러 메시지: ${purchaseResponse.body}`);
      errorRate.add(true);
      return;
    }
    
    console.log(`✅ VU ${__VU} (사용자 ${userId}): 주문 생성 성공`);
    
    // 2단계: 결제 생성
    let orderData;
    try {
      orderData = JSON.parse(purchaseResponse.body);
    } catch (e) {
      console.log(`❌ VU ${__VU} (사용자 ${userId}): 주문 데이터 파싱 실패`);
      errorRate.add(true);
      return;
    }
    
    const createPaymentResponse = http.post(
      `${BASE_URL}/api/payment?orderId=${orderData.id}`,
      null,
      { headers: HEADERS }
    );
    
    if (createPaymentResponse.status !== 200) {
      console.log(`❌ VU ${__VU} (사용자 ${userId}): 결제 생성 실패 - ${createPaymentResponse.status}`);
      console.log(`   에러 메시지: ${createPaymentResponse.body}`);
      errorRate.add(true);
      return;
    }
    
    console.log(`💳 VU ${__VU} (사용자 ${userId}): 결제 생성 성공`);
    
    // 3단계: 결제 처리
    let paymentData;
    try {
      paymentData = JSON.parse(createPaymentResponse.body);
    } catch (e) {
      console.log(`❌ VU ${__VU} (사용자 ${userId}): 결제 데이터 파싱 실패`);
      errorRate.add(true);
      return;
    }
    
    const processPaymentResponse = http.post(
      `${BASE_URL}/api/payment/process?paymentId=${paymentData.id}`,
      null,
      { headers: HEADERS }
    );
    
    if (processPaymentResponse.status !== 200) {
      console.log(`❌ VU ${__VU} (사용자 ${userId}): 결제 처리 실패 - ${processPaymentResponse.status}`);
      console.log(`   에러 메시지: ${processPaymentResponse.body}`);
      errorRate.add(true);
      return;
    }
    
    // 전체 구매 프로세스 완료 시간 계산
    const purchaseEndTime = Date.now();
    const totalPurchaseTime = purchaseEndTime - purchaseStartTime;
    
    // 성공 메트릭 기록
    completePurchaseTime.add(totalPurchaseTime);
    successfulPurchases.add(true);
    errorRate.add(false);
    
    console.log(`🎉 VU ${__VU} (사용자 ${userId}): 구매 완료 - ${totalPurchaseTime}ms`);
    console.log(`   주문 ID: ${orderData.id}, 결제 ID: ${paymentData.id}`);
    
  } catch (error) {
    console.error(`🚨 VU ${__VU} (사용자 ${userId}): 예외 발생 - ${error.message}`);
    errorRate.add(true);
    successfulPurchases.add(false);
  }
  
  // 요청 간 간격 (0.1~0.5초 랜덤)
  sleep(Math.random() * 0.4 + 0.1);
}

// 테스트 시작 전 실행
export function setup() {
  console.log('=== 사용자 중심 부하테스트 시작 ===');
  console.log(`테스트 대상: ${BASE_URL}`);
  console.log(`상품 ID: ${ITEM_ID}`);
  console.log(`사용자 ID 범위: 1~${USER_COUNT}`);
  console.log(`최대 동시 사용자: 10,000명`);
  console.log(`총 테스트 시간: 2분 (30초 증가 + 60초 유지 + 30초 감소)`);
  console.log(`측정 방식: 사용자별 완전한 구매 프로세스 (주문 → 결제생성 → 결제처리)`);
  console.log('=====================================');
  
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
  console.log('=== 사용자 중심 부하테스트 완료 ===');
  console.log(`📊 성능 지표:`);
  console.log(`   총 요청 수: ${data.metrics.http_reqs.values.count}`);
  console.log(`   성공한 구매: ${((data.metrics.successful_purchases.values.rate) * 100).toFixed(2)}%`);
  console.log(`   실패한 구매: ${((1 - data.metrics.successful_purchases.values.rate) * 100).toFixed(2)}%`);
  console.log(`   평균 구매 완료 시간: ${data.metrics.complete_purchase_time.values.avg.toFixed(2)}ms`);
  console.log(`   95% 구매 완료 시간: ${data.metrics.complete_purchase_time.values['p(95)'].toFixed(2)}ms`);
  console.log(`   최대 구매 완료 시간: ${data.metrics.complete_purchase_time.values.max.toFixed(2)}ms`);
  console.log(`   최소 구매 완료 시간: ${data.metrics.complete_purchase_time.values.min.toFixed(2)}ms`);
  console.log(`   평균 HTTP 응답 시간: ${data.metrics.http_req_duration.values.avg.toFixed(2)}ms`);
  console.log(`   95% HTTP 응답 시간: ${data.metrics.http_req_duration.values['p(95)'].toFixed(2)}ms`);
  console.log(`   에러율: ${(data.metrics.errors.values.rate * 100).toFixed(2)}%`);
  console.log(`   초당 처리량 (TPS): ${data.metrics.http_reqs.values.rate.toFixed(2)}`);
  console.log(`📈 비즈니스 관점:`);
  console.log(`   완전한 구매 프로세스 성공률: ${((data.metrics.successful_purchases.values.rate) * 100).toFixed(2)}%`);
  console.log(`   사용자 경험: ${data.metrics.complete_purchase_time.values.avg < 5000 ? '우수' : data.metrics.complete_purchase_time.values.avg < 10000 ? '양호' : '개선 필요'}`);
  console.log(`   시스템 안정성: ${data.metrics.errors.values.rate < 0.1 ? '우수' : data.metrics.errors.values.rate < 0.2 ? '양호' : '개선 필요'}`);
  console.log('=====================================');
} 