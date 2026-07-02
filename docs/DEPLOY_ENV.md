# 배포 환경변수

이 문서는 포트폴리오용 1차 배포에서 필요한 환경변수를 정리합니다.

로컬 개발은 `application.yml`과 `.env.local`을 사용합니다. 배포 환경은 `SPRING_PROFILES_ACTIVE=prod`를 켜서 `application-prod.yml`을 사용합니다.

## 백엔드 필수 환경변수

| 이름 | 예시 | 설명 |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | `prod` | 배포 설정 파일을 사용합니다. |
| `PORT` | Railway 자동 주입 | Railway가 외부 트래픽을 연결할 포트입니다. 직접 만들지 않아도 됩니다. |
| `SERVER_PORT` | `8080` | Railway가 아닌 환경에서 사용할 백업 포트입니다. |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://host:3306/movemission?serverTimezone=Asia/Seoul&characterEncoding=UTF-8` | 배포 DB 주소입니다. |
| `SPRING_DATASOURCE_USERNAME` | `move_user` | 배포 DB 사용자입니다. |
| `SPRING_DATASOURCE_PASSWORD` | `비공개` | 배포 DB 비밀번호입니다. |
| `APP_FRONTEND_BASE_URL` | `https://frontend.example.com` | 백엔드가 프론트 주소로 이동시킬 때 사용합니다. |
| `APP_CORS_ALLOWED_ORIGIN_VITE` | `https://frontend.example.com` | React 프론트에서 API 호출을 허용할 출처입니다. |
| `APP_CORS_ALLOWED_ORIGIN_CRA` | `https://frontend.example.com` | 현재 설정 구조상 두 번째 CORS 출처입니다. 같은 값으로 둬도 됩니다. |
| `PRIVACY_CRYPTO_SECRET` | `긴 랜덤 문자열` | 개인정보 암호화 키입니다. 배포 후 바꾸면 기존 암호문 복호화가 어려워질 수 있습니다. |
| `PRIVACY_HASH_SECRET` | `다른 긴 랜덤 문자열` | 연락처 조회용 해시 키입니다. 암호화 키와 다른 값으로 둡니다. |
| `ADMIN_INITIAL_USERNAME` | `admin` | 최초 관리자 계정 ID입니다. |
| `ADMIN_INITIAL_PASSWORD` | `강한 비밀번호` | 최초 관리자 계정 비밀번호입니다. `admin1234`를 사용하면 안 됩니다. |
| `ADMIN_INITIAL_ROLE` | `ADMIN` | 최초 관리자 권한입니다. |

## 프론트 필수 환경변수

| 이름 | 예시 | 설명 |
| --- | --- | --- |
| `VITE_API_BASE_URL` | `https://backend.example.com` | React가 호출할 백엔드 API 주소입니다. |

프론트 환경변수는 빌드 시점에 반영됩니다. 값을 바꾸면 프론트를 다시 빌드해야 합니다.

## 파일 업로드

| 이름 | 권장값 | 설명 |
| --- | --- | --- |
| `RESERVATION_PHOTO_UPLOAD_DIR` | `/app/uploads/reservation-photos` | 업로드 파일 저장 경로입니다. 배포 플랫폼에서 영구 저장 경로로 연결해야 합니다. |
| `RESERVATION_PHOTO_MAX_FILE_SIZE_BYTES` | `10485760` | 파일당 최대 10MB입니다. |
| `RESERVATION_PHOTO_MAX_FILES_PER_REQUEST` | `5` | 한 번에 업로드 가능한 파일 수입니다. |
| `RESERVATION_PHOTO_MAX_FILES_PER_RESERVATION` | `10` | 예약 1건당 최대 파일 수입니다. |

## 1차 배포에서는 꺼두는 값

이메일과 SMS는 실제 발신 계정, 비용, 사업자 정보가 필요할 수 있으므로 1차 데모 배포에서는 꺼두는 것을 권장합니다.

```env
NOTIFICATION_EMAIL_ENABLED=false
NOTIFICATION_SMS_ENABLED=false
SMS_PROVIDER=disabled
```

## 예시 파일

- 백엔드: `.env.production.example`
- 프론트: `frontend/.env.production.example`

실제 값이 들어간 `.env.production` 파일은 Git에 올리지 않습니다.

## Vercel/Railway 배포 순서

실제 배포 순서는 [docs/DEPLOY_VERCEL_RAILWAY.md](DEPLOY_VERCEL_RAILWAY.md)에 따로 정리했습니다.
