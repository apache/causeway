#!/bin/bash
export ANTORA_CACHE_DIR=.antora-cache-dir
sh scripts/ci/_build-site.sh $*
