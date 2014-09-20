#!/bin/bash
# Instructions:
# -Create an empty directory
# -Put a .txt file in it containing a list of all the urls of the zip files
# -Run this script
# TODO: enhance this script so it will stop when something is broken

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
        _execmayfail $download_cmd $fil.asc
    done
}

_verify(){
    for zip in *.zip
    do 
        echo 'Verifying '$zip   
        _execmayfail gpg --verify $zip.asc $zip 
    done
}

_unpack(){
    echo 'Unpacking '
    set -f
    _execmustpass unzip -q '*.zip'
    set +f
}

_fetch_dependencies(){
    _execmustpass mvn dependency:go-offline
}

_build(){
    echo 'Removing Isis from local repo '$module
    rm -rf ~/.m2/repository/org/apache/isis
    COUNTER=0
    for module in ./*/
    do
        # Surely better to check if "core" is in name?
        COUNTER=$[COUNTER+1]
        #if [ $COUNTER -eq 1 ]
        #if [[ $module == "*core*" ]]
        cd $module
        grep -q "Isis Core" pom.xml
        retcode=$?
        if [  $retcode -eq 0 ]
        then
            echo 'Building Core '$module
            _fetch_dependencies
            _execmustpass mvn clean install -o
            cd ..
        else
            echo 'Building Module '$module
            # _execmustpass mvn clean install
            cd ..
        fi
    done
}
# check the environment
# Check for curl or wget
download_cmd=
curl --version
if [[ $? -eq 0 ]]; then
    download_cmd=curl -O
fi 
if [[ -z "$download_cmd" ]]; then
    wget --version
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

