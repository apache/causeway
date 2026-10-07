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
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
MODE=boot
MAVEN_ARGS=()
usage() {
  cat <<'USAGE'
Usage: run-with-telemetry.sh [--agent] [-- Maven options...]

Boot-managed tracing by default; --agent uses agent-managed tracing.
Both modes export Micrometer metrics to Prometheus and traces to Jaeger.
Enable Boot duration filtering with CAUSEWAY_OBSERVATION_JPA_DURATION_THRESHOLD=1s.
Agent mode keeps duration filtering disabled.

Start Jaeger and the metrics pair separately before running Petclinic:
  ./scripts/jaeger-local.sh start
  ./scripts/metrics-local.sh start
Petclinic: http://localhost:8080/htmx  Jaeger: http://localhost:16686
Prometheus: http://localhost:9090  Grafana: http://localhost:3000
Use SERVER_PORT=8081 for another application port.
Override the agent JAR with OTEL_JAVAAGENT_PATH; otherwise Maven obtains agent 2.31.1.
USAGE
}
while [[ $# -gt 0 ]]; do
  case "$1" in
    --agent) MODE=agent; shift ;;
    -h|--help) usage; exit 0 ;;
    --) shift; MAVEN_ARGS=("$@"); break ;;
    *) echo "Unknown option: $1" >&2; usage >&2; exit 2 ;;
  esac
done
for ARG in ${MAVEN_ARGS[@]+"${MAVEN_ARGS[@]}"}; do
  case "$ARG" in
    -Dspring-boot.run.arguments*|-Dspring-boot.run.profiles*|-Dspring-boot.run.agents*)
      echo "This launcher owns application arguments, profiles and agents." >&2; exit 2 ;;
  esac
done
if [[ -z "${JAVA_HOME:-}" && -x /usr/libexec/java_home ]]; then
  export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
fi

PROFILES=observation
AGENT_ARGS=()
if [[ "$MODE" == agent ]]; then
  AGENT="${OTEL_JAVAAGENT_PATH:-$SCRIPT_DIR/target/agents/opentelemetry-javaagent.jar}"
  if [[ ! -f "$AGENT" && -z "${OTEL_JAVAAGENT_PATH:-}" ]]; then
    "${MVN:-mvn}" -f "$SCRIPT_DIR/../../../parent/pom.xml" \
      org.apache.maven.plugins:maven-dependency-plugin:3.11.0:copy \
      -Dartifact=io.opentelemetry.javaagent:opentelemetry-javaagent:2.31.1 \
      "-DoutputDirectory=$SCRIPT_DIR/target/agents" -Dmdep.stripVersion=true
  fi
  [[ -r "$AGENT" ]] || { echo "Cannot read Java agent: $AGENT" >&2; exit 1; }
  AGENT="$(cd "$(dirname "$AGENT")" && pwd)/$(basename "$AGENT")"
  
  export OTEL_SERVICE_NAME="${OTEL_SERVICE_NAME:-Causeway Pet Clinic}"
  export OTEL_TRACES_EXPORTER=otlp
  export OTEL_EXPORTER_OTLP_PROTOCOL=http/protobuf
  export OTEL_EXPORTER_OTLP_TRACES_PROTOCOL=http/protobuf
  export OTEL_EXPORTER_OTLP_TRACES_ENDPOINT="${OTEL_EXPORTER_OTLP_TRACES_ENDPOINT:-http://localhost:4318/v1/traces}"
  export OTEL_TRACES_SAMPLER=always_on
  export OTEL_METRICS_EXPORTER=none
  export OTEL_LOGS_EXPORTER=none
  
  # The agent exporter does not consume Spring's duration-discard predicate.
  export CAUSEWAY_OBSERVATION_JPA_DURATION_THRESHOLD=0ms
  PROFILES=observation,agent
  AGENT_ARGS=("-Dspring-boot.run.agents=$AGENT")
fi

echo "Tracing: $MODE-managed; metrics: Micrometer to Prometheus"
exec bash "$SCRIPT_DIR/run.sh" \
  -Pobservation \
  "-Dspring-boot.run.profiles=$PROFILES" \
  -Dspring-boot.run.main-class=org.apache.causeway.viewer.webcomponents.sample.htmx.petclinic.PetClinicHtmxApplication \
  ${AGENT_ARGS[@]+"${AGENT_ARGS[@]}"} ${MAVEN_ARGS[@]+"${MAVEN_ARGS[@]}"}
