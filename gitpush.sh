USAGE="$(basename $0): [-q] message"

QUICK=""

while getopts "qh" arg; do
  case $arg in
    h)
      echo $USAGE
      exit 0
      ;;
    q)
      QUICK="[quick]"
      shift
      ;;
    *)
      echo $USAGE >&2
      exit 1
  esac
done

if [ $# -lt 1 ];
then
  echo $USAGE >&2
  exit 1
fi

ISSUE=$(git rev-parse --abbrev-ref HEAD | cut -d- -f1,2)
MSG=$*

echo "ISSUE : $ISSUE"
echo "MSG   : $MSG"
echo "QUICK : $QUICK"

pushd _pipeline-resources || exit
git add .
git commit -m "$ISSUE: $MSG $QUICK"
git push
popd || exit

git add .
git commit -m "$ISSUE: $MSG $QUICK"
git push
