# 플라이딜 — Duffel API & 항공편 검색 기술 보강 (2026-03-26)

## 1. Duffel API 최신 변경사항 (2025-2026)

### 주요 업데이트
| 시기 | 변경 내용 |
|------|-----------|
| 2026.01 | Search Results와 Rates에 `expires_at` 필드 추가 |
| 2025.12 | Rate/Booking 응답에 estimated commission amount + 통화 정보 추가 |
| 2025.10 | 숙소 검색 최저가 투명 가격 표시, 체크인 전 보증금 요구사항 지원 |

### SDK 현황
- JS SDK: `@duffel/api` v4.21.6 (Python, Ruby, C#, Java도 지원)
- NPM: https://www.npmjs.com/package/@duffel/api

### 가격 모델
- **커미션 기반**: 항공사 커미션을 파트너와 공유 (판매량 비례)
- **초과 검색비**: $0.005/search (search:booking 비율 1500:1 초과 시만)
- **환율 수수료**: 2% (통화 변환 시)
- 초기비용 없음, 스타트업 친화적

### 항공사 커버리지
- 300+ 항공사 통합
- 3가지 채널: NDC (직접 연결, 독점 요금), GDS (Travelport), LCC

**출처:** https://changelog.duffel.com/, https://duffel.com/pricing, https://duffel.com/ndc

---

## 2. 2026년 항공편 검색 기술 트렌드

### AI 가격 예측
- **Hopper**: 95% 예측 정확도, 매일 수조 개 항공편 가격 분석
- **AirHint**: 80%+ 정확도 (업계 평균 60-70%)
- **Google Flights**: Google Pay 통한 자동 가격 하락 환불 + "Confidence Score"
- **Refare**: 지속적 모니터링 + 자동 환불 협상

### 자연어 검색 (2026 메인 트렌드)
- **Skyscanner ChatGPT 앱**: "일본 벚꽃 보러 가고 싶어" → 자동 검색
- **Google Flights AI**: Gemini 2.5 기반 자연어 의도 해석
- **Kayak & PROS**: ChatGPT 플러그인으로 자연어 검색

### Agentic AI
- 단순 나열 → 자동 최적 요금 예측 + 예약까지 수행
- Flight API 시장 규모: $1.65B (2026) → $3.29B (2035, CAGR 8.2%)

**출처:**
- https://globetrender.com/2026/02/27/skyscanner-launches-chatgpt-app-flight-search/
- https://blog.asaptrips.com/ai-flight-booking-assistants-in-2026
- https://www.refare.com/post/best-airfare-price-monitoring-tools-in-2026-including-automatic-refund-services

---

## 3. Duffel 대안 API 비교

| API | 강점 | 약점 | 적합 대상 |
|-----|------|------|-----------|
| **Duffel** | 모던 API, NDC 직접 연결, 커미션 기반 | 한국 LCC 미지원 | 스타트업 |
| **Amadeus** | 최대 글로벌 데이터, 업계 표준 | 통합 복잡, 비용 높음 | 엔터프라이즈 |
| **Travelport** | Galileo/Apollo/Worldspan 통합 | 레거시 시스템 | B2B 여행사 |
| **Kiwi.com (Tequila)** | 가상 인터라이닝 (최저가 다구간), LCC+GDS 결합 | 예약 안정성 이슈 | 가격 중심 서비스 |

- Amadeus/Sabre/Travelport가 시장의 65%, Duffel/Kiwi 등 신규 25%

**출처:**
- https://www.codebridge.tech/articles/top-5-flights-apis-for-travel-apps
- https://www.travelaiagent.com/news/kiwi-tequila-api-partnership-2026

---

## 4. 한국 출발 항공편 — 핵심 발견

### Duffel 한국 항공사 지원
- **대한항공만 지원** (Travelport GDS 경유)
- **한국 LCC (제주항공, 진에어, 티웨이) → Duffel 미지원** ⚠️
- LCC 커버하려면 Kiwi.com Tequila API 또는 직접 통합 필요

### 한국 LCC 현황
- 제주항공 (7C): 한국 최초 LCC, 월 625만 승객
- 진에어 (LJ): 대한항공 자회사, 와이드바디 LCC
- 티웨이항공 (TW): A330 장거리 운항

### 인천공항 인기 노선
- Top 5: 도쿄, 오사카, 후쿠오카, 방콕, 홍콩 (전체 트래픽 26%)
- 총 161개 논스톱 노선 (50개국)
- 한국 항공사 점유율 60.9% (FSC 38.7%, LCC 22.2%)

**출처:**
- https://duffel.com/flights/airlines/korean-air
- https://centreforaviation.com/analysis/reports/competitive-dynamics-are-shifting-for-south-korean-lccs-706876
- https://www.flightconnections.com/flights-from-seoul-icn

---

## 플라이딜 프로젝트 시사점

1. **Duffel만으로는 한국 LCC 커버 불가** — 제주항공/진에어/티웨이 미지원. 한국 사용자 타겟이면 치명적
2. **Kiwi.com Tequila API가 보완재로 유력** — LCC 포함 가상 인터라이닝으로 최저가 다구간 라우팅
3. **자연어 검색이 2026 핵심 차별화** — Skyscanner/Google이 도입 중이지만 한국어 지원은 미흡
4. **이중 API 전략 추천**: Duffel (FSC + NDC) + Kiwi.com (LCC + 최저가 라우팅)
5. **Duffel 커미션 모델**: 초기비용 없고 검색비율만 관리하면 되서 MVP에 적합

---

## 2차 보강: 2026 Q1 항공 API 최신 동향 (2026-03-29)

### Duffel API Q1 2026
- Stays API에 `description` 필드 추가 (2026.01)
- Search Results에 `expires_at` 필드 추가 — 캐시 만료 시점 확정적으로 알 수 있음
- **HTTP Streaming** 지원: `POST /air/offer_request`, `GET /air/batch_offer_requests` — 대규모 검색 성능 대폭 개선
- **Duffel Content**: 항공사 계약/자격 없이 Flights API 사용 가능 (20+ 항공사, 소액 per-order 수수료)
- 가격: 무료 가입, $3 고정 + 1% per order, FX 2%, 결제 ~2.9%
- 출처: https://changelog.duffel.com/

### 한국 항공사 API/NDC 현황
- **제주항공**: APG NDC Platform 합류 — 한국 LCC 중 유일하게 NDC 직접 연동 가능. 전 요금 + 부가서비스, 유통 수수료 없음
  - 출처: https://ftnnews.com/travel-news/technology/jeju-air-joins-apg-ndc-platform-to-expand-global-reach/
- **대한항공**: NDC 점진적 도입 중. 화물 API 출시 (실시간 스케줄, 요금, 예약)
- **진에어/에어부산/에어서울**: 공개 API 없음. 대한항공이 3사를 메가 LCC로 통합 계획 (55기, 2.5조원 매출) — API 전략 미공개
- **티웨이**: NDC/API 없음
- **실전**: 한국 LCC 프로그래밍 접근 = 제주항공만 APG NDC 직접 가능, 나머지는 Kiwi/Skyscanner/GDS 경유

### 신규 경쟁 API
- **Gordian Software**: 부가서비스 API (좌석, 수하물, 체크인). 100+ 항공사. Duffel 보완재로 활용 가능
- **Spotnana**: Travel-as-a-Service. API-first 마이크로서비스 (항공/호텔/차량/철도). 엔터프라이즈 중심
- **Mystifly**: B2B 750+ 항공사 집약. XML/JSON API
- Google Flights API는 2018년 종료, 공개 API 없음

### AI 항공 검색 트렌드 (2026)
- **Skyscanner + ChatGPT** (2026.02): 자연어 검색 프로덕션 출시 (UK/US). "12월에 뉴욕 제일 싼 항공편 찾아줘" → 실시간 결과
- **Google Price Guarantee**: 베타 졸업 → 핵심 기능. 가격 하락 예측 + 자동 환불
- **Agentic AI**: "보여주기"에서 "예측+자동 예약"으로 전환. 자율 에이전트가 요금 모니터링 → 최저가 감지 → 자동 예약
- 항공사들 주요 노선 하루 **50회까지** 가격 조정 (ML 기반)
- 출처: https://globetrender.com/2026/02/27/skyscanner-launches-chatgpt-app-flight-search/

### 플라이딜 적용 시사점
1. Duffel HTTP Streaming으로 검색 성능 대폭 개선 가능 → 반드시 적용
2. 제주항공 NDC 직접 연동 검토 (한국 LCC 유일한 옵션)
3. Duffel Content로 항공사 자격 없이도 시작 가능 → MVP 진입 장벽 낮아짐
4. 자연어 검색 + 가격 예측 = 차별화 핵심 (Skyscanner ChatGPT 앱과 경쟁/차별화)

---

## 3차 보강 (2026-03-31): API v2 + Amadeus 종료 + MCP Apps + Kiwi MCP

### Duffel 2026 Q1 변경사항
- **API v2 출시**: Flights/Stays 통합 엔드포인트
- Stays API: `expires_at` 필드, rate description 필드, 예상 커미션 금액 추가
- **Duffel Links**: 단일 링크로 300+ 항공사 예약 위젯, 마크업 직접 설정 가능 (수익화)
- SDK: JS, Python, Ruby, C#, Java 지원
- Sources: https://changelog.duffel.com/, https://duffel.com/links

### [중요] Amadeus Self-Service API 포털 종료
- **2026년 7월 17일부로 Self-Service API 완전 폐쇄**
- 신규 등록 그 이전에 중지, API 키 비활성화
- Enterprise는 유지 — 소규모 개발자/스타트업에 큰 타격
- → **플라이딜에 유리**: Amadeus 무료 티어 사라지면 Duffel/Kiwi로 개발자 유입 예상
- Sources: https://www.phocuswire.com/amadeus-shut-down-self-service-apis-portal-developers

### MCP Apps (2026.01.26 공식 출시)
- MCP 최초 공식 확장 — 도구가 채팅 내 인터랙티브 UI 렌더링 가능
- 대시보드, 폼, 시각화, 멀티스텝 워크플로우 지원
- Claude, ChatGPT, VS Code, Cursor 호환
- → 항공편 검색 결과를 대화 내 카드 UI로 표시 가능
- Sources: https://blog.modelcontextprotocol.io/posts/2026-01-26-mcp-apps/

### 여행 MCP 서버 생태계
| 서버 | 기능 |
|------|------|
| **Duffel MCP** | 자연어 항공편 검색, 공항 코드 조회 (커뮤니티) |
| **Kiwi MCP** | 800+ 항공사, 유연 날짜, 예약 링크 포함 (공식) |
| **Sabre MCP** | 50PB+ 여행 데이터, 전체 예약 라이프사이클 (CES 2026 시연) |
| **Avolal** | Claude/ChatGPT에서 직접 예약 가능 |

### Kiwi.com 최신
- MCP 서버: 편도/왕복 검색, 유연 날짜, 좌석 등급 지원. 다구간은 미지원
- **가상 인터라이닝** — 인터라인 협약 없는 항공사 조합 (경쟁사에 없는 핵심 기능)
- AI 기반 동적 오퍼 생성으로 전환 중, 파트너십 초대제
- Sources: https://media.kiwi.com/company-news/kiwi-com-releases-mcp-server-prototype/

### Sabre 에이전틱 AI (CES 2026)
- MCP 서버로 AI 에이전트가 쇼핑/예약/서비스 접근
- 음성/채팅 예약 + SabreMosaic 클라우드 통합
- 콜센터 프록시, 비자/컴플라이언스, 경비 처리 에이전트 등
- Sources: https://skift.com/2026/01/06/sabre-ces-agentic-ai-travel-trip-booking-demo/

### 플라이딜 적용 시사점 업데이트
1. **Amadeus Self-Service 종료 = 기회** — Duffel/Kiwi 중심 생태계 강화
2. **MCP Apps로 UI 혁신** — 채팅 내 항공편 카드/폼 렌더링, Duffel MCP + MCP Apps 조합
3. **Duffel Links로 빠른 수익화** — MVP 단계에서 마크업 설정으로 즉시 수익 가능
4. **Kiwi MCP + Duffel MCP 이중 사용** — Kiwi로 LCC 커버리지, Duffel로 직접 예약
5. **Sabre 수준의 에이전틱 AI는 장기 목표** — 먼저 자연어 검색 MVP로 시작
