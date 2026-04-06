# API 연동 가이드

## 개요

플라이딜은 두 개의 항공편 API를 병렬 호출하여 결과를 병합한다.

| API | 역할 | 커버리지 | 인증 |
|-----|------|----------|------|
| **Duffel** | FSC (풀서비스) | 대한항공, 아시아나 (GDS 경유) | Bearer Token |
| **Kiwi Tequila** | LCC (저비용) | 제주항공, 진에어, 에어부산, 에어서울 | apikey 헤더 |

---

## 1. Duffel API

### 1-1. 인증

```
Authorization: Bearer duffel_test_xxxxxxxxxxxxx
```

- Sandbox 토큰: `duffel_test_` 접두사 (가상 데이터, 실제 예약 불가)
- Live 토큰: `duffel_live_` 접두사 (사업자 인증 필요)
- 발급: https://app.duffel.com → Developers → Access tokens

### 1-2. 항공편 검색 (Offer Request)

**요청**

```http
POST https://api.duffel.com/air/offer_requests
Content-Type: application/json
Accept: application/json
Duffel-Version: v2
Authorization: Bearer duffel_test_xxxxxxxxxxxxx
```

```json
{
  "data": {
    "slices": [
      {
        "origin": "ICN",
        "destination": "NRT",
        "departure_date": "2026-05-22"
      },
      {
        "origin": "NRT",
        "destination": "ICN",
        "departure_date": "2026-05-25"
      }
    ],
    "passengers": [
      { "type": "adult" }
    ],
    "cabin_class": "economy",
    "return_offers": false
  }
}
```

- `slices`: 각 구간 (편도=1개, 왕복=2개)
- `return_offers: false` → 대규모 검색 시 비동기 처리 (offer_request_id로 후속 조회)
- `return_offers: true` → 소규모 검색 시 즉시 응답에 offers 포함

**응답**

```json
{
  "data": {
    "id": "orq_0000ABcDeFgHiJkLmN",
    "live_mode": false,
    "created_at": "2026-05-01T10:00:00Z",
    "slices": [...],
    "offers": []
  }
}
```

### 1-3. 검색 결과 조회 (Offers)

**요청**

```http
GET https://api.duffel.com/air/offers?offer_request_id=orq_0000ABcDeFgHiJkLmN&sort=total_amount&limit=20
Authorization: Bearer duffel_test_xxxxxxxxxxxxx
Duffel-Version: v2
```

**응답**

```json
{
  "data": [
    {
      "id": "off_0000ABcDeFgHiJkLmN",
      "total_amount": "185000.00",
      "total_currency": "KRW",
      "tax_amount": "28000.00",
      "base_amount": "157000.00",
      "owner": {
        "name": "Korean Air",
        "iata_code": "KE",
        "logo_symbol_url": "https://assets.duffel.com/img/airlines/for-light-background/full-color-logo/KE.svg"
      },
      "slices": [
        {
          "origin": {
            "iata_code": "ICN",
            "name": "Incheon International Airport",
            "city_name": "Seoul"
          },
          "destination": {
            "iata_code": "NRT",
            "name": "Narita International Airport",
            "city_name": "Tokyo"
          },
          "duration": "PT2H30M",
          "segments": [
            {
              "operating_carrier": { "iata_code": "KE", "name": "Korean Air" },
              "departing_at": "2026-05-22T10:00:00",
              "arriving_at": "2026-05-22T12:30:00",
              "origin": { "iata_code": "ICN" },
              "destination": { "iata_code": "NRT" },
              "duration": "PT2H30M",
              "stops": []
            }
          ]
        }
      ],
      "passengers": [
        { "id": "pas_0000ABcDeFgHiJkLmN", "type": "adult" }
      ],
      "expires_at": "2026-05-01T10:30:00Z"
    }
  ],
  "meta": {
    "after": "cursor_value",
    "before": null,
    "limit": 20
  }
}
```

**핵심 필드 매핑 → FlightOffer**

