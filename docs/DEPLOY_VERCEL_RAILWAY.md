# Vercel/Railway 배포 가이드

이 문서는 포트폴리오용 1차 배포를 기준으로 합니다.

## 선택한 구조

```text
사용자
-> Vercel React 프론트엔드
-> Railway Spring Boot 백엔드
-> Railway MySQL
-> Railway Volume 파일 업로드 저장소
```

이렇게 나눈 이유는 프론트와 백엔드의 역할이 다르기 때문입니다. React는 정적 파일로 빌드해서 빠르게 제공하는 Vercel이 편하고, Spring Boot와 MySQL은 서버 프로세스와 DB가 필요한 Railway가 더 단순합니다.

## 프로젝트에 미리 반영한 설정

| 파일 | 이유 |
| --- | --- |
| `railway.toml` | Railway가 백엔드를 빌드하고 실행할 명령을 명확히 알 수 있게 합니다. |
| `src/main/resources/application-prod.yml` | Railway가 자동으로 넣는 `PORT`를 읽어서 서버가 올바른 포트로 뜨게 합니다. |
| `frontend/vercel.json` | React 주소를 새로고침했을 때 404가 나지 않도록 모든 경로를 `index.html`로 돌립니다. |
| `.env.production.example` | 배포에 필요한 환경변수 이름만 예시로 남기고 실제 비밀번호는 Git에 올리지 않게 합니다. |

## 사용자가 해야 하는 최소 작업

### 1. GitHub에 최신 코드 올리기

```bash
git push origin codex/initial-moving-service
```

Railway와 Vercel은 GitHub 저장소를 기준으로 배포합니다. 그래서 먼저 GitHub에 현재 배포 설정이 올라가 있어야 합니다.

### 2. Railway 백엔드 만들기

1. Railway에서 `New Project`를 선택합니다.
2. `Deploy from GitHub repo`를 선택합니다.
3. `Choo97/Move-Mission-Control` 저장소를 선택합니다.
4. 서비스가 생성되면 `Variables`에 백엔드 환경변수를 넣습니다.
5. `Networking`에서 `Generate Domain`을 눌러 백엔드 URL을 만듭니다.

처음에는 프론트 URL이 아직 없으므로 아래 3개는 임시로 같은 값에 둬도 됩니다.

```env
APP_FRONTEND_BASE_URL=https://example.com
APP_CORS_ALLOWED_ORIGIN_VITE=https://example.com
APP_CORS_ALLOWED_ORIGIN_CRA=https://example.com
```

### 3. Railway MySQL 추가

1. Railway 프로젝트에서 `New` 또는 `Add Service`를 선택합니다.
2. `Database`에서 MySQL을 추가합니다.
3. MySQL 서비스의 연결 정보를 보고 백엔드 변수에 넣습니다.

백엔드에 필요한 DB 변수는 아래 형태입니다.

```env
SPRING_DATASOURCE_URL=jdbc:mysql://호스트:포트/DB명?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
SPRING_DATASOURCE_USERNAME=사용자명
SPRING_DATASOURCE_PASSWORD=비밀번호
```

### 4. Railway Volume 추가

짐 사진 업로드 파일은 서버 재배포 후에도 남아야 합니다. 그래서 Railway 백엔드 서비스에 Volume을 추가합니다.

```text
Mount Path: /app/uploads/reservation-photos
```

백엔드 환경변수도 같은 경로로 둡니다.

```env
RESERVATION_PHOTO_UPLOAD_DIR=/app/uploads/reservation-photos
```

### 5. Railway 백엔드 필수 환경변수

아래 값은 Railway 백엔드 서비스의 `Variables`에 넣습니다.

