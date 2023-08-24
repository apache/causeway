/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
module org.apache.causeway.viewer.restfulobjects.client {
    exports org.apache.causeway.viewer.restfulobjects.client.log;
    exports org.apache.causeway.viewer.restfulobjects.client;
    exports org.apache.causeway.viewer.restfulobjects.client.auth;
    exports org.apache.causeway.viewer.restfulobjects.client.auth.basic;
    exports org.apache.causeway.viewer.restfulobjects.client.auth.oauth2;
    exports org.apache.causeway.viewer.restfulobjects.client.auth.oauth2.azure;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires java.annotation;
    requires java.ws.rs;
    requires java.xml.bind;
    requires lombok;
    requires transitive org.apache.causeway.applib;
    requires transitive org.apache.causeway.commons;
    requires transitive org.apache.causeway.viewer.restfulobjects.applib;
    requires org.apache.logging.log4j;
    requires spring.core;
    requires org.apache.causeway.schema;
    requires org.slf4j;
}