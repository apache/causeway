#!/usr/bin/env bash
usage() {
  #echo "$(basename $0): [-a] [-c] [-e] [-m] [-o] [-s] " >&2
  echo "$(basename $0): [-c] [-e] [-m] [-s] "           >&2
  #echo "  -a : audit trail (extensions/security)"      >&2
  echo "  -c : command log (extensions/core)"           >&2
  echo "  -e : execution log (extensions/core)"         >&2
  echo "  -m : secman (extensions/security)"            >&2
  #echo "  -o : execution outbox (extensions/core)"     >&2
  echo "  -s : session log (extensions/security)"       >&2
}



AUDITTRAIL=""
COMMANDLOG=""
EXECUTIONLOG=""
EXECUTIONOUTBOX=""
SECMAN=""
SESSIONLOG=""

PATHS=()

#while getopts ":acemosh" arg; do
while getopts ":cemsh" arg; do
  case $arg in
    h)
      usage
      exit 0
      ;;
#    a)
#      AUDITTRAIL="enhance"
#      PATHS+=( "extensions/security/audittrail/persistence-jdo" )
#      ;;
    c)
      COMMANDLOG="enhance"
      PATHS+=( "extensions/core/commandlog/persistence-jdo" )
      ;;
    e)
      EXECUTIONLOG="enhance"
      PATHS+=( "extensions/core/executionlog/persistence-jdo" )
      ;;
    m)
      SECMAN="enhance"
      PATHS+=( "extensions/security/secman/persistence-jdo" )
      ;;
#    o)
#      EXECUTIONOUTBOX="enhance"
#      PATHS+=( "extensions/core/executionoutbox/persistence-jdo" )
#      ;;
    s)
      SESSIONLOG="enhance"
      PATHS+=( "extensions/security/sessionlog/persistence-jdo" )
      ;;
    *)
      usage
      exit 1
  esac
done

shift $((OPTIND-1))


echo "AUDITTRAIL      : $AUDITTRAIL"
echo "COMMANDLOG      : $COMMANDLOG"
echo "EXECUTIONLOG    : $EXECUTIONLOG"
echo "EXECUTIONOUTBOX : $EXECUTIONOUTBOX"
echo "SECMAN          : $SECMAN"
echo "SESSIONLOG      : $SESSIONLOG"


printf -v PATHS_SPLATTED '%s,' "${PATHS[@]}"
PL_ARG=$(echo "${PATHS_SPLATTED%,}")


echo mvn install -DskipTests -o -T1C -am -pl $PL_ARG
mvn install -DskipTests -o -T1C -am -pl $PL_ARG
