#!/bin/bash

# import shared vars (non secret!)
if [ ! -z "$SHARED_VARS_FILE" ] && [ -f "$SHARED_VARS_FILE" ]; then
  . $SHARED_VARS_FILE
  export $(cut -d= -f1 $SHARED_VARS_FILE)
fi

SITE_CONFIG=$1
GROOVY_CMD=`command -v groovy`

set -e

bash $CI_SCRIPTS_PATH/print-environment.sh "build-site"

echo ""
echo "\$SITE_CONFIG: ${SITE_CONFIG}"
echo "\$GROOVY_CMD:  ${GROOVY_CMD}"
echo ""
 
# for now meant to run with nightly builds only 
if [ -z "${GROOVY_CMD}" ]; then

  echo "no groovy: skipping automated site content script(s)"

else

  ## generate automated site content (adoc files) 
  ${GROOVY_CMD} $CI_SCRIPTS_PATH/../generateConfigDocs.groovy \
    -f $PROJECT_ROOT_PATH/core/config/target/classes/META-INF/spring-configuration-metadata.json \
    -o $PROJECT_ROOT_PATH/core/config/src/main/doc/modules/config/examples/generated
  
fi

## run antora
$(npm bin)/antora --stacktrace $SITE_CONFIG

# add a marker, that tells github not to use jekyll on the github pages folder
touch ${PROJECT_ROOT_PATH}/antora/target/site/.nojekyll
