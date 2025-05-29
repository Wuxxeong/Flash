-- 게이밍 마우스
INSERT INTO items (name, description, price, stock, sale_start, sale_end, created_at)
VALUES (
    '게이밍 마우스',
    '1ms 응답속도, RGB 조명, 8개의 프로그래밍 가능한 버튼',
    29000,
    100,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP + INTERVAL '24' HOUR,
    CURRENT_TIMESTAMP
);

-- 스마트워치
INSERT INTO items (name, description, price, stock, sale_start, sale_end, created_at)
VALUES (
    '스마트워치',
    '심박수 모니터링, GPS 내장, 7일 배터리',
    99000,
    50,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP + INTERVAL '24' HOUR,
    CURRENT_TIMESTAMP
);

-- 에어프라이어
INSERT INTO items (name, description, price, stock, sale_start, sale_end, created_at)
VALUES (
    '에어프라이어',
    '5.5L 대용량, 디지털 디스플레이, 8가지 조리 모드',
    89000,
    30,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP + INTERVAL '24' HOUR,
    CURRENT_TIMESTAMP
);

-- 사용자 30명 생성
INSERT INTO users (email, password, name, created_at)
SELECT 
    'user' || num || '@example.com',
    'password' || num,
    '사용자' || num,
    CURRENT_TIMESTAMP
FROM generate_series(1, 30) AS num;

-- 상품 100개 생성
INSERT INTO items (name, description, price, stock, sale_start, sale_end, created_at)
SELECT 
    '상품 ' || num,
    '상품 ' || num || '의 상세 설명입니다.',
    (random() * 90000 + 10000)::integer,  -- 10,000원 ~ 100,000원 사이의 랜덤 가격
    (random() * 90 + 10)::integer,        -- 10 ~ 100개 사이의 랜덤 재고
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP + INTERVAL '24' HOUR,
    CURRENT_TIMESTAMP
FROM generate_series(1, 100) AS num; 