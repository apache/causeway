#!/usr/bin/env bash

SECONDS=0
PLAYBOOK_FILE=site.yml
if [[ $# -gt 0 ]]; then

  if [ -f $1 ]; then
    PLAYBOOK_FILE=$1
  else
    echo "no such file $PLAYBOOK_FILE - using site.yml" >&2
  fi
  shift
fi

echo "building ..."
export ANTORA_CMD=antora
export CHECK_FOR_STALE_EXAMPLES=skip

echo "\$PLAYBOOK_FILE = $PLAYBOOK_FILE"
sh build-site.sh $PLAYBOOK_FILE || exit 1

# do some work
echo ""
echo "site built in ${SECONDS}s"
echo "serving ... (paste URL from clipboard)"
serve -S -p 5000 antora/target/site
