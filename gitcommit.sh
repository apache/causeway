#!/usr/bin/env bash
usage() {
  echo "$(basename $0): [-A] [-P] message" >&2
  echo "  -A : suppress adding automatically, ie don't call 'git add .'" >&2
  echo "  -p : also push" >&2
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
  echo $USAGE >&2
  exit 1
fi

shift $((OPTIND-1))

ISSUE=$(git rev-parse --abbrev-ref HEAD | cut -d- -f1,2)
MSG=$*

echo "ISSUE     : $ISSUE"
echo "MSG       : $MSG"
echo "(NO-)ADD  : $ADD"
echo "PUSH      : $PUSH"

if [ -d _pipeline-resources ]
then
  pushd _pipeline-resources || exit
  if [ -z "$ADD" ]
  then
    git add .
  fi
  git commit -m "$ISSUE: ${MSG}"
  if [ -n "$PUSH" ]
  then
    git push
  fi
  popd || exit
fi

if [ -z "$ADD" ]
then
  git add .
fi
git commit -m "$ISSUE: ${MSG}"
if [ -n "$PUSH" ]
then
  git push
fi
