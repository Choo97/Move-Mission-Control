# 개발 규칙

이 문서는 개인 프로젝트로 진행한 Move Mission Control을 추후 협업 상황에서도 일관성 있게 유지보수할 수 있도록 정리한 개발 규칙입니다.

실제 팀 협업 이력을 설명하기 위한 문서가 아니라, 다른 개발자가 프로젝트를 이어받거나 함께 작업할 때 참고할 수 있는 기준을 목적으로 합니다.

## 브랜치 네이밍

브랜치는 작업 목적이 먼저 드러나도록 `type/description` 형식을 사용합니다.

| 형식 | 용도 | 예시 |
| --- | --- | --- |
| `feature/기능명` | 새로운 기능 추가 | `feature/reservation-form` |
| `fix/수정내용` | 버그 수정 | `fix/date-validation` |
| `refactor/대상` | 기능 변화 없는 구조 개선 | `refactor/reservation-components` |
| `docs/문서명` | 문서 추가 또는 수정 | `docs/convention` |
| `style/대상` | UI, CSS, 포맷 수정 | `style/admin-reservation-list` |
| `test/대상` | 테스트 추가 또는 수정 | `test/reservation-service` |
| `chore/작업명` | 설정, 패키지, 빌드 관련 작업 | `chore/vite-config` |

브랜치 이름은 영어 소문자와 하이픈을 사용합니다. 작업 범위가 넓어질 경우 기능 단위로 브랜치를 분리합니다.

## 커밋 메시지

커밋 메시지는 Conventional Commits 형식을 참고해 `type: 내용` 구조로 작성합니다.

| 타입 | 의미 |
| --- | --- |
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `docs` | 문서 추가 또는 수정 |
| `style` | 동작 변화가 없는 코드 포맷, 공백, 정렬 수정 |
| `refactor` | 기능 변화 없는 코드 구조 개선 |
| `test` | 테스트 추가 또는 수정 |
| `chore` | 설정, 패키지, 빌드, 기타 유지보수 |

예시:

```text
feat: 예약 신청 폼 추가
fix: 예약 날짜 유효성 검증 오류 수정
docs: 실행 방법 문서 보완
feat: 관리자 예약 목록 반응형 레이아웃 개선
refactor: 예약 상세 컴포넌트 분리
test: 예약 중복 시간 검증 테스트 추가
chore: 프론트엔드 빌드 설정 정리
```

커밋은 한 가지 목적을 기준으로 작게 나눕니다. 기능 구현과 단순 포맷 변경처럼 성격이 다른 작업은 별도 커밋으로 분리합니다.

## 작업 단위

작업은 사용자가 확인할 수 있는 기능 흐름 또는 유지보수 단위로 나눕니다.

- 고객 예약 신청, 예약 조회, 리뷰 작성처럼 화면 흐름이 있는 기능은 화면과 API 연동을 함께 확인합니다.
- 관리자 예약 처리, 견적 관리, 알림 이력처럼 운영 기능은 상태 변경 결과와 이력 저장 여부를 함께 확인합니다.
- 문서 수정은 실제 실행 명령, 환경변수, 화면 경로가 현재 코드와 맞는지 확인합니다.
- 리팩터링은 기능 동작이 바뀌지 않았음을 테스트 또는 수동 시나리오로 확인합니다.

## 코드 작성 기준

### Java, Spring Boot

- 도메인별 패키지 구조를 유지합니다. 예: `reservation`, `admin`, `review`, `availability`.
- Controller는 요청과 응답의 진입점 역할에 집중하고, 비즈니스 로직은 Service에 둡니다.
- 요청 DTO와 응답 DTO는 목적별로 분리해 API 응답 구조가 엔티티에 직접 의존하지 않게 합니다.
- 예약 상태, 고객 요청 상태처럼 의미가 고정된 값은 enum을 우선 사용합니다.
- 개인정보, 예약번호, 연락처처럼 민감한 값은 로그와 화면 노출 범위를 신중하게 제한합니다.

### React, TypeScript

