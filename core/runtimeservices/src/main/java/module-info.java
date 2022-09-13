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
module org.apache.isis.core.runtimeservices {
    exports org.apache.isis.core.runtimeservices;
    exports org.apache.isis.core.runtimeservices.bookmarks;
    exports org.apache.isis.core.runtimeservices.command;
    exports org.apache.isis.core.runtimeservices.email;
    exports org.apache.isis.core.runtimeservices.eventbus;
    exports org.apache.isis.core.runtimeservices.executor;
    exports org.apache.isis.core.runtimeservices.factory;
    exports org.apache.isis.core.runtimeservices.homepage;
    exports org.apache.isis.core.runtimeservices.i18n.po;
    exports org.apache.isis.core.runtimeservices.icons;
    exports org.apache.isis.core.runtimeservices.interaction;
    exports org.apache.isis.core.runtimeservices.jaxb;
    exports org.apache.isis.core.runtimeservices.locale;
    exports org.apache.isis.core.runtimeservices.menubars;
    exports org.apache.isis.core.runtimeservices.menubars.bootstrap;
    exports org.apache.isis.core.runtimeservices.message;
    exports org.apache.isis.core.runtimeservices.placeholder;
    exports org.apache.isis.core.runtimeservices.publish;
    exports org.apache.isis.core.runtimeservices.recognizer;
    exports org.apache.isis.core.runtimeservices.recognizer.dae;
    exports org.apache.isis.core.runtimeservices.repository;
    exports org.apache.isis.core.runtimeservices.routing;
    exports org.apache.isis.core.runtimeservices.scratchpad;
    exports org.apache.isis.core.runtimeservices.serializing;
    exports org.apache.isis.core.runtimeservices.session;
    exports org.apache.isis.core.runtimeservices.sitemap;
    exports org.apache.isis.core.runtimeservices.spring;
    exports org.apache.isis.core.runtimeservices.transaction;
    exports org.apache.isis.core.runtimeservices.urlencoding;
    exports org.apache.isis.core.runtimeservices.user;
    exports org.apache.isis.core.runtimeservices.userreg;
    exports org.apache.isis.core.runtimeservices.wrapper;
    exports org.apache.isis.core.runtimeservices.wrapper.dispatchers;
    exports org.apache.isis.core.runtimeservices.wrapper.handlers;
    exports org.apache.isis.core.runtimeservices.wrapper.proxy;
    exports org.apache.isis.core.runtimeservices.xml;
    exports org.apache.isis.core.runtimeservices.xmlsnapshot;

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
    requires org.apache.isis.applib;
    requires org.apache.isis.commons;
    requires org.apache.isis.core.config;
    requires org.apache.isis.core.interaction;
    requires org.apache.isis.core.metamodel;
    requires org.apache.isis.core.runtime;
    requires org.apache.isis.core.transaction;
    requires org.apache.isis.schema;
    requires org.apache.isis.security.api;
    requires org.apache.logging.log4j;
    requires spring.beans;
    requires spring.context;
    requires spring.core;
    requires spring.tx;
    requires org.apache.isis.core.codegen.bytebuddy;

    opens org.apache.isis.core.runtimeservices.wrapper;
    opens org.apache.isis.core.runtimeservices.wrapper.proxy; //to org.apache.isis.core.codegen.bytebuddy
}