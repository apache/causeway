#!/bin/bash

export SKIP_EXAMPLES=true
export SKIP_CONFIGS=true
export SKIP_STALE_EXAMPLE_CHECK=true

sh scripts/ci/_build-site.sh $*
