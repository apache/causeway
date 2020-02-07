#!/bin/bash

#
# adjust as necessary...
#
REPOSROOTS="/c/GITHUB /c/BITBUCKET /c/GITLAB"

_repo_usage()
{
cat << EOF

usage: $0 options repo

OPTIONS:
   [repo]        identify (using regexp) the repo from input file
   
EXAMPLES:
    repo stack

EOF
}

repo() {

    local OPTIND grep_expression

    grep_expression="$*"

    if [ "Z${grep_expression}Z" == "ZZ" ]; then
        _repo_usage
        return 1
    fi

    for REPOSROOT in `echo $REPOSROOTS`
    do
        for a in $(ls $REPOSROOT/*/* -1d | \
                   grep "$grep_expression" | \
                   grep -v ^# )
        do
            echo $a
        done
    done

    for REPOSROOT in `echo $REPOSROOTS`
    do
        for a in $(ls $REPOSROOT/*/* -1d | \
                  grep "$grep_expression" | \
                  grep -v ^# )
        do
            pushd "$a" >/dev/null || return 1
            if [ $? != 0 ]; then
                echo "" >&2
                echo "">&2
                echo "bad directory: $a" >&2
                echo "">&2
                return 1
            fi
            return 0
        done
    done
}
