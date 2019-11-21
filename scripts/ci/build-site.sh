#!/bin/bash
set -e

SCRIPT_DIR=$( dirname "$0" )
if [ -z "$PROJECT_ROOT_PATH" ]; then
  PROJECT_ROOT_PATH=`cd $SCRIPT_DIR/../.. ; pwd`
fi

if [ -z "$REVISION" ]; then
  if [ ! -z "$SHARED_VARS_FILE" ] && [ -f "$SHARED_VARS_FILE" ]; then
    . $SHARED_VARS_FILE
    export $(cut -d= -f1 $SHARED_VARS_FILE)
  fi
fi

if [ -z "$REVISION" ]; then
  export REVISION="SNAPSHOT"
fi


SITE_CONFIG=$1


##
## run groovy
##
GROOVY_CMD=`command -v groovy`

bash $SCRIPT_DIR/print-environment.sh "build-site"

echo ""
echo "\$SITE_CONFIG: ${SITE_CONFIG}"
echo "\$GROOVY_CMD : ${GROOVY_CMD}"
echo ""
 
# for now meant to run with nightly builds only 
if [ -z "${GROOVY_CMD}" ]; then
  echo "doc gen: no groovy, skipping"
else
  if [ ! -f "$PROJECT_ROOT_PATH/core/config/target/classes/META-INF/spring-configuration-metadata.json" ]; then
    echo "doc gen: no spring-configuration-metadata.json to parse: skipping"
  else
    # generate automated site content (adoc files)
    echo "doc gen: generating config .adoc from Spring metadata ..."
    ${GROOVY_CMD} $SCRIPT_DIR/../generateConfigDocs.groovy \
      -f $PROJECT_ROOT_PATH/core/config/target/classes/META-INF/spring-configuration-metadata.json \
      -o $PROJECT_ROOT_PATH/core/config/src/main/doc/modules/config/examples/generated
  fi
fi


##
## copy over examples
##
echo "copying over examples ..."
for examples_sh in $(find $PROJECT_ROOT_PATH -name examples.sh -print)
do
  echo $examples_sh
  sh $examples_sh
done


##
## run antora
##
echo "running antora ..."
whence antora 2>&1 >/dev/null
if [ $? -eq 0 ]; then
  ANTORA_CMD=antora
else
  # this fails on Windows (git-bash), which is why try to use antora from path
  ANTORA_CMD=$(npm bin)/antora
fi

$ANTORA_CMD --stacktrace $SITE_CONFIG

# add a marker, that tells github not to use jekyll on the github pages folder
touch ${PROJECT_ROOT_PATH}/antora/target/site/.nojekyll
