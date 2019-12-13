#!/bin/bash
set -e

PLAYBOOK=$1

SCRIPT_DIR=$( dirname "$0" )
if [ -z "$PROJECT_ROOT_PATH" ]; then
  PROJECT_ROOT_PATH=$(cd $SCRIPT_DIR/../.. ; pwd)
fi

if [ -z "$PLAYBOOK" ]; then
  PLAYBOOK=site.yml
fi

if [ ! -f "$PLAYBOOK" ]; then
  PLAYBOOK=antora/playbooks/site-$PLAYBOOK.yml
fi
if [ ! -f "$PLAYBOOK" ]; then
  PLAYBOOK=site.yml
fi

PLAYBOOK=$PROJECT_ROOT_PATH/$PLAYBOOK
echo "\$PLAYBOOK = $PLAYBOOK"

if [ -z "$REVISION" ]; then
  if [ ! -z "$SHARED_VARS_FILE" ] && [ -f "$SHARED_VARS_FILE" ]; then
    . $SHARED_VARS_FILE
    export $(cut -d= -f1 $SHARED_VARS_FILE)
  fi
fi
if [ -z "$REVISION" ]; then
  export REVISION="SNAPSHOT"
fi

if [ -z "$ANTORA_CMD" ]; then
  ANTORA_CMD=$(command -v antora 2>/dev/null)
  if [ -z "$ANTORA_CMD" ]; then
    ANTORA_CMD=$(npm bin)/antora
  fi
fi

echo "running antora ..."
echo "$ANTORA_CMD --stacktrace $PLAYBOOK"
$ANTORA_CMD --stacktrace $PLAYBOOK