| Duffel 응답 | FlightOffer 필드 |
|-------------|-----------------|
| `id` | `id` |
| `"DUFFEL"` (하드코딩) | `source` |
| `owner.name` | `airline` |
| `owner.logo_symbol_url` | `airlineLogo` |
| `slices[0].segments[0].departing_at` | `departureTime` |
| `slices[0].segments[-1].arriving_at` | `arrivalTime` |
| `slices[0].origin.iata_code` | `origin` |
| `slices[0].destination.iata_code` | `destination` |
| `slices[0].duration` (ISO 8601) | `duration` |
| `slices[0].segments.length - 1` | `stops` |
| `total_amount` | `price` |
| `total_currency` | `currency` |
| `null` | `deepLink` (Duffel은 deep link 없음) |

### 1-4. Spring Boot 구현 패턴

```java
@Service
@RequiredArgsConstructor
public class DuffelClient {

    private final RestClient restClient;

    @Value("${duffel.api-token}")
    private String apiToken;

    private static final String BASE_URL = "https://api.duffel.com";

    public DuffelOfferRequestResponse searchFlights(FlightSearchRequest request) {
        DuffelOfferRequestBody body = DuffelOfferRequestBody.builder()
            .slices(buildSlices(request))
            .passengers(List.of(Map.of("type", "adult")))
            .cabinClass(request.getCabinClass())
            .returnOffers(true)
            .build();

        return restClient.post()
            .uri(BASE_URL + "/air/offer_requests")
            .header("Authorization", "Bearer " + apiToken)
            .header("Duffel-Version", "v2")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("data", body))
            .retrieve()
            .body(DuffelOfferRequestResponse.class);
    }
}
```

### 1-5. Sandbox 특이사항

- 가상 항공사/항공편 데이터 반환 (실제 항공편 아님)
- 가격은 랜덤하게 생성됨
- Offer 만료: 30분
- Rate limit: **분당 100 요청**
- Live 모드: 분당 1,000 요청

### 1-6. 에러 처리

| HTTP 코드 | 의미 | 대응 |
|-----------|------|------|
| 400 | 잘못된 요청 (IATA 코드 오류 등) | 요청 파라미터 검증 |
| 401 | 인증 실패 | 토큰 확인 |
| 422 | 유효성 검증 실패 (과거 날짜 등) | 에러 메시지 사용자에게 표시 |
| 429 | Rate limit 초과 | 재시도 (Retry-After 헤더 참고) |
| 500 | 서버 에러 | fallback → Kiwi 결과만 반환 |

**에러 응답 형식**

```json
{
  "errors": [
    {
      "type": "invalid_request_error",
      "title": "Validation error",
      "message": "departure_date must be in the future",
      "code": "validation_error"
    }
  ]
}
```

---

## 2. Kiwi Tequila API

### 2-1. 인증

```
apikey: your_tequila_api_key
```

- 헤더에 `apikey` 키로 전달 (Bearer 아님)
- 포털: https://tequila.kiwi.com/portal/login
- **주의**: 현재 초대 전용 (50K MAU 이상). 기존 키가 없으면 MCP 서버 대안 검토

### 2-2. 항공편 검색 (날짜 지정)

**요청**

```http
GET https://api.tequila.kiwi.com/v2/search?fly_from=ICN&fly_to=NRT&date_from=22/05/2026&date_to=22/05/2026&return_from=25/05/2026&return_to=25/05/2026&adults=1&curr=KRW&locale=ko&flight_type=round
apikey: your_api_key
```

**주요 파라미터**

| 파라미터 | 설명 | 예시 |
|----------|------|------|
| `fly_from` | 출발지 IATA | `ICN` |
| `fly_to` | 도착지 IATA | `NRT` |
| `date_from` | 출발일 시작 (DD/MM/YYYY) | `22/05/2026` |
| `date_to` | 출발일 끝 | `22/05/2026` |
| `return_from` | 귀국일 시작 | `25/05/2026` |
| `return_to` | 귀국일 끝 | `25/05/2026` |
| `adults` | 성인 수 | `1` |
| `curr` | 통화 | `KRW` |
| `locale` | 언어 | `ko` |
| `flight_type` | 편도/왕복 | `round` / `oneway` |
| `max_stopovers` | 최대 경유 수 | `0` (직항만) |
| `sort` | 정렬 | `price` / `duration` |
| `limit` | 결과 수 | `20` |

