# assumptions
#
# - github/apache-causeway-committers/causewaystuff/
# - github/apache/causeway/main                          # main worktree
#
# prereqs:
#
# - build causeway itself
#   # build.sh -tOdI
#
# - build causewaystuff
#   # pushd ../../../apache-causeway-committers/causewaystuff
#   # mvnd clean install
#   # popd
#
# - symlink
#
#   # mkdir -p tooling/cli/target
#   # ln -s ../../../apache-causeway-committers/causewaystuff/tooling/cli/target/causeway-tooling-cli.jar tooling/cli/target/causewaystuff-tooling-cli.jar

