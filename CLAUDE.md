# FlyDeal (플라이딜)

한국어 AI 자연어 검색 기반 항공편 가격 비교 서비스.
Duffel API(FSC) + Kiwi Tequila API(LCC) 이중 연동으로 한국 항공사 전체 커버.

## 기획 문서

- **제품 기획서**: `docs/planning/product-spec.md`
- **MVP 범위**: `docs/planning/mvp-scope.md`
- **API 연동 가이드**: `docs/planning/api-integration.md` — Duffel/Kiwi 요청·응답 예시, Spring Boot 구현 패턴, 결과 병합, Redis 캐싱, 에러 fallback
- **UI 와이어프레임**: `docs/planning/wireframe.md` — 3개 화면 ASCII 와이어프레임, 모바일 반응형, React 컴포넌트 구조
- **리서치**: `docs/research/`

## 기술 스택

| 레이어 | 기술 |
|--------|------|
| Backend | Spring Boot 3.2, Java 17, Gradle 8.6 (Kotlin DSL) |
| Frontend | React 18, Vite 5, TypeScript 5 |
| Database | MySQL 8, Redis 7 |
| API | Duffel API (FSC/NDC), Kiwi Tequila API (LCC) |

## 프로젝트 구조

```
flydeal/
├── backend/                    # Spring Boot
│   ├── src/main/java/com/flydeal/backend/
│   │   ├── controller/         # REST 컨트롤러
│   │   ├── service/            # 비즈니스 로직
│   │   ├── client/             # 외부 API 클라이언트 (Duffel)
│   │   ├── config/             # 설정
│   │   └── dto/                # 요청/응답 DTO
│   └── src/main/resources/
│       └── application.yml
├── frontend/                   # React + Vite + TypeScript
│   └── src/
│       ├── components/         # UI 컴포넌트
│       ├── api/                # API 호출
│       └── types/              # TypeScript 타입
├── docker-compose.yml          # MySQL + Redis
├── .env.example                # 환경변수 템플릿
└── CHANGELOG.md
```

## 로컬 실행

### 1. 환경변수 설정
```bash
cp .env.example .env
# .env 파일에 DUFFEL_API_TOKEN 등 입력
```

### 2. DB 실행
```bash
docker compose up -d
```

### 3. Backend
```bash
cd backend
./gradlew bootRun
# http://localhost:8081
```

### 4. Frontend
```bash
cd frontend
npm install
npm run dev
# http://localhost:5173
```

## 환경변수

`.env.example` 참조. Duffel sandbox 토큰은 `duffel_test_` 접두사.

## 코딩 컨벤션

### Java (Spring Boot)
- 네이밍: 클래스 UpperCamelCase, 메서드/변수 lowerCamelCase, 상수 UPPER_SNAKE_CASE
- Spring 클래스: `~Controller`, `~Service`, `~Repository`, `~Request`, `~Response`
- 메서드 30~50줄 이내, 파라미터 3개 이하 (초과 시 DTO)
- `@Transactional` 명시, 읽기 전용은 `readOnly = true`
- Lombok: `@Getter`, `@Builder`, `@RequiredArgsConstructor` 사용
- 와일드카드 import 금지

### Frontend (React + TypeScript)
- `var` 금지 → `const` 기본, 재할당 시 `let`
- 컴포넌트: function 선언, Props 타입 명시
- API 호출: `src/api/` 디렉토리에 분리

### Git 커밋
- `feat:` 새 기능 | `fix:` 버그 수정 | `refactor:` 리팩토링
- `docs:` 문서 | `chore:` 빌드/설정 | `test:` 테스트
- 한글 커밋 메시지 OK
- 커밋 시 CHANGELOG.md 업데이트 필수

### DB 마이그레이션
- schema.sql 직접 관리 금지 — Flyway 사용
- 마이그레이션 파일: `resources/db/migration/V1__description.sql` 형식
- MVP는 Redis 캐싱 위주, MySQL 테이블은 필요할 때 추가

### 테스트
- TDD 필수 — 기능 구현 전 테스트 먼저 작성
- Backend: JUnit 5 + Spring Boot Test
- Frontend: (추후 Vitest 추가 예정)

## API

### Duffel (FSC — 대한항공, 아시아나)
- 문서: https://duffel.com/docs
- SDK: `@duffel/api` (서버 전용)
- Sandbox 모드로 개발 (실제 예약 불가, 가상 데이터)
- Rate Limit: 분당 100 요청 (sandbox)

### Kiwi Tequila (LCC — 제주항공, 진에어, 티웨이, 에어부산)
- 문서: https://tequila.kiwi.com
- 인증: apikey 헤더
- 검색 무료, 예약 시 3% 커미션
- Virtual Interlining: 코드쉐어 없는 항공사 조합 가능
