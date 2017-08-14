#!/bin/bash
# Instructions:
# -Create an empty directory
# -Put a url.txt file in it containing a list of all the urls of the zip files
# -Run this script

_execmustpass(){
    echo $@
    $@
    status=$?
    if [ $status -eq 0 ] || [ $? -eq 0 ]; then
        return;
    fi
    echo "Command $@ failed! [error $status] Exiting..."
    exit 10
}

_execmayfail(){
    echo $@
    $@
    status=$?
    if [ $status -eq 0 ] || [ $? -eq 0 ]; then
        return;
    fi
    echo "Command $@ failed! [error $status] But continuing anyway..."
}

_download(){
    for fil in `cat *.txt`
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

_fetch_dependencies(){
    _execmayfail mvn dependency:go-offline
}

_build(){
    echo 'Removing Isis from local repo '$module
    rm -rf ~/.m2/repository/org/apache/isis
    for module in ./isis*/ ./*archetype*/
    do
        cd $module
        grep -q "Isis Core" pom.xml
        retcode=$?
        if [  $retcode -eq 0 ]
        then
            echo 'Building Core '$module
            _fetch_dependencies
            _execmustpass mvn clean install -o
        else
            echo 'Building Module '$module
            _execmustpass mvn clean install $offline
        fi
		cd ..
    done
}

if [[ $@ == *online* ]]
then
    echo "Enabling online mode."
	offline=
else
    echo "Enabling offline mode. Use '$0 --online' to use online mode."
	offline=-o
fi

# check the environment
# Check for curl or wget
download_cmd=
curl --version > /dev/null 2>&1
if [[ $? -eq 0 ]]; then
    download_cmd=curl -L -O
fi 
if [[ -z "$download_cmd" ]]; then
    wget --version > /dev/null 2>&1
    if [[ $? -eq 0 ]]; then
        download_cmd=wget
    else
        echo "No downloader found.. exitting.."
        exit 11
    fi 
fi

# The work starts here 
_download
_verify
_unpack
_build

