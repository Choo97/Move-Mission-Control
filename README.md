# Move Mission Control

Spring Boot 기반 이사 예약 웹 서비스입니다. 고객은 이사 예약을 신청하고, 관리자는 예약 현황을 확인하면서 상태와 견적을 관리할 수 있습니다.

## 기술 스택

- Java 17
- Spring Boot 3.3.6
- Spring MVC
- Thymeleaf
- Spring Data JPA
- MySQL
- Maven

## 주요 기능

### 고객 기능

- 이사 예약 신청
- 예약 번호와 연락처 기반 예약 조회
- 예약 상세 진행 상태 확인
- 예약 수정 및 취소 요청
- 견적 확정서 확인 및 PDF 다운로드
- 최종 견적 동의 및 예약 확정
- 짐 사진 업로드
- 고객 리뷰 및 평점 작성

### 관리자 예약 관리

- 예약 목록, 검색, 상태 필터, 기간 필터
- 예약 상세 정보 확인
- 예약 상태 변경 및 상태 변경 이력 관리
- 견적 금액 입력 및 견적 산정 내역 확인
- 이동 거리 직접 입력 및 지도 API 자동 계산
- 관리자 메모 관리
- 예약 목록 페이징 및 CSV 다운로드
- 이사 일정 달력 조회

### 운영 관리

- 이메일 예약 확인 안내 및 발송 이력 관리
- 실패 이메일 재발송
- FAQ 등록 및 수정
- 고객 안내 문구 관리
- 쿠폰 및 할인 정책 관리
- 쿠폰 사용 이력 관리
- 고객 리뷰 관리

### 보안 및 시스템 관리

- 관리자 로그인 및 로그아웃
- 관리자 계정 DB 로그인
- 관리자 계정 추가 및 비활성화
- 관리자 비밀번호 변경
- 로그인 실패 횟수 제한
- 관리자 작업 감사 로그

## 추후 개발 예정 기능

현재 프로젝트는 MVP 단계이므로, 먼저 예약 접수와 관리자 확인 흐름에 집중했습니다. 실제 서비스로 확장하려면 아래 기능들을 단계적으로 추가할 수 있습니다.

- 회원가입 및 로그인
- 관리자 권한 분리
- 예약 수정 및 취소
- 사다리차 자동 배차
- 담당 기사 또는 이사업체 배정
- SMS 또는 카카오 알림톡 발송
- 결제 기능
- 쿠폰 만료일 및 사용 횟수 제한
- PostgreSQL 전환 검토
- 파일 저장소 S3 또는 NCP Object Storage 연동
- 배포 환경 구성
- 운영 로그 및 에러 모니터링

## 화면 테마

초기 화면은 밝은 흰색 배경에 코랄 오렌지 포인트를 섞은 따뜻한 예약 플랫폼 방향으로 구성했습니다.

- 기본 배경: 흰색, 연한 코랄
- 포인트 컬러: 코랄 오렌지, 짙은 브라운
- UI: 카드형 예약/관리 화면
- 분위기: 따뜻한 예약 플랫폼 + 관제 보드

색상 테마는 화면 오른쪽 아래 전환 버튼으로 비교할 수 있습니다.

- `코랄`: 현재 기본 테마입니다. 붉은색과 주황색 사이의 따뜻한 느낌을 주되, 빨간색 경고 화면처럼 보이지 않도록 코랄 오렌지 계열로 조정했습니다.
- `하늘`: 이전 기본 테마입니다. 신뢰감과 깔끔한 예약 플랫폼 느낌을 비교할 수 있도록 보존했습니다.

선택한 테마는 브라우저 `localStorage`에 저장되어 다른 화면으로 이동해도 유지됩니다.

## 관리자 메뉴 구조

관리자 기능이 늘어나면서 상단 메뉴를 모두 나열하면 화면이 복잡해지고, 운영자가 자주 쓰는 기능을 찾기 어려워집니다. 그래서 관리자 메뉴는 아래 기준으로 묶었습니다.

- `예약 관리`: 예약 목록, 견적 기준, 알림 이력처럼 매일 확인하는 예약 처리 기능
- `운영 관리`: 리뷰, FAQ, 쿠폰처럼 고객 경험과 운영 정책을 관리하는 기능
- `시스템 관리`: 감사 로그, 계정, 비밀번호처럼 보안과 운영 이력을 관리하는 기능

