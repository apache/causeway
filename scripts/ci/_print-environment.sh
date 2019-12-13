#!/bin/bash

echo ""

echo "===========  ISIS CI SCRIPT [$1]  ================="
echo "\$REVISION                  = ${REVISION}"
echo "\$PROJECT_ROOT_PATH         = ${PROJECT_ROOT_PATH}"
echo "\$MVN_STAGES                = ${MVN_STAGES}"
echo "\$MVN_ADDITIONAL_OPTS       = ${MVN_ADDITIONAL_OPTS}"
echo "- Nightly Builds:"
echo "\$NIGHTLY_ROOT_PATH         = ${NIGHTLY_ROOT_PATH}"
echo "- Github:"
echo "\$GH_DEPLOY_OWNER           = ${GH_DEPLOY_OWNER}"
echo "\$GITHUB_REPOSITORY         = ${GITHUB_REPOSITORY}"
echo "- GCP App Engine Repo:"
echo "\$GCPAPPENGINEREPO_USERNAME = ${GCPAPPENGINEREPO_USERNAME}"
echo "\$GCPAPPENGINEREPO_PASSWORD = (suppressed)"
echo "- Docker Hub:"
echo "\$DOCKER_REGISTRY_USERNAME  = ${DOCKER_REGISTRY_USERNAME}"
echo "\$DOCKER_REGISTRY_PASSWORD  = (suppressed)"
echo "========================================================"

echo ""