- 화면 단위 상태 흐름은 `App.tsx` 또는 해당 뷰 컴포넌트에서 관리하고, 재사용 가능한 UI는 `components/` 아래로 분리합니다.
- API 호출 코드는 `frontend/src/api/`에 모아 화면 컴포넌트와 통신 로직을 분리합니다.
- API 응답 타입과 화면에서 공유되는 타입은 `frontend/src/types/` 또는 공통 타입 파일에 둡니다.
- 폼 입력값은 화면에서 바로 API 요청으로 넘기기보다 검증과 변환 단계를 명확히 둡니다.
- 관리자 화면은 반복 사용을 고려해 목록, 상세, 상태 변경, 알림 영역을 가능한 한 분리합니다.

### 스타일

- 고객 화면은 예약 흐름을 이해하기 쉽게 단계와 상태 안내를 명확히 보여줍니다.
- 관리자 화면은 정보 밀도와 반복 작업 효율을 우선합니다.
- CSS 파일은 관련 컴포넌트 옆에 두고, 전역 스타일과 컴포넌트 스타일의 역할을 구분합니다.
- 모바일 화면에서 버튼, 폼, 표의 텍스트가 겹치지 않는지 확인합니다.

## 파일 네이밍

| 대상 | 규칙 | 예시 |
| --- | --- | --- |
| Java class | PascalCase | `ReservationService.java` |
| Java package | lowercase | `com.moving.reservation.review` |
| React component | PascalCase | `ReservationCreateForm.tsx` |
| TypeScript module | camelCase 또는 목적 기반 이름 | `customerApi.ts` |
| CSS module/file | 컴포넌트명 기반 | `ReservationCreateForm.css` |
| 문서 | 대문자 스네이크 또는 명확한 이름 | `API.md`, `TEST_SCENARIOS.md` |

## 테스트와 검증

작업 완료 전에는 변경 범위에 맞게 아래 항목을 확인합니다.

```bash
# 백엔드 테스트
mvn test

# 프론트엔드 검증
cd frontend
npm run lint
npm run build
```

수동 검증이 필요한 경우 [docs/TEST_SCENARIOS.md](docs/TEST_SCENARIOS.md)의 흐름을 기준으로 고객 예약 신청부터 관리자 처리, 리뷰 작성까지 확인합니다.

## PR 작성 기준

실제 협업 상황에서 Pull Request를 만든다면 아래 내용을 포함합니다.

- 작업 목적
- 주요 변경 사항
- 확인한 테스트 또는 수동 검증 내용
- 화면 변경이 있다면 캡처 또는 확인 경로
- 남은 이슈나 후속 작업

예시:

```md
## 작업 목적

예약 신청 후 고객이 예약번호로 진행 상태를 조회할 수 있도록 조회 화면을 개선했습니다.

## 변경 사항

- 예약 조회 폼 유효성 검증 추가
- 예약 상세 상태 안내 문구 정리
- API 오류 응답 처리 보완

## 검증

- npm run build
- 예약 신청 -> 예약번호 조회 -> 관리자 상태 변경 -> 고객 화면 재조회 수동 확인
```

## 문서 관리 기준

문서마다 아래 역할을 유지해 같은 내용을 여러 파일에서 반복 관리하지 않습니다.

| 문서 | 역할 |
| --- | --- |
| `README.md` | 프로젝트 소개, 실제 화면, 핵심 기능, 빠른 실행, 문서 목록 |
| `docs/ARCHITECTURE.md` | 코드 구조, 패키지 역할, 예약 상태와 도메인 흐름 |
| `docs/API.md` | API 요청/응답, 인증 조건, 오류 응답 |
| `docs/RUNNING.md` | 상세 실행 방법, 접속 주소, 환경변수, 테스트, 관리자 계정 |
| `docs/PORTFOLIO_CASE_STUDY.md` | 문제 정의, 설계 판단, 검증 결과, 면접 답변, 시연 흐름 |
| `CONVENTION.md` | 브랜치, 커밋, 코드 작성, 검증, 문서 관리 규칙 |

- 실행 방법이 바뀌면 `README.md`와 `docs/RUNNING.md`를 함께 확인합니다.
- API 요청이나 응답 구조가 바뀌면 `docs/API.md`를 갱신합니다.
- 배포 환경변수가 바뀌면 `docs/DEPLOY_ENV.md`와 `.env.example`을 함께 확인합니다.
- 포트폴리오 설명이나 시연 흐름이 바뀌면 `docs/PORTFOLIO_CASE_STUDY.md`를 갱신합니다.
