# 플라이딜 MVP 범위 정의

## MVP 목표

**최소한의 기능으로 "항공편 검색 → 결과 표시"가 동작하는 상태**

포트폴리오로서의 가치:
- Duffel/Kiwi 이중 API 연동 경험
- 한국 LCC 포함 실제 항공편 데이터 처리
- 검색/필터/정렬 등 실서비스 수준 UX

## MVP 기능 범위

### 포함 (In Scope)

| 기능 | 설명 | API |
|------|------|-----|
| 항공편 검색 | 출발지/도착지/날짜/인원 입력 → 검색 | Duffel + Kiwi |
| 결과 리스트 | 가격순/시간순 정렬 | - |
| 필터 | 직항/경유, 항공사, 가격 범위 | - |
| 항공편 카드 | 항공사, 출발/도착 시간, 가격, 경유 정보 | - |
| 검색 결과 캐싱 | Redis로 동일 검색 결과 캐싱 | Redis |

### 제외 (Out of Scope — 2단계 이후)

- AI 자연어 검색
- 가격 알림/추적
- 회원가입/로그인
- 예약/결제
- 가격 히스토리 차트
- 가격 예측

## API 연동 범위

### Duffel API (MVP)
- `offerRequests.create()` — 항공편 검색
- `offers.list()` — 검색 결과 조회
- Sandbox 모드 사용

### Kiwi Tequila API (MVP)
- `GET /v2/search` — 항공편 검색 (편도/왕복)
- `GET /locations` — 공항/도시 자동완성

## 화면 구성

### 1. 검색 페이지 (/)
- 출발지 입력 (공항 자동완성)
- 도착지 입력 (공항 자동완성)
- 출발일 / 귀국일 (달력)
- 승객 수
- 좌석 등급 (이코노미/비즈니스)
- 검색 버튼

### 2. 결과 페이지 (/search)
- 검색 조건 요약
- 필터 사이드바 (직항/경유, 항공사, 가격 범위, 출발 시간대)
- 정렬 (최저가/최단시간/추천순)
- 항공편 카드 리스트
  - 항공사 로고 + 이름
  - 출발/도착 시간 + 공항
  - 소요 시간 + 경유 횟수
  - 가격 (KRW)
  - 데이터 출처 (Duffel/Kiwi)

## 백엔드 API 엔드포인트

| Method | Path | 설명 |
|--------|------|------|
| POST | /api/flights/search | 항공편 검색 (Duffel + Kiwi 병렬 호출 → 병합) |
| GET | /api/airports/search?q= | 공항 자동완성 |

## 데이터 모델

### FlightSearchRequest
```
origin: String (IATA 코드)
destination: String (IATA 코드)
departureDate: LocalDate
returnDate: LocalDate? (편도면 null)
passengers: int
cabinClass: String (economy/business)
```

### FlightOffer
```
id: String
source: String (DUFFEL/KIWI)
airline: String
airlineLogo: String
departureTime: LocalDateTime
arrivalTime: LocalDateTime
origin: String
destination: String
duration: Duration
stops: int
price: BigDecimal
currency: String (KRW)
deepLink: String? (Kiwi 예약 링크)
```

## 완료 기준

- [ ] Duffel sandbox로 항공편 검색이 동작한다
- [ ] Kiwi API로 한국 LCC 항공편이 검색된다
- [ ] 두 API 결과가 하나의 리스트로 병합/정렬된다
- [ ] 직항/경유 필터가 동작한다
- [ ] 항공사 필터가 동작한다
- [ ] 가격순/시간순 정렬이 동작한다
- [ ] Redis 캐싱이 동작한다 (동일 검색 재요청 시 캐시 히트)
- [ ] 모바일 반응형 UI
