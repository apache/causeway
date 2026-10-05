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
# Temporary Prometheus/Grafana pair for local Causeway metrics exploration.
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

usage() {
  echo "Usage: $0 {start|stop|status|logs|config}"
  echo "Prometheus: http://localhost:9090  Grafana: http://localhost:3000 (admin/admin)"
  echo "Data uses tmpfs and is discarded when containers stop. Requires Docker Compose v2+."
}
ACTION="${1:-help}"
case "$ACTION" in
  help|-h|--help) usage; exit 0 ;;
  start|stop|status|logs|config) ;;
  *) usage >&2; exit 2 ;;
esac
[[ $# -eq 1 ]] || { usage >&2; exit 2; }
command -v docker >/dev/null || { echo "Docker is required." >&2; exit 1; }
docker compose version >/dev/null 2>&1 || { echo "Docker Compose v2+ is required." >&2; exit 1; }
COMPOSE=(docker compose --project-name causeway-metrics-local --file "$SCRIPT_DIR/metrics-local/compose.yaml")
if [[ "$ACTION" == config ]]; then exec "${COMPOSE[@]}" config; fi
docker info >/dev/null 2>&1 || { echo "Cannot reach Docker. Start Docker Desktop or your Docker daemon first." >&2; exit 1; }
case "$ACTION" in
  start)
    command -v curl >/dev/null || { echo "curl is required for readiness checks." >&2; exit 1; }
    "${COMPOSE[@]}" up --detach
    for ((attempt=0; attempt<30; attempt++)); do
      if curl --fail --silent --output /dev/null --max-time 1 http://127.0.0.1:9090/-/ready && \
         curl --fail --silent --output /dev/null --max-time 1 http://127.0.0.1:3000/api/health; then
        echo "Prometheus ready: http://localhost:9090"
        echo "Grafana ready: http://localhost:3000 (login admin/admin; Prometheus is provisioned)"
        echo "OTLP metrics: http://localhost:9090/api/v1/otlp/v1/metrics"
        exit 0
      fi
      sleep 1
    done
    echo "Readiness timed out. Use '$0 logs' to diagnose or '$0 stop' to clean up." >&2
    exit 1
    ;;
  stop)
    "${COMPOSE[@]}" down --volumes
    echo "Prometheus and Grafana stopped; metrics and UI changes discarded."
    ;;
  status) "${COMPOSE[@]}" ps --all ;;
  logs) "${COMPOSE[@]}" logs --tail 100 ;;
esac
