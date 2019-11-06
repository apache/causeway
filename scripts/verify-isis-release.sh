#!/bin/bash
#
# usage: ./verify_isis_release.sh [nexus_repo_number] [isis_version]
#
# where nexus_repo_number and isis_version are as advised in RC vote message.
#
#    eg: ./verify_isis_release.sh 1086 1.17.0
#
#
# prereqs:
#    curl or wget
#    gpg
#    unzip
#    jdk 7
#    mvn 3.5.0+
#

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
    for module in ./isis*/ ./*archetype*/
    do
        pushd $module
        _execmustpass mvn clean install -Dskip.git
	    popd
    done
}

_generate_simpleapp(){
    ISISCPN=simpleapp
    rm -rf test-$ISISCPN
    mkdir test-$ISISCPN
    pushd test-$ISISCPN

    _execmustpass mvn archetype:generate  \
        -D archetypeCatalog=local \
        -D groupId=com.mycompany \
        -D artifactId=myapp \
        -D archetypeGroupId=org.apache.isis.archetype \
        -D archetypeArtifactId=$ISISCPN-archetype \
        -B

    pushd myapp
    _execmustpass mvn clean install
    popd; popd
}

_generate_helloworld(){
    ISISCPN=helloworld
    rm -rf test-$ISISCPN
    mkdir test-$ISISCPN
    pushd test-$ISISCPN

    _execmustpass mvn archetype:generate  \
        -D archetypeCatalog=local \
        -D groupId=com.mycompany \
        -D artifactId=myapp \
        -D archetypeGroupId=org.apache.isis.archetype \
        -D archetypeArtifactId=$ISISCPN-archetype \
        -B

    pushd myapp
    _execmustpass mvn clean install
    popd; popd
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

NEXUSREPONUM=$1
shift
VERSION=$1
shift

if [[ -z "$NEXUSREPONUM" || -z "$VERSION" ]]; then
    echo "usage: `basename $0` [nexus_repo_number] [isis_version]" >&2
    exit 1
fi

cat <<EOF >/tmp/url.txt
http://repository.apache.org/content/repositories/orgapacheisis-$NEXUSREPONUM/org/apache/isis/core/isis/$VERSION/isis-$VERSION-source-release.zip
http://repository.apache.org/content/repositories/orgapacheisis-$NEXUSREPONUM/org/apache/isis/archetype/helloworld-archetype/$VERSION/helloworld-archetype-$VERSION-source-release.zip
http://repository.apache.org/content/repositories/orgapacheisis-$NEXUSREPONUM/org/apache/isis/archetype/simpleapp-archetype/$VERSION/simpleapp-archetype-$VERSION-source-release.zip
EOF

# The work starts here
_download
_verify
_unpack
_build
_generate_simpleapp
_generate_helloworld

# print instructions for final testing
clear
cat <<EOF

# Test out simpleapp using:
pushd test-simpleapp/myapp
mvn -pl webapp jetty:run
popd

# Test out helloworld using:
pushd test-helloworld/myapp
mvn jetty:run
popd

EOF