로그인 전 화면에서는 내부 관리자 메뉴를 노출하지 않고 `관리자 로그인`만 보여줍니다. 로그인 전 사용자는 아직 운영 권한이 없기 때문에, 접근 가능한 행동만 보여주는 편이 화면도 단순하고 보안 흐름도 명확합니다.

## 관리자 대시보드 구조

관리자 첫 화면은 전체 예약 수만 보여주는 화면이 아니라, 운영자가 오늘 무엇을 먼저 처리해야 하는지 판단하는 화면으로 구성했습니다.

### 대시보드 핵심 영역

- `요약 지표`: 전체 예약, 접수, 상담중, 견적안내, 확정, 완료 등 현재 운영 상태를 숫자로 확인합니다.
- `오늘 해야 할 일`: 접수 대기, 상담중, 고객 동의 대기, 거리 확인 필요, 이메일 실패처럼 즉시 처리할 항목을 모아 보여줍니다.
- `최근 예약/리뷰`: 새로 들어온 예약과 고객 피드백을 빠르게 확인합니다.

### 예약 운영 흐름

- 예약 목록은 기본적으로 처리 우선순위 기준으로 정렬합니다.
- 검색, 상태 필터, 이사일 기간 필터, 빠른 기간 필터로 필요한 예약만 좁혀 볼 수 있습니다.
- 예약이 많아질 때를 대비해 목록 페이징과 표시 건수 선택을 제공합니다.
- 현재 필터와 정렬 조건을 유지한 채 CSV로 다운로드할 수 있습니다.

### 일정 관리

- `이사 일정 달력`에서 월간 예약 분포와 선택 날짜의 일정을 함께 확인합니다.
- 예약 상태별 색상, 오늘 일정 강조, 선택 날짜 일정 카드로 일정 확인 속도를 높였습니다.
- 달력에서도 상세 이동, 날짜별 목록 확인, CSV 다운로드, 상태 빠른 변경을 할 수 있습니다.

### 상세 처리

- 예약 상세 화면 상단에는 현재 예약에서 먼저 처리해야 할 일을 표시합니다.
- 오른쪽 처리 영역에는 상태 변경, 견적 저장, 거리 저장, 메모 저장, 이메일 발송처럼 자주 쓰는 작업을 모았습니다.
- 상태 변경처럼 운영 이력에 영향을 주는 작업은 확인 메시지와 감사 로그를 남깁니다.
- 상세 화면에서 목록으로 돌아갈 때 기존 필터, 정렬, 페이지 위치를 유지합니다.

고객 예약 상세 화면에는 예약 진행 상태를 `접수 -> 상담중 -> 견적안내 -> 확정 -> 완료` 단계로 보여줍니다. 고객은 관리자처럼 내부 상태를 알 필요가 없기 때문에, 현재 단계와 다음에 할 일 중심으로 안내해 예약 진행 상황을 쉽게 이해할 수 있도록 했습니다.

고객 예약 상세 화면에는 현재 상태별 고객 안내도 함께 표시합니다. 상태만 보여주면 고객이 실제로 무엇을 준비해야 하는지 모를 수 있으므로, 연락 준비, 현장 조건 확인, 견적 동의, 이사 전 준비처럼 고객 행동으로 이어지는 내용을 예약 진행 상태 바로 아래에 배치했습니다.

고객 안내 문구는 관리자 `운영 관리 > 고객 안내` 화면에서 관리합니다. 안내 문구는 예약 상태별로 등록하고 정렬 순서를 지정할 수 있으며, 공개 또는 숨김 처리할 수 있습니다. 이렇게 분리하면 고객에게 보여줄 문구를 바꿀 때 Java 코드를 수정하지 않아도 됩니다.

고객이 예약을 수정하거나 취소하면 예약 요청 이력에 기록합니다. 수정 이력에는 변경 전후 값이 남고, 취소 이력에는 취소 요청 당시 상태가 남습니다. 고객 요청 이력은 고객 상세 화면과 관리자 예약 상세 화면에서 함께 확인할 수 있어 운영자가 예약 변경 과정을 놓치지 않도록 했습니다.

