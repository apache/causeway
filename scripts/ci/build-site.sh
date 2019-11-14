#!/bin/bash
set -e

SITE_CONFIG=$1

bash $CI_SCRIPTS_PATH/print-environment.sh "build-site"

echo ""
echo "\$SITE_CONFIG: ${SITE_CONFIG}"
echo ""

## generate automated site content (adoc files) 
groovy $CI_SCRIPTS_PATH/../generateConfigDocs.groovy \
  -f $PROJECT_ROOT_PATH/core/config/target/classes/META-INF/spring-configuration-metadata.json \
  -o $PROJECT_ROOT_PATH/core/config/src/main/doc/modules/config/examples/generated

## run antora
$(npm bin)/antora --stacktrace $SITE_CONFIG

