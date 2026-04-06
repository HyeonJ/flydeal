# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added
- SerpApi Mock 클라이언트 (한국 LCC 5개사: 제주항공, 진에어, 티웨이, 에어부산, 에어서울)
  - 12개 노선 템플릿 기반 Mock 데이터 생성 (serpapi.mock=true 기본)
  - LccClient 인터페이스로 Mock/Real 전환 가능
  - SerpApiClient 껍데기 (실제 연동 시 serpapi.mock=false로 전환)
- Duffel + LCC 병렬 검색 — FlightSearchService에서 CompletableFuture 병렬 호출 후 결과 병합

### Changed
- Kiwi API 설정을 SerpApi로 교체 (RestClientConfig, application.yml)
- .gitignore에 backend/gradle.properties 추가 (로컬 JAVA_HOME 경로 제외)
- Line ending 정규화 (.gitattributes 기반 CRLF → LF)

## [0.2.0] - 2026-04-06

### Changed
- 백엔드 DTO/Config를 기획 문서(mvp-scope.md) 기준으로 전면 재설계
  - FlightOffer, FlightSearchRequest, FlightSearchResult, ApiResponse DTO
  - RestTemplate → RestClient 전환 (타임아웃 connect 5초, read 10초)
  - GlobalExceptionHandler 추가
- DuffelClient 재작성 — 왕복 슬라이스, cabin_class, 타입 안전 매핑
- Controller: GET → POST /api/flights/search, @Valid 입력 검증
- 프론트엔드 타입/API를 백엔드 DTO와 정렬
- 불필요한 의존성 제거 (JPA, Redis, Mail — 사용 시점에 재추가)

### Discovered
- Kiwi Tequila API: MAU 50K 조건으로 가입 사실상 불가 → SerpApi 대안 검토 중
- Duffel sandbox: 한국 LCC 미커버 확인

## [0.1.0] - 2026-03-29

### Added
- Spring Boot 3.2 백엔드 초기 세팅 (Java 17, Gradle Kotlin DSL)
- React + Vite + TypeScript 프론트엔드 초기 세팅
- Duffel API 연동 (항공편 검색)
- 검색 폼, 검색 결과, 항공편 카드 컴포넌트
