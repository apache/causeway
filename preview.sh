#!/usr/bin/env bash
rm -rf public
rm -rf _docs/.cache
antora generate site.yml --cache-dir _docs/.cache
serve public
