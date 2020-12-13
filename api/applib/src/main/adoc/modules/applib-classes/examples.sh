#!/bin/bash
#  Licensed to the Apache Software Foundation (ASF) under one
#  or more contributor license agreements.  See the NOTICE file
#  distributed with this work for additional information
#  regarding copyright ownership.  The ASF licenses this file
#  to you under the Apache License, Version 2.0 (the
#  "License"); you may not use this file except in compliance
#  with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing,
#  software distributed under the License is distributed on an
#  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#  KIND, either express or implied.  See the License for the
#  specific language governing permissions and limitations
#  under the License.

SRC_MAIN_JAVA=../../../java
SCRIPT_DIR=$( dirname "$0" )
cd $SCRIPT_DIR || exit 1

echo "==================="
echo "= MIGRATION NOTES ="
echo "==================="
echo "the java file examples for antora were migrated to use the global document index" 
echo "hence this script is a no-op"
exit 0


SRC_APPLIB=$SRC_MAIN_JAVA/org/apache/isis/applib

for dir in clock domain events layout mixins security spec graph util value
do
  rm -rf examples/$dir
  mkdir -p examples/$dir
  cp -R $SRC_APPLIB/$dir/* examples/$dir
done
cp $SRC_APPLIB/ViewModel.java examples/.