## 실행 방법

이 프로젝트는 Java 17, Maven, MySQL이 설치되어 있으면 Windows, macOS, Linux에서 실행할 수 있습니다.

### 1. 공통 준비 사항

- Java 17 설치
- Maven 설치
- MySQL 실행
- MySQL에 `movemission` 데이터베이스 생성

MySQL 접속 정보는 아래 `MySQL Database 접속 정보` 섹션과 맞춰 둡니다.

```sql
CREATE DATABASE movemission CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

애플리케이션은 `spring.jpa.hibernate.ddl-auto=update` 설정을 사용하므로, 서버가 처음 실행될 때 필요한 테이블을 자동으로 생성합니다.

### Git Bash, macOS, Linux

먼저 Bash에서 Java 17이 잡히는지 확인합니다.

```bash
java -version
```

Windows Git Bash에서 Java 11로 표시된다면 현재 터미널에서만 Java 17을 우선 사용하도록 설정합니다.

```bash
export JAVA_HOME="/c/Dev/java/jdk-17.0.7"
export PATH="$JAVA_HOME/bin:$PATH"
```

WSL의 Bash는 Windows에 설치된 Java를 자동으로 사용하지 않습니다. WSL에서 실행하려면 WSL 안에 Java 17과 Maven을 설치해야 합니다. 지금처럼 Windows에 Java 17이 설치되어 있다면 Git Bash 또는 `cmd`에서 아래 명령으로 실행하는 편이 더 단순합니다.

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

로컬 SMTP 설정까지 함께 적용해서 실행하려면 `.env.example`을 복사해 `.env.local`을 만들고 실제 값을 입력한 뒤 실행합니다.

```bash
cp .env.example .env.local
bash run-local.sh
```

`.env.local`에는 실제 이메일 계정과 앱 비밀번호가 들어가므로 Git에 올리지 않습니다.

서버가 실행되면 브라우저에서 아래 주소로 접속합니다.

```text
http://localhost:8081/
```

관리자 화면은 아래 기본 계정으로 로그인할 수 있습니다.

```text
ID       admin
Password admin1234
```

## 테스트 방법

자동 테스트는 실제 MySQL 데이터베이스를 사용하지 않고 H2 인메모리 데이터베이스로 실행합니다. 그래서 테스트를 실행해도 로컬 `movemission` 데이터는 변경되지 않습니다.

```bash
mvn clean test
```

GitHub Actions도 같은 테스트 명령어를 실행합니다. GitHub 저장소에 코드를 push하거나 pull request를 만들면 GitHub 서버에서 Java 17 환경을 준비한 뒤 `mvn clean test`를 자동으로 실행합니다.

현재 자동 테스트는 아래 흐름을 검증합니다.

| 테스트 파일 | 검증 범위 |
| --- | --- |
| `ReservationServiceTest` | 예약 신청/조회, 사진 정보 저장, 수정/취소 이력, 상태 변경 이력, 이메일 알림 이력, 쿠폰 할인 |
| `ReservationControllerTest` | 예약 신청 화면, 예약 조회 화면, 예약 상세 접근 인증, 예약 신청 완료 이동 |
| `ReservationApiControllerTest` | 고객 예약 신청/조회/수정/취소/견적동의/사진 업로드 REST API 성공 응답, 입력 오류, 연락처 불일치 오류, CSRF 없이 호출 |
| `ReservationEstimateCalculatorTest` | 기본가, 거리 추가요금, 엘리베이터 없음, 고층 작업, 사다리차 견적 규칙 |
| `ReservationPhotoStorageTest` | 허용 이미지 확장자 저장, 잘못된 확장자 거부 |
| `SecurityConfigTest` | 관리자 로그인 성공/실패, 관리자 권한 접근, 고객 화면 공개 접근, OpenAPI 문서 공개 접근 |
| `AdminReservationControllerTest` | 관리자 예약 목록, 예약 상세, 달력 화면, 상태 변경 요청 |
| `AdminAuditLogServiceTest` | 관리자 감사 로그 저장, 예약별 조회, 검색, 작업명 목록 |
| `ReviewServiceTest` | 완료 예약 리뷰 작성, 미완료 예약 차단, 중복 리뷰 차단, 평균 평점 |
| `ReviewApiControllerTest` | 고객 리뷰 작성 REST API 성공 응답, 미완료 예약 차단, 연락처 불일치 오류, CSRF 없이 JSON 호출 |

## Kakao 지도 API 설정

관리자 예약 상세 화면에서 `지도 API로 거리 자동 계산` 버튼을 사용하려면 Kakao REST API 키가 필요합니다.

```bat
set KAKAO_MAP_ENABLED=true
set KAKAO_REST_API_KEY=발급받은_REST_API_KEY
```

API 키가 설정되지 않은 상태에서도 서비스는 실행됩니다. 이 경우 관리자가 이동 거리(km)를 직접 입력하면 견적 계산에 반영됩니다.

## 이메일 발송 설정

예약 접수 시 이메일을 입력하면 이메일 안내 이력이 `발송 준비` 상태로 생성됩니다. 실제 SMTP 발송을 사용하려면 서버 실행 전에 아래 환경변수를 설정합니다.

```bat
set NOTIFICATION_EMAIL_ENABLED=true
set NOTIFICATION_EMAIL_FROM=보내는메일@example.com
set MAIL_HOST=smtp.example.com
set MAIL_PORT=587
set MAIL_USERNAME=SMTP_계정
set MAIL_PASSWORD=SMTP_비밀번호
set MAIL_SMTP_AUTH=true
set MAIL_SMTP_STARTTLS_ENABLE=true
```

SMTP 설정이 꺼져 있으면 애플리케이션은 정상 실행되지만, 관리자 화면에서 이메일 발송을 시도할 때 실패 이력으로 기록됩니다.

## 주요 URL

```text
메인 화면       http://localhost:8081/
예약 신청       http://localhost:8081/reservations/new
자주 묻는 질문  http://localhost:8081/faq
관리자 화면     http://localhost:8081/admin/reservations
관리자 달력     http://localhost:8081/admin/calendar
React 고객 화면 http://localhost:5173/
Swagger UI     http://localhost:8081/swagger-ui/index.html
OpenAPI JSON   http://localhost:8081/v3/api-docs
```

## REST API

React 같은 별도 프론트엔드에서 사용할 수 있도록 고객 기능 API를 제공합니다. HTML 화면과 같은 예약 규칙을 사용하며, 예약 번호와 예약 당시 연락처로 고객 요청을 확인합니다.

브라우저에서 `http://localhost:8081/swagger-ui/index.html`에 접속하면 API 목록을 확인하고 직접 요청을 테스트할 수 있습니다.
요청/응답 DTO에는 Swagger 설명과 예시 값을 추가해 각 필드의 의미를 문서 화면에서 바로 확인할 수 있습니다.
고객 예약 API와 고객 리뷰 API는 Swagger 그룹, 엔드포인트 요약, 성공/실패 상태코드 설명을 제공합니다.