**응답**

```json
{
  "search_id": "abc123",
  "currency": "KRW",
  "data": [
    {
      "id": "1a2b3c4d5e6f",
      "price": 185000,
      "airlines": ["7C"],
      "route": [
        {
          "flyFrom": "ICN",
          "flyTo": "NRT",
          "cityFrom": "Seoul",
          "cityTo": "Tokyo",
          "local_departure": "2026-05-22T08:00:00.000Z",
          "local_arrival": "2026-05-22T10:30:00.000Z",
          "airline": "7C",
          "flight_no": 1234,
          "operating_carrier": "7C"
        },
        {
          "flyFrom": "NRT",
          "flyTo": "ICN",
          "local_departure": "2026-05-25T18:00:00.000Z",
          "local_arrival": "2026-05-25T21:00:00.000Z",
          "airline": "7C",
          "flight_no": 1235,
          "operating_carrier": "7C"
        }
      ],
      "deep_link": "https://www.kiwi.com/deep?...",
      "duration": {
        "departure": 9000,
        "return": 10800,
        "total": 19800
      },
      "fly_duration": "2h 30m",
      "return_duration": "3h 0m",
      "nightsInDest": 3,
      "quality": 150.5,
      "bags_price": {
        "1": 25000,
        "2": 50000
      },
      "availability": {
        "seats": 5
      }
    }
  ]
}
```

### 2-3. 유연한 날짜 검색 (핵심 기능)

Kiwi API의 `date_from`/`date_to` 범위와 `fly_days`를 조합하면 유연한 날짜 검색이 가능하다.

**요청 예시: 2026년 전체, 금요일 출발 ~ 월요일 귀국, 3박**

```http
GET https://api.tequila.kiwi.com/v2/search?fly_from=ICN&fly_to=NRT&date_from=01/01/2026&date_to=31/12/2026&return_from=01/01/2026&return_to=31/12/2026&nights_in_dst_from=3&nights_in_dst_to=3&fly_days=5&ret_fly_days=1&adults=1&curr=KRW&sort=price&limit=50
apikey: your_api_key
```

**유연 검색 전용 파라미터**

| 파라미터 | 설명 | 예시 |
|----------|------|------|
| `date_from` / `date_to` | 출발일 검색 범위 | `01/01/2026` ~ `31/12/2026` |
| `return_from` / `return_to` | 귀국일 검색 범위 | 출발 범위와 동일하게 |
| `nights_in_dst_from` | 최소 숙박일 | `3` |
| `nights_in_dst_to` | 최대 숙박일 | `3` (같으면 고정) |
| `fly_days` | 출발 요일 (0=일, 1=월, ..., 5=금, 6=토) | `5` (금요일) |
| `ret_fly_days` | 귀국 요일 | `1` (월요일) |

**응답에서 FlexibleSearchResult 변환**

```java
// Kiwi 응답의 각 항목에서 추출
FlexibleSearchResult result = FlexibleSearchResult.builder()
    .departureDate(parseDate(item.getRoute().get(0).getLocalDeparture()))
    .returnDate(parseDate(item.getRoute().get(lastIndex).getLocalArrival()))
    .cheapestPrice(BigDecimal.valueOf(item.getPrice()))
    .currency("KRW")
    .airline(item.getAirlines().get(0))
    .stops(item.getRoute().size() / 2 - 1)  // 왕복 기준
    .offerId(item.getId())
    .build();
```

### 2-4. 공항 자동완성 (Locations)

**요청**

```http
GET https://api.tequila.kiwi.com/locations/query?term=tokyo&locale=ko&location_types=airport&limit=5
apikey: your_api_key
```

**응답**

```json
{
  "locations": [
    {
      "id": "NRT",
      "name": "나리타 국제공항",
      "code": "NRT",
      "city": { "name": "도쿄", "code": "TYO" },
      "country": { "name": "일본", "code": "JP" },
      "type": "airport"
    },
    {
      "id": "HND",
      "name": "하네다 공항",
      "code": "HND",
      "city": { "name": "도쿄", "code": "TYO" },
      "country": { "name": "일본", "code": "JP" },
      "type": "airport"
    }
  ]
}
```

