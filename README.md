# Move Mission Control

Spring Boot 기반 이사 예약 웹 서비스입니다. 고객은 이사 예약을 신청하고 진행 상태를 확인할 수 있으며, 관리자는 예약 현황, 견적, 일정, 리뷰, 운영 이력을 관리할 수 있습니다.

## 기술 스택

- Java 17
- Spring Boot 3.3.6
- Spring MVC, Thymeleaf
- Spring Data JPA
- MySQL
- Maven
- React, Vite, TypeScript

## 주요 기능

### 고객 기능

- 이사 예약 신청
- 예약 작성 내용 7일 임시 저장
- 영업일과 기존 예약을 반영한 예약 가능 시간 선택
- 예약 번호와 연락처 기반 예약 조회
- 예약 진행 단계 확인
- 예약 수정 및 취소
- 견적 동의 및 예약 확정
- 짐 사진 업로드
- 완료 예약 리뷰 작성
- React 자주 묻는 질문 조회

### 관리자 기능

- 예약 목록, 검색, 상태/기간 필터
- 예약 상세 확인 및 상태 변경
- 견적 금액, 이동 거리, 관리자 메모 관리
- 예약 목록 페이징, CSV 다운로드, 일정 달력
- 이메일 발송 이력, FAQ, 고객 안내, 쿠폰, 리뷰 관리
- 관리자 계정, 비밀번호, 감사 로그 관리
- 중복 시간 예약 시도 확인 및 30일 보관 후 자동 삭제
- 요일별 운영시간과 지정 휴무일 관리

## 빠른 실행

자세한 실행 방법, 환경변수, 테스트, 관리자 계정 설정은 [docs/RUNNING.md](docs/RUNNING.md)에 정리했습니다.

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
```

## 주요 URL

```text
메인 화면       http://localhost:8081/
예약 신청       http://localhost:8081/?view=create
예약 조회       http://localhost:8081/?view=search
자주 묻는 질문  http://localhost:8081/?view=faq
자주 묻는 질문  http://localhost:8081/faq
관리자 화면     http://localhost:8081/admin/reservations
관리자 달력     http://localhost:8081/admin/calendar
React 고객 화면 http://localhost:5173/
Swagger UI     http://localhost:8081/swagger-ui/index.html
OpenAPI JSON   http://localhost:8081/v3/api-docs
```

## 문서

| 문서 | 내용 |
| --- | --- |
| [docs/API.md](docs/API.md) | 고객 예약, 사진 업로드, 리뷰 작성 REST API |
| [docs/RUNNING.md](docs/RUNNING.md) | 실행 방법, 테스트, 환경변수, DB, 관리자 계정 |
| [docs/ROADMAP.md](docs/ROADMAP.md) | 앞으로 개발할 기능과 우선순위 |
| [docs/ADMIN.md](docs/ADMIN.md) | 관리자 화면, 대시보드, 운영 구조 |
| [docs/DESIGN.md](docs/DESIGN.md) | 고객 화면 디자인 방향, UI/UX 기준 |

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
├── home
└── reservation

src/main/resources
├── application.yml
├── static/css/style.css
└── templates

frontend/src
├── App.tsx
├── api
├── components
├── reservationData.ts
└── types.ts
```

React 고객 화면은 `App.tsx`가 화면 상태 흐름을 담당하고, 실제 화면은 `components/` 아래 폼과 상세 패널로 분리했습니다. API 호출은 `api/`, 초기값과 선택지는 `reservationData.ts`, 요청/응답 타입은 `types.ts`에 모아 두었습니다.

## Git

현재 작업 브랜치:

```text
codex/initial-moving-service
```
