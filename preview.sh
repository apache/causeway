#!/usr/bin/env bash

export ANTORA_CACHE_DIR=.antora-cache-dir
export ANTORA_TARGET_SITE=antora/target/site

#
# for now, we disable index generation, because (a) java 11 dependency,
# probably not available on CI build server, and (b) need to build tooling.
#
# nevertheless, a committer can use this script to easily regenerate the
# index (only) using the -I flag.
#
PLAYBOOK_FILE=antora/playbooks/site.yml

while getopts 'ECPAKSecpaksxyhf:' opt
do
  case $opt in
    E) export SKIP_EXAMPLES=false
       forcing=true ;;
    C) export SKIP_CONFIGS=false
       forcing=true ;;
    W) export SKIP_PROJDOC_GENERATION=false
       forcing=true ;;
    A) export SKIP_ANTORA_GENERATION=false
       export SKIP_CLEAR_CACHE=false
       export SKIP_CLEAR_PREVIOUS=false
       forcing=true ;;
    K) export SKIP_STALE_EXAMPLE_CHECK=false
       forcing=true ;;
    S) export SKIP_SERVE=false
       forcing=true ;;

    e) export SKIP_EXAMPLES=true ;;
    c) export SKIP_CONFIGS=true ;;
    p) export SKIP_PROJDOC_GENERATION=true ;;
    a) export SKIP_ANTORA_GENERATION=true
       export SKIP_CLEAR_CACHE=true
       export SKIP_CLEAR_PREVIOUS=true
      ;;
    k) export SKIP_STALE_EXAMPLE_CHECK=true ;;
    s) export SKIP_SERVE=true ;;

    x) export SKIP_CLEAR_CACHE=true ;;
    y) export SKIP_CLEAR_PREVIOUS=true ;;
    f) PLAYBOOK_FILE=$OPTARG ;;
    h) echo ""
       echo "preview.sh options:"
       echo ""
       echo "  Skip options:"
       echo "  -e skip examples"
       echo "  -k skip stale example check"
       echo "  -c skip config doc generation"
       echo "  -w skip projdoc generation"
       echo "  -a skip Antora generation"
       echo "  -s skip serving generated site"
       echo ""
       echo "  Force options (override skip):"
       echo "  -E force examples"
       echo "  -K force stale example check"
       echo "  -C force config doc generation"
       echo "  -P force projdoc generation"
       echo "  -A force Antora generation"
       echo "  -S force serving generated site"
       echo ""
       echo "  -f antora/playbooks/site-xxx.yml"
       exit 1
       ;;
    *) echo "unknown option $opt - aborting" >&2
       exit 1
      ;;
  esac
done

if [ "$forcing" = "true" ]; then
    if [ -z "$SKIP_EXAMPLES" ]; then
      export SKIP_EXAMPLES=true
    fi
    if [ -z "$SKIP_CONFIGS" ]; then
      export SKIP_CONFIGS=true
    fi
    if [ -z "$SKIP_PROJDOC_GENERATION" ]; then
      export SKIP_PROJDOC_GENERATION=true
    fi
    if [ -z "$SKIP_ANTORA_GENERATION" ]; then
      export SKIP_ANTORA_GENERATION=true
      export SKIP_CLEAR_CACHE=true
      export SKIP_CLEAR_PREVIOUS=true
    fi
    if [ -z "$SKIP_STALE_EXAMPLE_CHECK" ]; then
      export SKIP_STALE_EXAMPLE_CHECK=true
    fi
    if [ -z "$SKIP_SERVE" ]; then
      export SKIP_SERVE=true
    fi
fi

echo ""
echo "SKIP_EXAMPLES              : $SKIP_EXAMPLES"
echo "SKIP_STALE_EXAMPLE_CHECK   : $SKIP_STALE_EXAMPLE_CHECK"
echo "SKIP_PROJDOC_GENERATION    : $SKIP_PROJDOC_GENERATION"
echo "SKIP_CONFIGS               : $SKIP_CONFIGS"
echo "SKIP_ANTORA_GENERATION     : $SKIP_ANTORA_GENERATION"
echo "SKIP_SERVE                 : $SKIP_SERVE"
echo "SKIP_CLEAR_PREVIOUS (site) : $SKIP_CLEAR_PREVIOUS"
echo "SKIP_CLEAR_CACHE (template): $SKIP_SKIP_CLEAR_CACHE"
echo ""

if [[ "$SKIP_CLEAR_CACHE" == "true" ]]; then
  echo "skipping clearing the Antora cache"
else
  rm -rf $ANTORA_CACHE_DIR
fi

if [[ "$SKIP_CLEAR_PREVIOUS" == "true" ]]; then
  echo "skipping clearing any previous build site"
else
  rm -rf $ANTORA_TARGET_SITE
fi

if [ ! -f $PLAYBOOK_FILE ]; then
  echo "no such file $PLAYBOOK_FILE" >&2
  exit 1
fi

export ANTORA_CMD=antora

SECONDS=0
echo "\$PLAYBOOK_FILE = $PLAYBOOK_FILE"
sh build-site.sh $PLAYBOOK_FILE || exit 1


if [[ "$SKIP_SERVE" == "true" ]]; then
  echo "skipping serving"
else
  echo ""
  echo "http://localhost:5000/docs/latest/about.html"
  echo ""

  serve -S -p 5000 $ANTORA_TARGET_SITE
fi
