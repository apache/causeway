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
module org.apache.causeway.viewer.restfulobjects.rendering {
    exports org.apache.causeway.viewer.restfulobjects.rendering.service.conneg;
    exports org.apache.causeway.viewer.restfulobjects.rendering.service.swagger;
    exports org.apache.causeway.viewer.restfulobjects.rendering.service.valuerender;
    exports org.apache.causeway.viewer.restfulobjects.rendering.util;
    exports org.apache.causeway.viewer.restfulobjects.rendering.domainobjects;
    exports org.apache.causeway.viewer.restfulobjects.rendering;
    exports org.apache.causeway.viewer.restfulobjects.rendering.service.swagger.internal;
    exports org.apache.causeway.viewer.restfulobjects.rendering.domaintypes;
    exports org.apache.causeway.viewer.restfulobjects.rendering.service.acceptheader;
    exports org.apache.causeway.viewer.restfulobjects.rendering.service;

    requires org.apache.causeway.viewer.restfulobjects.applib;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires jakarta.activation;
    requires java.annotation;
    requires java.inject;
    requires java.sql;
    requires java.ws.rs;
    requires java.xml.bind;
    requires lombok;
    requires org.apache.causeway.applib;
    requires org.apache.causeway.commons;
    requires org.apache.causeway.core.config;
    requires org.apache.causeway.core.metamodel;
    requires org.apache.causeway.core.runtime;
    requires org.apache.causeway.schema;
    requires org.apache.logging.log4j;
    requires org.joda.time;
    requires spring.beans;
    requires spring.context;
    requires spring.core;
    requires io.swagger.v3.oas.models;
    requires io.swagger.v3.core;
}