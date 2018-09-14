#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
FROM tomcat:9.0.10-jre8-alpine

RUN rm -rf ${CATALINA_HOME}/webapps/examples
RUN rm -rf ${CATALINA_HOME}/webapps/docs
RUN rm -rf ${CATALINA_HOME}/webapps/ROOT

RUN mkdir -p ${CATALINA_HOME}/conf/Catalina/localhost

COPY ${docker-plugin.resource.tomcat-users.xml} ${CATALINA_HOME}/conf/.
COPY ${docker-plugin.resource.catalina.properties} ${CATALINA_HOME}/conf/.
# RUN sed -i 's|shared.loader=$|shared.loader="${catalina.base}/isis/lib","${catalina.base}/isis/lib/*.jar"|g' ${CATALINA_HOME}/conf/catalina.properties

COPY ${docker-plugin.resource.manager.xml} ${CATALINA_HOME}/conf/Catalina/localhost/.
COPY ${docker-plugin.resource.host-manager.xml} ${CATALINA_HOME}/conf/Catalina/localhost/.

RUN mkdir -p ${CATALINA_HOME}/isis/lib
COPY ${docker-plugin.resource.zip} ${CATALINA_HOME}/isis/${assembly-plugin.finalName}.zip
RUN unzip ${CATALINA_HOME}/isis/${assembly-plugin.finalName}.zip -d ${CATALINA_HOME}/isis/.

EXPOSE 8080