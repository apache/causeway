#!/usr/bin/env bash
mvn compile -Dskip.pdf -o && ruby monitor.rb -b
