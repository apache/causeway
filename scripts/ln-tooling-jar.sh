mkdir -p tooling/cli/target
pushd tooling/cli/target
rm -f causewaystuff-tooling-cli.jar
ln -s $HOME/.m2/repository/io/github/causewaystuff/causewaystuff-tooling-cli/1.0.0-SNAPSHOT/causewaystuff-tooling-cli-1.0.0-SNAPSHOT-spring-boot.jar causewaystuff-tooling-cli.jar
popd
