#!/usr/bin/env bash


#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
#

#
# example usage:
#
#     sh build.sh -ptOvI
#

#
# see serve-timeline.sh to serve up the generated website (requires JDK18)
#

BASENAME_0=$(basename $0)

usage() {
 echo ""                                                                                               >&2
 echo "$BASENAME_0 options:"                                                                           >&2
 echo ""                                                                                               >&2
 echo "  -C run 'git clean -dfx first"                                                                 >&2
 echo "  -H run 'git reset --hard origin/<branchname>' first"                                          >&2
 echo "  -p run 'git pull --ff-only' first"                                                            >&2
 echo "  -c include 'clean' goal"                                                                      >&2
 echo "  -O do NOT add '-o' (offline) flag, ie bring down any new dependencies"                        >&2
 echo "  -t skip tests"                                                                                >&2
 echo "  -l single threaded, do NOT add '-T1C' flag"                                                   >&2
 echo "  -q build with telemetry (run jaeger.sh first)"                                                >&2
 echo "  -k use 'package' rather than 'install'.  Does not run integ tests.  Cannot combine with '-y'" >&2
 echo "  -y use 'verify' rather than 'install'.  Cannot combine with '-k'"                             >&2
 echo "  -a append '-Dmodule-all'.  Cannot combine with '-I' or '-K'"                                  >&2
 echo "  -K append '-Dmodule-all-except-kroviz'.  Cannot combine with '-a' or '-I'"                    >&2
 echo "  -I append '-Dmodule-all-except-incubator'.  Cannot combine with '-a' or '-K'"                 >&2
 echo "  -d use mvnd rather than mvn (requires: mvndaemon)"                                            >&2
 echo "  -n serve up timeline afterwards (prereq: npm i -g serve)"                                     >&2
 echo "  -F do NOT search for Failures and Errors at the end"                                          >&2
 echo "  -S do NOT print summary or last 50 lines at the end"                                          >&2
 echo "  -w whatif - don't run the command but do print it out.  Implies -v (verbose)"                 >&2
 echo "  -v verbose"                                                                                   >&2
 echo "  -e edit log file at end.  Cannot combine with '-v'"                                           >&2
 echo ""                                                                                               >&2
 echo ""                                                                                               >&2
 echo "example usage:"                                                                                 >&2
 echo ""                                                                                               >&2
 echo "sh build.sh -tOd           # no tests, no offline, use mvnd, released only"                     >&2
 echo "sh build.sh -ptOdI         # pull, no tests, no offline, use mvnd, no incubator"                >&2
 echo "sh build.sh -pctOvI        # pull, clean, no tests, no offline, verbose, no incubator"          >&2
 echo "sh build.sh -CHOdI         # fetch, clean and reset, no offline, use mvnd, no incubator"         >&2
 echo "sh build.sh -qOdI          # telemetry, no offline, use mvnd, no incubator"         >&2
 echo ""                                                                                               >&2
}

GIT_CLEAN_HARD=false
GIT_RESET_HARD=false
GIT_PULL=false
CLEAN=false
SKIP_TESTS=false
SKIP_OFFLINE=false
PACKAGE_ONLY=false
VERIFY_ONLY=false
TELEMETRY=false
TIMELINE=false
WHATIF=false
SINGLE_THREADED=false
ALL=false
ALL_EXCEPT_KROVIZ=false
ALL_EXCEPT_INCUBATOR=false
USE_MVND=false
SKIP_SEARCH_FOR_FAILURES=false
SKIP_SUMMARY=false
EDIT=false
VERBOSE=false

MVN_LOG=/tmp/$BASENAME_0.$$.log

while getopts 'CHprcntlqkyaIKOdFSwveh' opt
do
  case $opt in
    C) GIT_CLEAN_HARD=true ;;
    H) GIT_RESET_HARD=true ;;
    p) GIT_PULL=true ;;
    c) CLEAN=true ;;
    O) SKIP_OFFLINE=true ;;
    l) SINGLE_THREADED=true ;;
    q) TELEMETRY=true ;;
    t) SKIP_TESTS=true ;;
    k) PACKAGE_ONLY=true ;;
    y) VERIFY_ONLY=true ;;
    a) ALL=true ;;
    I) ALL_EXCEPT_INCUBATOR=true ;;
    K) ALL_EXCEPT_KROVIZ=true ;;
    n) TIMELINE=true ;;
    d) USE_MVND=true ;;
    F) SKIP_SEARCH_FOR_FAILURES=true ;;
    S) SKIP_SUMMARY=true ;;
    w) WHATIF=true ;;
    v) VERBOSE=true ;;
    e) EDIT=true ;;
    h) usage
       exit 1
       ;;
    *) echo "unknown option $opt - aborting" >&2
       usage
       exit 1
      ;;
  esac
