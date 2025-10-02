#!/usr/bin/env bash
usage() {
  echo "$(basename $0): [-A] [-p] message" >&2
  echo "  -A : suppress adding automatically, ie don't call 'git add .'" >&2
  echo "  -p : also push" >&2
  echo "" >&2
  echo "  Cannot specify -1 and -2 together, obvs. " >&2
}

PUSH=""
ADD=""

while getopts ":hAp" arg; do
  case $arg in
    h)
      usage
      exit 0
      ;;
    A)
      ADD="no-add"
      ;;
    p)
      PUSH="push"
      ;;
    *)
      usage
      exit 1
  esac
done

if [ $# -lt 1 ];
then
  usage
  exit 1
fi


shift $((OPTIND-1))

ISSUE=$(git rev-parse --abbrev-ref HEAD | cut -d- -f1,2)
MSG=$*

echo "     ISSUE       : ${ISSUE}"
echo "     MSG         : ${MSG}"
echo "-A : (NO-)ADD    : ${ADD}"
echo "-p : PUSH        : ${PUSH}"

if [ -z "$ADD" ]
then
  git add .
fi
git commit -m "$ISSUE: ${MSG}"
if [ -n "$PUSH" ]
then
  git push
fi
