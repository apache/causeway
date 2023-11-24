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
module org.apache.isis.viewer.restfulobjects.viewer {
    exports org.apache.isis.viewer.restfulobjects.viewer.jaxrsapp;
    exports org.apache.isis.viewer.restfulobjects.viewer.webmodule;
    exports org.apache.isis.viewer.restfulobjects.viewer.context;
    exports org.apache.isis.viewer.restfulobjects.viewer;
    exports org.apache.isis.viewer.restfulobjects.viewer.webmodule.auth;
    exports org.apache.isis.viewer.restfulobjects.viewer.resources;
    exports org.apache.isis.viewer.restfulobjects.viewer.util;
    exports org.apache.isis.viewer.restfulobjects.viewer.mappers.entity;
    exports org.apache.isis.viewer.restfulobjects.viewer.mappers;
    exports org.apache.isis.viewer.restfulobjects.viewer.resources.serialization;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires java.annotation;
    requires java.inject;
    requires java.transaction;
    requires java.ws.rs;
    requires java.xml.bind;
    requires javax.servlet.api;
    requires lombok;
    requires org.apache.isis.applib;
    requires org.apache.isis.commons;
    requires org.apache.isis.core.config;
    requires org.apache.isis.core.metamodel;
    requires org.apache.isis.core.webapp;
    requires org.apache.isis.security.api;
    requires org.apache.isis.viewer.commons.applib;
    requires org.apache.isis.viewer.commons.services;
    requires org.apache.isis.viewer.restfulobjects.applib;
    requires org.apache.isis.viewer.restfulobjects.rendering;
    requires org.apache.logging.log4j;
    requires spring.beans;
    requires spring.context;
    requires spring.core;
    requires spring.web;
}
