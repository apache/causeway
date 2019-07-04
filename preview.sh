#!/usr/bin/env bash
rm -rf antora/target/site \
&& antora site.yml $@ \
&& serve --local antora/target/site
