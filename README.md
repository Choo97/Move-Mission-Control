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
- 관리자 예약 목록
- 예약 상태 변경
- 견적 금액 입력
- 관리자 예약 메모
- 관리자 로그인 및 로그아웃

## 추후 개발 예정 기능

현재 프로젝트는 MVP 단계이므로, 먼저 예약 접수와 관리자 확인 흐름에 집중했습니다. 실제 서비스로 확장하려면 아래 기능들을 단계적으로 추가할 수 있습니다.

- 회원가입 및 로그인
- 관리자 권한 분리
- 예약 조회용 고객 인증
- 예약 수정 및 취소
- 짐 사진 업로드
- 주소 검색 API 연동
- 지도 기반 이동 거리 계산
- 이사 유형별 기본 견적 계산
- 사다리차, 엘리베이터, 층수 옵션
- 담당 기사 또는 이사업체 배정
- 예약 상태 변경 이력 관리
- SMS 또는 카카오 알림톡 발송
- 이메일 예약 확인 안내
- 결제 기능
- 쿠폰 및 할인 정책
- 고객 리뷰 및 평점
- 자주 묻는 질문 페이지
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

## 주요 URL

```text
메인 화면       http://localhost:8081/
예약 신청       http://localhost:8081/reservations/new
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

현재 관리자 계정은 Spring Security 메모리 계정으로 설정되어 있습니다.

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
