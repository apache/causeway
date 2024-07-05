#!/bin/bash
#  Licensed to the Apache Software Foundation (ASF) under one
#  or more contributor license agreements.  See the NOTICE file
#  distributed with this work for additional information
#  regarding copyright ownership.  The ASF licenses this file
#  to you under the Apache License, Version 2.0 (the
#  "License"); you may not use this file except in compliance
#  with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing,
#  software distributed under the License is distributed on an
#  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#  KIND, either express or implied.  See the License for the
#  specific language governing permissions and limitations
#  under the License.


#set -x
#trap read debug
set -o nounset
set -o errexit


# Usage: jira-release-notes.sh causeway 1.9.0
#
#        where 
#               - causeway is the JIRA project and repo
#               - 1.9.0 is the JIRA ticket number
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


#
# validate script args
#
if [ $# -ne 2 -a $# -ne 3 ]; then
    die "usage: jira-release-notes.sh proj version [maxResults]"
fi

project=$1
version=$2

if [ $# -eq 3  ]; then
    maxResults=$3
else
    maxResults=500
fi

project_lower=$(echo $project | tr '[:upper:]' '[:lower:]')
project_upper=$(echo $project | tr '[:lower:]' '[:upper:]')


function rawurlencode() {
  local string="${1}"
  local strlen=${#string}
  local encoded=""

  for (( pos=0 ; pos<strlen ; pos++ )); do
     c=${string:$pos:1}
     case "$c" in
        [-_.~a-zA-Z0-9] ) o="${c}" ;;
        * )               printf -v o '%%%02x' "'$c"
     esac
     encoded+="${o}"
  done
  echo "${encoded}"    # You can either set a return variable (FASTER) 
  REPLY="${encoded}"   #+or echo the result (EASIER)... or both... :p
}




function jira_by_type () {

    type=$1
    type_trimmed=$( echo "$type" | awk '{$1=$1};1' )
    type_url_encoded=$( rawurlencode "$type_trimmed" )

    maxResults=$2
    
    jira_url="https://issues.apache.org/jira/rest/api/2/search?jql=project%20in%20($project_upper)%20AND%20fixVersion%20in%20($version)%20AND%20type=\"$type_url_encoded\"&fields=summary&maxResults=${maxResults}"

    #echo $jira_url

    jira_json=$(curl -s "$jira_url")
    if [ $? -ne 0 ]; then
        die "Failed to query JIRA for issue; url: $jira_url"
    fi

    #we could create some nice JSON using this, but then we would need to iterate over it somehow in bash...
    #echo $jira_json | jq '.issues []  | { key: .key, summary: .fields .summary }'

    #so instead, let's do a cheap-n-nasty approach of creating two simple arrays of same length
    echo $jira_json | jq --raw-output '.issues []  | .key '             > /tmp/$$.keys
    echo $jira_json | jq --raw-output '.issues []  | .fields .summary ' > /tmp/$$.summaries
    
    keys=()
    i=0
    while read line
    do
        keys[i]=$line
        i=$(($i + 1))
    done < /tmp/$$.keys

    summaries=()
    i=0
    while read line
    do
        summaries[i]=$line
        i=$(($i + 1))
    done < /tmp/$$.summaries


    echo
    echo
    echo "== $type"
    echo

    total=${#keys[*]}
    for (( i=0; i<=$(( $total -1 )); i++ ))
    do
        key=$(echo "${keys[$i]}" | xargs )
        summary="${summaries[$i]}"
        echo "* link:https://issues.apache.org/jira/browse/$key[$key] - $summary"
    done

    
}


function contains() {
    local n=$#
    local value=${!n}
    for ((i=1;i < $#;i++)) {
        if [ "${!i}" == "${value}" ]; then
            echo "y"
            return 0
        fi
    }
    echo "n"
    return 1
}



#
# find all issue types
# nb: there is a maxResults of 100; not much we can do about that... ?
#
jira_url="https://issues.apache.org/jira/rest/api/2/search?jql=project%20in%20($project_upper)%20AND%20fixVersion%20in%20($version)&fields=issuetype&maxResults=100"
jira_json=$(curl -s "$jira_url")
if [ $? -ne 0 ]; then
    die "Failed to query JIRA for issue; url: $jira_url"
fi

echo $jira_json | jq --raw-output '.issues []  | .fields .issuetype .name ' > /tmp/$$.1
known_types=()



i=0
while read line
do
    known_types[i]=$(echo $line | awk '{$1=$1};1')
    i=$(($i + 1))
done < /tmp/$$.1


#
# order the available types in the order specified; any others in the order returned by the JIRA query
# 
declare -a ordered_types=("New Feature" "Improvement" "Bug" "Dependency upgrade")


types=()

# if the $ordered_type is present in the $known_types, then add to $types
for ordered_type in "${ordered_types[@]}"; do
    if [ $(contains "${known_types[@]}" "$ordered_type") == "y" ]; then 
        types+=("$ordered_type")
    fi
done

# for all $known_types that we haven't yet seen, add into $types
for known_type in "${known_types[@]}"; do
    if [ $(contains "${types[@]}" "$known_type") == "n" ]; then 
        types+=("$known_type")
    fi
done



#
# Now generate the Asciidoc report
#

echo
echo
echo
echo "[[r$version]]"
echo "= $version"
echo 
echo "NOTE: TODO - add description"
echo 
echo


for type in "${types[@]}"; do
    jira_by_type "$type" "$maxResults"
done

