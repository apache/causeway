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
module org.apache.causeway.applib {
    exports org.apache.causeway.applib;
    exports org.apache.causeway.applib.annotation;
    exports org.apache.causeway.applib.client;
    exports org.apache.causeway.applib.clock;
    exports org.apache.causeway.applib.domain;
    exports org.apache.causeway.applib.events.domain;
    exports org.apache.causeway.applib.events.lifecycle;
    exports org.apache.causeway.applib.events.metamodel;
    exports org.apache.causeway.applib.events.ui;
    exports org.apache.causeway.applib.events;
    exports org.apache.causeway.applib.exceptions.recoverable;
    exports org.apache.causeway.applib.exceptions.unrecoverable;
    exports org.apache.causeway.applib.exceptions;
    exports org.apache.causeway.applib.graph.tree;
    exports org.apache.causeway.applib.graph;
    exports org.apache.causeway.applib.id;
    exports org.apache.causeway.applib.jaxb;
    exports org.apache.causeway.applib.layout.component;
    exports org.apache.causeway.applib.layout.grid.bootstrap;
    exports org.apache.causeway.applib.layout.grid;
    exports org.apache.causeway.applib.layout.links;
    exports org.apache.causeway.applib.layout.menubars.bootstrap;
    exports org.apache.causeway.applib.layout.menubars;
    exports org.apache.causeway.applib.layout;
    exports org.apache.causeway.applib.locale;
    exports org.apache.causeway.applib.mixins.dto;
    exports org.apache.causeway.applib.mixins.layout;
    exports org.apache.causeway.applib.mixins.metamodel;
    exports org.apache.causeway.applib.mixins.rest;
    exports org.apache.causeway.applib.mixins.security;
    exports org.apache.causeway.applib.mixins.system;
    exports org.apache.causeway.applib.mixins.updates;
    exports org.apache.causeway.applib.query;
    exports org.apache.causeway.applib.services.acceptheader;
    exports org.apache.causeway.applib.services.appfeat;
    exports org.apache.causeway.applib.services.appfeatui;
    exports org.apache.causeway.applib.services.bookmark.idstringifiers;
    exports org.apache.causeway.applib.services.bookmark;
    exports org.apache.causeway.applib.services.bookmarkui;
    exports org.apache.causeway.applib.services.clock;
    exports org.apache.causeway.applib.services.command;
    exports org.apache.causeway.applib.services.commanddto.conmap;
    exports org.apache.causeway.applib.services.commanddto.processor.spi;
    exports org.apache.causeway.applib.services.commanddto.processor;
    exports org.apache.causeway.applib.services.commanddto;
    exports org.apache.causeway.applib.services.confview;
    exports org.apache.causeway.applib.services.conmap;
    exports org.apache.causeway.applib.services.email;
    exports org.apache.causeway.applib.services.error;
    exports org.apache.causeway.applib.services.eventbus;
    exports org.apache.causeway.applib.services.exceprecog;
    exports org.apache.causeway.applib.services.factory;
    exports org.apache.causeway.applib.services.grid;
    exports org.apache.causeway.applib.services.health;
    exports org.apache.causeway.applib.services.hint;
    exports org.apache.causeway.applib.services.homepage;
    exports org.apache.causeway.applib.services.i18n;
    exports org.apache.causeway.applib.services.iactn;
    exports org.apache.causeway.applib.services.iactnlayer;
    exports org.apache.causeway.applib.services.inject;
    exports org.apache.causeway.applib.services.jaxb;
    exports org.apache.causeway.applib.services.keyvaluestore;
    exports org.apache.causeway.applib.services.layout;
    exports org.apache.causeway.applib.services.linking;
    exports org.apache.causeway.applib.services.locale;
    exports org.apache.causeway.applib.services.menu;
    exports org.apache.causeway.applib.services.message;
    exports org.apache.causeway.applib.services.metamodel;
    exports org.apache.causeway.applib.services.metrics;
    exports org.apache.causeway.applib.services.placeholder;
    exports org.apache.causeway.applib.services.publishing.log;
    exports org.apache.causeway.applib.services.publishing.spi;
    exports org.apache.causeway.applib.services.queryresultscache;
    exports org.apache.causeway.applib.services.registry;
    exports org.apache.causeway.applib.services.repository;
    exports org.apache.causeway.applib.services.routing;
    exports org.apache.causeway.applib.services.scratchpad;
    exports org.apache.causeway.applib.services.session;
    exports org.apache.causeway.applib.services.sitemap;
    exports org.apache.causeway.applib.services.sudo;
    exports org.apache.causeway.applib.services.swagger;
    exports org.apache.causeway.applib.services.tablecol;
    exports org.apache.causeway.applib.services.title;
    exports org.apache.causeway.applib.services.urlencoding;
    exports org.apache.causeway.applib.services.user;
    exports org.apache.causeway.applib.services.userreg.events;
    exports org.apache.causeway.applib.services.userreg;
    exports org.apache.causeway.applib.services.userui;
    exports org.apache.causeway.applib.services.wrapper.callable;
    exports org.apache.causeway.applib.services.wrapper.control;
    exports org.apache.causeway.applib.services.wrapper.events;
    exports org.apache.causeway.applib.services.wrapper.listeners;
    exports org.apache.causeway.applib.services.wrapper;
    exports org.apache.causeway.applib.services.xactn;
    exports org.apache.causeway.applib.services.xml;
    exports org.apache.causeway.applib.services.xmlsnapshot;
    exports org.apache.causeway.applib.snapshot;
    exports org.apache.causeway.applib.spec;
    exports org.apache.causeway.applib.types;
    exports org.apache.causeway.applib.util.schema;
    exports org.apache.causeway.applib.util;
    exports org.apache.causeway.applib.value;
    exports org.apache.causeway.applib.value.semantics;

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
    requires org.apache.causeway.commons;
    requires transitive org.apache.causeway.schema;
    requires org.apache.logging.log4j;
    requires transitive org.joda.time;
    requires transitive spring.beans;
    requires transitive spring.context;
    requires transitive spring.core;
    requires spring.tx;

    // JAXB viewmodels
    opens org.apache.causeway.applib.layout.component;
    opens org.apache.causeway.applib.layout.grid.bootstrap;
    opens org.apache.causeway.applib.layout.grid;
    opens org.apache.causeway.applib.layout.links;
    opens org.apache.causeway.applib.layout.menubars.bootstrap;
    opens org.apache.causeway.applib.layout.menubars;


}