### 공통 규칙

- JSON API는 `Content-Type: application/json`을 사용합니다.
- 파일 업로드 API는 `Content-Type: multipart/form-data`를 사용합니다.
- 고객 예약 변경 API는 예약 당시 연락처가 일치해야 처리됩니다.
- 성공 응답은 생성 작업이면 `201 Created`, 조회/수정/상태 변경 작업이면 `200 OK`를 사용합니다.
- 실패 응답은 아래 형식의 JSON을 반환합니다.

```json
{
  "code": "BAD_REQUEST",
  "message": "오류 메시지"
}
```

`code`는 React 같은 화면 코드에서 오류 종류를 구분할 때 사용하고, `message`는 고객이나 관리자에게 보여줄 안내 문구로 사용합니다.

### API 요약

| 구분 | Method | URL | 용도 |
| --- | --- | --- | --- |
| 고객 예약 | `POST` | `/api/reservations` | 예약 신청 |
| 고객 예약 | `POST` | `/api/reservations/search` | 예약 번호와 연락처로 예약 조회 |
| 고객 예약 | `PATCH` | `/api/reservations/{reservationId}` | 예약 정보 수정 |
| 고객 예약 | `POST` | `/api/reservations/{reservationId}/cancel` | 예약 취소 |
| 고객 예약 | `POST` | `/api/reservations/{reservationId}/estimate/accept` | 최종 견적 동의 및 예약 확정 |
| 파일 업로드 | `POST` | `/api/reservations/{reservationId}/photos` | 짐 사진 업로드 |
| 고객 리뷰 | `POST` | `/api/reviews` | 완료 예약 리뷰 작성 |

