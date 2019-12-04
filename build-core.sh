#!/bin/bash
export MVN_STAGES="install"
export BATCH_MODE_FLAG=off
sh scripts/ci/build-core.sh $*