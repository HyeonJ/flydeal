# 플라이딜 Duffel API 구현 가이드 (2026-03-23)

## 1. 시작하기

### 계정 생성 + API 키
1. https://app.duffel.com 가입 (1분)
2. Developers → Access tokens → 토큰 생성
3. 테스트 모드(sandbox)로 시작 — 실제 예약 안 됨, 무료

### Node.js SDK 설치
```bash
npm install @duffel/api
```

### 초기화
```javascript
import { Duffel } from '@duffel/api'
const duffel = new Duffel({
  token: process.env.DUFFEL_ACCESS_TOKEN,
})
```

## 2. 핵심 API 플로우

### 항공편 검색 (Offer Request)
```javascript
const offerRequest = await duffel.offerRequests.create({
  slices: [{
    origin: 'ICN',      // 인천
    destination: 'NRT', // 나리타 (도쿄)
    departure_date: '2026-04-17',
  }],
  passengers: [{ type: 'adult' }],
  cabin_class: 'economy',
  return_offers: false, // true면 결과 즉시 반환 (소규모)
})
```

### 검색 결과 조회 (Offers)
```javascript
const offers = await duffel.offers.list({
  offer_request_id: offerRequest.data.id,
  sort: 'total_amount',  // 최저가순
  limit: 20,
})

// 각 offer에 포함된 정보:
// - total_amount: 총 가격
// - total_currency: 통화
// - slices[].segments[]: 각 구간 (출발/도착 시간, 항공사, 경유 등)
// - owner: 항공사 정보
```

### 예약 생성 (Order)
```javascript
const order = await duffel.orders.create({
  selected_offers: [offerId],
  payments: [{
    type: 'balance',
    amount: offer.total_amount,
    currency: offer.total_currency,
  }],
  passengers: [{
    id: offer.passengers[0].id,
    given_name: 'Hyeonin',
    family_name: 'Jeong',
    born_on: '1995-01-01',
    // ...
  }],
})
```

## 3. 플라이딜 MVP에 필요한 API 엔드포인트

| 기능 | Duffel API | 메서드 |
|------|-----------|--------|
| 항공편 검색 | Offer Requests | `duffel.offerRequests.create()` |
| 검색 결과 | Offers | `duffel.offers.list()` |
| 항공편 상세 | Offers | `duffel.offers.get()` |
| 좌석 선택 | Seat Maps | `duffel.seatMaps.list()` |
| 수하물 추가 | Offer Passengers | offer에 포함 |
| 예약 | Orders | `duffel.orders.create()` |
| 예약 취소 | Order Cancellations | `duffel.orderCancellations.create()` |

## 4. 가격 알림 구현 방안

Duffel은 가격 알림 API가 없으므로 직접 구현:

```javascript
// cron으로 매시간 실행
async function checkPriceAlert(alert) {
  const offers = await duffel.offerRequests.create({
    slices: [{ origin: alert.origin, destination: alert.destination, departure_date: alert.date }],
    passengers: [{ type: 'adult' }],
    return_offers: true,
  })

  const cheapest = offers.data.offers
    .sort((a, b) => parseFloat(a.total_amount) - parseFloat(b.total_amount))[0]

  if (parseFloat(cheapest.total_amount) <= alert.target_price) {
    sendNotification(alert.user, cheapest) // 이메일/푸시
  }

  // 가격 히스토리 저장
  savePriceHistory(alert.route, cheapest.total_amount, new Date())
}
```

## 5. AI 자연어 검색 (Claude API 연동)

```javascript
// 사용자 입력: "4월 셋째주 도쿄 가장 싼 비행기"
// Claude가 파싱 → Duffel 검색 파라미터로 변환

const response = await anthropic.messages.create({
  model: 'claude-sonnet-4-6',
  messages: [{ role: 'user', content: userQuery }],
  tools: [{
    name: 'search_flights',
    description: '항공편 검색',
    input_schema: {
      type: 'object',
      properties: {
        origin: { type: 'string', description: 'IATA 공항 코드 (예: ICN)' },
        destination: { type: 'string' },
        departure_date: { type: 'string', format: 'date' },
      },
      required: ['origin', 'destination', 'departure_date']
    }
  }]
})
// → Claude가 tool_use로 search_flights 호출 → Duffel API 실행
```

## 6. Duffel MCP Server (최신)

Duffel MCP Server가 출시됨 — Claude Desktop/Claude Code에서 직접 항공편 검색 가능:
- https://lobehub.com/mcp/naren8642-duffel-mcp
- 대화형으로 "도쿄 4월 17일 항공편 검색해줘" → 바로 결과

## 7. 주의사항

