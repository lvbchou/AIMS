#!/usr/bin/env bash
# Stop backend on port 8080 and Gradle bootRun daemons.
set -euo pipefail
cd "$(dirname "$0")"

echo "Stopping Gradle daemons..."
./gradlew --stop >/dev/null 2>&1 || true

pids=$(lsof -ti :8080 2>/dev/null || true)
if [[ -n "$pids" ]]; then
  echo "Stopping process(es) on port 8080: $pids"
  kill $pids 2>/dev/null || true
  sleep 1
  pids=$(lsof -ti :8080 2>/dev/null || true)
  if [[ -n "$pids" ]]; then
    kill -9 $pids 2>/dev/null || true
  fi
fi

pkill -f "AimsBackendApplication" 2>/dev/null || true

if lsof -ti :8080 >/dev/null 2>&1; then
  echo "WARNING: port 8080 is still in use."
  lsof -i :8080
  exit 1
fi

echo "Port 8080 is free."
