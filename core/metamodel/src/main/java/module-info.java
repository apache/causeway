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
open module org.apache.isis.core.metamodel {
    exports org.apache.isis.core.metamodel;
    exports org.apache.isis.core.metamodel._testing;
    exports org.apache.isis.core.metamodel.commons; //XXX eventually move to ~.util or replace by core-commons
    exports org.apache.isis.core.metamodel.consent;
    exports org.apache.isis.core.metamodel.context;
    exports org.apache.isis.core.metamodel.execution;
    exports org.apache.isis.core.metamodel.facetapi;
    exports org.apache.isis.core.metamodel.facets;

    //XXX ... org.apache.isis.core.metamodel.facets.* : probably don't expose directly
    exports org.apache.isis.core.metamodel.facets.actions.action.invocation;
    exports org.apache.isis.core.metamodel.facets.actions.layout;
    exports org.apache.isis.core.metamodel.facets.actions.notinservicemenu;
    exports org.apache.isis.core.metamodel.facets.actions.semantics;
    exports org.apache.isis.core.metamodel.facets.actcoll.typeof;
    exports org.apache.isis.core.metamodel.facets.all.described;
    exports org.apache.isis.core.metamodel.facets.all.i8n.imperative;
    exports org.apache.isis.core.metamodel.facets.all.i8n.staatic;
    exports org.apache.isis.core.metamodel.facets.all.named;
    exports org.apache.isis.core.metamodel.facets.members.cssclass;
    exports org.apache.isis.core.metamodel.facets.members.cssclassfa.annotprop;
    exports org.apache.isis.core.metamodel.facets.members.cssclassfa;
    exports org.apache.isis.core.metamodel.facets.members.layout.group;
    exports org.apache.isis.core.metamodel.facets.members.publish.command;
    exports org.apache.isis.core.metamodel.facets.members.publish.execution;
    exports org.apache.isis.core.metamodel.facets.object.bookmarkpolicy;
    exports org.apache.isis.core.metamodel.facets.object.callbacks;
    exports org.apache.isis.core.metamodel.facets.object.domainservice;
    exports org.apache.isis.core.metamodel.facets.object.domainservicelayout;
    exports org.apache.isis.core.metamodel.facets.object.entity;
    exports org.apache.isis.core.metamodel.facets.object.icon;
    exports org.apache.isis.core.metamodel.facets.object.mixin;
    exports org.apache.isis.core.metamodel.facets.object.value;
    exports org.apache.isis.core.metamodel.facets.object.viewmodel;
    exports org.apache.isis.core.metamodel.facets.objectvalue.labelat;
    exports org.apache.isis.core.metamodel.facets.properties.property.modify;
    exports org.apache.isis.core.metamodel.facets.value.semantics;

    exports org.apache.isis.core.metamodel.interactions;
    exports org.apache.isis.core.metamodel.interactions.managed;
    exports org.apache.isis.core.metamodel.interactions.managed.nonscalar;

    exports org.apache.isis.core.metamodel.object;
    exports org.apache.isis.core.metamodel.objectmanager.memento;
    exports org.apache.isis.core.metamodel.objectmanager;

    exports org.apache.isis.core.metamodel.services.appfeat;
    exports org.apache.isis.core.metamodel.services.classsubstitutor;
    exports org.apache.isis.core.metamodel.services.command;
    exports org.apache.isis.core.metamodel.services.devutils;
    exports org.apache.isis.core.metamodel.services.events;
    exports org.apache.isis.core.metamodel.services.exceprecog;
    exports org.apache.isis.core.metamodel.services.grid.bootstrap;
    exports org.apache.isis.core.metamodel.services.grid;
    exports org.apache.isis.core.metamodel.services.idstringifier;
    exports org.apache.isis.core.metamodel.services.ixn;
    exports org.apache.isis.core.metamodel.services.layout;
    exports org.apache.isis.core.metamodel.services.message;
    exports org.apache.isis.core.metamodel.services.metamodel;
    exports org.apache.isis.core.metamodel.services.objectlifecycle;
    exports org.apache.isis.core.metamodel.services.publishing;
    exports org.apache.isis.core.metamodel.services.registry;
    exports org.apache.isis.core.metamodel.services.schema;
    exports org.apache.isis.core.metamodel.services.tablecol;
    exports org.apache.isis.core.metamodel.services.title;

    exports org.apache.isis.core.metamodel.spec;
    exports org.apache.isis.core.metamodel.spec.feature;
    exports org.apache.isis.core.metamodel.spec.feature.memento
        //TODO don't expose impl. details
        to org.apache.isis.viewer.wicket.model;

    exports org.apache.isis.core.metamodel.specloader
        to org.apache.isis.core.runtimeservices,
        //TODO probably don't expose SpecificationLoader to viewers
        org.apache.isis.viewer.wicket.model, org.apache.isis.viewer.wicket.ui,
        org.apache.isis.incubator.viewer.graphql.viewer;

    exports org.apache.isis.core.metamodel.specloader.validator;

    exports org.apache.isis.core.metamodel.util;
    exports org.apache.isis.core.metamodel.util.pchain;
    exports org.apache.isis.core.metamodel.util.snapshot;

    exports org.apache.isis.core.metamodel.valuesemantics;
    exports org.apache.isis.core.metamodel.valuesemantics.temporal;
    exports org.apache.isis.core.metamodel.valuesemantics.temporal.legacy;

    exports org.apache.isis.core.metamodel.valuetypes;

    requires jakarta.activation;
    requires java.annotation;
    requires java.desktop;
    requires java.sql;
    requires java.validation;
    requires java.xml;
    requires java.xml.bind;
    requires java.inject;
    requires lombok;
    requires transitive org.apache.isis.applib;
    requires transitive org.apache.isis.commons;
    requires transitive org.apache.isis.core.config;
    requires transitive org.apache.isis.schema;
    requires org.apache.isis.security.api;
    requires org.apache.logging.log4j;
    requires org.joda.time;
    requires spring.beans;
    requires spring.context;
    requires spring.core;
    requires org.apache.isis.core.privileged;

//JUnit testing stuff, not required as long this module is an 'open' one
//    opens org.apache.isis.core.metamodel.services to spring.core;
//    opens org.apache.isis.core.metamodel.services.registry to spring.core;
//    opens org.apache.isis.core.metamodel.services.grid to java.xml.bind;
//    opens org.apache.isis.core.metamodel.services.metamodel to java.xml.bind;
}