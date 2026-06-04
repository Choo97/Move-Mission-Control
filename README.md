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

- 메인 화면
- 이사 예약 신청
- 예약 접수 완료 화면
- 예약 조회용 고객 인증
- 주소 검색 API 연동
- 이사 유형별 기본 견적 계산
- 견적 산정 내역 표시
- 예약별 견적 확정서 출력
- 견적 확정서 PDF 다운로드
- 고객용 견적 확정서 공유 링크
- 고객 견적 동의 및 예약 확정
- 고객 견적 동의 금액 및 일시 기록
- 고객 동의 금액과 현재 견적 차이 표시
- 관리자 견적 기준 관리
- 견적 기준 변경 이력 관리
- 층수 및 사다리차 옵션
- 지도 API 자동 거리 계산
- 관리자 예약 목록
- 관리자 대시보드 요약 지표
- 예약 상태 변경
- 견적 금액 입력
- 관리자 예약 메모
- 관리자 로그인 및 로그아웃
- 예약 상태 변경 이력 관리
- 이메일 예약 확인 안내
- SMTP 이메일 실제 발송 연동
- 알림 발송 성공 및 실패 상태 필터
- 실패 이메일 재발송
- 관리자 운영 대시보드 개선
- 자주 묻는 질문 페이지
- 관리자 FAQ 등록 및 수정
- 예약 신청 전 예상 견적 미리보기
- 예약 신청 화면 섹션 정리
- 관리자 예약 검색 및 상태 필터
- 관리자 이사일 기간 필터
- 관리자 빠른 기간 필터
- 고객 예약 취소
- 고객 예약 수정
- 짐 사진 업로드
- 쿠폰 및 할인 정책
- 쿠폰 사용 이력 관리
- 고객 리뷰 및 평점
- 고객 알림 발송 준비 이력
- 관리자 처리자 기록
- 관리자 계정 DB 로그인
- 관리자 비밀번호 변경
- 관리자 계정 추가
- 관리자 계정 목록 및 비활성화

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

초기 화면은 밝은 흰색 배경에 하늘색 포인트를 섞은 `Clean Sky Mission` 방향으로 구성했습니다.

- 기본 배경: 흰색, 연한 하늘색
- 포인트 컬러: 하늘색, 짙은 네이비
- UI: 카드형 예약/관리 화면
- 분위기: 깔끔한 예약 플랫폼 + 관제 보드

## 실행 방법

현재 PC에는 Java 11과 Java 17이 함께 있으므로, CMD에서 Java 17을 먼저 잡고 실행합니다.

```bat
cd "C:\Users\Chanho\Documents\이사 예약 웹 서비스"
set JAVA_HOME=C:\Dev\java\jdk-17.0.7
set PATH=%JAVA_HOME%\bin;%PATH%
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

서버가 실행되면 브라우저에서 아래 주소로 접속합니다.

```text
http://localhost:8081/
```

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
```

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

현재 관리자 계정은 데이터베이스에 저장됩니다. 최초 실행 시 기본 관리자 계정이 자동 생성됩니다.

```text
ID       admin
Password admin1234
```

`/admin/**` 경로는 로그인한 관리자만 접근할 수 있습니다. 고객용 예약 신청과 예약 조회 화면은 로그인 없이 사용할 수 있습니다.

## 예약 상태

```text
RECEIVED    접수
CONSULTING  상담중
CONFIRMED   확정
COMPLETED   완료
CANCELED    취소
```

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
