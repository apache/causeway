#!/usr/bin/env bash
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#       https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
# Jaeger all-in-one: https://www.jaegertracing.io/docs/2.21/getting-started/
# Local, transient traces only. Stopping the container discards stored traces.
set -euo pipefail

CONTAINER=causeway-jaeger-local
IMAGE=cr.jaegertracing.io/jaegertracing/jaeger:2.21.0
LABEL=org.apache.causeway.local-jaeger

usage() {
  echo "Usage: $0 {start|stop|status|logs}"
  echo "UI: http://localhost:16686  OTLP HTTP: http://localhost:4318/v1/traces"
  echo "Requires Docker. Trace storage is in memory and is lost on stop."
}

ACTION="${1:-help}"
case "$ACTION" in
  help|-h|--help) usage; exit 0 ;;
  start|stop|status|logs) ;;
  *) usage >&2; exit 2 ;;
esac
[[ $# -eq 1 ]] || { usage >&2; exit 2; }
command -v docker >/dev/null || { echo "Docker is required (for example, Docker Desktop)." >&2; exit 1; }
[[ "$ACTION" != start ]] || echo "Checking Docker..."
docker info >/dev/null 2>&1 || { echo "Cannot reach Docker. Start Docker Desktop or your Docker daemon first." >&2; exit 1; }

EXISTS=false
if docker container inspect "$CONTAINER" >/dev/null 2>&1; then
  EXISTS=true
  OWNER="$(docker container inspect --format "{{ index .Config.Labels \"$LABEL\" }}" "$CONTAINER")"
  [[ "$OWNER" == true ]] || { echo "Container $CONTAINER belongs to another setup; refusing to modify it." >&2; exit 1; }
fi

case "$ACTION" in
  start)
    command -v curl >/dev/null || { echo "curl is required for the readiness check." >&2; exit 1; }
    if [[ "$EXISTS" == true ]]; then
      if [[ "$(docker container inspect --format '{{.State.Running}}' "$CONTAINER")" != true ]]; then
        echo "Starting Jaeger container..."
        docker start "$CONTAINER" >/dev/null
      fi
    else
      echo "Starting Jaeger container (Docker may need to download $IMAGE)..."
      docker run --detach --rm --name "$CONTAINER" --label "$LABEL=true" \
        --publish 127.0.0.1:16686:16686 \
        --publish 127.0.0.1:4317:4317 \
        --publish 127.0.0.1:4318:4318 \
        "$IMAGE" >/dev/null
    fi
    echo "Waiting for Jaeger at http://127.0.0.1:16686 (up to about 60 seconds)..."
    for ((attempt=0; attempt<30; attempt++)); do
      if [[ "$(docker container inspect --format '{{.State.Running}}' "$CONTAINER" 2>/dev/null || true)" != true ]]; then
        echo "Jaeger exited during startup. Try '$0 start' again and check Docker's error output." >&2
        exit 1
      fi
      # Use the stable v3 query API; the legacy /api/services can return 404.
      # Ignore ~/.curlrc (which can enable retries) and proxies for this local probe.
      if READINESS_ERROR="$(curl --disable --noproxy '*' --fail --silent --show-error --output /dev/null --max-time 1 http://127.0.0.1:16686/api/v3/services 2>&1)"; then
        echo "Jaeger is ready: http://localhost:16686"
        echo "OTLP HTTP receiver: http://localhost:4318/v1/traces"
        exit 0
      fi
      sleep 1
    done
    echo "Jaeger is running, but its query API did not become ready. Inspect with '$0 logs'; stop with '$0 stop'." >&2
    echo "Last readiness error: $READINESS_ERROR" >&2
    exit 1
    ;;
  stop)
    if [[ "$EXISTS" == true ]]; then
      docker stop "$CONTAINER" >/dev/null
      echo "Jaeger stopped; in-memory traces discarded."
    else
      echo "Jaeger is already stopped."
    fi
    ;;
  status)
    if [[ "$EXISTS" == true ]]; then
      docker container inspect --format '{{.State.Status}}' "$CONTAINER"
    else
      echo "Jaeger is stopped."
    fi
    ;;
  logs)
    if [[ "$EXISTS" == true ]]; then
      docker logs --tail 100 "$CONTAINER"
    else
      echo "Jaeger is stopped; no container logs are available."
    fi
    ;;
esac
