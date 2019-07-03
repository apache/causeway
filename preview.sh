#!/usr/bin/env bash
rm -rf antora/target/site && antora site.yml $@ && serve antora/target/site

#rm -rf antora/target/site
#antora site.yml $@
##cp -R antora/hp-overlay
#serve antora/target/site
