#!/bin/bash
export ANTORA_CACHE_DIR=.antora-cache-dir
bash scripts/ci/_build-site.sh $*
