# FlyDeal (플라이딜)

항공편 가격 비교 서비스 — Duffel API로 항공편 검색, 최저가 알림 제공.

## 기술 스택

| 레이어 | 기술 |
|--------|------|
| Backend | Spring Boot 3.2, Java 17, Gradle 8.6 (Kotlin DSL) |
| Frontend | React 18, Vite 5, TypeScript 5 |
| Database | MySQL 8, Redis 7 |
| API | Duffel API (항공편 검색/예약) |

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

### 테스트
- TDD 필수 — 기능 구현 전 테스트 먼저 작성
- Backend: JUnit 5 + Spring Boot Test
- Frontend: (추후 Vitest 추가 예정)

## API

- Duffel API: https://duffel.com/docs
- Sandbox 모드로 개발 (실제 예약 불가, 가상 데이터)
- Rate Limit: 분당 100 요청 (sandbox)