- **Sandbox 모드**: 테스트용. 실제 예약 안 됨. 가상 항공사 데이터 반환
- **Live 모드**: 실제 예약 가능. Duffel에 사업자 인증 필요
- **Rate Limit**: 분당 100 요청 (sandbox), 분당 1000 요청 (live)
- **가격 캐싱**: 검색 결과 캐싱 필수 (같은 요청 반복 방지)
- **Offer 만료**: 검색 결과(offer)는 보통 30분~24시간 후 만료

**출처:**
- https://duffel.com/docs/guides/getting-started-with-flights
- https://www.npmjs.com/package/@duffel/api
- https://github.com/duffelhq/hackathon-starter-kit
- https://skywork.ai/skypage/en/ai-engineer-conversational-flight-search/1978387508614516736
- https://lobehub.com/mcp/naren8642-duffel-mcp

---

## 2차 보강: 실전 구현 가이드 업데이트 (2026-03-30)

### Duffel Node.js SDK (v4.21.6, 2026-03-19)
- `npm i @duffel/api`, Node.js v18+ 필수
- **HTTP Streaming**: `/air/offers`에서 지원. chunked JSON 라인별 파싱
- **Batch Offer Requests**: `POST /air/batch_offer_requests` → poll `remaining_batches=0`까지. supplier_timeout 2~60초
- **@duffel/components**: 검색 결과, 좌석 선택 등 React 컴포넌트 제공
- 출처: https://www.npmjs.com/package/@duffel/api

### 자연어 검색 구현 방법 3가지
1. **MCP Server (Claude용 가장 쉬움)**: ravinahp/flights-mcp — 라운드트립/편도/다구간 지원
   - 출처: https://github.com/ravinahp/flights-mcp
2. **Tool Use**: origin/destination/date/passengers 스키마 정의 → Claude가 자연어 파싱
3. **오픈소스 참고**: llm-flight-booking (Duffel+LLM 풀 통합)
   - 출처: https://github.com/mnpatil17/llm-flight-booking

### Next.js + Duffel 통합
- 공식 보일러플레이트 없음. **Duffel Hackathon Starter Kit** 시작점
  - 출처: https://github.com/duffelhq/hackathon-starter-kit
- App Router: `app/api/flights/search/route.ts`에서 `duffel.offerRequests.create()`
- UI: `@duffel/components` npm 패키지로 가속

### 가격 알림 아키텍처
- **하이브리드**: Cron (15분 폴링) + Event (10%+ 가격 변동 시 즉시 알림)
- **DB**: TimescaleDB (PostgreSQL 확장) — time-series 데이터 최적화
- 스키마: `(time, flight_id, route, departure_date, price, airline)`, LAG() 윈도우 함수로 변화 감지
- 오픈소스: flight-spy (15분 폴링 + Slack/이메일)
  - 출처: https://github.com/jeancsil/flight-spy

### 제주항공 NDC API
- **공개 개발자 포털 없음**. APG 플랫폼 통해 배포 (XML, IATA NDC 표준)
- 접근 방법: APG 파트너십 신청(apg-ga.com) 또는 Amadeus GDS 경유
- 기능: 쇼핑/예약, 취소/환불, 부가서비스(수하물/기내식), 좌석 선택(예정)
- 한국 LCC 중 유일한 NDC 채택. 진에어/에어부산/에어서울은 미채택
- 출처: https://apg-ga.com/jeju-airs-full-content-is-now-available-on-the-apg-platform/

### 플라이딜 MVP 다음 단계
1. Duffel Hackathon Starter Kit clone → Next.js App Router로 전환
2. duffel.offerRequests.create()로 기본 검색 구현
3. MCP Server(flights-mcp) 연동으로 자연어 검색 추가
4. TimescaleDB로 가격 히스토리 저장 + 알림 시스템
5. @duffel/components로 UI 빠르게 구성

---

## 3차 보강 (2026-03-31): Next.js App Router 실전 통합 + 가격 알림 + API 비교 + 한국 LCC + AI 검색

### Next.js App Router + Duffel 실전 패턴
- Duffel JS SDK는 **서버 전용** — Route Handlers 또는 Server Actions에서만 사용
- Server Actions + **next-safe-action**으로 인증/rate limiting 미들웨어 적용 권장
- **Suspense Streaming**으로 검색 결과 점진적 렌더링 가능 (Duffel HTTP Streaming과 결합)
- Sources: https://nextjs.org/docs/app, https://next-safe-action.dev/

