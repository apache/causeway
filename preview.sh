#!/usr/bin/env bash
PLAYBOOK_FILE=site.yml
if [[ $# -gt 0 ]]; then
  PLAYBOOK_FILE=antora/playbooks/site-$1.yml
  shift
fi

echo "building ..."
export ANTORA_CMD=antora

echo "\$PLAYBOOK_FILE = $PLAYBOOK_FILE"
sh build-site.sh $PLAYBOOK_FILE || exit 1

echo "serving ..."
serve antora/target/site
