#!/usr/bin/env bash
FILE=site.yml
if [[ $# -gt 0 ]]; then
  FILE=antora/playbooks/site-$1.yml
  shift
fi

echo "copying over examples ..."
for examples_sh in $(find . -name examples.sh -print)
do
  echo $examples_sh
  sh $examples_sh
done


echo "generating config docs ..."
rm -rf core/config/src/main/doc/modules/config/examples/generated \
&& groovy scripts/generateConfigDocs -f "./core/config/target/classes/META-INF/spring-configuration-metadata.json" -o ./core/config/src/main/doc/modules/config/examples/generated


echo "generating site ..."
rm -rf antora/target/site \
&& antora $FILE $*


echo "serving ..."
serve antora/target/site
