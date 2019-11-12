#!/bin/bash

echo "\n\n"

echo "===========  ISIS CI SCRIPT [$1]  ================="
echo "\$PROJECT_ROOT_PATH        = ${PROJECT_ROOT_PATH}"
echo "\$MVN_STAGES               = ${MVN_STAGES}"
echo "\$REVISION                 = ${REVISION}"
echo "\$CORE_ADDITIONAL_OPTS     = ${CORE_ADDITIONAL_OPTS}"
echo "Nightly Builds:"
echo "\$NIGHTLY_ROOT_PATH        = ${NIGHTLY_ROOT_PATH}"
echo "\$GH_DEPLOY_OWNER          = ${GH_DEPLOY_OWNER}"
echo "\$GITHUB_REPOSITORY        = ${GITHUB_REPOSITORY}"
echo "\$DOCKER_REGISTRY_USERNAME = ${DOCKER_REGISTRY_USERNAME}"
echo "\$DOCKER_REGISTRY_PASSWORD = (suppressed)"
echo "========================================================"

echo "\n\n"

