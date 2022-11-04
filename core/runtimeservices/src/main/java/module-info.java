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
module org.apache.causeway.core.runtimeservices {
    exports org.apache.causeway.core.runtimeservices;
    exports org.apache.causeway.core.runtimeservices.bookmarks;
    exports org.apache.causeway.core.runtimeservices.command;
    exports org.apache.causeway.core.runtimeservices.email;
    exports org.apache.causeway.core.runtimeservices.eventbus;
    exports org.apache.causeway.core.runtimeservices.executor;
    exports org.apache.causeway.core.runtimeservices.factory;
    exports org.apache.causeway.core.runtimeservices.homepage;
    exports org.apache.causeway.core.runtimeservices.i18n.po;
    exports org.apache.causeway.core.runtimeservices.icons;
    exports org.apache.causeway.core.runtimeservices.interaction;
    exports org.apache.causeway.core.runtimeservices.jaxb;
    exports org.apache.causeway.core.runtimeservices.locale;
    exports org.apache.causeway.core.runtimeservices.menubars.bootstrap;
    exports org.apache.causeway.core.runtimeservices.message;
    exports org.apache.causeway.core.runtimeservices.placeholder;
    exports org.apache.causeway.core.runtimeservices.publish;
    exports org.apache.causeway.core.runtimeservices.recognizer;
    exports org.apache.causeway.core.runtimeservices.recognizer.dae;
    exports org.apache.causeway.core.runtimeservices.repository;
    exports org.apache.causeway.core.runtimeservices.routing;
    exports org.apache.causeway.core.runtimeservices.scratchpad;
    exports org.apache.causeway.core.runtimeservices.serializing;
    exports org.apache.causeway.core.runtimeservices.session;
    exports org.apache.causeway.core.runtimeservices.sitemap;
    exports org.apache.causeway.core.runtimeservices.spring;
    exports org.apache.causeway.core.runtimeservices.transaction;
    exports org.apache.causeway.core.runtimeservices.urlencoding;
    exports org.apache.causeway.core.runtimeservices.user;
    exports org.apache.causeway.core.runtimeservices.userreg;
    exports org.apache.causeway.core.runtimeservices.wrapper;
    exports org.apache.causeway.core.runtimeservices.wrapper.dispatchers;
    exports org.apache.causeway.core.runtimeservices.wrapper.handlers;
    exports org.apache.causeway.core.runtimeservices.wrapper.proxy;
    exports org.apache.causeway.core.runtimeservices.xml;
    exports org.apache.causeway.core.runtimeservices.xmlsnapshot;

    requires commons.email;
    requires jakarta.activation;
    requires jakarta.mail;
    requires java.annotation;
    requires java.desktop;
    requires java.sql;
    requires java.xml;
    requires java.xml.bind;
    requires java.inject;
    requires lombok;
    requires org.apache.causeway.applib;
    requires org.apache.causeway.commons;
    requires org.apache.causeway.core.config;
    requires org.apache.causeway.core.interaction;
    requires org.apache.causeway.core.metamodel;
    requires org.apache.causeway.core.runtime;
    requires org.apache.causeway.core.transaction;
    requires org.apache.causeway.schema;
    requires org.apache.causeway.security.api;
    requires org.apache.logging.log4j;
    requires spring.beans;
    requires spring.context;
    requires spring.core;
    requires spring.tx;
    requires org.apache.causeway.core.codegen.bytebuddy;

    opens org.apache.causeway.core.runtimeservices.wrapper;
    opens org.apache.causeway.core.runtimeservices.wrapper.proxy; //to org.apache.causeway.core.codegen.bytebuddy
}