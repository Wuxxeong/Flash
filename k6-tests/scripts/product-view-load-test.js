import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

// 커스텀 메트릭 정의
const errorRate = new Rate('errors');
const productViewTime = new Trend('product_view_time');
const successfulViews = new Rate('successful_views');
const concurrentUsers = new Counter('concurrent_users');

// 테스트 설정
export const options = {
  stages: [
    // 1단계: 점진적 증가 (2만명까지)
    { duration: '30s', target: 20000 },
    // 2단계: 최대 부하 유지 (1분)
    { duration: '60s', target: 20000 },
    // 3단계: 점진적 감소
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'], // 95%의 요청이 2초 이내 완료
    errors: ['rate<0.05'], // 에러율 5% 미만
    product_view_time: ['p(95)<3000'], // 상품 조회 시간 95%가 3초 이내
    successful_views: ['rate>0.95'], // 성공률 95% 이상
  },
};

// 테스트 데이터
const BASE_URL = 'http://localhost:8080';
const ITEM_ID = 12; // 테스트 상품 ID
const USER_COUNT = 10000; // 1~10000번 사용자

// 헤더 설정
const HEADERS = {
  'Content-Type': 'application/json',
  'Accept': 'application/json',
};

export default function () {
  // 각 VU(가상 사용자)마다 고유한 사용자 ID 할당
  const userId = (__VU % USER_COUNT) + 1;
  
  // 상품 조회 시작 시간 기록
  const viewStartTime = Date.now();
  
  try {
    // 상품 조회 요청
    const productResponse = http.get(
      `${BASE_URL}/api/items/${ITEM_ID}`,
      { headers: HEADERS }
    );
    
    // 응답 체크
    const isSuccess = check(productResponse, {
      '상품 조회 성공': (r) => r.status === 200,
      '응답 시간 < 2초': (r) => r.timings.duration < 2000,
      'JSON 응답': (r) => r.headers['Content-Type'] && r.headers['Content-Type'].includes('application/json'),
    });
    
    if (isSuccess) {
      // 상품 조회 완료 시간 계산
      const viewEndTime = Date.now();
      const totalViewTime = viewEndTime - viewStartTime;
      
      // 성공 메트릭 기록
      productViewTime.add(totalViewTime);
      successfulViews.add(true);
      errorRate.add(false);
      
      // 상품 정보 파싱 및 검증
      try {
        const productData = JSON.parse(productResponse.body);
        check(productData, {
          '상품 ID 존재': (data) => data.id === ITEM_ID,
          '상품명 존재': (data) => data.name && data.name.length > 0,
          '가격 정보 존재': (data) => data.price && data.price > 0,
          '재고 정보 존재': (data) => data.stock !== undefined,
        });
      } catch (e) {
        errorRate.add(true);
        successfulViews.add(false);
      }
      
    } else {
      errorRate.add(true);
      successfulViews.add(false);
    }
    
  } catch (error) {
    errorRate.add(true);
    successfulViews.add(false);
  }
  
  // 요청 간 간격 (0.1~0.3초 랜덤)
  sleep(Math.random() * 0.2 + 0.1);
}

// 테스트 시작 전 실행
export function setup() {
  console.log('=== 상품 조회 부하테스트 시작 ===');
  console.log(`테스트 대상: ${BASE_URL}`);
  console.log(`상품 ID: ${ITEM_ID}`);
  console.log(`사용자 ID 범위: 1~${USER_COUNT}`);
  console.log(`최대 동시 사용자: 20,000명`);
  console.log(`총 테스트 시간: 2분 (30초 증가 + 60초 유지 + 30초 감소)`);
  console.log(`측정 방식: 상품 조회 API 호출`);
  console.log(`실시간 대시보드: http://localhost:3000 (admin/admin123)`);
  console.log('==========================================================');
  
  // 상품 정보 확인
  const itemResponse = http.get(`${BASE_URL}/api/items/${ITEM_ID}`);
  if (itemResponse.status === 200) {
    try {
      const itemData = JSON.parse(itemResponse.body);
      console.log(`📦 상품 정보: ${itemData.name} (재고: ${itemData.stock}개, 가격: ${itemData.price}원)`);
      console.log(`🎯 예상 결과: 20,000명의 동시 조회, 시스템 성능 측정`);
    } catch (e) {
      console.log('상품 정보 파싱 실패');
    }
  } else {
    console.log(`⚠️  상품 정보 조회 실패: ${itemResponse.status}`);
  }
}

// 테스트 종료 후 실행
export function teardown(data) {
  console.log('=== 상품 조회 부하테스트 완료 ===');
  console.log(`📊 성능 지표:`);
  console.log(`   총 요청 수: ${data.metrics.http_reqs.values.count}`);
  console.log(`   성공한 조회: ${((data.metrics.successful_views.values.rate) * 100).toFixed(2)}%`);
  console.log(`   실패한 조회: ${((1 - data.metrics.successful_views.values.rate) * 100).toFixed(2)}%`);
  console.log(`   평균 조회 시간: ${data.metrics.product_view_time.values.avg.toFixed(2)}ms`);
  console.log(`   95% 조회 시간: ${data.metrics.product_view_time.values['p(95)'].toFixed(2)}ms`);
  console.log(`   최대 조회 시간: ${data.metrics.product_view_time.values.max.toFixed(2)}ms`);
  console.log(`   최소 조회 시간: ${data.metrics.product_view_time.values.min.toFixed(2)}ms`);
  console.log(`   평균 HTTP 응답 시간: ${data.metrics.http_req_duration.values.avg.toFixed(2)}ms`);
  console.log(`   95% HTTP 응답 시간: ${data.metrics.http_req_duration.values['p(95)'].toFixed(2)}ms`);
  console.log(`   에러율: ${(data.metrics.errors.values.rate * 100).toFixed(2)}%`);
  console.log(`   초당 처리량 (RPS): ${data.metrics.http_reqs.values.rate.toFixed(2)}`);
  console.log(`   초당 성공 조회 (TPS): ${(data.metrics.successful_views.values.rate * data.metrics.http_reqs.values.rate).toFixed(2)}`);
  console.log(`📈 시스템 안정성 분석:`);
  console.log(`   상품 조회 성공률: ${((data.metrics.successful_views.values.rate) * 100).toFixed(2)}%`);
  console.log(`   시스템 안정성: ${data.metrics.errors.values.rate < 0.05 ? '우수' : data.metrics.errors.values.rate < 0.1 ? '양호' : '개선 필요'}`);
  console.log(`   응답 성능: ${data.metrics.http_req_duration.values.avg < 1000 ? '우수' : data.metrics.http_req_duration.values.avg < 2000 ? '양호' : '개선 필요'}`);
  console.log(`   동시 처리 능력: ${data.metrics.http_reqs.values.rate > 1000 ? '우수' : data.metrics.http_reqs.values.rate > 500 ? '양호' : '개선 필요'}`);
  console.log(`🔍 상세 분석:`);
  console.log(`   Grafana 대시보드에서 실시간 그래프 확인: http://localhost:3000`);
  console.log(`   InfluxDB에서 원시 데이터 확인: http://localhost:8086`);
  console.log('==========================================================');
} 