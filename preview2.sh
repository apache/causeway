#!/usr/bin/env bash
PLAYBOOK_FILE=site.yml
if [[ $# -gt 0 ]]; then
  PLAYBOOK_FILE=antora/playbooks/site-$1.yml
  shift
fi

echo "building ..."
sh build-site.sh $PLAYBOOK_FILE

echo "serving ..."
serve antora/target/site