### React 전환 준비 점검

현재 REST API는 고객 화면을 React로 전환하는 데 필요한 핵심 흐름을 먼저 제공합니다. 고객은 예약 신청, 예약 조회, 예약 수정, 예약 취소, 견적 동의, 짐 사진 업로드, 리뷰 작성을 API로 처리할 수 있습니다.

| 구분 | 현재 상태 | 판단 |
| --- | --- | --- |
| 고객 예약 신청 | `/api/reservations` 제공 | React 예약 신청 화면에서 바로 사용 가능 |
| 고객 예약 조회 | `/api/reservations/search` 제공 | 예약 번호와 연락처 인증 흐름 유지 가능 |
| 고객 예약 수정/취소 | 수정, 취소 API 제공 | 고객 셀프 관리 화면 구현 가능 |
| 견적 동의 | 견적 동의 API 제공 | 견적 안내 후 확정 흐름 구현 가능 |
| 짐 사진 업로드 | multipart API 제공 | React에서 `FormData`로 업로드 가능 |
| 고객 리뷰 | `/api/reviews` 제공 | 완료 예약 리뷰 작성 화면 구현 가능 |
| 오류 처리 | `code`, `message` 공통 응답 제공 | React에서 오류 종류별 화면 처리 가능 |
| API 문서 | Swagger UI 제공 | 프론트 개발자가 요청/응답 구조 확인 가능 |

React 전환 전에 우선 확인할 부분은 아래와 같습니다.

1. 고객 화면만 React로 바꿀지, 관리자 화면까지 React로 바꿀지 결정합니다.
2. 관리자 화면까지 React로 바꾼다면 관리자 예약 목록, 예약 상세, 상태 변경, 견적 관리 API를 별도로 분리해야 합니다.
3. 사진 응답의 `fileUrl`은 `/uploads/reservation-photos/...` 형식이며, 로그인 없이 조회할 수 있도록 테스트로 검증합니다.
4. React 개발 서버는 기본적으로 `http://localhost:5173`, `http://localhost:3000`에서 Spring API와 업로드 파일에 접근할 수 있도록 CORS를 허용합니다.

React 개발 서버 주소를 바꿔야 한다면 `.env.local`에서 아래 값을 수정합니다.

```bash
APP_CORS_ALLOWED_ORIGIN_VITE=http://localhost:5173
APP_CORS_ALLOWED_ORIGIN_CRA=http://localhost:3000
```

현재 혼자 개발하는 MVP 단계에서는 고객 REST API를 먼저 안정화하고, 관리자 기능은 기존 Thymeleaf 화면을 유지하는 방식이 현실적입니다. 관리자 API까지 한 번에 분리하면 작업 범위가 커지므로, React 전환 범위를 정한 뒤 단계적으로 진행하는 편이 좋습니다.

### React 전환 전략

React 전환은 고객 화면부터 시작하고, 관리자 화면은 당분간 기존 Thymeleaf 화면을 유지합니다. 이렇게 나누면 사용자 화면 개선은 빠르게 진행하면서도 관리자 기능 전체를 한 번에 다시 만드는 부담을 줄일 수 있습니다.

| 영역 | 전략 | 이유 |
| --- | --- | --- |
| Spring Boot | API 서버와 관리자 Thymeleaf 화면 유지 | 예약, 견적, 인증, DB 처리 로직을 그대로 사용하기 위함 |
| React | 고객 화면 담당 | 고객이 직접 보는 예약 흐름의 UI 개선 효과가 가장 큼 |
| `/api/**` | React가 호출하는 REST API | 화면과 데이터 처리를 분리하기 위함 |
| `/admin/**` | Thymeleaf 유지 | 관리자 기능은 범위가 넓어 나중에 별도 전환 여부를 결정하는 편이 안전함 |
| `/reservations/**` | 기존 고객 Thymeleaf 화면 유지 | React 전환 중에도 기존 화면으로 기능 확인이 가능해야 함 |
| `frontend/` | React 프로젝트 위치 | 백엔드와 프론트 코드를 같은 저장소 안에서 명확히 분리하기 위함 |

