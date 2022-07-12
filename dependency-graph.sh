#!/usr/bin/env bash

GRAPHFORMAT=dot
#GRAPHFORMAT=puml
#GRAPHFORMAT=text
mvn com.github.ferstl:depgraph-maven-plugin:aggregate -DgraphFormat=$GRAPHFORMAT

echo "generated:"
echo "target/dependency-graph.dot"
echo "target/dependency-graph.png"