### 2-5. Spring Boot 구현 패턴

```java
@Service
@RequiredArgsConstructor
public class KiwiClient {

    private final RestClient restClient;

    @Value("${kiwi.api-key}")
    private String apiKey;

    private static final String BASE_URL = "https://api.tequila.kiwi.com";

    public KiwiSearchResponse searchFlights(FlightSearchRequest request) {
        return restClient.get()
            .uri(BASE_URL + "/v2/search", uriBuilder -> {
                uriBuilder
                    .queryParam("fly_from", request.getOrigin())
                    .queryParam("fly_to", request.getDestination())
                    .queryParam("date_from", formatDate(request.getDepartureDate()))
                    .queryParam("date_to", formatDate(request.getDepartureDate()))
                    .queryParam("flight_type", request.getReturnDate() != null ? "round" : "oneway")
                    .queryParam("adults", request.getPassengers())
                    .queryParam("curr", "KRW")
                    .queryParam("locale", "ko")
                    .queryParam("sort", "price")
                    .queryParam("limit", 20);
                if (request.getReturnDate() != null) {
                    uriBuilder
                        .queryParam("return_from", formatDate(request.getReturnDate()))
                        .queryParam("return_to", formatDate(request.getReturnDate()));
                }
                return uriBuilder.build();
            })
            .header("apikey", apiKey)
            .retrieve()
            .body(KiwiSearchResponse.class);
    }

    public KiwiSearchResponse searchFlexible(FlexibleSearchRequest request) {
        return restClient.get()
            .uri(BASE_URL + "/v2/search", uriBuilder -> uriBuilder
                .queryParam("fly_from", request.getOrigin())
                .queryParam("fly_to", request.getDestination())
                .queryParam("date_from", formatDate(request.getDateFrom()))
                .queryParam("date_to", formatDate(request.getDateTo()))
                .queryParam("return_from", formatDate(request.getDateFrom()))
                .queryParam("return_to", formatDate(request.getDateTo()))
                .queryParam("nights_in_dst_from", request.getNights())
                .queryParam("nights_in_dst_to", request.getNights())
                .queryParam("fly_days", request.getDepartureWeekday().getValue() % 7)
                .queryParam("ret_fly_days", request.getReturnWeekday().getValue() % 7)
                .queryParam("adults", request.getPassengers())
                .queryParam("curr", "KRW")
                .queryParam("sort", "price")
                .queryParam("limit", 50)
                .build())
            .header("apikey", apiKey)
            .retrieve()
            .body(KiwiSearchResponse.class);
    }

    // Kiwi 날짜 형식: DD/MM/YYYY
    private String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
```

### 2-6. 핵심 필드 매핑 → FlightOffer

| Kiwi 응답 | FlightOffer 필드 |
|-----------|-----------------|
| `id` | `id` |
| `"KIWI"` (하드코딩) | `source` |
| `airlines[0]` (IATA 코드) | `airline` (코드→이름 변환 필요) |
| IATA 코드 기반 로고 URL | `airlineLogo` |
| `route[0].local_departure` | `departureTime` |
| `route[0].local_arrival` (편도) 또는 `route[귀국편].local_arrival` | `arrivalTime` |
| `route[0].flyFrom` | `origin` |
| `route[0].flyTo` | `destination` |
| `duration.departure` (초 단위 → Duration 변환) | `duration` |
| `route` 내 가는편 세그먼트 수 - 1 | `stops` |
| `price` (정수, KRW) | `price` |
| `"KRW"` | `currency` |
| `deep_link` | `deepLink` |

**주의**: Kiwi `duration`은 **초 단위** (예: 9000 = 2시간 30분). `Duration.ofSeconds()`로 변환.

### 2-7. 에러 처리

| HTTP 코드 | 의미 | 대응 |
|-----------|------|------|
| 400 | 잘못된 파라미터 | 요청 검증 |
| 401 | API 키 오류 | 키 확인 |
| 429 | Rate limit | 재시도 |
| 500 | 서버 에러 | fallback → Duffel 결과만 반환 |

---

