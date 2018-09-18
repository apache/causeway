#!/usr/bin/env bash
mvn compile  $* && python -m webbrowser -t http://localhost:4000 && pushd target/site && python -m http.server 4000 && popd
#mvn compile -Dreindex -o && python -m webbrowser -t http://localhost:4000 && pushd target/site && python -m http.server 4000 && popd

#mvn compile -o && python -m webbrowser -t http://localhost:4000/guides/ugvw/ugvw.html && pushd target/site && python -m http.server 4000 && popd
#mvn compile -o -Dreindex && python -m webbrowser -t http://localhost:4000/guides/ugvw/ugvw.html && pushd target/site && python -m http.server 4000 && popd
