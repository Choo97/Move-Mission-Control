# 프로젝트 구조와 예약 흐름

이 문서는 Move Mission Control의 코드 구조, 화면 구성, 예약 상태 흐름을 정리합니다.

README는 프로젝트 첫 화면에서 빠르게 이해해야 할 요약을 담고, 이 문서는 구현 구조와 도메인 흐름을 조금 더 자세히 설명하는 용도로 분리합니다.

## 전체 구성

Move Mission Control은 Spring Boot 백엔드와 React 프론트엔드를 같은 저장소에서 관리하는 구조입니다.

```text
React 고객/관리자 화면
-> Spring Boot REST API
-> JPA Service/Repository
-> MySQL
```

백엔드는 REST API, 기존 Thymeleaf 관리자 화면, 인증, 예약 도메인 로직을 담당합니다. 프론트엔드는 고객 예약 흐름과 React 관리자 화면을 담당합니다.

## 주요 프로젝트 구조

```text
src/main/java/com/moving/reservation
├── MovingReservationApplication.java
├── admin
├── api
├── auth
├── availability
├── config
├── coupon
├── estimate
├── faq
├── home
├── notification
├── privacy
├── reservation
└── review

src/main/resources
├── application.yml
├── static/css/style.css
└── templates

frontend/src
├── App.tsx
├── api
├── assets
├── components
├── types
├── adminAuthNavigation.ts
├── reservationData.ts
├── reservationDraft.ts
└── types.ts
```

## 백엔드 패키지 역할

| 패키지 | 역할 |
| --- | --- |
| `reservation` | 예약 신청, 조회, 수정/취소 요청, 상태 변경, 사진 업로드, 견적 스냅샷 |
| `admin` | 관리자 예약 목록/상세, 고객 요청 처리, 알림 이력, 감사 로그 |
| `auth` | 관리자 로그인, 계정, 세션, 로그인 실패 제한 |
| `availability` | 운영시간, 휴무일, 예약 가능 시간 |
| `coupon` | 쿠폰 생성, 활성 상태, 예약 할인 적용 |
| `estimate` | 견적 기준 설정과 변경 이력 |
| `notification` | 이메일/SMS 알림 이력과 발송 처리 |
| `review` | 완료 예약 리뷰, 공개 여부, 관리자 답변 |
| `faq` | 고객 FAQ와 관리자 FAQ 관리 |
| `privacy` | 개인정보 암호화, 해시, 마스킹, 보관 기간 처리 |
| `api` | 공통 API 오류 응답 |
| `home` | Spring Boot 기본 홈 화면 진입점 |
| `config` | 보안, CORS, 업로드 경로, OpenAPI 설정 |

백엔드는 도메인별 패키지 구조를 유지합니다. Controller는 요청/응답 진입점에 집중하고, 예약 상태 전이, 견적 계산, 고객 요청 승인 같은 규칙은 Service에서 처리합니다.

## 프론트엔드 구조

`App.tsx`가 고객/관리자 경로와 화면 상태 흐름을 연결하고, 실제 화면은 `components/` 아래 폼, 목록, 상세 패널로 분리했습니다.

| 경로 | 역할 |
| --- | --- |
| `frontend/src/api` | Spring Boot REST API 호출 |
| `frontend/src/assets` | 고객 랜딩 화면에서 사용하는 이미지 자산 |
| `frontend/src/components` | 고객 화면과 관리자 화면 컴포넌트 |
| `frontend/src/types` | API 응답과 화면 공유 타입 |
| `frontend/src/reservationData.ts` | 예약 화면 선택값과 표시 데이터 |
| `frontend/src/reservationDraft.ts` | 예약 작성 중 임시 저장 데이터 관리 |
| `frontend/src/adminAuthNavigation.ts` | 관리자 인증 만료 시 로그인 화면 이동 처리 |

API 호출 코드는 화면 컴포넌트에서 분리해 두었습니다. 이렇게 하면 화면 구조를 바꾸더라도 백엔드 요청 경로와 오류 처리 기준을 한곳에서 관리할 수 있습니다.

## 예약 기본 흐름

```text
고객 예약 신청
-> 관리자 예약 확인
-> 상담 및 견적 안내
-> 고객 견적 동의
-> 예약 확정
-> 이사 완료
-> 고객 리뷰 작성
```

이 흐름은 고객 화면, 관리자 화면, 테스트 시나리오에서 같은 기준으로 설명합니다.

## 예약 상태

| 상태 | 표시 | 의미 |
| --- | --- | --- |
| `RECEIVED` | 접수 | 고객 예약이 접수된 초기 상태 |
| `CONSULTING` | 상담중 | 관리자가 고객 정보와 이사 조건을 확인하는 상태 |
| `ESTIMATE_SENT` | 견적안내 | 관리자가 최종 견적을 안내한 상태 |
| `CONFIRMED` | 확정 | 고객이 견적에 동의하고 예약이 확정된 상태 |
| `COMPLETED` | 완료 | 이사가 완료되어 리뷰 작성이 가능한 상태 |
| `CANCELED` | 취소 | 예약이 취소된 상태 |

기본 흐름은 `접수 -> 상담중 -> 견적안내 -> 확정 -> 완료`입니다. 진행 중인 예약은 고객 요청이나 운영 판단에 따라 `취소`로 변경할 수 있습니다.

## 고객 수정/취소 요청

예약 수정과 취소는 고객이 요청하면 바로 반영하지 않고, 관리자가 승인한 뒤 실제 예약 정보나 취소 상태로 반영합니다.

이렇게 한 이유는 일정 변경과 취소가 다른 예약 시간, 견적, 운영 준비에 영향을 주기 때문입니다. 고객에게는 직접 요청할 수 있는 흐름을 제공하되, 운영자는 변경 내용을 확인한 뒤 반영할 수 있게 했습니다.

```text
고객 수정/취소 요청
-> 관리자 요청 확인
-> 승인 또는 반려
-> 승인 시 예약 정보 또는 상태 반영
-> 고객 상세 화면과 관리자 상세 화면에 이력 표시
```

## 화면 전환 구조

고객 화면은 React를 기본으로 사용하고, 백엔드의 기존 Thymeleaf 화면은 일부 관리자 기능과 호환 경로를 위해 유지합니다.

| 화면 | 기본 주소 | 역할 |
| --- | --- | --- |
| React 고객 화면 | `http://localhost:5173/` | 예약 신청, 조회, 수정/취소 요청, 견적 동의, 리뷰 |
| React 관리자 화면 | `http://localhost:5173/admin/reservations` | 예약 목록, 상세 처리, 알림, 리뷰, FAQ, 운영 설정 |
| Spring Boot 홈 | `http://localhost:8081/` | 백엔드 기본 화면 |
| 기존 관리자 화면 | `http://localhost:8081/admin/reservations` | Thymeleaf 기반 관리자 백업 화면 |

React 전환 중에도 기존 관리자 화면을 유지한 이유는 예약 운영 기능이 많아 한 번에 모두 전환하면 검증 범위가 커지기 때문입니다. 핵심 예약 운영 화면을 React에서 먼저 검증하고, 필요하면 기존 화면으로 되돌아갈 수 있게 구성했습니다.
