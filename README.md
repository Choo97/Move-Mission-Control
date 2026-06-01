# Move Mission Control

Spring Boot 기반 이사 예약 웹 서비스입니다. 고객은 이사 예약을 신청하고, 관리자는 예약 현황을 확인하면서 상태와 견적을 관리할 수 있습니다.

## 기술 스택

- Java 17
- Spring Boot 3.3.6
- Spring MVC
- Thymeleaf
- Spring Data JPA
- H2 Database
- Maven

## 주요 기능

- 메인 화면
- 이사 예약 신청
- 예약 접수 완료 화면
- 관리자 예약 목록
- 예약 상태 변경
- 견적 금액 입력

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
- 관리자 메모 기능
- 담당 기사 또는 이사업체 배정
- 예약 상태 변경 이력 관리
- SMS 또는 카카오 알림톡 발송
- 이메일 예약 확인 안내
- 결제 기능
- 쿠폰 및 할인 정책
- 고객 리뷰 및 평점
- 자주 묻는 질문 페이지
- MySQL 또는 PostgreSQL 전환
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
H2 콘솔         http://localhost:8081/h2-console
```

## H2 Database 접속 정보

```text
JDBC URL  jdbc:h2:mem:moving-reservation
User      sa
Password  없음
```

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
