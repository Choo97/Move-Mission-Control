# 실행 및 환경 설정

이 문서는 로컬 개발 환경에서 Move Mission Control을 실행하고 테스트하는 방법을 정리합니다.

## 실행 방법

이 프로젝트는 Java 17, Maven, MySQL이 설치되어 있으면 Windows, macOS, Linux에서 실행할 수 있습니다.

컴퓨터를 껐다 켜면 이전에 실행한 백엔드/프론트엔드 서버는 모두 종료됩니다. 재부팅 후에는 아래 순서대로 다시 실행합니다.

### 재부팅 후 실행 순서

터미널을 2개 열어 각각 실행합니다.

터미널 1: 백엔드 서버

```bash
bash run-backend.sh
```

터미널 2: 프론트엔드 서버

```bash
bash run-frontend.sh
```

실행 후 접속 주소는 아래와 같습니다.

```text
고객 React 화면  http://localhost:5173/
React 관리자 화면 http://localhost:5173/admin/reservations
백엔드 화면      http://localhost:8081/
기존 관리자 화면 http://localhost:8081/admin/reservations
```

프론트엔드는 `.env.local`의 `VITE_API_BASE_URL` 값을 사용해 백엔드 API에 연결합니다. 기본값은 `http://localhost:8081`입니다.

### 공통 준비 사항

- Java 17 설치
- Maven 설치
- MySQL 실행
- MySQL에 `movemission` 데이터베이스 생성

MySQL 접속 정보는 아래 `MySQL Database 접속 정보` 섹션과 맞춰 둡니다.

```sql
CREATE DATABASE movemission CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

애플리케이션은 `spring.jpa.hibernate.ddl-auto=update` 설정을 사용하므로, 서버가 처음 실행될 때 필요한 테이블을 자동으로 생성합니다.

### Git Bash, macOS, Linux 백엔드 실행

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

백엔드만 직접 실행하려면 아래 명령을 사용합니다.

```bash
bash run-backend.sh
```

`run-backend.sh`는 내부에서 `run-local.sh`를 호출합니다. `run-local.sh`는 `.env.local`을 읽고 `SERVER_PORT` 값으로 Spring Boot 서버를 실행합니다.

로컬 이메일/SMS 설정까지 함께 적용하려면 `.env.example`을 복사해 `.env.local`을 만들고 실제 값을 입력한 뒤 실행합니다.

```bash
cp .env.example .env.local
bash run-backend.sh
```

`.env.local`에는 실제 이메일 계정, 앱 비밀번호, SMS API 키가 들어갈 수 있으므로 Git에 올리지 않습니다.

백엔드 서버가 실행되면 브라우저에서 아래 주소로 접속합니다.

```text
http://localhost:8081/
```

### React 프론트엔드 실행

고객용 React 화면은 별도 터미널에서 실행합니다.

```bash
bash run-frontend.sh
```

`run-frontend.sh`는 `.env.local`을 읽고 아래 값을 사용합니다.

```text
FRONTEND_PORT=5173
VITE_API_BASE_URL=http://localhost:8081
```

프론트엔드 서버가 실행되면 아래 주소로 접속합니다.

```text
http://localhost:5173/
```

만약 Windows가 `5173` 포트를 예약해서 실행되지 않으면 `.env.local`에서 `FRONTEND_PORT`를 `3000`으로 바꾼 뒤 다시 실행합니다.

```text
FRONTEND_PORT=3000
VITE_API_BASE_URL=http://localhost:8081
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

## 이메일 발송 설정

예약 접수 시 이메일을 입력하면 이메일 안내 이력이 `발송 준비` 상태로 생성됩니다. 실제 SMTP 발송을 사용하려면 서버 실행 전에 아래 환경변수를 설정합니다.

```bash
NOTIFICATION_EMAIL_ENABLED=true
NOTIFICATION_EMAIL_FROM=보내는메일@example.com
MAIL_HOST=smtp.example.com
MAIL_PORT=587
MAIL_USERNAME=SMTP_계정
MAIL_PASSWORD=SMTP_비밀번호
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS_ENABLE=true
```

SMTP 설정이 꺼져 있으면 애플리케이션은 정상 실행되지만, 관리자 화면에서 이메일 발송을 시도할 때 실패 이력으로 기록됩니다.

## SMS 발송 설정

예약 접수 시 고객 연락처로 SMS 안내 이력이 `발송 준비` 상태로 생성됩니다. `SMS_ADMIN_TO` 값을 입력하면 새 예약 접수 시 관리자에게 보낼 SMS 안내 이력도 함께 생성됩니다.

현재 코드는 실제 SMS 업체 API를 바로 호출하지 않고, 발송 구조와 이력 관리부터 준비합니다. 개발 중 성공 흐름만 확인하려면 `SMS_PROVIDER=mock`을 사용합니다. 실제 문자 비용이 나가는 업체 연동은 발신번호 등록과 API 키 준비가 끝난 뒤 별도로 연결합니다.

```bash
NOTIFICATION_SMS_ENABLED=false
SMS_PROVIDER=disabled
SMS_FROM=01012345678
SMS_ADMIN_TO=01012345678
SMS_ACCESS_KEY=your-sms-access-key
SMS_SECRET_KEY=your-sms-secret-key
SMS_SERVICE_ID=your-sms-service-id
```

개발용으로 발송 성공 처리만 확인하려면 아래처럼 설정합니다.

```bash
NOTIFICATION_SMS_ENABLED=true
SMS_PROVIDER=mock
SMS_FROM=01012345678
SMS_ADMIN_TO=01012345678
```

실제 API 키처럼 특수문자가 들어간 값은 `.env.local`에서 따옴표로 감쌉니다.

```bash
SMS_SECRET_KEY='special#secret=value'
```

SMS 설정이 꺼져 있으면 애플리케이션은 정상 실행되지만, 관리자 화면에서 SMS 발송을 시도할 때 실패 이력으로 기록됩니다.

## 개인정보 보관 기간 설정

예약으로 성립하지 않은 중복 시간 예약 시도 기록은 기본 30일 동안 보관하고, 매일 오전 3시에 자동 삭제합니다.

```text
PRIVACY_CONFLICT_ATTEMPT_RETENTION_DAYS=30
PRIVACY_RETENTION_CLEANUP_CRON=0 0 3 * * *
```

실제 예약, 견적, 결제 근거 데이터는 이 자동 삭제 대상에 포함하지 않습니다. 운영 환경에서 관련 법률과 사업 정책에 따른 보관 기간을 확정한 뒤 별도의 익명화 또는 파기 정책을 적용해야 합니다.

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