done

shift $((OPTIND-1))

echo ""

if [ "$VERBOSE" = "true" ]; then
  echo "-C (git clean -dfx)                          : $GIT_CLEAN_HARD"
  echo "-H (git reset --hard)                        : $GIT_RESET_HARD"
  echo "-p (git pull --ff-only)                      : $GIT_PULL"
  echo "-c mvn clean                                 : $CLEAN"
  echo "-t skip all tests (mvn -DskipTests)          : $SKIP_TESTS"
  echo "-O not mvn --offline                         : $SKIP_OFFLINE"
  echo "-l not mvn -T1C                              : $SINGLE_THREADED"
  echo "-q with otel telemetry (run jaeger.sh first) : $TELEMETRY"
  echo "-k mvn package                               : $PACKAGE_ONLY"
  echo "-y mvn verify                                : $VERIFY_ONLY"
  echo "-a include ALL modules                       : $ALL"
  echo "-I include all modules except incubator      : $ALL_EXCEPT_INCUBATOR"
  echo "-K include all modules except kroviz         : $ALL_EXCEPT_KROVIZ"
  echo "-d use mvnd rather than mvn                  : $USE_MVND"
  echo "-n serve up timeline                         : $TIMELINE"
  echo "-F skip search for failures                  : $SKIP_SEARCH_FOR_FAILURES"
  echo "-S skip summary                              : $SKIP_SUMMARY"
  echo "-w what-if                                   : $WHATIF"
  echo "-v verbose                                   : $VERBOSE"
  echo ""
fi

if [ "$PACKAGE_ONLY" = "true" ] && [ "$VERIFY_ONLY" = "true" ]; then
  echo "$BASENAME_0 : cannot use '-y' and '-k' flags together"  >&2
  usage
  exit 1
fi

if [ "$ALL" = "true" ] && [ "$ALL_EXCEPT_INCUBATOR" = "true" ]; then
  echo "$BASENAME_0 : cannot use '-a' and '-I' flags together"  >&2
  usage
  exit 1
fi

if [ "$ALL" = "true" ] && [ "$ALL_EXCEPT_KROVIZ" = "true" ]; then
  echo "$BASENAME_0 : cannot use '-a' and '-K' flags together"  >&2
  usage
  exit 1
fi

if [ "$ALL_EXCEPT_INCUBATOR" = "true" ] && [ "$ALL_EXCEPT_KROVIZ" = "true" ]; then
  echo "$BASENAME_0 : cannot use '-I' and '-K' flags together"  >&2
  usage
  exit 1
fi

if [ "$VERBOSE" = "true" ] && [ "$EDIT" = "true" ]; then
  echo "$BASENAME_0 : cannot use '-v' and '-e' flags together"  >&2
  usage
  exit 1
fi


OPTS=""

if [ "$CLEAN" = "true" ]; then
  OPTS="$OPTS clean"
fi

if [ "$SKIP_TESTS" = "true" ]; then
  OPTS="$OPTS -DskipTests=true"
fi

if [ "$TELEMETRY" = "true" ]; then
  OPTS="$OPTS -Dtelemetry -Dotel.traces.exporter=otlp"
fi

if [ "$ALL" = "true" ]; then
  OPTS="$OPTS -Dmodule-all"
fi

if [ "$ALL_EXCEPT_INCUBATOR" = "true" ]; then
  OPTS="$OPTS -Dmodule-all-except-incubator"
fi

if [ "$ALL_EXCEPT_KROVIZ" = "true" ]; then
  OPTS="$OPTS -Dmodule-all-except-kroviz"
fi

if [ "$SKIP_OFFLINE" = "false" ]; then
  OPTS="$OPTS -o"
fi

if [ "$SINGLE_THREADED" = "false" ]; then
  OPTS="$OPTS -T1C"
fi

if [ "$PACKAGE_ONLY" = "true" ]; then
  OPTS="$OPTS package"
