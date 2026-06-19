# REST API 문서

React 같은 별도 프론트엔드에서 사용할 수 있도록 고객 기능 API를 제공합니다. HTML 화면과 같은 예약 규칙을 사용하며, 예약 번호와 예약 당시 연락처로 고객 요청을 확인합니다.

브라우저에서 `http://localhost:8081/swagger-ui/index.html`에 접속하면 API 목록을 확인하고 직접 요청을 테스트할 수 있습니다.
요청/응답 DTO에는 Swagger 설명과 예시 값을 추가해 각 필드의 의미를 문서 화면에서 바로 확인할 수 있습니다.
고객 예약 API와 고객 리뷰 API는 Swagger 그룹, 엔드포인트 요약, 성공/실패 상태코드 설명을 제공합니다.

## 공통 규칙

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

## API 요약

| 구분 | Method | URL | 용도 |
| --- | --- | --- | --- |
| 고객 예약 | `POST` | `/api/reservations` | 예약 신청 |
| 고객 예약 | `POST` | `/api/reservations/search` | 예약 번호와 연락처로 예약 조회 |
| 고객 예약 | `PATCH` | `/api/reservations/{reservationId}` | 예약 정보 수정 |
| 고객 예약 | `POST` | `/api/reservations/{reservationId}/cancel` | 예약 취소 |
| 고객 예약 | `POST` | `/api/reservations/{reservationId}/estimate/accept` | 최종 견적 동의 및 예약 확정 |
| 파일 업로드 | `POST` | `/api/reservations/{reservationId}/photos` | 짐 사진 업로드 |
| 고객 리뷰 | `POST` | `/api/reviews` | 완료 예약 리뷰 작성 |

## React 전환 준비 점검

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

## React 전환 전략

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

중요한 기준은 같은 고객 기능 안에서 Thymeleaf와 React URL을 섞지 않는 것입니다. 전환 중에는 기존 `/reservations/**`는 유지하고, React 고객 화면은 `frontend/` 개발 서버에서 먼저 검증한 뒤 배포 방식을 결정합니다. 현재 React 고객 화면은 예약 신청, 예약 번호/연락처 기반 예약 조회, 예약 진행 단계 확인, 예약 수정, 예약 취소, 견적 동의, 짐 사진 업로드, 고객 리뷰 작성을 제공합니다.

## 고객 예약 API

### 예약 신청

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

### 예약 조회

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

### 예약 수정

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

### 예약 취소

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

### 견적 동의

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

## 파일 업로드 API

### 짐 사진 업로드

```http
POST /api/reservations/{reservationId}/photos
Content-Type: multipart/form-data
```

```text
phone=010-1234-5678
photos=boxes.jpg
```

예약 당시 연락처가 일치하고 현재 상태가 `접수` 또는 `상담중`이면 짐 사진을 업로드할 수 있습니다. 허용 확장자는 `jpg`, `jpeg`, `png`, `webp`입니다. 업로드가 완료되면 저장된 사진의 원본 파일명과 접근 URL을 JSON 배열로 응답합니다. 예약 신청, 조회, 수정, 취소, 견적 동의 응답에는 현재 예약에 연결된 `photos` 목록이 함께 포함됩니다.

## 고객 리뷰 API

### 리뷰 작성

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
React 고객 화면에서는 완료 상태의 예약을 조회했을 때 리뷰 작성 폼을 표시합니다.
