#!/usr/bin/env bash
FILE=site.yml
if [[ $# -gt 0 ]]; then
  FILE=site-$1.yml
  shift
fi

#echo $FILE

rm -rf antora/target/site \
&& rm -rf core/config/_adoc/modules/config/examples/generated \
&& groovy scripts/generateConfigDocs -f "./core/config/target/classes/META-INF/spring-configuration-metadata.json" -o ./core/config/src/main/doc/modules/config/examples/generated \
&& antora $FILE $* \
&& serve antora/target/site
