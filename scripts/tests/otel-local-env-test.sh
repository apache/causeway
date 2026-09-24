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

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
HELPER="$ROOT_DIR/scripts/otel-local-env.sh"
TEST_ROOT="$(mktemp -d "${TMPDIR:-/tmp}/otel-local-env-test.XXXXXX")"
AGENT="$TEST_ROOT/opentelemetry-javaagent.jar"
trap 'rm -rf "$TEST_ROOT"' EXIT

touch "$AGENT"

fail() {
    echo "FAIL: $*" >&2
    exit 1
}

assert_contains() {
    local output="$1"
    local expected="$2"
    [[ "$output" == *"$expected"* ]] \
        || fail "expected output to contain '$expected', got: $output"
}

assert_not_contains() {
    local output="$1"
    local unexpected="$2"
    [[ "$output" != *"$unexpected"* ]] \
        || fail "expected output not to contain '$unexpected', got: $output"
}

assert_occurrences() {
    local output="$1"
    local needle="$2"
    local expected="$3"
    local remaining="$output"
    local actual=0
    while [[ "$remaining" == *"$needle"* ]]; do
        remaining="${remaining#*"$needle"}"
        actual=$((actual + 1))
    done
    [[ "$actual" -eq "$expected" ]] \
        || fail "expected $expected occurrences of '$needle', found $actual in: $output"
}

run_helper() {
    local working_directory="$1"
    shift
    (
        cd "$working_directory"
        env -i \
            HOME="$HOME" \
            PATH="$PATH" \
            HELPER="$HELPER" \
            AGENT="$AGENT" \
            "$@" \
            bash --noprofile --norc -c '
                export OTEL_JAVA_AGENT_PATH="$AGENT"
                source "$HELPER"
                printf "RESULT service.name=%s\n" "$OTEL_SERVICE_NAME"
                printf "RESULT service.role=%s\n" "${OTEL_SERVICE_ROLE-<unset>}"
                printf "RESULT service.version=%s\n" "${OTEL_SERVICE_VERSION-<unset>}"
                printf "RESULT resource.attributes=%s\n" "${OTEL_RESOURCE_ATTRIBUTES-<unset>}"
                printf "RESULT java.tool.options=%s\n" "${JAVA_TOOL_OPTIONS-<unset>}"
                printf "RESULT spring.profiles.include=%s\n" "${SPRING_PROFILES_INCLUDE-<unset>}"
            '
    ) 2>&1
}

CLEAN_REPO="$TEST_ROOT/clean-repo"
mkdir -p "$CLEAN_REPO"
git -C "$CLEAN_REPO" init --quiet
git -C "$CLEAN_REPO" config user.name "Tracing Test"
git -C "$CLEAN_REPO" config user.email "tracing-test@example.invalid"
echo "fixture" > "$CLEAN_REPO/tracked.txt"
git -C "$CLEAN_REPO" add tracked.txt
git -C "$CLEAN_REPO" commit --quiet -m "fixture"
FULL_SHA="$(git -C "$CLEAN_REPO" rev-parse --verify HEAD)"

explicit_output="$(run_helper "$CLEAN_REPO" \
    OTEL_SERVICE_NAME=comparison-app \
    OTEL_SERVICE_ROLE=baseline \
    OTEL_SERVICE_VERSION=release-candidate \
    OTEL_RESOURCE_ATTRIBUTES=cloud.region=west,service.role=old,service.version=old,host.name=dev)"
assert_contains "$explicit_output" "RESULT service.name=comparison-app"
assert_contains "$explicit_output" "RESULT service.role=baseline"
assert_contains "$explicit_output" "RESULT service.version=release-candidate"
assert_contains "$explicit_output" \
    "RESULT resource.attributes=cloud.region=west,host.name=dev,service.role=baseline,service.version=release-candidate"
assert_not_contains "$explicit_output" "worktree contains uncommitted changes"