### 가격 알림 아키텍처 (스케일별)
| 단계 | 도구 | 적합한 경우 |
|------|------|-----------|
| 소규모 | **Vercel Cron** | MVP, 일 수백 건 이하 |
| 중규모 | **QStash** (Upstash) | 수천 건, 비동기 큐 필요 |
| 대규모 | Self-hosted Worker | 수만 건, 커스텀 스케줄링 |
- n8n 워크플로우에 Aviation Stack + Gmail/Telegram 연동 레퍼런스 다수
- ML 가격 예측: AirHint (80%+), Hopper (95%) — 대량 데이터 필요
- Sources: https://vercel.com/templates/next.js/vercel-cron, https://upstash.com/blog/qstash-periodic-data-updates

### API 경쟁사 비교 (2026)
| API | 항공사 커버리지 | 무료 티어 | 특징 |
|-----|---------------|----------|------|
| **Duffel** | 300+ | 테스트 무료, 프로덕션 커미션 기반 | DX 최고, IATA 불필요, 한국 LCC 미지원 |
| **Amadeus** | 435+ | 월 2000 호출 | 최대 커버리지, 학습 곡선 높음 |
| **Kiwi Tequila** | 800+ | 검색 무료 | 가상 인터라이닝, 최저가 다구간, MCP 서버 출시 |
- **Kiwi MCP Server** (2025.08): Claude/ChatGPT에서 자연어 검색 가능
- **플라이딜 전략**: Duffel (메인) + Kiwi (LCC 보완) 이중 API
- Sources: https://dev.to/ravi_makhija/duffel-vs-amadeus-which-works-better-for-modern-api-first-integration-49p4, https://www.scrapingbee.com/blog/top-flights-apis-for-travel-apps/

### 한국 LCC 접근 현황 (2026)
| 항공사 | API 접근 | 비고 |
|--------|---------|------|
| 대한항공 | Duffel ✅ (GDS) | 풀 콘텐츠 |
| 아시아나 | Duffel ✅ (GDS) | 풀 콘텐츠 |
| **제주항공** | **APG NDC** ✅ | 한국 LCC 유일 개발자 접근 가능 |
| 진에어 | ❌ | 메가 LCC 통합 예정 (2026 이후) |
| 에어부산 | ❌ | 아시아나 자회사, 통합 대기 |
| 에어서울 | ❌ | 아시아나 자회사 |
| 티웨이 | ❌ | Trinity Air 리브랜딩 예정, NDC 없음 |
- Sources: https://duffel.com/flights/airlines/korean-air, https://airtravelinfo.kr/wiki/index.php/NDC

### AI 항공편 검색 동향
- **Skyscanner + ChatGPT** (2026.02): 자연어 항공편 검색 프로덕션 출시 (UK/US 한정)
- **Google Agentic Travel** (2025.11): 에이전틱 여행 예약 구축 중
- **플라이딜 차별화**: **한국어 자연어 검색** (경쟁사 영어 한정)
- 구현: Vercel AI SDK + Duffel/Kiwi API 조합
- 주의: LLM 환각 위험 → API 결과와 교차 검증 필수
- Sources: https://globetrender.com/2026/02/27/skyscanner-launches-chatgpt-app-flight-search/, https://alpic.ai/blog/behind-the-kiwi-com-mcp-server-deploying-an-agentic-flight-booking-service

### 상세 리서치
- 전체 내용: memory/research-results/2026-03-30-flydeal-tech-research.md

---

## 4차 보강 (2026-04-01): 테스트 전략 + 배포 가이드

### 테스트 전략 (Next.js App Router 2026 표준)

#### 테스트 스택
```
Unit/Component: Vitest + React Testing Library (Jest 대체, 더 빠름)
API Mocking: MSW (Mock Service Worker) — 네트워크 레벨 모킹
E2E: Playwright — 크로스 브라우저 자동화
```

#### 테스트 피라미드 (플라이딜 적용)

**1. Unit Tests (Vitest) — 빠르고 많이**
- Duffel API 응답 파싱 로직
- 가격 비교/정렬 알고리즘
- 날짜/시간 변환 유틸리티
- 검색 필터 로직

```typescript
// 예: 항공편 가격 정렬 테스트
import { describe, it, expect } from 'vitest'
import { sortByPrice } from '@/lib/flights'

describe('sortByPrice', () => {
  it('최저가순 정렬', () => {
    const offers = [
      { total_amount: '300.00' },
      { total_amount: '150.00' },
      { total_amount: '200.00' },
    ]
    const sorted = sortByPrice(offers)
    expect(sorted[0].total_amount).toBe('150.00')
  })
})
```

**2. Integration Tests (Vitest + MSW) — API 연동 검증**
- Duffel API 호출 → 응답 처리 → UI 렌더링 흐름
- MSW로 Duffel API 모킹 (네트워크 레벨)
- Server Actions 테스트

