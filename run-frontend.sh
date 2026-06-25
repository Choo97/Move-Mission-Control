#!/usr/bin/env bash

set -e

cd "$(dirname "$0")"

if [ ! -f ".env.local" ]; then
  echo ".env.local 파일이 없습니다."
  echo "cp .env.example .env.local 명령으로 생성해 주세요."
  exit 1
fi

set -a
source .env.local
set +a

BACKEND_PORT="${SERVER_PORT:-8081}"
FRONTEND_PORT="${FRONTEND_PORT:-5173}"
export VITE_API_BASE_URL="${VITE_API_BASE_URL:-http://localhost:${BACKEND_PORT}}"

echo "프론트엔드 서버를 실행합니다."
echo "주소: http://localhost:${FRONTEND_PORT}/"
echo "백엔드 API: ${VITE_API_BASE_URL}"

cd frontend

if [ ! -d "node_modules" ]; then
  echo "node_modules가 없어 npm install을 먼저 실행합니다."
  npm install
fi

npm run dev -- --host 127.0.0.1 --port "${FRONTEND_PORT}"
