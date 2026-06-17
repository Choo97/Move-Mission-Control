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

if [ -n "${JAVA_HOME:-}" ]; then
  export PATH="$JAVA_HOME/bin:$PATH"
fi

echo "사용 중인 Java 버전:"
java -version

mvn spring-boot:run \
  "-Dspring-boot.run.arguments=--server.port=${SERVER_PORT:-8081}"