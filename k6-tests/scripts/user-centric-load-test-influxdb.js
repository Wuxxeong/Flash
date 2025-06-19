import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

// 커스텀 메트릭 정의
const errorRate = new Rate('errors');
const completePurchaseTime = new Trend('complete_purchase_time');
const successfulPurchases = new Rate('successful_purchases');
const stockExhausted = new Counter('stock_exhausted');

// 테스트 설정
export const options = {
  stages: [
    // 1단계: 급격한 트래픽 증가 (Flash Sale 시작)
    { duration: '5s', target: 1000 },
    // 2단계: 최대 부하 (10초간)
    { duration: '10s', target: 10000 },
    // 3단계: 재고 소진 후 점진적 감소
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<5000'], // 95%의 요청이 5초 이내 완료
    errors: ['rate<0.99'], // 에러율 99% 미만 (재고 부족은 정상)
    complete_purchase_time: ['p(95)<8000'], // 완전한 구매 완료 시간 95%가 8초 이내
    successful_purchases: ['rate>0.005'], // 성공률 0.5% 이상 (재고 대비)
  },
};

// 테스트 데이터
const BASE_URL = 'http://localhost:8080';
const ITEM_ID = 12; // 테스트 상품 ID
const USER_COUNT = 5000; // 1~5000번 사용자 (5만개 사용자 데이터)

// 헤더 설정
const HEADERS = {
  'Content-Type': 'application/json',
  'Accept': 'application/json',
};

// 재고 소진 플래그 (공유 변수)
let isStockExhausted = false;

export default function () {
  // 재고가 소진되었으면 테스트 종료
  if (isStockExhausted) {
    return;
  }
  
  // 각 VU(가상 사용자)마다 고유한 사용자 ID 할당
  const userId = (__VU % USER_COUNT) + 1;
  
  // 전체 구매 프로세스 시작 시간 기록
  const purchaseStartTime = Date.now();
  
  try {
    // 1단계: 주문 생성
    const purchaseResponse = http.post(
      `${BASE_URL}/api/purchase?itemId=${ITEM_ID}&userId=${userId}&quantity=1`,
      null,
      { headers: HEADERS }
    );
    
    // 재고 부족 응답 체크
    if (purchaseResponse.status === 409) {
      // 재고 부족 - 테스트 종료 플래그 설정
      isStockExhausted = true;
      stockExhausted.add(1);
      console.log(`🛑 재고 소진! 테스트를 종료합니다. (사용자 ${userId})`);
      return;
    }
    
    if (purchaseResponse.status !== 200) {
      errorRate.add(true);
      return;
    }
    
    // 2단계: 결제 생성
    let orderData;
    try {
      orderData = JSON.parse(purchaseResponse.body);
    } catch (e) {
      errorRate.add(true);
      return;
    }
    
    const createPaymentResponse = http.post(
      `${BASE_URL}/api/payment?orderId=${orderData.id}`,
      null,
      { headers: HEADERS }
    );
    
    if (createPaymentResponse.status !== 200) {
      errorRate.add(true);
      return;
    }
    
    // 3단계: 결제 처리
    let paymentData;
    try {
      paymentData = JSON.parse(createPaymentResponse.body);
    } catch (e) {
      errorRate.add(true);
      return;
    }
    
    const processPaymentResponse = http.post(
      `${BASE_URL}/api/payment/process?paymentId=${paymentData.id}`,
      null,
      { headers: HEADERS }
    );
    
    if (processPaymentResponse.status !== 200) {
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
    
    console.log(`✅ 구매 성공! 사용자 ${userId}, 소요시간: ${totalPurchaseTime}ms`);
    
  } catch (error) {
    errorRate.add(true);
    successfulPurchases.add(false);
  }
  
  // 요청 간 간격 (0.1~0.5초 랜덤)
  sleep(Math.random() * 0.4 + 0.1);
}

// 테스트 시작 전 실행
export function setup() {
  console.log('=== Flash Sale 현실적 부하테스트 시작 ===');
  console.log(`테스트 대상: ${BASE_URL}`);
  console.log(`상품 ID: ${ITEM_ID}`);
  console.log(`사용자 ID 범위: 1~${USER_COUNT}`);
  console.log(`최대 동시 사용자: 10,000명 (Flash Sale 시나리오)`);
  console.log(`총 테스트 시간: 45초 (5초 급격한 트래픽 증가 + 10초 최대 부하 + 30초 재고 소진 후 점진적 감소)`);
  console.log(`측정 방식: 사용자별 완전한 구매 프로세스 (주문 → 결제생성 → 결제처리)`);
  console.log(`재고 소진 시 자동 종료: 활성화`);
  console.log(`실시간 대시보드: http://localhost:3000 (admin/admin123)`);
  console.log('==========================================================');
  
  // 상품 정보 확인
  const itemResponse = http.get(`${BASE_URL}/api/items/${ITEM_ID}`);
  if (itemResponse.status === 200) {
    try {
      const itemData = JSON.parse(itemResponse.body);
      console.log(`📦 상품 정보: ${itemData.name} (재고: ${itemData.stock}개, 가격: ${itemData.price}원)`);
      console.log(`🎯 Flash Sale 예상 결과:`);
      console.log(`   - 성공: 약 ${itemData.stock}개 (재고 수량만큼)`);
      console.log(`   - 실패: 약 ${10000 - itemData.stock}개 (재고 부족)`);
      console.log(`   - 예상 에러율: ${((10000 - itemData.stock) / 10000 * 100).toFixed(1)}% (정상적인 Flash Sale 현상)`);
    } catch (e) {
      console.log('상품 정보 파싱 실패');
    }
  } else {
    console.log(`⚠️  상품 정보 조회 실패: ${itemResponse.status}`);
  }
}

// 테스트 종료 후 실행
export function teardown(data) {
  console.log('=== Flash Sale 현실적 부하테스트 완료 ===');
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
  console.log(`   재고 소진 이벤트: ${data.metrics.stock_exhausted ? data.metrics.stock_exhausted.values.count : 0}회`);
  console.log(`📈 Flash Sale 분석:`);
  console.log(`   완전한 구매 프로세스 성공률: ${((data.metrics.successful_purchases.values.rate) * 100).toFixed(2)}%`);
  console.log(`   시스템 안정성: ${data.metrics.http_req_duration.values.avg < 3000 ? '우수' : data.metrics.http_req_duration.values.avg < 5000 ? '양호' : '개선 필요'}`);
  console.log(`   동시성 제어 효과: ${data.metrics.errors.values.rate > 0.9 ? '정상 (재고 부족으로 인한 높은 에러율)' : '비정상 (동시성 제어 실패)'}`);
  console.log(`   사용자 경험: ${data.metrics.complete_purchase_time.values.avg < 3000 ? '우수' : data.metrics.complete_purchase_time.values.avg < 8000 ? '양호' : '개선 필요'}`);
  console.log(`🔍 상세 분석:`);
  console.log(`   Grafana 대시보드에서 실시간 그래프 확인: http://localhost:3000`);
  console.log(`   InfluxDB에서 원시 데이터 확인: http://localhost:8086`);
  console.log('==========================================================');
} 