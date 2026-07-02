# Move Mission Control

Spring Boot 기반 이사 예약 관리 웹 서비스입니다.

고객은 이사 예약을 신청하고 예약번호와 연락처로 진행 상태를 조회할 수 있습니다. 관리자는 예약 목록, 일정, 견적, 알림, 리뷰, 운영 설정을 관리합니다.

현재 프로젝트는 고객 화면을 React로 전환하고, 백엔드는 Spring Boot API와 관리자 화면을 함께 제공하는 MVP 구조입니다.

## 실제 화면

로컬에서 생성한 시연 데이터로 캡처한 포트폴리오용 화면입니다. `고객 예약 접수 -> 예약 조회 -> 관리자 처리 -> 완료 후 리뷰 관리` 흐름이 한 번에 보이도록 구성했습니다.

| 고객 예약 | 예약 조회 |
| --- | --- |
| ![고객 예약 단계별 입력 화면](docs/screenshots/customer-reservation.png) | ![예약번호와 연락처로 조회한 예약 상세 화면](docs/screenshots/reservation-lookup.png) |
| 고객 정보, 연락처, 이사 유형을 단계별 예약 폼에서 입력합니다. | 예약번호와 연락처로 현재 상태, 이사 일정, 예상 금액을 확인합니다. |

| 관리자 예약 관리 | 리뷰 관리 |
| --- | --- |
| ![관리자 예약 목록과 예약 상세 관리 화면](docs/screenshots/admin-reservations.png) | ![관리자 리뷰 목록과 답변 관리 화면](docs/screenshots/admin-reviews.png) |
| 관리자는 예약 목록에서 처리 대상 예약을 선택하고 견적, 상태, 메모, 알림 흐름을 확인합니다. | 완료 예약의 고객 리뷰를 확인하고 공개 여부와 관리자 답변을 관리합니다. |

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
- 예약 수정/취소 요청 접수 및 관리자 승인/반려, 견적 동의, 짐 사진 업로드
- 완료 예약 리뷰 작성
- 관리자 예약 목록, 상세, 고객 요청 처리, 상태 변경, 견적 관리
- 운영시간, 휴무일, 중복 시간 예약 시도 관리
- 이메일/SMS 알림 이력, FAQ, 고객 안내, 리뷰, 감사 로그 관리

전체 테스트 흐름은 [docs/TEST_SCENARIOS.md](docs/TEST_SCENARIOS.md)에서 확인할 수 있습니다.
취업 포트폴리오와 면접 설명용 요약은 [docs/PORTFOLIO_CASE_STUDY.md](docs/PORTFOLIO_CASE_STUDY.md)에 정리했습니다.

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

실행 상태가 헷갈릴 때:

```bash
bash check-local.sh
```

```text
고객 React 화면  http://localhost:5173/
React 관리자 화면 http://localhost:5173/admin/reservations
백엔드 화면      http://localhost:8081/
기존 관리자 화면 http://localhost:8081/admin/reservations
```

자세한 실행 방법, 환경변수, 테스트, 관리자 계정 설정은 [docs/RUNNING.md](docs/RUNNING.md)에 정리했습니다.

## 주요 URL

```text
React 고객 화면 http://localhost:5173/
React 관리자    http://localhost:5173/admin/reservations
백엔드 홈       http://localhost:8081/
기존 관리자     http://localhost:8081/admin/reservations
관리자 달력     http://localhost:8081/admin/calendar
Swagger UI     http://localhost:8081/swagger-ui/index.html
OpenAPI JSON   http://localhost:8081/v3/api-docs
```

## 포트폴리오 시연 흐름

처음 보는 사람이 기능을 빠르게 이해할 수 있도록 아래 순서로 시연합니다.

| 순서 | 화면 | 확인할 내용 |
| --- | --- | --- |
| 1 | 고객 React 화면 | `이사 예약하기`로 고객명, 연락처, 이사일, 출발지, 도착지, 현장 정보를 입력합니다. |
| 2 | 예약 완료 화면 | 예약번호가 표시되는지 확인하고, 이후 조회에 예약번호와 연락처가 필요하다는 점을 설명합니다. |
| 3 | 예약 조회 화면 | 예약번호와 연락처로 본인 예약만 조회되는지 확인합니다. |
| 4 | React 관리자 예약 목록 | 관리자 로그인 후 새 예약이 목록과 처리 우선순위 요약에 표시되는지 확인합니다. |
| 5 | React 관리자 예약 상세 | 고객 흐름 요약, 현장 정보, 짐 사진, 고객 요청, 알림 이력을 한 화면에서 확인합니다. |
| 6 | 운영 처리 | 상태를 `상담중`으로 변경하고 관리자 메모, 이동 거리, 최종 견적을 저장합니다. |
| 7 | 고객 견적 확인 | 고객 예약 상세에서 견적 금액을 확인하고 견적 동의를 진행합니다. |
| 8 | 예약 확정 및 완료 | 관리자가 예약을 `확정`, `완료` 상태로 변경하고 상태 이력이 남는지 확인합니다. |
| 9 | 리뷰 작성 | 완료된 예약에서 고객 리뷰와 평점을 작성합니다. |
| 10 | 운영 관리 | 알림 이력, FAQ 관리, 운영시간/휴무일 관리처럼 실제 운영에 필요한 보조 기능을 확인합니다. |

이 순서로 보여주는 이유는 `고객 예약 접수 -> 관리자 처리 -> 고객 확인 -> 완료 후 리뷰`까지 이어지는 실제 서비스 흐름을 한 번에 설명할 수 있기 때문입니다. 단순 CRUD가 아니라 예약 운영 과정 전체를 관리하는 서비스라는 점을 보여주는 데 초점을 둡니다.

## 문서

| 문서 | 내용 |
| --- | --- |
| [docs/API.md](docs/API.md) | 고객 예약, 사진 업로드, 리뷰 작성 REST API |
| [docs/RUNNING.md](docs/RUNNING.md) | 실행 방법, 테스트, 환경변수, DB, 관리자 계정 |
| [docs/TEST_SCENARIOS.md](docs/TEST_SCENARIOS.md) | 고객 예약부터 관리자 처리까지 전체 수동 테스트 흐름 |
| [docs/PORTFOLIO_CASE_STUDY.md](docs/PORTFOLIO_CASE_STUDY.md) | 이력서 bullet, 면접 답변, 시연 흐름, 설계 판단 |
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

예약 수정과 취소는 고객이 요청하면 바로 반영하지 않고, 관리자가 승인한 뒤 실제 예약 정보나 취소 상태로 반영합니다. 이렇게 한 이유는 일정 변경과 취소가 다른 예약 시간, 견적, 운영 준비에 영향을 주기 때문입니다.

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
