# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
# 
#      http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


# 
# Intended to be sourced from a UNIX shell, eg:
#
# . /path/to/isis/root/bash_aliases
# 
#

alias mcia="mvn clean install -D modules=all"
alias mci="mvn clean install -D modules=standard"

alias msdf="mvn site-deploy -D modules=standard -D site=full -D deploy=local"
alias msd="mvn site-deploy -D modules=standard -D deploy=local"

alias mssdf="mvn site-deploy -D modules=site -D site=full -D deploy=local"
alias mssd="mvn site-deploy -D modules=site -D deploy=local"

alias svnst="svn status --ignore-externals | grep -v ^X"
alias svnup="svn update --ignore-externals | grep -v ^X"

alias asfrat="java -jar apache-rat-0.8-SNAPSHOT.jar -d . -exclude .project .classpath .settings dtd-4.5 fop-cust.xsl html-cust.xsl .ucd .java.hsp target MANIFEST.MF .launch .ent .dtd"
