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

## 문서

| 문서 | 내용 |
| --- | --- |
| [CONVENTION.md](CONVENTION.md) | 협업을 고려한 브랜치, 커밋, 코드 작성, 테스트 기준 |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | 프로젝트 구조, 패키지 역할, 예약 상태 흐름 |
| [docs/API.md](docs/API.md) | 고객 예약, 사진 업로드, 리뷰 작성 REST API |
| [docs/RUNNING.md](docs/RUNNING.md) | 실행 방법, 테스트, 환경변수, DB, 관리자 계정 |
| [docs/DEPLOY_ENV.md](docs/DEPLOY_ENV.md) | 배포 환경변수, prod 프로필, 프론트/백엔드 주소 설정 |
| [docs/DEPLOY_VERCEL_RAILWAY.md](docs/DEPLOY_VERCEL_RAILWAY.md) | Vercel/Railway 배포 구조, 사용자 작업 순서, 배포 후 확인 방법 |
| [docs/TEST_SCENARIOS.md](docs/TEST_SCENARIOS.md) | 고객 예약부터 관리자 처리까지 전체 수동 테스트 흐름 |
| [docs/PORTFOLIO_CASE_STUDY.md](docs/PORTFOLIO_CASE_STUDY.md) | 이력서 bullet, 면접 답변, 시연 흐름, 설계 판단 |
| [docs/ROADMAP.md](docs/ROADMAP.md) | 앞으로 개발할 기능과 우선순위 |
| [docs/ADMIN.md](docs/ADMIN.md) | 관리자 화면, 대시보드, 운영 구조 |
| [docs/DESIGN.md](docs/DESIGN.md) | 고객 화면 디자인 방향, UI/UX 기준 |
| [docs/NON_PROFIT_LANDING_GUIDE.md](docs/NON_PROFIT_LANDING_GUIDE.md) | 비영리 도움 요청 랜딩페이지 정보 구조와 문구 기준 |
