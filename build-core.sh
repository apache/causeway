#!/bin/bash

if [ -z "$MVN_STAGES" ]; then
  export MVN_STAGES="install"
fi

export BATCH_MODE_FLAG=off
sh scripts/ci/build-core.sh $*