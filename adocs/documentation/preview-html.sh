#!/usr/bin/env bash
mvn compile -o && python -m webbrowser -t http://localhost:4000/downloads.html && pushd target/site && python -m http.server 4000 && popd
#mvn compile -Dreindex -o && python -m webbrowser -t http://localhost:4000 && pushd target/site && python -m http.server 4000 && popd
