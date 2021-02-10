#!/usr/bin/env bash

SKIP_TESTS=false

while getopts 'th' opt
do
  case $opt in
    t) export SKIP_TESTS=true
       ;;
    h) echo ""
       echo "build-tooling.sh options:"
       echo ""
       echo "  Skip options:"
       echo "  -t : skip tests"
       echo ""
       exit 1
       ;;
    *) echo "unknown option $opt - aborting" >&2
       exit 1
      ;;
  esac
done

echo ""
echo "SKIP_TESTS : $SKIP_TESTS"

OPTS=""
if [[ "$SKIP_TESTS" == "true" ]]; then
  OPTS="$OPTS -DskipTests "
fi

mvnd -Dmodule-tooling -Dskip.essential $OPTS install
