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
open module org.apache.causeway.core.metamodel {
    exports org.apache.causeway.core.metamodel;
    exports org.apache.causeway.core.metamodel._testing;
    exports org.apache.causeway.core.metamodel.commons; //XXX eventually move to ~.util or replace by core-commons
    exports org.apache.causeway.core.metamodel.consent;
    exports org.apache.causeway.core.metamodel.context;
    exports org.apache.causeway.core.metamodel.execution;
    exports org.apache.causeway.core.metamodel.facetapi;
    exports org.apache.causeway.core.metamodel.facets;

    //XXX ... org.apache.causeway.core.metamodel.facets.* : probably don't expose directly
    exports org.apache.causeway.core.metamodel.facets.actions.action.invocation;
    exports org.apache.causeway.core.metamodel.facets.actions.layout;
    exports org.apache.causeway.core.metamodel.facets.actions.semantics;
    exports org.apache.causeway.core.metamodel.facets.actcoll.typeof;
    exports org.apache.causeway.core.metamodel.facets.all.described;
    exports org.apache.causeway.core.metamodel.facets.all.i8n.imperative;
    exports org.apache.causeway.core.metamodel.facets.all.i8n.staatic;
    exports org.apache.causeway.core.metamodel.facets.all.named;
    exports org.apache.causeway.core.metamodel.facets.collections;
    exports org.apache.causeway.core.metamodel.facets.members.cssclass;
    exports org.apache.causeway.core.metamodel.facets.members.iconfa.annotprop;
    exports org.apache.causeway.core.metamodel.facets.members.iconfa;
    exports org.apache.causeway.core.metamodel.facets.members.disabled;
    exports org.apache.causeway.core.metamodel.facets.members.layout.group;
    exports org.apache.causeway.core.metamodel.facets.members.publish.command;
    exports org.apache.causeway.core.metamodel.facets.members.publish.execution;
    exports org.apache.causeway.core.metamodel.facets.object.bookmarkpolicy;
    exports org.apache.causeway.core.metamodel.facets.object.callbacks;
    exports org.apache.causeway.core.metamodel.facets.object.domainobject;
    exports org.apache.causeway.core.metamodel.facets.object.domainservicelayout;
    exports org.apache.causeway.core.metamodel.facets.object.entity;
    exports org.apache.causeway.core.metamodel.facets.object.grid;
    exports org.apache.causeway.core.metamodel.facets.object.icon;
    exports org.apache.causeway.core.metamodel.facets.object.immutable;
    exports org.apache.causeway.core.metamodel.facets.object.mixin;
    exports org.apache.causeway.core.metamodel.facets.object.navchild
        to org.apache.causeway.core.runtimeservices;
    exports org.apache.causeway.core.metamodel.facets.object.objectvalidprops;
    exports org.apache.causeway.core.metamodel.facets.object.publish.entitychange;
    exports org.apache.causeway.core.metamodel.facets.object.title;
    exports org.apache.causeway.core.metamodel.facets.object.value;
    exports org.apache.causeway.core.metamodel.facets.object.viewmodel;
    exports org.apache.causeway.core.metamodel.facets.objectvalue.digits;
    exports org.apache.causeway.core.metamodel.facets.objectvalue.labelat;
    exports org.apache.causeway.core.metamodel.facets.objectvalue.mandatory;
    exports org.apache.causeway.core.metamodel.facets.objectvalue.maxlen;
    exports org.apache.causeway.core.metamodel.facets.objectvalue.typicallen;

    exports org.apache.causeway.core.metamodel.facets.propcoll.accessor;
    exports org.apache.causeway.core.metamodel.facets.properties.defaults;
    exports org.apache.causeway.core.metamodel.facets.properties.property.entitychangepublishing;
    exports org.apache.causeway.core.metamodel.facets.properties.property.mandatory;
    exports org.apache.causeway.core.metamodel.facets.properties.property.modify;
    exports org.apache.causeway.core.metamodel.facets.properties.update.clear;
    exports org.apache.causeway.core.metamodel.facets.properties.update.modify;
    exports org.apache.causeway.core.metamodel.facets.value.semantics;

    exports org.apache.causeway.core.metamodel.facets.object.parented
            to org.apache.causeway.persistence.jdo.metamodel;
    exports org.apache.causeway.core.metamodel.facets.object.ignore.datanucleus
            to org.apache.causeway.persistence.jdo.metamodel;
    exports org.apache.causeway.core.metamodel.facets.object.ignore.jdo
            to org.apache.causeway.persistence.jdo.metamodel;

