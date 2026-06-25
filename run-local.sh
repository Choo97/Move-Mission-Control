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
  JAVA_HOME="${JAVA_HOME%$'\r'}"
  export PATH="$JAVA_HOME/bin:$PATH"
elif [ -x "/c/Dev/java/jdk-17.0.7/bin/java" ]; then
  export JAVA_HOME="/c/Dev/java/jdk-17.0.7"
  export PATH="$JAVA_HOME/bin:$PATH"
fi

if ! command -v java >/dev/null 2>&1 && [ -x "/c/Dev/java/jdk-17.0.7/bin/java" ]; then
  export JAVA_HOME="/c/Dev/java/jdk-17.0.7"
  export PATH="$JAVA_HOME/bin:$PATH"
fi

if ! command -v java >/dev/null 2>&1; then
  echo "Java를 찾을 수 없습니다."
  echo "Git Bash에서는 JAVA_HOME을 Java 17 경로로 설정해 주세요."
  echo "WSL에서는 Windows Java가 자동으로 연결되지 않으므로 WSL 안에 Java 17을 설치해야 합니다."
  exit 1
fi

echo "백엔드 서버를 실행합니다."
echo "주소: http://localhost:${SERVER_PORT:-8081}/"
echo "사용 중인 Java 버전:"
java -version

mvn spring-boot:run \
  "-Dspring-boot.run.arguments=--server.port=${SERVER_PORT:-8081}"