## 3. 결과 병합 전략

### 3-1. 통합 모델 변환

두 API 응답을 `FlightOffer` 통합 모델로 변환한 뒤 병합한다.

```java
@Service
@RequiredArgsConstructor
public class FlightSearchService {

    private final DuffelClient duffelClient;
    private final KiwiClient kiwiClient;

    public List<FlightOffer> search(FlightSearchRequest request) {
        // 병렬 호출
        CompletableFuture<List<FlightOffer>> duffelFuture = CompletableFuture
            .supplyAsync(() -> duffelClient.searchFlights(request))
            .thenApply(this::convertDuffelOffers)
            .exceptionally(ex -> {
                log.error("[search] Duffel API 실패", ex);
                return List.of();  // fallback: 빈 리스트
            });

        CompletableFuture<List<FlightOffer>> kiwiFuture = CompletableFuture
            .supplyAsync(() -> kiwiClient.searchFlights(request))
            .thenApply(this::convertKiwiOffers)
            .exceptionally(ex -> {
                log.error("[search] Kiwi API 실패", ex);
                return List.of();  // fallback: 빈 리스트
            });

        // 병합
        List<FlightOffer> duffelOffers = duffelFuture.join();
        List<FlightOffer> kiwiOffers = kiwiFuture.join();

        List<FlightOffer> merged = new ArrayList<>();
        merged.addAll(duffelOffers);
        merged.addAll(kiwiOffers);

        // 중복 제거 후 가격순 정렬
        return merged.stream()
            .filter(distinctByKey(this::deduplicationKey))
            .sorted(Comparator.comparing(FlightOffer::getPrice))
            .toList();
    }
}
```

### 3-2. 중복 판별 로직

같은 항공편이 두 API에 모두 나올 수 있다 (대한항공은 Duffel + Kiwi 둘 다 가능).

```java
// 중복 판별 키: 항공사 + 출발시간 + 도착시간 + 출발지 + 도착지
private String deduplicationKey(FlightOffer offer) {
    return offer.getAirline()
        + "|" + offer.getDepartureTime().truncatedTo(ChronoUnit.MINUTES)
        + "|" + offer.getArrivalTime().truncatedTo(ChronoUnit.MINUTES)
        + "|" + offer.getOrigin()
        + "|" + offer.getDestination();
}
```

**중복 시 우선순위**: 가격이 낮은 쪽 선택. 동일 가격이면 Duffel 우선 (공식 GDS 데이터).

### 3-3. 가격 통화 처리

- Duffel: `total_currency` 필드로 통화 확인. KRW 요청 시 KRW로 반환
- Kiwi: `curr=KRW` 파라미터로 KRW 지정 가능
- 둘 다 KRW로 요청하므로 별도 환율 변환 불필요

### 3-4. 병렬 호출 타임아웃

```java
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient duffelRestClient() {
        return RestClient.builder()
            .requestFactory(clientHttpRequestFactory(5000, 10000))  // connect 5초, read 10초
            .build();
    }

    @Bean
    public RestClient kiwiRestClient() {
        return RestClient.builder()
            .requestFactory(clientHttpRequestFactory(5000, 10000))
            .build();
    }

    private ClientHttpRequestFactory clientHttpRequestFactory(int connectTimeout, int readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeout));
        factory.setReadTimeout(Duration.ofMillis(readTimeout));
        return factory;
    }
}
```

- CompletableFuture 전체 타임아웃: 15초 (`orTimeout(15, TimeUnit.SECONDS)`)
- 개별 API 타임아웃: connect 5초, read 10초

---

## 4. 캐싱 전략 (Redis)

### 4-1. 캐시 키 설계

```
flight:search:{origin}:{destination}:{departureDate}:{returnDate}:{passengers}:{cabinClass}
flight:flexible:{origin}:{destination}:{depWeekday}:{retWeekday}:{nights}:{dateFrom}:{dateTo}
airport:search:{query}
```

**예시**

```
flight:search:ICN:NRT:2026-05-22:2026-05-25:1:economy
flight:flexible:ICN:NRT:FRIDAY:MONDAY:3:2026-01-01:2026-12-31
airport:search:tokyo
```

