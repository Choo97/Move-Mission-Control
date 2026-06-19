# Move Mission Control Frontend

React 고객 화면입니다. Spring Boot 서버의 `/api/**`를 호출합니다.

## 실행

```bash
cp .env.example .env.local
npm install
npm run dev
```

기본 주소는 `http://localhost:5173`입니다.

## 환경변수

```bash
VITE_API_BASE_URL=http://localhost:8081
```

`VITE_API_BASE_URL`은 React가 호출할 Spring Boot 서버 주소입니다.

## 검증

```bash
npm run lint
npm run build
```
