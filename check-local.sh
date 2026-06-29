#!/usr/bin/env bash

set -u

cd "$(dirname "$0")"

if [ -f ".env.local" ]; then
  set -a
  # shellcheck disable=SC1091
  source .env.local
  set +a
else
  echo "[WARN] .env.local 파일이 없습니다. 기본 포트로만 점검합니다."
fi

strip_cr() {
  printf "%s" "${1%$'\r'}"
}

SERVER_PORT="$(strip_cr "${SERVER_PORT:-8081}")"
FRONTEND_PORT="$(strip_cr "${FRONTEND_PORT:-5173}")"
BACKEND_URL="$(strip_cr "${VITE_API_BASE_URL:-http://localhost:${SERVER_PORT}}")"
FRONTEND_URL="http://localhost:${FRONTEND_PORT}"
CURL_BIN="$(strip_cr "${CURL_BIN:-}")"

failures=0
warnings=0

if [ -z "$CURL_BIN" ]; then
  if { [ -f /proc/version ] && grep -qi microsoft /proc/version; } && command -v curl.exe >/dev/null 2>&1; then
    CURL_BIN="curl.exe"
  elif command -v curl.exe >/dev/null 2>&1 && [[ "$(uname -s)" =~ MINGW|MSYS|CYGWIN ]]; then
    CURL_BIN="curl.exe"
  elif command -v curl >/dev/null 2>&1; then
    CURL_BIN="curl"
  elif command -v curl.exe >/dev/null 2>&1; then
    CURL_BIN="curl.exe"
  fi
fi

pass() {
  echo "[OK] $1"
}

warn() {
  warnings=$((warnings + 1))
  echo "[WARN] $1"
}

fail() {
  failures=$((failures + 1))
  echo "[FAIL] $1"
}

http_status() {
  local status

  status="$("$CURL_BIN" -sS -o /dev/null -w "%{http_code}" --max-time 5 "$1" 2>/dev/null)"
  if [ "$?" -ne 0 ] || [ -z "$status" ]; then
    printf "000"
    return
  fi

  printf "%s" "$status"
}

http_status_with_cookie() {
  local cookie_file="$1"
  local url="$2"
  local status

  status="$("$CURL_BIN" -sS -o /dev/null -w "%{http_code}" --max-time 5 -b "$cookie_file" "$url" 2>/dev/null)"
  if [ "$?" -ne 0 ] || [ -z "$status" ]; then
    printf "000"
    return
  fi

  printf "%s" "$status"
}

check_http() {
  local name="$1"
  local url="$2"
  local expected_pattern="$3"
  local status

  status="$(http_status "$url")"

  if [[ "$status" =~ ^(${expected_pattern})$ ]]; then
    pass "${name}: ${url} (${status})"
  else
    fail "${name}: ${url} (기대: ${expected_pattern}, 실제: ${status})"
  fi
}

json_escape() {
  printf "%s" "$1" | sed 's/\\/\\\\/g; s/"/\\"/g'
}

echo "Move Mission Control 로컬 실행 상태 점검"
echo "백엔드: ${BACKEND_URL}"
echo "프론트엔드: ${FRONTEND_URL}"
echo

if [ -z "$CURL_BIN" ]; then
  fail "curl 명령을 찾을 수 없습니다. Git Bash, macOS, Linux 기본 터미널에서 다시 실행해 주세요."
else
  echo "HTTP 점검 도구: ${CURL_BIN}"
  echo
  check_http "백엔드 홈" "${BACKEND_URL}/" "200|302"
  check_http "공개 API" "${BACKEND_URL}/api/faqs" "200"
  check_http "관리자 API 보호 상태" "${BACKEND_URL}/api/admin/session/me" "200|401"
  check_http "React 고객 화면" "${FRONTEND_URL}/" "200"
  check_http "React 관리자 라우트" "${FRONTEND_URL}/admin/reservations" "200"
fi

admin_check_username="$(strip_cr "${ADMIN_CHECK_USERNAME:-${ADMIN_INITIAL_USERNAME:-}}")"
admin_check_password="$(strip_cr "${ADMIN_CHECK_PASSWORD:-${ADMIN_INITIAL_PASSWORD:-}}")"
admin_check_reservation_id="$(strip_cr "${ADMIN_CHECK_RESERVATION_ID:-}")"

if [ -n "$CURL_BIN" ]; then
  if [ -n "$admin_check_username" ] && [ -n "$admin_check_password" ]; then
    cookie_file="$(mktemp)"
    cookie_file_for_curl="$cookie_file"
    if [[ "$CURL_BIN" == *".exe" ]] && command -v wslpath >/dev/null 2>&1; then
      cookie_file_for_curl="$(wslpath -w "$cookie_file")"
    fi
    login_body="{\"username\":\"$(json_escape "$admin_check_username")\",\"password\":\"$(json_escape "$admin_check_password")\"}"
    login_status="$(
      "$CURL_BIN" -sS -o /dev/null -w "%{http_code}" --max-time 5 \
        -c "$cookie_file_for_curl" \
        -H "Content-Type: application/json" \
        -d "$login_body" \
        "${BACKEND_URL}/api/admin/session/login" 2>/dev/null
    )"
    if [ "$?" -ne 0 ] || [ -z "$login_status" ]; then
      login_status="000"
    fi

    if [ "$login_status" = "200" ]; then
      pass "관리자 로그인 API: ${admin_check_username} (${login_status})"

      admin_list_status="$(http_status_with_cookie "$cookie_file_for_curl" "${BACKEND_URL}/api/admin/reservations?size=1")"
      if [ "$admin_list_status" = "200" ]; then
        pass "관리자 예약 목록 API (${admin_list_status})"
      else
        fail "관리자 예약 목록 API (기대: 200, 실제: ${admin_list_status})"
      fi

      if [ -n "$admin_check_reservation_id" ]; then
        admin_detail_status="$(http_status_with_cookie "$cookie_file_for_curl" "${BACKEND_URL}/api/admin/reservations/${admin_check_reservation_id}")"
        if [ "$admin_detail_status" = "200" ]; then
          pass "관리자 예약 상세 API: ${admin_check_reservation_id}번 (${admin_detail_status})"
        else
          fail "관리자 예약 상세 API: ${admin_check_reservation_id}번 (기대: 200, 실제: ${admin_detail_status})"
        fi
      else
        warn "ADMIN_CHECK_RESERVATION_ID가 없어 관리자 상세 API 점검은 건너뜁니다."
      fi
    else
      warn "관리자 로그인 API 점검 실패 또는 건너뜀 (상태: ${login_status}). 계정 확인이 필요하면 ADMIN_CHECK_USERNAME/ADMIN_CHECK_PASSWORD를 지정하세요."
    fi

    rm -f "$cookie_file"
  else
    warn "관리자 로그인 기반 점검은 건너뜁니다. 필요하면 ADMIN_CHECK_USERNAME, ADMIN_CHECK_PASSWORD를 지정하세요."
  fi
fi

echo
if [ "$failures" -eq 0 ]; then
  pass "로컬 실행 상태 점검 완료 (경고 ${warnings}건)"
  exit 0
fi

fail "로컬 실행 상태 점검 실패 (${failures}건)"
echo "백엔드 코드를 수정했다면 백엔드 서버를 재시작한 뒤 다시 실행해 보세요."
exit 1
