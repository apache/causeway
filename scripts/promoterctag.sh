#!/usr/bin/env bash
#  Licensed to the Apache Software Foundation (ASF) under one
#  or more contributor license agreements.  See the NOTICE file
#  distributed with this work for additional information
#  regarding copyright ownership.  The ASF licenses this file
#  to you under the Apache License, Version 2.0 (the
#  "License"); you may not use this file except in compliance
#  with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing,
#  software distributed under the License is distributed on an
#  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#  KIND, either express or implied.  See the License for the
#  specific language governing permissions and limitations
#  under the License.


#
# promoterctag — promote an RC tag to final release
#
# Removes an RC tag (e.g., causeway-1.2.0-RC1) and replaces it with the final tag (e.g., causeway-1.2.0).
#
# Usage:
#   promoterctag <baseTag> <rcSuffix> [--dry-run|--whatif]
#
# Example:
#   promoterctag causeway-1.2.0 RC1
#
#   promoterctag causeway-1.2.0 RC1 --dry-run
#

set -euo pipefail

usage() {
  echo "Usage: $0 <baseTag> <rcSuffix> [--dry-run|--whatif]"
  echo
  echo "Promotes a release candidate (RC) tag to its final release."
  echo
  echo "Example:"
  echo "  $0 causeway-1.2.0 RC1"
  echo "  $0 causeway-1.2.0 RC1 --dry-run"
  exit 1
}

# --- parse args ---
if [[ $# -lt 2 ]]; then
  usage
fi

BASE_TAG="$1"
RC_SUFFIX="$2"
RC_TAG="${BASE_TAG}-${RC_SUFFIX}"
DRY_RUN=false

if [[ $# -eq 3 ]]; then
  case "$3" in
    --dry-run|--whatif)
      DRY_RUN=true
      ;;
    *)
      echo "Unknown option: $3"
      usage
      ;;
  esac
fi

# --- sanity checks ---
if ! git rev-parse "$RC_TAG" >/dev/null 2>&1; then
  echo "❌ RC tag '$RC_TAG' does not exist."
  exit 1
fi

if git rev-parse "$BASE_TAG" >/dev/null 2>&1; then
  echo "⚠️  Final tag '$BASE_TAG' already exists and will be overwritten."
fi

echo "=========================================="
echo " Promoting RC tag to final release"
echo "------------------------------------------"
echo " Base tag:   $BASE_TAG"
echo " RC tag:     $RC_TAG"
echo " Remote:     origin"
echo " Dry-run:    $DRY_RUN"
echo "=========================================="
echo

# --- perform operations ---
run() {
  if $DRY_RUN; then
    echo "[dry-run] $*"
  else
    echo "▶ $*"
    eval "$@"
  fi
}

run "git tag -f \"$BASE_TAG\" \"$RC_TAG\""
run "git tag -d \"$RC_TAG\""
run "git push origin :refs/tags/$RC_TAG"
run "git push origin refs/tags/$BASE_TAG:refs/tags/rel/$BASE_TAG"

echo
if $DRY_RUN; then
  echo "✅ Dry run complete. No changes made."
else
  echo "✅ RC tag '$RC_TAG' promoted to '$BASE_TAG' and pushed."
fi
