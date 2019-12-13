#!/bin/bash
sh scripts/ci/_adoc-copy-examples.sh
sh scripts/ci/_adoc-gen-config.sh

echo
echo

git status --porcelain
