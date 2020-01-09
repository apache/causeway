#!/bin/bash

PLAYBOOK_FILE=$1

if [ -z "$PROJECT_ROOT_PATH" ]; then
  echo "\$PROJECT_ROOT_PATH must be set" >&2
  exit 1
fi

if [ -z "$PLAYBOOK_FILE" ]; then
  PLAYBOOK_FILE=antora/playbooks/site.yml
fi

if [ ! -f "$PLAYBOOK_FILE" ]; then
  echo "Playbook file '$PLAYBOOK_FILE' does not exist" >&2
  exit 1
fi

echo "\$PLAYBOOK_FILE = $PLAYBOOK_FILE"

# temporarily copy the file down to the root.
cp $PLAYBOOK_FILE $PROJECT_ROOT_PATH
PLAYBOOK=$(basename $PLAYBOOK_FILE)

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

# clean up
rm $PLAYBOOK