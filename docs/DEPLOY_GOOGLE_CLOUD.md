# Google Cloud 배포 가이드

Vercel의 React 프론트엔드는 유지하고, 백엔드와 데이터 저장소를 Google Cloud로 옮기는 절차입니다.

## 배포 구조

```text
사용자
-> Vercel React 프론트엔드
-> Google Cloud Run Spring Boot 백엔드
   -> Cloud SQL for MySQL
   -> Cloud Storage 사진 버킷(볼륨 마운트)
```

## 현재 리소스

| 항목 | 값 |
| --- | --- |
| 프로젝트 ID | `nalpo-504100` |
| 리전 | `asia-northeast3` (서울) |
| Cloud SQL 연결 이름 | `nalpo-504100:asia-northeast3:chanho-instance-mysql` |
| 데이터베이스 | `movemission` |
| 데이터베이스 사용자 | `nalpo_app` |
| Cloud Storage 버킷 | `chanho-bucket` |
| Cloud Run 서비스 이름 | `nalpo-backend` |
| 사진 마운트 경로 | `/app/uploads/reservation-photos` |

비밀번호와 암호화 키는 이 문서나 Git에 넣지 않습니다. Cloud Storage의 공개 액세스 방지는 계속 사용 설정해 둡니다.

## 1. API 활성화

Google Cloud 콘솔에서 Cloud Shell을 열고 실행합니다.

```bash
gcloud config set project nalpo-504100

gcloud services enable \
  run.googleapis.com \
  cloudbuild.googleapis.com \
  artifactregistry.googleapis.com \
  sqladmin.googleapis.com \
  secretmanager.googleapis.com
```

## 2. Cloud Run 서비스 계정 만들기

아래 명령은 서비스 계정을 처음 만들 때 한 번만 실행합니다.

```bash
gcloud iam service-accounts create nalpo-cloud-run \
  --display-name="24nalpo Cloud Run"

SERVICE_ACCOUNT="nalpo-cloud-run@nalpo-504100.iam.gserviceaccount.com"

gcloud projects add-iam-policy-binding nalpo-504100 \
  --member="serviceAccount:${SERVICE_ACCOUNT}" \
  --role="roles/cloudsql.client"

gcloud projects add-iam-policy-binding nalpo-504100 \
  --member="serviceAccount:${SERVICE_ACCOUNT}" \
  --role="roles/secretmanager.secretAccessor"

gcloud storage buckets add-iam-policy-binding gs://chanho-bucket \
  --member="serviceAccount:${SERVICE_ACCOUNT}" \
  --role="roles/storage.objectUser"
```

`Cloud SQL Client`는 암호화된 DB 연결에, `Storage Object User`는 사진 읽기·쓰기·삭제에 사용됩니다. 버킷 자체를 공개할 필요는 없습니다.

## 3. Secret Manager에 비밀 값 저장하기

Google Cloud 콘솔의 `보안 > Secret Manager`에서 아래 보안 비밀을 만듭니다.

| 보안 비밀 이름 | 저장할 값 |
| --- | --- |
| `nalpo-db-password` | `nalpo_app`의 Cloud SQL 비밀번호 |
| `nalpo-privacy-crypto-secret` | 충분히 긴 개인정보 암호화용 랜덤 문자열 |
| `nalpo-privacy-hash-secret` | 위 값과 다른 개인정보 해시용 랜덤 문자열 |
| `nalpo-admin-password` | 배포용 관리자 계정의 강한 비밀번호 |

각 보안 비밀은 값을 입력하고 버전 `1`을 생성하면 됩니다. 실제 값은 `.env` 파일이나 GitHub에 커밋하지 않습니다.

## 4. 최신 코드를 Cloud Shell에 준비하기

먼저 로컬 변경사항을 GitHub에 올린 다음 Cloud Shell에서 저장소를 받습니다.

```bash
git clone https://github.com/Choo97/Move-Mission-Control.git
cd Move-Mission-Control
```

기본 브랜치가 아닌 브랜치를 배포한다면 `git checkout 브랜치명`도 실행합니다.

## 5. Cloud Run에 처음 배포하기

아래 명령은 빈 Cloud SQL에 테이블과 데모 데이터 12건을 만들도록 `DEMO_SEED_ENABLED=true`로 배포합니다. 일반 이사 예약 6건과 비영리 도움 요청 6건이 생성됩니다.

