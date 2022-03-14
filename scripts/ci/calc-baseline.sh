#!/bin/bash

echo ""
echo ""
echo ""

BASELINE=$(curl -k "https://search.maven.org/solrsearch/select?q=g:org.apache.isis.core+AND+a:isis-applib&rows=1&wt=json" | jq -r ".response.docs[0].latestVersion")

echo "##[set-output name=baseline;]${BASELINE}"
