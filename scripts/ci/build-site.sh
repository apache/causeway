#!/bin/bash
set -e

SITE_CONFIG=$1

sh $CI_SCRIPTS_PATH/print-environment.sh "build-site"

echo ""
echo "\$SITE_CONFIG: ${SITE_CONFIG}"
echo ""

# run antora
$(npm bin)/antora --stacktrace $SITE_CONFIG