```env
SPRING_PROFILES_ACTIVE=prod
SPRING_JPA_HIBERNATE_DDL_AUTO=update

SPRING_DATASOURCE_URL=jdbc:mysql://...
SPRING_DATASOURCE_USERNAME=...
SPRING_DATASOURCE_PASSWORD=...

APP_FRONTEND_BASE_URL=https://example.com
APP_CORS_ALLOWED_ORIGIN_VITE=https://example.com
APP_CORS_ALLOWED_ORIGIN_CRA=https://example.com

PRIVACY_CRYPTO_SECRET=긴_랜덤_문자열
PRIVACY_HASH_SECRET=다른_긴_랜덤_문자열

ADMIN_INITIAL_USERNAME=admin
ADMIN_INITIAL_PASSWORD=강한_관리자_비밀번호
ADMIN_INITIAL_ROLE=ADMIN

RESERVATION_PHOTO_UPLOAD_DIR=/app/uploads/reservation-photos

NOTIFICATION_EMAIL_ENABLED=false
NOTIFICATION_SMS_ENABLED=false
SMS_PROVIDER=disabled
```

`PORT`는 Railway가 자동으로 넣으므로 직접 만들지 않습니다.

### 6. Vercel 프론트엔드 만들기

1. Vercel에서 `Add New Project`를 선택합니다.
2. GitHub 저장소 `Choo97/Move-Mission-Control`을 선택합니다.
3. `Root Directory`를 `frontend`로 설정합니다.
4. Framework는 Vite로 감지되면 그대로 둡니다.
5. 환경변수에 아래 값을 넣습니다.

```env
VITE_API_BASE_URL=https://Railway에서_생성한_백엔드_URL
```

Vercel은 `npm run build`로 빌드하고 `dist` 폴더를 배포하면 됩니다.

### 7. 프론트 URL을 Railway에 다시 반영

Vercel 배포가 끝나면 프론트 URL이 생깁니다. 이제 Railway 백엔드 변수 3개를 실제 Vercel URL로 바꿉니다.

```env
APP_FRONTEND_BASE_URL=https://Vercel에서_생성한_프론트_URL
APP_CORS_ALLOWED_ORIGIN_VITE=https://Vercel에서_생성한_프론트_URL
APP_CORS_ALLOWED_ORIGIN_CRA=https://Vercel에서_생성한_프론트_URL
```

이 작업이 필요한 이유는 브라우저 보안 정책 때문입니다. React 화면이 다른 도메인의 Spring Boot API를 호출하려면 백엔드가 해당 프론트 주소를 허용해야 합니다.

### 8. 배포 후 확인할 URL

```text
프론트 메인        https://Vercel_URL/
예약 접수          https://Vercel_URL/?view=create
예약 조회          https://Vercel_URL/?view=lookup
관리자 로그인      https://Vercel_URL/admin/login
관리자 예약 목록   https://Vercel_URL/admin/reservations
백엔드 Swagger     https://Railway_URL/swagger-ui/index.html
```

## 배포 확인 순서

1. 프론트 메인 화면이 열리는지 확인합니다.
2. 예약 접수가 되는지 확인합니다.
3. 예약번호와 연락처로 조회되는지 확인합니다.
4. 관리자 로그인이 되는지 확인합니다.
5. 관리자 예약 목록에서 방금 만든 예약이 보이는지 확인합니다.
6. 짐 사진 업로드가 되는지 확인합니다.

이 순서로 확인하는 이유는 사용자의 핵심 흐름이 `예약 접수 -> 예약 조회 -> 관리자 처리`이기 때문입니다. 이 세 가지가 배포 환경에서 되면 포트폴리오 시연의 기본은 통과한 것입니다.

## 기존 DB를 데모 데이터로 교체하기

일반 이사 예약과 비영리 도움 요청이 함께 들어 있는 포트폴리오용 데이터로 초기화하려면 [DEMO_DATA.md](DEMO_DATA.md)의 절차를 따릅니다. `ddl-auto=create`는 한 번만 사용하고 데이터 생성 확인 직후 반드시 `update`로 되돌립니다.

## 공식 문서

- [Railway Spring Boot 배포 가이드](https://docs.railway.com/guides/spring-boot)
- [Railway Public Networking](https://docs.railway.com/networking/public-networking)
- [Railway Volumes](https://docs.railway.com/volumes)
- [Vercel Vite 가이드](https://vercel.com/docs/frameworks/frontend/vite)
