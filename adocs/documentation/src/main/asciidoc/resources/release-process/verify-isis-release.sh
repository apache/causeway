#!/bin/bash
# Instructions:
# -Create an empty directory
# -Put a .txt file in it containing a list of all the urls of the zip files
# -Run this script
# TODO: enhance this script so it will stop when something is broken
_download(){
    for fil in `cat *.txt`
    do
        echo 'Downloading '$fil
        curl -L -O $fil
        curl -L -O $fil.asc
    done
}
_verify(){
    for zip in *.zip
    do 
        echo 'Verifying '$zip   
        gpg --verify $zip.asc $zip 
    done
}
_unpack(){
    echo 'Unpacking '
    unzip -q '*.zip'
}
_build(){
    echo 'Removing Isis from local repo '$module
    rm -rf ~/.m2/repository/org/apache/isis
    COUNTER=0
    for module in ./*/
    do
        COUNTER=$[COUNTER+1]
        if [ $COUNTER -eq 1 ]
        then
            cd $module
            echo 'Building Core '$module
            mvn clean install -o
            cd ..
        else
            cd $module
            echo 'Building Module '$module
            mvn clean install
            cd ..
        fi
    done
}
# The work starts here 
_download
_verify
_unpack
_build

