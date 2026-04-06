# FlyDeal (플라이딜)

항공권·숙소 가격 비교 서비스. 여러 예약 플랫폼의 가격을 한 곳에서 비교하고 최저가 알림을 받을 수 있습니다.

## 기술 스택

| 레이어 | 기술 |
|--------|------|
| Backend | Spring Boot 3, Java 17, Gradle (Kotlin DSL) |
| Database | MySQL, Redis |
| Frontend | React, Vite, TypeScript |

## 프로젝트 구조

```
flydeal/
├── backend/          # Spring Boot 애플리케이션
└── frontend/         # React + Vite + TypeScript
```

## 로컬 실행

### Backend
```bash
cd backend
./gradlew bootRun
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```
