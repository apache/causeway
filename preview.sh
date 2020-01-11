#!/usr/bin/env bash

PLAYBOOK_FILE=antora/playbooks/site.yml
if [[ $# -gt 0 ]]; then

  if [[ $1 == "-x" ]]; then
    SKIP_GENERATION=true
	shift
  fi

  if [ -f antora/playbooks/$1 ]; then
    PLAYBOOK_FILE=$1
  else
    echo "no such file $PLAYBOOK_FILE - using site.yml" >&2
  fi
  shift
fi

if [[ "$SKIP_GENERATION" == "true" ]]; then

  echo "skipping site generation"

else
  echo "building ..."
  export ANTORA_CMD=antora
  export CHECK_FOR_STALE_EXAMPLES=skip

  SECONDS=0
  echo "\$PLAYBOOK_FILE = $PLAYBOOK_FILE"
  sh build-site.sh $PLAYBOOK_FILE || exit 1

  echo ""
  echo "site built in ${SECONDS}s"

fi

echo ""
echo "http://localhost:5000"
echo ""

serve -S -p 5000 antora/target/site