개발 중에는 아래처럼 두 서버를 함께 실행합니다.

```text
Spring Boot API   http://localhost:8081
React dev server  http://localhost:5173
```

React 고객 화면은 Spring Boot의 `/api/**`를 호출합니다.

```text
React 고객 화면 -> http://localhost:8081/api/reservations
```

React 개발 서버를 실행하려면 아래 명령을 사용합니다.

```bash
cd frontend
cp .env.example .env.local
npm install
npm run dev
```

`frontend/.env.local`의 `VITE_API_BASE_URL`은 React가 호출할 Spring Boot 주소입니다.

전환 순서는 아래 기준으로 진행합니다.

1. `frontend/` 폴더에 Vite React 프로젝트를 생성합니다.
2. 고객 예약 신청 화면을 React로 구현하고 `/api/reservations`와 연결합니다.
3. 고객 예약 조회와 상세 화면을 React로 구현합니다.
4. 예약 수정, 취소, 견적 동의 화면을 React로 구현합니다.
5. 짐 사진 업로드와 리뷰 작성 화면을 React로 구현합니다.
6. 고객 React 화면이 안정화되면 기존 `/reservations/**` Thymeleaf 화면 제거 여부를 결정합니다.
7. 관리자 화면 React 전환은 고객 화면 전환 이후 별도 작업으로 판단합니다.

중요한 기준은 같은 고객 기능 안에서 Thymeleaf와 React URL을 섞지 않는 것입니다. 전환 중에는 기존 `/reservations/**`는 유지하고, React 고객 화면은 `frontend/` 개발 서버에서 먼저 검증한 뒤 배포 방식을 결정합니다. 현재 React 고객 화면은 예약 신청, 예약 번호/연락처 기반 예약 조회, 예약 수정, 예약 취소를 제공합니다.

### 고객 예약 API

#### 예약 신청

```http
POST /api/reservations
Content-Type: application/json
```

```json
{
  "customerName": "홍길동",
  "phone": "010-1234-5678",
  "email": "customer@example.com",
  "moveDate": "2026-07-01",
  "moveTime": "10:30",
  "fromAddress": "서울시 강남구 테헤란로 1",
  "toAddress": "서울시 송파구 올림픽로 1",
  "moveType": "STUDIO",
  "fromElevator": true,
  "toElevator": true,
  "fromFloor": 3,
  "toFloor": 5,
  "fromLadderTruck": false,
  "toLadderTruck": false,
  "memo": "파손 주의 물품이 있습니다."
}
```

성공하면 `201 Created`와 함께 생성된 예약 정보를 JSON으로 응답합니다. 짐 사진 업로드는 파일 전송 방식이 다르므로 별도 API로 분리했습니다.

#### 예약 조회

```http
POST /api/reservations/search
Content-Type: application/json
```

```json
{
  "reservationId": 1,
  "phone": "010-1234-5678"
}
```

예약 번호와 예약 당시 연락처가 일치하면 예약 상태, 이사 일정, 주소, 견적 금액, 견적 산정 내역을 JSON으로 응답합니다. 일치하지 않으면 `404 Not Found`와 오류 메시지를 응답합니다.

#### 예약 수정

```http
PATCH /api/reservations/{reservationId}
Content-Type: application/json
```

```json
{
  "phone": "010-1234-5678",
  "email": "customer@example.com",
  "moveDate": "2026-07-03",
  "moveTime": "14:00",
  "fromAddress": "서울시 마포구 월드컵북로 1",
  "toAddress": "서울시 용산구 한강대로 1",
  "fromFloor": 7,
  "toFloor": 9,
  "fromLadderTruck": true,
  "toLadderTruck": false,
  "memo": "이사 시간이 변경되었습니다."
}
```

예약 당시 연락처가 일치하고 현재 상태가 `접수` 또는 `상담중`이면 예약 정보를 수정합니다. 수정이 완료되면 변경된 예약 정보를 JSON으로 응답합니다.

#### 예약 취소

```http
POST /api/reservations/{reservationId}/cancel
Content-Type: application/json
```

