#!/bin/bash
set -e

SCRIPT_DIR=$( dirname "$0" )
if [ -z "$PROJECT_ROOT_PATH" ]; then
  PROJECT_ROOT_PATH=`cd $SCRIPT_DIR/../.. ; pwd`
fi

SITE_CONFIG=$1

if [ -z "$REVISION" ]; then
  if [ ! -z "$SHARED_VARS_FILE" ] && [ -f "$SHARED_VARS_FILE" ]; then
    . $SHARED_VARS_FILE
    export $(cut -d= -f1 $SHARED_VARS_FILE)
  fi
fi
if [ -z "$REVISION" ]; then
  export REVISION="SNAPSHOT"
fi

echo "running antora ..."
if [ -z "$ANTORA_CMD" ]; then
  ANTORA_CMD=$(command -v antora 2>/dev/null)
  if [ -z "$ANTORA_CMD" ]; then
    ANTORA_CMD=$(npm bin)/antora
  fi
fi

$ANTORA_CMD --stacktrace $SITE_CONFIG

