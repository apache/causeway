#!/usr/bin/env bash
PLAYBOOK_FILE=site.yml
if [[ $# -gt 0 ]]; then

  if [ -f $1 ]; then
    PLAYBOOK_FILE=$1
  else
    PLAYBOOK_FILE=antora/playbooks/site-$1.yml
  fi
  shift
fi

echo "building ..."
export ANTORA_CMD=antora
export CHECK_FOR_STALE_EXAMPLES=skip

echo "\$PLAYBOOK_FILE = $PLAYBOOK_FILE"
sh build-site.sh $PLAYBOOK_FILE || exit 1

echo "serving ..."
serve antora/target/site