```typescript
// MSW handler 예시
import { http, HttpResponse } from 'msw'

export const handlers = [
  http.post('https://api.duffel.com/air/offer_requests', () => {
    return HttpResponse.json({
      data: { id: 'orq_test', offers: [...] }
    })
  }),
]
```

**3. E2E Tests (Playwright) — 사용자 흐름 검증**
- 검색 → 결과 → 상세 → 예약 전체 흐름
- 비동기 Server Components (Vitest에서 미지원) → E2E로 커버
- Playwright의 route.fulfill()로 외부 API 가로채기

```typescript
// Playwright로 Duffel API 모킹
test('항공편 검색', async ({ page }) => {
  await page.route('**/api/search', route =>
    route.fulfill({ json: mockSearchResults })
  )
  await page.goto('/search')
  await page.fill('[name=origin]', 'ICN')
  await page.fill('[name=destination]', 'NRT')
  await page.click('button[type=submit]')
  await expect(page.locator('.flight-card')).toHaveCount(5)
})
```

#### ⚠️ 주의: 비동기 Server Components
- Vitest에서 async Server Components **미지원** (2026 현재)
- 동기 Server/Client Components → Vitest로 단위 테스트
- 비동기 Server Components → **Playwright E2E로 테스트**

#### 테스트 커버리지 목표
- Unit: 80%+ (핵심 로직)
- Integration: 주요 API 흐름 3~5개
- E2E: 핵심 사용자 시나리오 3개 (검색, 필터, 가격 알림 설정)

### 배포 가이드 (플라이딜)

#### 추천: Vercel Free Tier (포트폴리오/MVP)

**Vercel 무료 티어:**
- 100GB 대역폭/월
- 100K 서버리스 함수 호출/월
- 무제한 배포
- 자동 HTTPS + 글로벌 CDN
- Cold start <100ms

**설정 순서:**
1. `npm i -g vercel` → `vercel` 실행 → GitHub 연동
2. 환경변수 설정: `vercel env add DUFFEL_API_TOKEN`
3. Git push → 자동 배포 (Preview + Production)

#### CI/CD: GitHub Actions + Vercel

```yaml
# .github/workflows/ci.yml
name: CI/CD
on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with: { node-version: 22 }
      - run: npm ci
      - run: npm run test          # Vitest
      - run: npx playwright install --with-deps
      - run: npm run test:e2e      # Playwright

  deploy:
    needs: test
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: amondnet/vercel-action@v25
        with:
          vercel-token: ${{ secrets.VERCEL_TOKEN }}
          vercel-org-id: ${{ secrets.VERCEL_ORG_ID }}
          vercel-project-id: ${{ secrets.VERCEL_PROJECT_ID }}
          vercel-args: '--prod'
```

#### 환경변수 관리

```
# Vercel 환경별 분리
Production:  DUFFEL_API_TOKEN (본 키)
Preview:     DUFFEL_API_TOKEN (테스트 키)
Development: .env.local (로컬)
```

- `vercel env pull` → 로컬에 .env.local 자동 생성
- Duffel 테스트 모드: `duffel_test_` prefix 키 사용

#### 배포 플랫폼 비교 (플라이딜 기준)

| 플랫폼 | 월 비용 | Cold Start | 추천 시점 |
|--------|---------|-----------|----------|
| **Vercel Free** | $0 | <100ms | MVP/포트폴리오 ✅ |
| Vercel Pro | $20 | <100ms | 트래픽 증가 시 |
| Railway | $5~ | 500ms | DB 포함 필요 시 |
| DigitalOcean | $5~12 | 1~2s | 예산 절약 |
| AWS ECS | $15~50+ | 가변 | 대규모 서비스 |

**플라이딜 MVP → Vercel Free** (100K 호출이면 일 ~3,300회, 포트폴리오로 충분)

#### 포트폴리오 어필 포인트
- **테스트 코드 존재**: "대부분의 사이드 프로젝트에 테스트가 없는데, 이 프로젝트는 3레이어 테스트 전략 적용"
- **CI/CD 파이프라인**: "PR마다 자동 테스트 → 통과 시만 배포" = 실무 수준
- **환경 분리**: "Production/Preview/Development 환경변수 분리" = 운영 경험

### 출처
- https://nextjs.org/docs/app/guides/testing/vitest
- https://nextjs.org/docs/app/guides/testing
- https://shinagawa-web.com/en/blogs/nextjs-app-router-testing-setup
- https://dev.to/zahg_81752b307f5df5d56035/the-complete-guide-to-deploying-nextjs-apps-in-2026
- https://vercel.com/kb/guide/how-can-i-use-github-actions-with-vercel
- https://dev.to/dumebii/how-to-e2e-test-ai-agents-mocking-api-responses-with-playwright-in-nextjs-nic
