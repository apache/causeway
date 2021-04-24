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



#
# usage: ./verify_isis_release.sh [nexus_repo_number] [isis_version] [RC_number]
#
# where nexus_repo_number, isis_version and [RC_number] are as advised in
# releese candidate vote message.
#
#    eg: ./verify_isis_release.sh 1101 2.0.0-M3 RC1
#
#
# prereqs:
#    curl
#    gpg
#    unzip
#    jdk 8
#    mvn 3.6.0+
#

# shellcheck disable=SC2164

_execmustpass(){
    echo $@
    $@
    status=$?
    if [ $status -eq 0 ] || [ $? -eq 0 ]; then
        return;
    fi
    echo "Command $@ failed! [error $status] Exiting..." >&2
    exit 10
}

_execmayfail(){
    echo $@
    $@
    status=$?
    if [ $status -eq 0 ] || [ $? -eq 0 ]; then
        return;
    fi
    echo "Command $@ failed! [error $status] But continuing anyway..." >&2
}

_download(){
    for fil in `cat /tmp/url.txt | grep -v ^#`
    do
        echo 'Downloading '$fil
        _execmustpass $download_cmd $fil
        _execmustpass $download_cmd $fil.asc
    done
}

_verify(){
    for zip in *.zip
    do
        echo 'Verifying '$zip
        _execmustpass gpg --verify $zip.asc $zip
    done
}

_unpack(){
    echo 'Unpacking '
    set -f
    _execmustpass unzip -q '*.zip'
    set +f
}

_build(){
    echo 'Removing Isis from local repo '$module
    rm -rf ~/.m2/repository/org/apache/isis

    echo 'Building'
    # previously there were multiple directories, now just the one.
    pushd isis*/isis-parent
    _execmustpass mvn clean install -Dskip.git -Preleased,-all
	popd
}

_download_app(){
    APP=$1
    BRANCH=$2

    REPO=isis-app-$APP
    DIR=$REPO-$BRANCH

    rm -rf $DIR
    curl "https://codeload.github.com/apache/$REPO/zip/$BRANCH" | jar xv

    pushd $DIR
    _execmustpass mvn clean install
    popd
}


# check the environment
# Check for curl or wget
download_cmd=
curl --version > /dev/null 2>&1
if [[ $? -eq 0 ]]; then
    download_cmd="curl -L -O"
fi
if [[ -z "$download_cmd" ]]; then
    wget --version > /dev/null 2>&1
    if [[ $? -eq 0 ]]; then
        download_cmd=wget
    else
        echo "No downloader found.. exiting.."
        exit 11
    fi
fi

export NEXUSREPONUM=$1
shift
export VERSION=$1
shift
export RC=$1
shift

if [[ -z "$NEXUSREPONUM" || -z "$VERSION" || -z "$RC" ]]; then
    echo "usage: `basename $0` [nexus_repo_number] [isis_version] [RC]" >&2
    exit 1
fi

cat <<EOF >/tmp/url.txt
http://repository.apache.org/content/repositories/orgapacheisis-$NEXUSREPONUM/org/apache/isis/isis-parent/$VERSION/isis-parent-$VERSION-source-release.zip
EOF

# The work starts here
_download
_verify
_unpack
_build
_download_app helloworld jdo
_download_app helloworld jpa
_download_app simpleapp jdo
_download_app simpleapp jpa

# print instructions for final testing
clear
cat <<EOF

# Test out helloworld (jdo) using:
pushd isis-app-helloworld-jdo
mvn spring-boot:run
popd

# Test out helloworld (jpa) using:
pushd isis-app-helloworld-jpa
mvn spring-boot:run
popd

# Test out simpleapp (jdo) using:
pushd isis-app-simpleapp-jdo
mvn -pl webapp spring-boot:run
popd

# Test out simpleapp (jpa) using:
pushd isis-app-simpleapp-jpa
mvn -pl webapp spring-boot:run
popd

EOF
