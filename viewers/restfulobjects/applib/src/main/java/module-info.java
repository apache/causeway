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
module org.apache.causeway.viewer.restfulobjects.applib {
    exports org.apache.causeway.viewer.restfulobjects.applib.homepage;
    exports org.apache.causeway.viewer.restfulobjects.applib.errors;
    exports org.apache.causeway.viewer.restfulobjects.applib.util;
    exports org.apache.causeway.viewer.restfulobjects.applib.domaintypes;
    exports org.apache.causeway.viewer.restfulobjects.applib.domainobjects;
    exports org.apache.causeway.viewer.restfulobjects.applib.version;
    exports org.apache.causeway.viewer.restfulobjects.applib.boot;
    exports org.apache.causeway.viewer.restfulobjects.applib;
    exports org.apache.causeway.viewer.restfulobjects.applib.health;
    exports org.apache.causeway.viewer.restfulobjects.applib.menubars;
    exports org.apache.causeway.viewer.restfulobjects.applib.user;
    exports org.apache.causeway.viewer.restfulobjects.applib.dtos;

    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires java.ws.rs;
    requires lombok;
    requires org.apache.causeway.applib;
    requires org.apache.causeway.commons;
    requires org.joda.time;
    requires spring.context;
    requires spring.core;
}