```json
{
  "phone": "010-1234-5678"
}
```

예약 당시 연락처가 일치하고 현재 상태가 `접수` 또는 `상담중`이면 예약 상태를 `취소`로 변경합니다. 취소가 완료되면 변경된 예약 정보를 JSON으로 응답합니다.

#### 견적 동의

```http
POST /api/reservations/{reservationId}/estimate/accept
Content-Type: application/json
```

```json
{
  "phone": "010-1234-5678"
}
```

예약 당시 연락처가 일치하고 최종 견적이 있는 예약이면 견적 동의 처리 후 예약 상태를 `확정`으로 변경합니다. 동의가 완료되면 확정된 예약 정보와 동의 금액을 JSON으로 응답합니다.

### 파일 업로드 API

#### 짐 사진 업로드

```http
POST /api/reservations/{reservationId}/photos
Content-Type: multipart/form-data
```

```text
phone=010-1234-5678
photos=boxes.jpg
```

예약 당시 연락처가 일치하고 현재 상태가 `접수` 또는 `상담중`이면 짐 사진을 업로드할 수 있습니다. 허용 확장자는 `jpg`, `jpeg`, `png`, `webp`입니다. 업로드가 완료되면 저장된 사진의 원본 파일명과 접근 URL을 JSON 배열로 응답합니다.

### 고객 리뷰 API

#### 리뷰 작성

```http
POST /api/reviews
Content-Type: application/json
```

```json
{
  "reservationId": 1,
  "phone": "010-1234-5678",
  "rating": 5,
  "content": "친절하고 정확했습니다."
}
```

예약 당시 연락처가 일치하고 예약 상태가 `완료`이면 리뷰를 작성할 수 있습니다. 같은 예약에는 리뷰를 한 번만 작성할 수 있습니다.

## MySQL Database 접속 정보

```text
Host      localhost
Port      3306
Database  movemission
User      root
Password  mysql
```

애플리케이션은 `spring.jpa.hibernate.ddl-auto=update` 설정을 사용합니다. 개발 단계에서 엔티티 필드가 추가되면 Hibernate가 테이블 구조를 자동으로 반영합니다.

## 관리자 계정

현재 관리자 계정은 데이터베이스에 저장됩니다. 최초 실행 시 초기 관리자 계정이 자동 생성됩니다.

```text
ID       admin
Password admin1234
```

개발 중에는 위 기본값으로 바로 로그인할 수 있지만, 실제 운영 환경에서는 서버 실행 전에 아래 환경변수로 초기 관리자 계정을 반드시 바꿔야 합니다.

```bat
set ADMIN_INITIAL_USERNAME=운영관리자ID
set ADMIN_INITIAL_PASSWORD=충분히_긴_초기비밀번호
set ADMIN_INITIAL_ROLE=ADMIN
```

이미 기본 비밀번호로 생성된 계정이 있으면 관리자 대시보드에 비밀번호 변경 경고가 표시됩니다.

`/admin/**` 경로는 로그인한 관리자만 접근할 수 있습니다. 고객용 예약 신청과 예약 조회 화면은 로그인 없이 사용할 수 있습니다.

관리자 보안 설정은 아래 기준으로 적용했습니다.

- 관리자 화면은 `ADMIN` 권한 계정만 접근 가능
- 로그인 성공 시 세션 고정 공격 방지를 위해 세션 재발급
- 동일 관리자 계정의 동시 로그인은 1개 세션으로 제한
- 관리자 세션은 30분 동안 요청이 없으면 만료
- 세션 쿠키는 `HttpOnly`, `SameSite=Strict` 설정 적용
- 관리자 로그인은 기본 5회 실패 시 10분 동안 제한

로그인 실패 제한 기준은 환경변수로 조정할 수 있습니다.

```bat
set ADMIN_LOGIN_MAX_FAILURE_COUNT=5
set ADMIN_LOGIN_LOCK_MINUTES=10
```

## 예약 상태

```text
RECEIVED    접수
CONSULTING  상담중
ESTIMATE_SENT 견적안내
CONFIRMED   확정
COMPLETED   완료
CANCELED    취소
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
```

## Git

현재 작업 브랜치:

```text
codex/initial-moving-service
```