    exports org.apache.causeway.core.metamodel.interactions;
    exports org.apache.causeway.core.metamodel.interactions.acc;
    exports org.apache.causeway.core.metamodel.interactions.managed;
    exports org.apache.causeway.core.metamodel.interactions.use;
    exports org.apache.causeway.core.metamodel.interactions.val;
    exports org.apache.causeway.core.metamodel.interactions.vis;
    exports org.apache.causeway.core.metamodel.tabular;
    exports org.apache.causeway.core.metamodel.tabular.simple;
    exports org.apache.causeway.core.metamodel.tree;

    exports org.apache.causeway.core.metamodel.methods;

    exports org.apache.causeway.core.metamodel.object;
    exports org.apache.causeway.core.metamodel.objectmanager.memento;
    exports org.apache.causeway.core.metamodel.objectmanager;

    exports org.apache.causeway.core.metamodel.progmodel;

    exports org.apache.causeway.core.metamodel.services.appfeat;
    exports org.apache.causeway.core.metamodel.services.classsubstitutor;
    exports org.apache.causeway.core.metamodel.services.command;
    exports org.apache.causeway.core.metamodel.services.devutils;
    exports org.apache.causeway.core.metamodel.services.events;
    exports org.apache.causeway.core.metamodel.services.exceprecog;
    exports org.apache.causeway.core.metamodel.services.grid.bootstrap;
    exports org.apache.causeway.core.metamodel.services.grid;
    exports org.apache.causeway.core.metamodel.services.idstringifier;
    exports org.apache.causeway.core.metamodel.services.ixn;
    exports org.apache.causeway.core.metamodel.services.layout;
    exports org.apache.causeway.core.metamodel.services.message;
    exports org.apache.causeway.core.metamodel.services.metamodel;
    exports org.apache.causeway.core.metamodel.services.objectlifecycle;
    exports org.apache.causeway.core.metamodel.services.publishing;
    exports org.apache.causeway.core.metamodel.services.registry;
    exports org.apache.causeway.core.metamodel.services.schema;
    exports org.apache.causeway.core.metamodel.services.tablecol;
    exports org.apache.causeway.core.metamodel.services.title;

    exports org.apache.causeway.core.metamodel.spec;
    exports org.apache.causeway.core.metamodel.spec.feature;

    exports org.apache.causeway.core.metamodel.specloader
            to org.apache.causeway.core.runtimeservices,
            org.apache.causeway.persistence.commons,
            //TODO probably don't expose SpecificationLoader to persistence
            org.apache.causeway.persistence.jdo.metamodel,
            //TODO probably don't expose SpecificationLoader to viewers
            org.apache.causeway.viewer.restfulobjects.rendering,
            org.apache.causeway.viewer.restfulobjects.viewer,
            org.apache.causeway.viewer.wicket.model,
            org.apache.causeway.viewer.wicket.ui,
            org.apache.causeway.incubator.viewer.graphql.viewer, org.apache.causeway.incubator.viewer.graphql.model, org.apache.causeway.extensions.titlecache.jcache, org.apache.causeway.extensions.titlecache.caffeine, org.apache.causeway.persistence.querydsl.integration;

    exports org.apache.causeway.core.metamodel.facets.object.tabledec to org.apache.causeway.viewer.wicket.ui;
    exports org.apache.causeway.core.metamodel.facets.object.layout;
    exports org.apache.causeway.core.metamodel.facets.all.hide;
    exports org.apache.causeway.core.metamodel.postprocessors;
    exports org.apache.causeway.core.metamodel.services.grid.spi;
    exports org.apache.causeway.core.metamodel.specloader.validator;
    exports org.apache.causeway.core.metamodel.util;
    exports org.apache.causeway.core.metamodel.util.pchain;
    exports org.apache.causeway.core.metamodel.util.snapshot;
    exports org.apache.causeway.core.metamodel.valuesemantics;
    exports org.apache.causeway.core.metamodel.valuesemantics.temporal;
    exports org.apache.causeway.core.metamodel.valuesemantics.temporal.legacy;
    exports org.apache.causeway.core.metamodel.valuetypes;
    exports org.apache.causeway.core.metamodel.spi;
    exports org.apache.causeway.core.metamodel.services.deadlock;
    exports org.apache.causeway.core.metamodel.facets.object.autocomplete;

    requires jakarta.activation;
    requires jakarta.annotation;
    requires java.desktop;
    requires java.sql;
    requires jakarta.validation;
    requires java.xml;
    requires jakarta.xml.bind;
    requires jakarta.inject;
    requires static lombok;
    requires transitive org.apache.causeway.applib;
    requires transitive org.apache.causeway.commons;
    requires transitive org.apache.causeway.core.config;
    requires transitive org.apache.causeway.schema;
    requires org.apache.causeway.security.api;
    requires org.slf4j;
    requires spring.beans;
    requires spring.context;
    requires spring.core;
    requires spring.boot.autoconfigure;
    requires org.jspecify;

}