```bash
gcloud run deploy nalpo-backend \
  --source=. \
  --project=nalpo-504100 \
  --region=asia-northeast3 \
  --execution-environment=gen2 \
  --service-account=nalpo-cloud-run@nalpo-504100.iam.gserviceaccount.com \
  --allow-unauthenticated \
  --cpu=1 \
  --memory=1Gi \
  --concurrency=8 \
  --min-instances=0 \
  --max-instances=3 \
  --set-env-vars="SPRING_PROFILES_ACTIVE=prod,SPRING_DATASOURCE_URL=jdbc:mysql:///movemission?cloudSqlInstance=nalpo-504100:asia-northeast3:chanho-instance-mysql&socketFactory=com.google.cloud.sql.mysql.SocketFactory&cloudSqlRefreshStrategy=lazy&ipTypes=PUBLIC&serverTimezone=Asia/Seoul&characterEncoding=UTF-8,SPRING_DATASOURCE_USERNAME=nalpo_app,SPRING_JPA_HIBERNATE_DDL_AUTO=update,DB_MAX_POOL_SIZE=5,APP_FRONTEND_BASE_URL=https://24nalpo.vercel.app,APP_CORS_ALLOWED_ORIGIN_VITE=https://24nalpo.vercel.app,APP_CORS_ALLOWED_ORIGIN_CRA=https://24nalpo.vercel.app,ADMIN_INITIAL_USERNAME=admin,ADMIN_INITIAL_ROLE=ADMIN,RESERVATION_PHOTO_UPLOAD_DIR=/app/uploads/reservation-photos,DEMO_SEED_ENABLED=true,NOTIFICATION_EMAIL_ENABLED=false,NOTIFICATION_SMS_ENABLED=false,SMS_PROVIDER=disabled" \
  --set-secrets="SPRING_DATASOURCE_PASSWORD=nalpo-db-password:latest,PRIVACY_CRYPTO_SECRET=nalpo-privacy-crypto-secret:latest,PRIVACY_HASH_SECRET=nalpo-privacy-hash-secret:latest,ADMIN_INITIAL_PASSWORD=nalpo-admin-password:latest" \
  --add-volume="mount-path=/app/uploads/reservation-photos,type=cloud-storage,bucket=chanho-bucket,readonly=false"
```

Cloud Run이 `PORT`를 자동으로 주입하므로 `PORT`나 `SERVER_PORT`는 따로 만들지 않습니다. Cloud SQL Java Connector를 사용하므로 공개 IP 허용 목록에 Cloud Run 주소를 추가할 필요도 없습니다.

## 6. 데이터 생성 확인 후 시드 끄기

배포 로그를 확인합니다.

```bash
gcloud run services logs read nalpo-backend \
  --region=asia-northeast3 \
  --limit=100
```

아래 메시지가 보이면 데이터 생성에 성공한 것입니다.

```text
일반 이사 6건과 비영리 도움 요청 6건의 데모 시드를 생성했습니다.
```

확인 직후 시드를 끕니다.

```bash
gcloud run services update nalpo-backend \
  --region=asia-northeast3 \
  --update-env-vars="DEMO_SEED_ENABLED=false"
```

시더는 예약 테이블이 비어 있을 때만 실행되므로 중복 생성되지는 않지만, 운영 설정은 `false`로 유지하는 것이 명확합니다.

## 7. Vercel을 새 백엔드에 연결하기

Cloud Run 배포 결과에 표시된 `https://...run.app` 주소를 복사합니다. Vercel 프로젝트의 환경변수를 아래처럼 바꾸고 Production을 다시 배포합니다.

```env
VITE_API_BASE_URL=https://Cloud_Run_서비스_URL
```

Vercel 도메인이 `https://24nalpo.vercel.app`와 다르면 Cloud Run의 아래 환경변수 3개도 실제 Vercel 주소로 바꿉니다.

```text
APP_FRONTEND_BASE_URL
APP_CORS_ALLOWED_ORIGIN_VITE
APP_CORS_ALLOWED_ORIGIN_CRA
```

## 8. 배포 확인

1. Vercel 메인 화면과 `/help` 화면이 열리는지 확인합니다.
2. 일반 이사 예약과 비영리 도움 요청을 각각 한 건씩 접수합니다.
3. 예약번호와 연락처로 조회합니다.
4. 관리자 로그인 후 총 12건의 데모 데이터가 보이는지 확인합니다.
5. 사진을 올린 뒤 재배포해도 사진이 남아 있는지 확인합니다.
6. `Cloud Storage > chanho-bucket`에서 업로드 객체가 생겼는지 확인합니다.

Cloud Storage 볼륨은 일반 파일 경로처럼 보이지만 내부적으로 Cloud Storage FUSE를 사용합니다. 현재 코드는 UUID 파일명을 사용하므로 같은 파일을 동시에 덮어쓰는 충돌을 피합니다. 사진 업로드 메모리를 고려해 Cloud Run 메모리는 `1Gi`로 설정했습니다.

## 기존 데이터까지 지우고 다시 만들 때

새 Cloud SQL은 `ddl-auto=update`와 첫 시드만으로 충분합니다. 이미 예약 데이터가 있는 DB를 의도적으로 모두 초기화할 때만 [데모 데이터 가이드](DEMO_DATA.md)의 파괴적 초기화 절차를 사용합니다.

## 공식 문서

- [Cloud Run에서 Cloud SQL 연결](https://cloud.google.com/sql/docs/mysql/connect-run)
- [Cloud SQL Java Connector](https://github.com/GoogleCloudPlatform/cloud-sql-jdbc-socket-factory)
- [Cloud Run Cloud Storage 볼륨 마운트](https://cloud.google.com/run/docs/configuring/services/cloud-storage-volume-mounts)
- [Cloud Run 보안 비밀 설정](https://cloud.google.com/run/docs/configuring/services/secrets)
