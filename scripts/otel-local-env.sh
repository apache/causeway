#!/usr/bin/env bash

# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

if [[ "${BASH_SOURCE[0]}" == "$0" ]]; then
    echo "Source this script so that it can update the current shell:" >&2
    echo "  source scripts/otel-local-env.sh" >&2
    exit 1
fi

export OTEL_JAVA_AGENT_PATH="${OTEL_JAVA_AGENT_PATH:-$PWD/.local/otel/opentelemetry-javaagent-1.31.0.jar}"

if [[ ! -f "$OTEL_JAVA_AGENT_PATH" ]]; then
    echo "OpenTelemetry Java agent not found: $OTEL_JAVA_AGENT_PATH" >&2
    echo "Download it as described in adoc/micrometer-tracing-operations.adoc," >&2
    echo "or export OTEL_JAVA_AGENT_PATH before sourcing this script." >&2
    return 1
fi

if [[ "$OTEL_JAVA_AGENT_PATH" == *" "* ]]; then
    echo "The Java agent path must not contain spaces: $OTEL_JAVA_AGENT_PATH" >&2
    return 1
fi

case "${JAVA_TOOL_OPTIONS:-}" in
    *"-javaagent:$OTEL_JAVA_AGENT_PATH"*)
        ;;
    *-javaagent:*)
        echo "JAVA_TOOL_OPTIONS already contains a different Java agent." >&2
        echo "Remove it before enabling the OpenTelemetry Java agent." >&2
        return 1
        ;;
    *)
        export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:+$JAVA_TOOL_OPTIONS }-javaagent:$OTEL_JAVA_AGENT_PATH"
        ;;
esac

case ",${SPRING_PROFILES_INCLUDE:-}," in
    *,observation,*)
        ;;
    ,,)
        export SPRING_PROFILES_INCLUDE="observation"
        ;;
    *)
        export SPRING_PROFILES_INCLUDE="${SPRING_PROFILES_INCLUDE},observation"
        ;;
esac

export OTEL_SERVICE_NAME="${OTEL_SERVICE_NAME:-my-causeway-app-local}"
export OTEL_TRACES_EXPORTER="otlp"
export OTEL_METRICS_EXPORTER="none"
export OTEL_LOGS_EXPORTER="none"
export OTEL_EXPORTER_OTLP_PROTOCOL="http/protobuf"
export OTEL_EXPORTER_OTLP_ENDPOINT="http://localhost:4318"
export OTEL_TRACES_SAMPLER="always_on"

echo "Local tracing environment enabled for service '$OTEL_SERVICE_NAME'."
echo "OpenTelemetry Java agent: $OTEL_JAVA_AGENT_PATH"
echo "OTLP traces endpoint: $OTEL_EXPORTER_OTLP_ENDPOINT"