else
  if [ "$VERIFY_ONLY" = "true" ]; then
    OPTS="$OPTS verify"
  else
    OPTS="$OPTS install"
  fi
fi

if [ "$USE_MVND" = "true" ]; then
  CMD="mvnd"
else
  CMD="mvn"
fi

CURRENT_BRANCH=$(git branch --show-current)

if [ "$WHATIF" = "true" ]; then

  echo git fetch

  if [ "$GIT_CLEAN_HARD" = "true" ]; then
    echo git clean -dfx
  fi

  if [ "$GIT_RESET_HARD" = "true" ]; then
    echo git reset --hard origin/$CURRENT_BRANCH
  fi

  if [ "$GIT_PULL" = "true" ]; then
    echo git pull --ff-only
  fi

  if [ "$VERBOSE" = "true" ]; then
    echo "$CMD $OPTS $* 2>&1 | tee $MVN_LOG "
  else
    OPTS="$OPTS --log-file $MVN_LOG"
    echo $CMD $OPTS "$@"
  fi


  if [ "$SKIP_SEARCH_FOR_FAILURES" = "false" ]; then
    echo "... grep for failures/errors"
  fi

  if [ "$SKIP_SUMMARY" = "false" ]; then
    echo "... print summary"
  fi

  if [ "$TIMELINE" = "true" ]; then
    echo "serve -d target/timeline"
  fi

  echo ""
else

  git fetch
  if [ $? -ne 0 ]; then
    echo "git fetch failed; aborting" >&2
    exit 1
  fi

  if [ "$GIT_CLEAN_HARD" = "true" ]; then
    git clean -dfx
    if [ $? -ne 0 ]; then
      echo "git clean -dfx failed; aborting" >&2
      exit 1
    fi
  fi

  if [ "$GIT_RESET_HARD" = "true" ]; then
    git reset --hard origin/$CURRENT_BRANCH
    if [ $? -ne 0 ]; then
      echo "git reset --hard origin/$CURRENT_BRANCH failed; aborting" >&2
      exit 1
    fi
  fi

  if [ "$GIT_PULL" = "true" ]; then
    git pull --ff-only
    if [ $? -ne 0 ]; then
      echo "git pull --ff-only failed; aborting" >&2
      exit 1
    fi
  fi

  if [ "$VERBOSE" = "true" ]; then
    echo "$CMD $OPTS $* 2>&1 | tee $MVN_LOG"
    $CMD $OPTS "$@" 2>&1 | tee $MVN_LOG
  else
    OPTS="$OPTS --log-file $MVN_LOG"
    echo "$CMD $OPTS $*"
    $CMD $OPTS "$@"
  fi

  if [ "$SKIP_SEARCH_FOR_FAILURES" = "false" ]; then
    if [ "$VERBOSE" = "true" ]; then
      echo "searching for failures and errors (in $MVN_LOG) ..."
    fi
    grep -in -E 'Failures:\s[1-9]+, Errors: [0-9]+, Skipped: [0-9]+$' -B 20 $MVN_LOG
    grep -in -E 'Failures:\s[0-9]+, Errors: [1-9]+, Skipped: [0-9]+$' -B 20 $MVN_LOG
  else
    if [ "$VERBOSE" = "true" ]; then
      echo "NOT searching for failures and errors"
    fi
  fi

  if [ "$SKIP_SUMMARY" = "false" ]; then
    if [ "$VERBOSE" = "true" ]; then
      echo "printing summary..."
    fi
    if grep -n "Segment walltime" $MVN_LOG ; then
      if [ "$VERBOSE" = "true" ]; then
        echo "found 'Segment walltime' ......"
      fi
      tail -n +$(grep -n "Segment walltime" $MVN_LOG | cut -f1 -d:) $MVN_LOG
    else
      if [ "$VERBOSE" = "true" ]; then
        echo "did NOT find 'Segment walltime', so printing last 120 lines of log"
      fi
      tail -n 120 $MVN_LOG
    fi
  else
    if [ "$VERBOSE" = "true" ]; then
      echo "NOT printing any summary"
    fi
  fi

  if [ "$EDIT" = "true" ]; then
    vi $MVN_LOG
  fi

  if [ "$TIMELINE" = "true" ]; then

    ## when we eventually target only jdk18 and above, then can replace with:
    ## jwebserver -d $(pwd)/target/timeline -b ::

    serve -d target/timeline
  fi

fi


