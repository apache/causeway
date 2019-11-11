#!/bin/bash

echo "\n\n"

echo "===========  ISIS CI SCRIPT [$1]  ================="
echo "\$PROJECT_ROOT_PATH        = ${PROJECT_ROOT_PATH}"
echo "\$MVN_STAGES               = ${MVN_STAGES}"
echo "\$REVISION                 = ${REVISION}"
echo "\$CORE_ADDITIONAL_OPTS     = ${CORE_ADDITIONAL_OPTS}"
echo "\$GH_DEPLOY_REPO_OWNER     = ${GH_DEPLOY_REPO_OWNER}" 
echo "\$DOCKER_REGISTRY_USERNAME = ${DOCKER_REGISTRY_USERNAME}"
echo "\$DOCKER_REGISTRY_PASSWORD = (suppressed)"
echo "========================================================"

echo "\n\n"

