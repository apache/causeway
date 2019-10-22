#!/usr/bin/env bash
rm -rf antora/target/site \
&& rm -rf core/config/_adoc/modules/cfg/examples/generated \
&& groovy scripts/generateConfigDocs -f "./core/config/target/classes/META-INF/spring-configuration-metadata.json" -o ./core/config/_adoc/modules/cfg/examples/generated \
&& antora site.yml $@ \
&& serve antora/target/site
