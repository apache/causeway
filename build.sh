#!/usr/bin/env bash

mvnd -s _pipeline-resources/build/deployable/.m2/settings.xml install $*
