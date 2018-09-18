#!/usr/bin/env bash
mvn compile -f pom-pdf.xml && mvn compile -o && python -m webbrowser -t http://localhost:4000 && pushd target/site && python -m http.server 4000 && popd
