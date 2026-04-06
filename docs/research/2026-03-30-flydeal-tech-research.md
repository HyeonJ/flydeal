# 플라이딜 기술 리서치 — 종합 (2026-03-30)

> 5개 분야, 15개 검색 쿼리 기반 최신 정보 (2025-2026)

---

## 1. Next.js App Router + Duffel API 통합 패턴

### 1-1. App Router 2026 현황
- **Next.js 15 안정화, 16 출시** — Pages Router는 유지보수 모드. App Router가 표준
- **Server Components + Server Actions**가 핵심 패러다임
- Route Handlers (`app/api/*/route.ts`)로 REST API 엔드포인트 생성 가능

**출처:**
- [Next.js App Router Docs](https://nextjs.org/docs/app)
- [Next.js App Router 2026 Complete Guide — DEV](https://dev.to/ottoaria/nextjs-app-router-in-2026-the-complete-guide-for-full-stack-developers-5bjl)
- [Next.js App Router Patterns 2026 — DEV](https://dev.to/teguh_coding/nextjs-app-router-the-patterns-that-actually-matter-in-2026-146)

### 1-2. Duffel API + Next.js 통합 핵심
- **Duffel JS SDK는 서버 전용** — 브라우저에서 실행 불가. Route Handlers 또는 Server Actions에서만 사용
- 초기화: `import { Duffel } from '@duffel/api'` → `new Duffel({ token: process.env.DUFFEL_ACCESS_TOKEN })`
- 페이지네이션: `listWithGenerator()` 또는 `await aircraft.next()` 지원
- **UI 컴포넌트**: `@duffel/components` 패키지 — 좌석 선택, 수하물 추가 등 프리빌트 컴포넌트 제공

**출처:**
- [Duffel JS SDK — NPM](https://www.npmjs.com/package/@duffel/api)
- [Duffel JS Client Library Guide](https://duffel.com/docs/guides/javascript-client-library)
- [Duffel Components — GitHub](https://github.com/duffelhq/duffel-components)
- [Duffel Getting Started with Flights](https://duffel.com/docs/guides/getting-started-with-flights)

### 1-3. Server Actions vs Route Handlers 선택 기준

| 용도 | 권장 패턴 |
|------|-----------|
| 폼 제출 (검색 폼, 예약 폼) | **Server Actions** — useActionState로 pending/error 관리 |
| 외부 API 호출 (Duffel 검색) | **Server Actions** (키 노출 방지) 또는 **Route Handlers** |
| 외부 서비스 공개 API | **Route Handlers** (`app/api/*/route.ts`) |
| 데이터 읽기 (캐시 가능) | **Server Components** — fetch + cache |
| 데이터 변경 (mutation) | **Server Actions** — POST 기반, 캐시 불가 |

- **next-safe-action** 라이브러리: Server Actions에 미들웨어 패턴 (인증, rate limiting) 적용 가능

**출처:**
- [Next.js Server Actions Complete Guide 2026 — MakerKit](https://makerkit.dev/blog/tutorials/nextjs-server-actions)
- [Next.js Advanced Patterns 2026 — Medium](https://medium.com/@beenakumawat002/next-js-app-router-advanced-patterns-for-2026-server-actions-ppr-streaming-edge-first-b76b1b3dcac7)
- [Server Actions vs API Routes — DEV](https://dev.to/myogeshchavan97/nextjs-server-actions-vs-api-routes-dont-build-your-app-until-you-read-this-4kb9)
- [next-safe-action](https://next-safe-action.dev/)

### 1-4. Streaming + Suspense 패턴 (항공편 검색에 핵심)
- **즉시 페이지 쉘 렌더** → Suspense fallback 표시 → 데이터 도착하면 스트림으로 채움
- 검색 결과 페이지: 필터 사이드바(캐시 가능) + 결과 리스트(동적) 별도 Suspense 래핑
- `export const dynamic = "force-dynamic"` + `cache: "no-store"` 조합
- **Duffel HTTP Streaming** 지원: `POST /air/offer_request`, `GET /air/batch_offer_requests` — 대규모 검색 시 성능 대폭 개선
- React Flight Protocol: 서버 → 클라이언트 스트리밍 와이어 포맷. 컴포넌트 트리를 점진적 파싱

**출처:**
- [Next.js Streaming Guide — DEV](https://dev.to/boopykiki/a-complete-nextjs-streaming-guide-loadingtsx-suspense-and-performance-9g9)
- [RSC in Practice — Medium](https://medium.com/@vyakymenko/react-server-components-in-practice-next-js-d1c3c8a4971f)
- [Next.js Streaming Learn](https://nextjs.org/learn/dashboard-app/streaming)
- [Real-time AI Streaming with Vercel AI SDK — LogRocket](https://blog.logrocket.com/nextjs-vercel-ai-sdk-streaming/)

### 1-5. 권장 아키텍처 패턴 (플라이딜용)

```
[Client Component: 검색 폼]
    ↓ Server Action 호출
[Server Action: Duffel SDK로 offerRequest 생성]
    ↓ offerRequest ID 반환
[Server Component + Suspense: 결과 스트리밍]
    ↓ Duffel offers.list() 호출
[Client Component: 결과 렌더링 + 필터/정렬]
```

- Server Action으로 검색 키 노출 방지
- Suspense로 로딩 중 스켈레톤 표시
- Duffel HTTP Streaming으로 대규모 결과 점진적 수신

---

## 2. 항공편 가격 알림 아키텍처

### 2-1. 기존 서비스 아키텍처 분석

| 서비스 | 아키텍처 특징 | 정확도 |
|--------|-------------|--------|
| **AirHint** | 항공사별 전용 신경망 학습, 딥러닝 커스터마이징 | 80%+ |
| **FareCompare** | 947개 항공사 실시간 스캔, "Real Time Low Fare Alert" | - |
| **Google Flights** | ML로 100+ 변수 분석 (시즌, SNS 활동 등), Confidence Score | - |
| **Hopper** | 매일 수조 개 가격 분석, 95% 예측 정확도 | 95% |

**출처:**
- [7 Effective Flight Price Alert Tools — MightyTravels](https://www.mightytravels.com/2025/05/7-most-effective-flight-price-alert-tools-compared-in-2025/)
- [AirHint Flight Price Predictor](https://www.airhint.com/)
- [Google Flights Price Tracking Features — MightyTravels](https://www.mightytravels.com/2025/01/how-to-track-flight-price-alerts-effectively-using-google-flights-latest-2025-features/)

### 2-2. 가격 트래킹 시스템 구축 방법

#### 옵션 A: Vercel Cron (서버리스)
- `vercel.json`에 cron 스케줄 정의 → Route Handler 트리거
- 장점: 인프라 관리 불필요, Sentry 모니터링 자동 연동
- 단점: 실행 시간 제한 (Hobby: 10초, Pro: 60초)

#### 옵션 B: 외부 스케줄러 (QStash / Schedo.dev)
- **QStash** (Upstash): CRON 기반 HTTP 요청 → API Route 호출
- **Schedo.dev**: Next.js 전용 cron 관리 서비스
- 장점: 서버리스 호환, 실행 시간 제한 우회 가능

#### 옵션 C: Self-hosted (Node Cron / Convex)
- `node-cron`으로 Node.js 프로세스 내 스케줄러 실행
- **Convex**: 서버리스 백엔드에서 cron 지원
- 장점: 유연성 최대, 장기 실행 가능
- 단점: 인프라 직접 관리

**출처:**
- [Vercel Cron Job Example](https://vercel.com/templates/next.js/vercel-cron)
- [QStash Periodic Data Updates — Upstash](https://upstash.com/blog/qstash-periodic-data-updates)
- [NextJS Cron Jobs — Schedo.dev](https://www.schedo.dev/nextjs)
- [Cron Jobs in Next.js with Convex — Telerik](https://www.telerik.com/blogs/cron-jobs-nextjs-app-using-convex)
- [Next.js Cron Jobs — LogRocket](https://blog.logrocket.com/automate-repetitive-tasks-next-js-cron-jobs/)

### 2-3. n8n 워크플로우 (노코드 참고)
- 여러 플랫폼 (Kayak, Skyscanner, Expedia, Google Flights) 가격 병렬 스크래핑 → NLP 비교 → 이메일/텔레그램 알림
- Aviation Stack API + Gmail/Telegram 연동 워크플로우 존재
- Gemini + SerpAPI + Telegram 조합 워크플로우도 있음

**출처:**
- [n8n: Compare Flight Prices](https://n8n.io/workflows/9788-compare-flight-prices-across-multiple-booking-platforms-with-email-reports/)
- [n8n: Flight Fare Tracker](https://n8n.io/workflows/6235-live-flight-fare-tracker-with-aviation-stack-api-alerts-via-gmail-and-telegram/)
- [n8n: Monitor Flight Price Drops](https://n8n.io/workflows/6503-monitor-flight-price-drops-and-send-email-alerts-with-serpapi-and-gmail/)
- [n8n: Gemini Flight Search](https://n8n.io/workflows/12971-search-flights-with-gemini-via-telegram-and-send-serpapi-price-alerts/)

### 2-4. 플라이딜 가격 알림 권장 아키텍처

```
[사용자: 경로/날짜 등록]
    ↓ DB 저장
[Cron Job: 주기적 가격 체크]
    ↓ Duffel API 검색
[가격 비교 로직]
    ├─ 가격 하락 감지 → Push/이메일/카톡 알림
    └─ 히스토리 DB 저장 → 가격 추이 차트 제공
```

- 초기: Vercel Cron + Duffel API (간단)
- 확장: QStash + 별도 Worker (대규모 알림)
- 고급: ML 가격 예측 모델 (Hopper 방식)

---

## 3. Duffel API 경쟁사 & 대안

### 3-1. 종합 비교표

| API | 항공사 수 | 가격 모델 | DX | 한국 지원 | Free Tier | 특징 |
|-----|----------|-----------|-----|-----------|-----------|------|
| **Duffel** | 300+ | 커미션 기반, 초기비용 0 | 최고 | 대한항공+아시아나 (GDS) | Sandbox 무료 | NDC 직접 연결, IATA 인증 불필요 |
| **Amadeus** | 435+ | API 호출 기반 | 보통 | 광범위 | 월 2000 호출 무료 | 최대 글로벌 커버리지, Self-Service + Enterprise |
| **Kiwi Tequila** | 800+ | 커미션 | 좋음 | LCC 포함 | 무료 티어 있음 | 가상 인터라이닝, 최저가 다구간 |
| **Skyscanner** | 1200+ | 어필리에이트 | 좋음 | 한국 LCC 포함 | 무료 | 메타서치, 가격 비교 전문 |
| **Aviationstack** | - | 구독 | 좋음 | - | 월 100 호출 무료 | 실시간 운항 정보 특화 |
| **FlightAPI** | - | 구독 | 보통 | - | 20 호출 무료 | 가격/추적/상태 API |
| **Sabre** | 400+ | 엔터프라이즈 | 복잡 | 광범위 | 없음 | B2B, 여행사 표준 |
| **Travelport** | 400+ | 엔터프라이즈 | 복잡 | 광범위 | 없음 | Galileo/Apollo/Worldspan 통합 |

### 3-2. Duffel vs Amadeus 상세 비교

| 항목 | Duffel | Amadeus |
|------|--------|---------|
| API 방식 | RESTful + OAuth 2.0 | REST + SOAP |
| 학습 곡선 | 낮음 (API-first 설계) | 높음 (방대한 기능) |
| NDC 지원 | 30+ 항공사 직접 연결 | 20+ 항공사 |
| GDS 연결 | Travelport 경유 | 자체 GDS (최대) |
| 인증 요구 | IATA 인증 불필요 | IATA/ARC 필요 (Enterprise) |
| 응답 속도 | 빠름 (모던 인프라) | 보통 |
| UI 컴포넌트 | @duffel/components 제공 | 없음 |
| 가격 | 커미션 기반 + $3/order | API 호출 기반 |

**출처:**
- [Duffel vs Amadeus — DEV](https://dev.to/ravi_makhija/duffel-vs-amadeus-which-works-better-for-modern-api-first-integration-49p4)
- [Amadeus vs Duffel Comparison — TechoSolution](https://techosolution.com/detailed-comparison-of-amadeus-and-duffel-apis-for-flight-and-hotel-integration/)
- [Top 5 Flight APIs 2026 — ScrapingBee](https://www.scrapingbee.com/blog/top-flights-apis-for-travel-apps/)
- [Top 10 Flight Booking APIs 2026 — OneClickIT](https://www.oneclickitsolution.com/blog/flight-booking-apis)
- [Best Aviation Data APIs 2026 — Netrows](https://www.netrows.com/blog/best-aviation-data-apis-2026)
- [Duffel Pricing](https://duffel.com/pricing)

### 3-3. 신규 플레이어 (2025-2026)
- **Gordian Software**: 부가서비스 API (좌석, 수하물, 체크인). 100+ 항공사. Duffel 보완재
- **Spotnana**: Travel-as-a-Service, API-first 마이크로서비스
- **Mystifly**: B2B 750+ 항공사, XML/JSON API

---

## 4. 한국 LCC 항공편 API 접근 방법

### 4-1. 공식 공공 API
- **한국공항공사 (KAC) Open API**: 공항코드, 국내선/국제선 운항 스케줄, 실시간 운항 정보
  - 신청: [공공데이터포털](https://www.data.go.kr/data/15000126/openapi.do)
  - 제공 데이터: 지연/결항/운항 상태 (가격 정보 없음)
- **항공정보포털시스템**: https://www.airportal.go.kr/

**출처:**
- [한국공항공사 Open API](https://www.airport.co.kr/www/cms/frCon/index.do?MENU_ID=1270)
- [공공데이터포털 — 항공기 운항정보](https://www.data.go.kr/data/15000126/openapi.do)
- [공공데이터포털 — 실시간 항공운항 현황](https://www.data.go.kr/data/15113771/openapi.do)

### 4-2. Duffel의 한국 항공사 커버리지
- **대한항공 (KE)**: 지원 (Travelport GDS 경유). 410개 목적지, 72개국
- **아시아나항공 (OZ)**: 지원 (Travelport GDS 경유). 240개 목적지, 51개국
- **한국 LCC (제주항공, 진에어, 티웨이, 에어부산, 에어서울)**: 미지원

**출처:**
- [Duffel — Korean Air](https://duffel.com/flights/airlines/korean-air)
- [Duffel — Asiana Airlines](https://duffel.com/flights/airlines/asiana-airlines)

### 4-3. 한국 LCC NDC/API 현황 (2026)

| 항공사 | NDC/API 상태 | 비고 |
|--------|-------------|------|
| **제주항공 (7C)** | APG NDC Platform 합류 — 직접 연동 가능 | 한국 LCC 유일 NDC 보유. 전 요금 + 부가서비스, 유통 수수료 없음 |
| **진에어 (LJ)** | 공개 API 없음 | 대한항공 자회사. 에어부산/에어서울과 통합 예정 (2026 이후) |
| **티웨이 (TW)** | NDC/API 없음 | 2026년 Trinity Air로 리브랜딩 예정 |
| **에어부산** | 공개 API 없음 | 진에어 중심 메가 LCC 통합 대상 |
| **에어서울** | 공개 API 없음 | 진에어 중심 메가 LCC 통합 대상 |
| **대한항공 (KE)** | NDC 점진 도입, 화물 API 출시 | Duffel/Amadeus에서 GDS 경유 접근 가능 |
| **아시아나 (OZ)** | NDC 인증 항공사 | Duffel/Amadeus에서 GDS 경유 접근 가능 |

- **핵심**: 한국 LCC 프로그래밍 접근 = 제주항공만 APG NDC 직접 가능, 나머지는 Kiwi/Skyscanner/GDS 경유
- **Tidedsquare**: 한국 최초 IATA NDC Capable Level 3 + NDC Aggregator Level 4 인증 (B2B 연동 플랫폼)

**출처:**
- [제주항공 APG NDC Platform 합류 — FTN News](https://ftnnews.com/travel-news/technology/jeju-air-joins-apg-ndc-platform-to-expand-global-reach/)
- [NDC — 항공위키](https://airtravelinfo.kr/wiki/index.php/NDC)
- [LCC 3사 통합 뉴스 — 항공정보포털](https://www.airportal.go.kr/news/eventNewsDetail.do?num=102562)
- [Travelport NDC 한국어 페이지](https://marketing.cloud.travelport.com/ndc-ko)

### 4-4. 한국 LCC 접근을 위한 우회 전략

1. **Kiwi.com Tequila API**: 800+ 항공사 포함, 한국 LCC 데이터 접근 가능성 높음
2. **Skyscanner API**: 메타서치로 한국 LCC 포함 가격 비교
3. **제주항공 NDC 직접 연동**: APG NDC Platform 통해 개발자 접근 가능
4. **스크래핑 (최후 수단)**: 네이버 항공권, 카약 한국 등에서 가격 수집

---

## 5. AI 기반 항공편 검색 UX

### 5-1. 주요 서비스 동향 (2025-2026)

#### Google Agentic Travel Booking
- Gemini 기반 자연어 여행 계획 + 예약
- Booking.com, Expedia, Marriott 등 주요 파트너 협업
- "Flight Deals" — AI 기반 항공편 할인 검색 도구
- Google Pay 통한 자동 가격 하락 환불 (Price Guarantee)

**출처:** [Google Agentic Travel Booking — Skift](https://skift.com/2025/11/17/google-is-building-agentic-travel-booking-plus-other-travel-ai-updates/)

#### Skyscanner + ChatGPT (2026.02 출시)
- ChatGPT 내에서 "Skyscanner, 12월에 뉴욕 제일 싼 항공편" → 실시간 검색 결과
- 후속 메시지로 날짜/공항/목적지 변경 가능
- 현재 UK/US 한정, 항공편만 지원
- 기존 Skyscanner 검색 엔진 위에 ChatGPT 인터페이스 레이어

**출처:**
- [Skyscanner ChatGPT App — Globetrender](https://globetrender.com/2026/02/27/skyscanner-launches-chatgpt-app-flight-search/)
- [Skyscanner + ChatGPT — AltexSoft](https://www.altexsoft.com/travel-industry-news/skyscanner-adds-flight-search-in-chatgpt/)
- [Skyscanner ChatGPT — TravelPulse](https://www.travelpulse.com/news/airlines-airports/skyscanner-launches-app-within-chatgpt-to-help-travelers-find-cheap-flights)
- [Skyscanner ChatGPT — PhocusWire](https://www.phocuswire.com/news/technology/skyscanner-chatgpt-app-flights)

#### PROS Fare Finder Agent
- AI 위젯으로 항공사 웹사이트에 내장
- 대화형 인터페이스로 최저가 검색

**출처:** [PROS Fare Finder Agent](https://pros.com/learn/blog/fare-finder-agent-ai-widget-changing-how-travelers-book-flights/)

### 5-2. LLM + 항공편 API 통합

#### Kiwi.com MCP Server (2025.08 출시)
- **Model Context Protocol** 서버 — ChatGPT Pro, Claude, Cursor에서 자연어로 항공편 검색
- `search-flight` 도구: 자연어 → 구조화된 API 호출 자동 변환
- AI 에이전트용 인터페이스 (사람/스크립트 대상 아님)
- Alpic.ai와 협업 개발

**출처:**
- [Kiwi MCP Server — Alpic AI](https://alpic.ai/blog/behind-the-kiwi-com-mcp-server-deploying-an-agentic-flight-booking-service)
- [Kiwi Flight Search MCP Guide — Skywork](https://skywork.ai/skypage/en/kiwi-flight-search-ai-engineer-guide/1978646492166266880)

#### LLM + Duffel API 오픈소스
- **llm-flight-booking** (GitHub): GPT + Duffel API로 자연어 항공편 예약
- **LangChain/Langflow**: 여행 에이전트 빌드 프레임워크
- **Semantic Kernel (MS)**: AutoGen으로 AI 에이전트 항공편 예약 데모

**출처:**
- [LLM Flight Booking — GitHub](https://github.com/mnpatil17/llm-flight-booking)
- [Secure Flight Booking with LLM Agent — DEV](https://dev.to/permit_io/building-a-secure-flight-booking-system-with-llm-agent-in-langflow-3ml)
- [AI Flight Booking Agents — GitHub](https://github.com/HenriSchulte-MS/FlightBookingWithAIAgents)

### 5-3. 현재 한계점
- **환각(Hallucination)**: LLM이 실제 존재하지 않는 항공편/가격 제시 위험 → 검증 단계 필수
- **소비자 신뢰도 낮음**: 완전 자율 AI 예약에 대한 소비자 저항 존재
- **정확도**: LLM은 "추정"하는 경향 — 여행 예약에서는 위험
- **예상 타임라인**: 2027년경 결제/여권/개인화까지 포함한 엔드투엔드 여행 컨시어지 등장 전망

**출처:**
- [Automated Flight Booking by AI — World Aviation Festival](https://worldaviationfestival.com/blog/airlines/automated-flight-booking-by-ai-how-close-are-we/)
- [AI Flight Booking Revolution 2026 — My AI Front Desk](https://www.myaifrontdesk.com/blogs/how-ai-for-flight-booking-is-revolutionizing-the-way-we-find-cheap-flights-in-2026)
- [5 AI Tools Flight Search Test — Medium](https://medium.com/@patrick.taylor_5595/i-asked-5-ai-tools-to-find-me-a-cheap-flight-heres-what-actually-happened-7f7598b620eb)

### 5-4. 플라이딜 AI 검색 UX 적용 방안

```
[사용자: "4월에 제주도 2박3일 제일 싼 거"]
    ↓ LLM 의도 파싱
[구조화: origin=GMP/ICN, dest=CJU, dates=4월 중 2박3일, sort=price]
    ↓ Duffel/Kiwi API 호출
[검색 결과 + LLM 요약]
    ↓
[사용자: "직항만 보여줘" / "5만원대 없어?"]
    ↓ 필터 적용 + 재검색
```

- **차별화 포인트**: 한국어 자연어 검색 (Skyscanner ChatGPT는 영어 UK/US 한정)
- **기술 스택**: Vercel AI SDK로 스트리밍 응답 + Duffel API + 한국어 LLM
- **검증 필수**: LLM 출력 → API 실제 결과와 교차 검증

---

## 종합 시사점 & 플라이딜 권장 전략

### 기술 스택
- **프론트엔드**: Next.js App Router + Server Components + Suspense Streaming
- **API 통합**: Duffel (FSC/NDC) + Kiwi Tequila (LCC) 이중 전략
- **가격 알림**: Vercel Cron → QStash 확장
- **AI 검색**: Vercel AI SDK + Server Actions

### 한국 시장 전략
- Duffel로 대한항공/아시아나 커버
- Kiwi Tequila로 한국 LCC + 최저가 다구간 커버
- 제주항공 NDC 직접 연동 검토 (한국 LCC 유일 옵션)
- 한국어 자연어 검색으로 Skyscanner ChatGPT와 차별화

### MVP 우선순위
1. Duffel Sandbox로 항공편 검색 프로토타입
2. Next.js App Router + Streaming으로 검색 UX
3. Kiwi Tequila 연동으로 LCC 커버리지 확장
4. 가격 알림 (Vercel Cron 기반)
5. AI 자연어 검색 (한국어)
