#!/usr/bin/env bash
mvn compile -o && python -m webbrowser -t http://localhost:8000 && pushd target/site && python -m http.server 8000 && popd