### 4-2. TTL (Time To Live)

| 캐시 대상 | TTL | 이유 |
|-----------|-----|------|
| 항공편 검색 결과 | **15분** | Offer 만료 (Duffel: 30분, 여유 포함) |
| 유연한 날짜 결과 | **30분** | 날짜 범위 검색은 변동 적음 |
| 공항 자동완성 | **24시간** | 거의 변하지 않는 데이터 |

### 4-3. Spring Boot 캐시 구현

```java
@Service
@RequiredArgsConstructor
public class CachedFlightSearchService {

    private final RedisTemplate<String, String> redisTemplate;
    private final FlightSearchService flightSearchService;
    private final ObjectMapper objectMapper;

    public List<FlightOffer> search(FlightSearchRequest request) {
        String cacheKey = buildCacheKey(request);
        String cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            log.info("[search] 캐시 히트: {}", cacheKey);
            return objectMapper.readValue(cached, new TypeReference<>() {});
        }

        List<FlightOffer> results = flightSearchService.search(request);
        redisTemplate.opsForValue().set(
            cacheKey,
            objectMapper.writeValueAsString(results),
            Duration.ofMinutes(15)
        );

        return results;
    }
}
```

### 4-4. 캐시 무효화

- TTL 기반 자동 만료 (주 전략)
- 사용자가 "새로고침" 클릭 시 강제 재검색 (`?refresh=true` → 캐시 무시)

---

## 5. 에러 처리 공통 전략

### 5-1. API 장애 시 Fallback

```
Duffel 성공 + Kiwi 성공 → 병합 결과 반환
Duffel 실패 + Kiwi 성공 → Kiwi 결과만 반환 (FSC 없음 안내)
Duffel 성공 + Kiwi 실패 → Duffel 결과만 반환 (LCC 없음 안내)
Duffel 실패 + Kiwi 실패 → 에러 응답 (재시도 안내)
```

한쪽 API가 실패해도 다른 쪽 결과는 반드시 반환한다.

### 5-2. 재시도 정책

```java
@Configuration
public class RetryConfig {
    // 429 (Rate Limit) 또는 5xx → 최대 2회 재시도
    // 지수 백오프: 1초 → 2초
    // 4xx (400, 401, 422) → 재시도 안 함 (요청 자체가 잘못됨)
}
```

### 5-3. 응답 형식

**성공**

```json
{
  "success": true,
  "data": {
    "offers": [...],
    "sources": ["DUFFEL", "KIWI"],
    "cached": false,
    "totalCount": 15
  }
}
```

**부분 실패 (한쪽 API 장애)**

```json
{
  "success": true,
  "data": {
    "offers": [...],
    "sources": ["KIWI"],
    "cached": false,
    "totalCount": 8,
    "warnings": ["Duffel API 일시 장애로 FSC 항공편이 포함되지 않았습니다"]
  }
}
```

**전체 실패**

```json
{
  "success": false,
  "message": "항공편 검색에 실패했습니다. 잠시 후 다시 시도해주세요."
}
```

---

## 6. 요일 변환 참고

Java `DayOfWeek` → Kiwi `fly_days` 변환:

| Java DayOfWeek | getValue() | Kiwi fly_days |
|----------------|-----------|---------------|
| MONDAY | 1 | 1 |
| TUESDAY | 2 | 2 |
| WEDNESDAY | 3 | 3 |
| THURSDAY | 4 | 4 |
| FRIDAY | 5 | 5 |
| SATURDAY | 6 | 6 |
| SUNDAY | 7 | 0 |

```java
// Java DayOfWeek → Kiwi fly_days
private int toKiwiFlyDays(DayOfWeek day) {
    return day.getValue() % 7;  // SUNDAY(7) → 0, 나머지 동일
}
```

---

## 참고 문서

- Duffel API: https://duffel.com/docs/api
- Duffel Getting Started: https://duffel.com/docs/guides/getting-started-with-flights
- Kiwi Tequila: https://tequila.kiwi.com/portal/docs/tequila_api
- Kiwi 검색 파라미터: https://tequila.kiwi.com/portal/docs/tequila_api/search_api
