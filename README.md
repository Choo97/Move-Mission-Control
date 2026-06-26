# Move Mission Control

Spring Boot 기반 이사 예약 관리 웹 서비스입니다.

고객은 이사 예약을 신청하고 예약번호와 연락처로 진행 상태를 조회할 수 있습니다. 관리자는 예약 목록, 일정, 견적, 알림, 리뷰, 운영 설정을 관리합니다.

현재 프로젝트는 고객 화면을 React로 전환하고, 백엔드는 Spring Boot API와 관리자 화면을 함께 제공하는 MVP 구조입니다.

## 기술 스택

- Java 17
- Spring Boot 3.3.6
- Spring MVC, Thymeleaf
- Spring Data JPA
- MySQL
- Maven
- React, Vite, TypeScript

## 주요 기능

- 고객 예약 신청, 임시 저장, 예약 조회
- 예약 수정, 취소, 견적 동의, 짐 사진 업로드
- 완료 예약 리뷰 작성
- 관리자 예약 목록, 상세, 상태 변경, 견적 관리
- 운영시간, 휴무일, 중복 시간 예약 시도 관리
- 이메일 알림 이력, FAQ, 고객 안내, 리뷰, 감사 로그 관리

전체 테스트 흐름은 [docs/TEST_SCENARIOS.md](docs/TEST_SCENARIOS.md)에서 확인할 수 있습니다.

## 빠른 실행

MySQL에 `movemission` 데이터베이스를 만든 뒤 `.env.example`을 복사해 `.env.local`을 만들고 실행합니다.

```bash
cp .env.example .env.local
```

터미널 1:

```bash
bash run-backend.sh
```

터미널 2:

```bash
bash run-frontend.sh
```

```text
고객 React 화면  http://localhost:5173/
백엔드 화면      http://localhost:8081/
관리자 화면      http://localhost:8081/admin/reservations
```

자세한 실행 방법, 환경변수, 테스트, 관리자 계정 설정은 [docs/RUNNING.md](docs/RUNNING.md)에 정리했습니다.

## 주요 URL

```text
React 고객 화면 http://localhost:5173/
백엔드 홈       http://localhost:8081/
관리자 화면     http://localhost:8081/admin/reservations
관리자 달력     http://localhost:8081/admin/calendar
Swagger UI     http://localhost:8081/swagger-ui/index.html
OpenAPI JSON   http://localhost:8081/v3/api-docs
```

## 문서

| 문서 | 내용 |
| --- | --- |
| [docs/API.md](docs/API.md) | 고객 예약, 사진 업로드, 리뷰 작성 REST API |
| [docs/RUNNING.md](docs/RUNNING.md) | 실행 방법, 테스트, 환경변수, DB, 관리자 계정 |
| [docs/TEST_SCENARIOS.md](docs/TEST_SCENARIOS.md) | 고객 예약부터 관리자 처리까지 전체 수동 테스트 흐름 |
| [docs/ROADMAP.md](docs/ROADMAP.md) | 앞으로 개발할 기능과 우선순위 |
| [docs/ADMIN.md](docs/ADMIN.md) | 관리자 화면, 대시보드, 운영 구조 |
| [docs/DESIGN.md](docs/DESIGN.md) | 고객 화면 디자인 방향, UI/UX 기준 |

## 기본 흐름

```text
고객 예약 신청
-> 관리자 예약 확인
-> 상담 및 견적 안내
-> 고객 견적 동의
-> 예약 확정
-> 이사 완료
-> 고객 리뷰 작성
```

## 예약 상태

```text
RECEIVED      접수
CONSULTING    상담중
ESTIMATE_SENT 견적안내
CONFIRMED     확정
COMPLETED     완료
CANCELED      취소
```

기본 흐름은 `접수 -> 상담중 -> 견적안내 -> 확정 -> 완료`입니다. 진행 중인 예약은 고객 요청이나 운영 판단에 따라 `취소`로 변경할 수 있습니다.

## 프로젝트 구조

```text
src/main/java/com/moving/reservation
├── MovingReservationApplication.java
├── admin
├── api
├── auth
├── availability
├── coupon
├── estimate
├── faq
├── home
├── notification
├── reservation
└── review

src/main/resources
├── application.yml
├── static/css/style.css
└── templates

frontend/src
├── App.tsx
├── api
├── components
├── types
├── reservationData.ts
└── types.ts
```

React 고객 화면은 `App.tsx`가 화면 상태 흐름을 담당하고, 실제 화면은 `components/` 아래 폼과 상세 패널로 분리했습니다.

백엔드는 예약, 관리자, 알림, 리뷰, FAQ, 운영시간 같은 도메인을 패키지 단위로 나눴습니다.

## Git

현재 작업 브랜치:

```text
codex/initial-moving-service
```
