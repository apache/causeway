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
module org.apache.isis.applib {
    exports org.apache.isis.applib;
    exports org.apache.isis.applib.annotation;
    exports org.apache.isis.applib.client;
    exports org.apache.isis.applib.clock;
    exports org.apache.isis.applib.domain;
    exports org.apache.isis.applib.events.domain;
    exports org.apache.isis.applib.events.lifecycle;
    exports org.apache.isis.applib.events.metamodel;
    exports org.apache.isis.applib.events.ui;
    exports org.apache.isis.applib.events;
    exports org.apache.isis.applib.exceptions.recoverable;
    exports org.apache.isis.applib.exceptions.unrecoverable;
    exports org.apache.isis.applib.exceptions;
    exports org.apache.isis.applib.graph.tree;
    exports org.apache.isis.applib.graph;
    exports org.apache.isis.applib.id;
    exports org.apache.isis.applib.jaxb;
    exports org.apache.isis.applib.layout.component;
    exports org.apache.isis.applib.layout.grid.bootstrap;
    exports org.apache.isis.applib.layout.grid;
    exports org.apache.isis.applib.layout.links;
    exports org.apache.isis.applib.layout.menubars.bootstrap;
    exports org.apache.isis.applib.layout.menubars;
    exports org.apache.isis.applib.layout;
    exports org.apache.isis.applib.locale;
    exports org.apache.isis.applib.mixins.dto;
    exports org.apache.isis.applib.mixins.layout;
    exports org.apache.isis.applib.mixins.metamodel;
    exports org.apache.isis.applib.mixins.rest;
    exports org.apache.isis.applib.mixins.security;
    exports org.apache.isis.applib.mixins.system;
    exports org.apache.isis.applib.mixins.updates;
    exports org.apache.isis.applib.query;
    exports org.apache.isis.applib.services.acceptheader;
    exports org.apache.isis.applib.services.appfeat;
    exports org.apache.isis.applib.services.appfeatui;
    exports org.apache.isis.applib.services.bookmark.idstringifiers;
    exports org.apache.isis.applib.services.bookmark;
    exports org.apache.isis.applib.services.bookmarkui;
    exports org.apache.isis.applib.services.clock;
    exports org.apache.isis.applib.services.command;
    exports org.apache.isis.applib.services.commanddto.conmap;
    exports org.apache.isis.applib.services.commanddto.processor.spi;
    exports org.apache.isis.applib.services.commanddto.processor;
    exports org.apache.isis.applib.services.commanddto;
    exports org.apache.isis.applib.services.confview;
    exports org.apache.isis.applib.services.conmap;
    exports org.apache.isis.applib.services.email;
    exports org.apache.isis.applib.services.error;
    exports org.apache.isis.applib.services.eventbus;
    exports org.apache.isis.applib.services.exceprecog;
    exports org.apache.isis.applib.services.factory;
    exports org.apache.isis.applib.services.grid;
    exports org.apache.isis.applib.services.health;
    exports org.apache.isis.applib.services.hint;
    exports org.apache.isis.applib.services.homepage;
    exports org.apache.isis.applib.services.i18n;
    exports org.apache.isis.applib.services.iactn;
    exports org.apache.isis.applib.services.iactnlayer;
    exports org.apache.isis.applib.services.inject;
    exports org.apache.isis.applib.services.jaxb;
    exports org.apache.isis.applib.services.keyvaluestore;
    exports org.apache.isis.applib.services.layout;
    exports org.apache.isis.applib.services.linking;
    exports org.apache.isis.applib.services.locale;
    exports org.apache.isis.applib.services.menu;
    exports org.apache.isis.applib.services.message;
    exports org.apache.isis.applib.services.metamodel;
    exports org.apache.isis.applib.services.metrics;
    exports org.apache.isis.applib.services.placeholder;
    exports org.apache.isis.applib.services.publishing.log;
    exports org.apache.isis.applib.services.publishing.spi;
    exports org.apache.isis.applib.services.queryresultscache;
    exports org.apache.isis.applib.services.registry;
    exports org.apache.isis.applib.services.repository;
    exports org.apache.isis.applib.services.routing;
    exports org.apache.isis.applib.services.scratchpad;
    exports org.apache.isis.applib.services.session;
    exports org.apache.isis.applib.services.sitemap;
    exports org.apache.isis.applib.services.sudo;
    exports org.apache.isis.applib.services.swagger;
    exports org.apache.isis.applib.services.tablecol;
    exports org.apache.isis.applib.services.title;
    exports org.apache.isis.applib.services.urlencoding;
    exports org.apache.isis.applib.services.user;
    exports org.apache.isis.applib.services.userreg.events;
    exports org.apache.isis.applib.services.userreg;
    exports org.apache.isis.applib.services.userui;
    exports org.apache.isis.applib.services.wrapper.callable;
    exports org.apache.isis.applib.services.wrapper.control;
    exports org.apache.isis.applib.services.wrapper.events;
    exports org.apache.isis.applib.services.wrapper.listeners;
    exports org.apache.isis.applib.services.wrapper;
    exports org.apache.isis.applib.services.xactn;
    exports org.apache.isis.applib.services.xml;
    exports org.apache.isis.applib.services.xmlsnapshot;
    exports org.apache.isis.applib.snapshot;
    exports org.apache.isis.applib.spec;
    exports org.apache.isis.applib.types;
    exports org.apache.isis.applib.util.schema;
    exports org.apache.isis.applib.util;
    exports org.apache.isis.applib.value;
    exports org.apache.isis.applib.value.semantics;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires transitive jakarta.activation;
    requires transitive java.annotation;
    requires transitive java.desktop;
    requires transitive java.instrument;
    requires transitive java.persistence;
    requires transitive java.sql;
    requires transitive java.ws.rs;
    requires transitive java.xml.bind;
    requires transitive java.xml;
    requires transitive java.inject;
    requires lombok;
    requires org.apache.isis.commons;
    requires transitive org.apache.isis.schema;
    requires org.apache.logging.log4j;
    requires transitive org.joda.time;
    requires transitive spring.beans;
    requires transitive spring.context;
    requires transitive spring.core;
    requires spring.tx;

    // JAXB viewmodels
    opens org.apache.isis.applib.layout.component;
    opens org.apache.isis.applib.layout.grid.bootstrap;
    opens org.apache.isis.applib.layout.grid;
    opens org.apache.isis.applib.layout.links;
    opens org.apache.isis.applib.layout.menubars.bootstrap;
    opens org.apache.isis.applib.layout.menubars;


}