derived_output="$(run_helper "$CLEAN_REPO" OTEL_SERVICE_ROLE=candidate)"
assert_contains "$derived_output" "RESULT service.name=my-causeway-app-local"
assert_contains "$derived_output" "RESULT service.role=candidate"
assert_contains "$derived_output" "RESULT service.version=$FULL_SHA"
assert_contains "$derived_output" "RESULT resource.attributes=service.role=candidate,service.version=$FULL_SHA"
assert_not_contains "$derived_output" "could not be derived from Git"

DIRTY_REPO="$TEST_ROOT/dirty-repo"
git clone --quiet "$CLEAN_REPO" "$DIRTY_REPO"
echo "untracked" > "$DIRTY_REPO/untracked.txt"
dirty_output="$(run_helper "$DIRTY_REPO")"
assert_contains "$dirty_output" "RESULT service.version=$FULL_SHA"
assert_contains "$dirty_output" "worktree contains uncommitted changes"
assert_not_contains "$dirty_output" "$FULL_SHA-dirty"

NON_GIT_DIR="$TEST_ROOT/non-git"
mkdir -p "$NON_GIT_DIR"
non_git_output="$(run_helper "$NON_GIT_DIR" OTEL_RESOURCE_ATTRIBUTES=cloud.region=west)"
assert_contains "$non_git_output" "service version could not be derived from Git"
assert_contains "$non_git_output" "RESULT service.version=<unset>"
assert_contains "$non_git_output" "RESULT resource.attributes=cloud.region=west"

existing_role_output="$(run_helper "$CLEAN_REPO" \
    OTEL_RESOURCE_ATTRIBUTES=service.role=external,cloud.region=east)"
assert_contains "$existing_role_output" \
    "RESULT resource.attributes=service.role=external,cloud.region=east,service.version=$FULL_SHA"
assert_contains "$existing_role_output" "RESULT service.role=<unset>"

repeated_output="$(
    (
        cd "$CLEAN_REPO"
        env -i \
            HOME="$HOME" \
            PATH="$PATH" \
            HELPER="$HELPER" \
            AGENT="$AGENT" \
            OTEL_SERVICE_ROLE=candidate \
            bash --noprofile --norc -c '
                export OTEL_JAVA_AGENT_PATH="$AGENT"
                source "$HELPER"
                source "$HELPER"
                printf "RESULT resource.attributes=%s\n" "$OTEL_RESOURCE_ATTRIBUTES"
                printf "RESULT java.tool.options=%s\n" "$JAVA_TOOL_OPTIONS"
                printf "RESULT spring.profiles.include=%s\n" "$SPRING_PROFILES_INCLUDE"
            '
    ) 2>&1
)"
repeated_attributes="$(printf '%s\n' "$repeated_output" \
    | grep '^RESULT resource.attributes=')"
assert_occurrences "$repeated_attributes" "service.role=candidate" 1
assert_occurrences "$repeated_attributes" "service.version=$FULL_SHA" 1
assert_contains "$repeated_output" "RESULT java.tool.options=-javaagent:$AGENT"
assert_contains "$repeated_output" "RESULT spring.profiles.include=observation"

direct_output="$(bash "$HELPER" 2>&1 || true)"
assert_contains "$direct_output" "Source this script"

missing_agent_output="$(
    (
        cd "$NON_GIT_DIR"
        env -i HOME="$HOME" PATH="$PATH" HELPER="$HELPER" \
            bash --noprofile --norc -c '
                export OTEL_JAVA_AGENT_PATH="$PWD/missing-agent.jar"
                if source "$HELPER"; then
                    echo "unexpected source success"
                    exit 1
                fi
                echo "EXPECTED_SOURCE_FAILURE"
            '
    ) 2>&1
)"
assert_contains "$missing_agent_output" "OpenTelemetry Java agent not found"
assert_contains "$missing_agent_output" "EXPECTED_SOURCE_FAILURE"
assert_not_contains "$missing_agent_output" "unexpected source success"

helper_source="$(<"$HELPER")"
assert_not_contains "$helper_source" "OpenTelemetrySdk"
assert_not_contains "$helper_source" "SdkTracerProvider"
assert_not_contains "$helper_source" "SpanExporter"

printf "PASS: otel-local-env.sh comparison metadata tests\n"
