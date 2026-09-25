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

set -e

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
JAEGER_CONFIG_FILE="$SCRIPT_DIR/jaeger-local-config.yaml"
JAEGER_CONTAINER_NAME="${JAEGER_CONTAINER_NAME:-jaeger}"
JAEGER_VERSION="${JAEGER_VERSION:-2.21.0}"
JAEGER_MEMORY_LIMIT="${JAEGER_MEMORY_LIMIT:-512m}"
JAEGER_MAX_TRACES="${JAEGER_MAX_TRACES:-10000}"

if ! command -v docker >/dev/null 2>&1; then
    echo "Docker is required to run Jaeger." >&2
    exit 1
fi

if [[ ! "$JAEGER_MAX_TRACES" =~ ^[1-9][0-9]*$ ]]; then
    echo "JAEGER_MAX_TRACES must be a positive integer." >&2
    exit 1
fi

if [[ ! -f "$JAEGER_CONFIG_FILE" ]]; then
    echo "Jaeger configuration not found: $JAEGER_CONFIG_FILE" >&2
    exit 1
fi

if docker container inspect "$JAEGER_CONTAINER_NAME" >/dev/null 2>&1; then
    echo "A container named '$JAEGER_CONTAINER_NAME' already exists." >&2
    echo "Stop it with: docker stop $JAEGER_CONTAINER_NAME" >&2
    exit 1
fi

echo "Starting Jaeger $JAEGER_VERSION with a $JAEGER_MEMORY_LIMIT memory limit and space for $JAEGER_MAX_TRACES traces..."

docker run --detach --rm \
    --name "$JAEGER_CONTAINER_NAME" \
    --memory "$JAEGER_MEMORY_LIMIT" \
    --memory-swap "$JAEGER_MEMORY_LIMIT" \
    --env JAEGER_MAX_TRACES="$JAEGER_MAX_TRACES" \
    --volume "$JAEGER_CONFIG_FILE:/etc/jaeger/config.yaml:ro" \
    -p 16686:16686 \
    -p 4318:4318 \
    "cr.jaegertracing.io/jaegertracing/jaeger:$JAEGER_VERSION" \
    --config=file:/etc/jaeger/config.yaml

echo "Jaeger UI: http://localhost:16686"
echo "OTLP HTTP endpoint: http://localhost:4318"
echo "Monitor memory: docker stats $JAEGER_CONTAINER_NAME"
echo "Stop Jaeger: docker stop $JAEGER_CONTAINER_NAME"
