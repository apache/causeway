#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
#

#set -x
#trap read debug
set -o nounset
set -o errexit


#
# Merges github PRs into local git clone of ASF project
# ('https://github.com/apache/xxx')
#
# Process:
# - locate/raise JIRA ticket, eg CAUSEWAY-1162
# - checkout branch from which PR was forked (usually just 'master')
# - merge PR into temporary branch using this script
# - check build
# - merge temporary branch into mainline, and commit
#
#
# Usage: github-pr.sh -p CAUSEWAY -j 1162 -g 31
#
#        where
#               - causeway is the JIRA project and repo
#               - 1162 is the JIRA ticket number
#               - 31   is the gthub PR issue number
#
# uses 'jq' to parse JSON
# - on Linux: aptitude install jq
# - on Mac: brew install jq
# - on Windows: download exe from http://stedolan.github.io/jq/download/
#

function die {
	local prefix="[$(date +%Y/%m/%d\ %H:%M:%S)]: "
	echo "${prefix} ERROR: $@" 1>&2
	exit 10
}

project="CAUSEWAY"
jira_suffix=""
pr_number=""
skip_build="false"

#
# validate script args
#

while getopts ":p:j:g:s" opt; do
  case $opt in
    p)
      project=$OPTARG
      ;;
    j)
      jira_suffix=$OPTARG
      ;;
    g)
      pr_number=$OPTARG
      ;;
    s)
      skip_build="true"
      ;;
    \?)
      echo "Invalid option: -$OPTARG" >&2
      ;;
  esac
done

echo ""
echo "project   : $project"
echo "ASF jira  : $jira_suffix"
echo "github PR : $pr_number"
echo "skip_build: $skip_build"
echo ""

if [ "$jira_suffix" == "" -o "$pr_number" == "" ]; then
    die "usage: github-pr.sh -j nnn -g pp [-s] [-p pppp]"
fi


project_lower=$(echo $project | tr '[:upper:]' '[:lower:]')
project_upper=$(echo $project | tr '[:lower:]' '[:upper:]')

jira_number="$project_upper-$jira_suffix"

#
# validate JIRA ticket
#
jira_url="https://issues.apache.org/jira/rest/api/2/search?jql=key%20in%20($jira_number)"
jira_json=$(curl -s "$jira_url")
if [ $? -ne 0 ]; then
    die "Failed to query JIRA for issue; url: $jira_url"
fi

err_message=$(echo $jira_json | jq --raw-output '.errorMessages')

if [ "$err_message" != "null" ]; then
	die "Cannot find the info about JIRA issue $jira_number"
fi

echo ""
echo "Found JIRA ticket"


#
# validate github PR
#
github_url="https://api.github.com/repos/apache/$project_lower/pulls/$pr_number"
github_json=$(curl -s $github_url)
if [ $? -ne 0 ]; then
    die "Failed to query github for PR; url: $github_url"
fi

err_message=$(echo $github_json | jq --raw-output '.message')
if [ "x$err_message" = "xNot Found" ]; then
	die "Cannot find the info about PR $pr_number"
fi

echo "Found github PR"

branch_name_local=$(git rev-parse --abbrev-ref HEAD)
username=$(echo $github_json | jq --raw-output '.head .user .login')
repo_full_name=$(echo $github_json | jq --raw-output '.head .repo .full_name')
repo_clone_url=$(echo $github_json | jq --raw-output '.head .repo .clone_url')
branch_name_fork=$(echo $github_json | jq --raw-output '.head .ref')

branch_name_temp="${jira_number}_pr-$pr_number"

echo "branch_name_local: $branch_name_local"
echo "username         : $username"
echo "repo_full_name   : $repo_full_name"
echo "repo_clone_url   : $repo_clone_url"
echo "branch_name_fork : $branch_name_fork"
echo ""

if [ "x$repo_full_name" = "xnull" ]; then
    die "Could not determine the repo to merge from"
fi


echo "merging into: $branch_name_temp"
echo ""


branch_exists=$(git branch --list $branch_name_temp)
if [ "x$branch_exists" != "x" ]; then
	echo "Deleting branch '$branch_name_temp'"
	git branch -D $branch_name_temp
fi

echo "Creating the branch $branch_name_temp"
git checkout -b $branch_name_temp $branch_name_local

echo "Pulling the changes from $repo_clone_url $branch_name_fork"
git pull $repo_clone_url $branch_name_fork

echo ""


if [ "$skip_build" == "false" ]
then
    echo "Merged the PR; hit enter to build"
    read
    echo "Building..."
    echo

    mvn clean install -o

    echo
    echo
    echo
    echo "If build successful and happy to merge, execute:"
    echo
    echo "git checkout $branch_name_local && git merge --no-ff $branch_name_temp && git branch -d $branch_name_temp"
    echo
else
    echo
    echo "Merging..."
    echo
    git checkout $branch_name_local && git merge --no-ff $branch_name_temp && git branch -d $branch_name_temp
fi
