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
    exports org.apache.causeway.core.metamodel.facets.actions.notinservicemenu;
    exports org.apache.causeway.core.metamodel.facets.actions.semantics;
    exports org.apache.causeway.core.metamodel.facets.actcoll.typeof;
    exports org.apache.causeway.core.metamodel.facets.all.described;
    exports org.apache.causeway.core.metamodel.facets.all.i8n.imperative;
    exports org.apache.causeway.core.metamodel.facets.all.i8n.staatic;
    exports org.apache.causeway.core.metamodel.facets.all.named;
    exports org.apache.causeway.core.metamodel.facets.collections;
    exports org.apache.causeway.core.metamodel.facets.members.cssclass;
    exports org.apache.causeway.core.metamodel.facets.members.cssclassfa.annotprop;
    exports org.apache.causeway.core.metamodel.facets.members.cssclassfa;
    exports org.apache.causeway.core.metamodel.facets.members.disabled;
    exports org.apache.causeway.core.metamodel.facets.members.layout.group;
    exports org.apache.causeway.core.metamodel.facets.members.publish.command;
    exports org.apache.causeway.core.metamodel.facets.members.publish.execution;
    exports org.apache.causeway.core.metamodel.facets.object.bookmarkpolicy;
    exports org.apache.causeway.core.metamodel.facets.object.callbacks;
    exports org.apache.causeway.core.metamodel.facets.object.domainobject;
    exports org.apache.causeway.core.metamodel.facets.object.domainservice;
    exports org.apache.causeway.core.metamodel.facets.object.domainservicelayout;
    exports org.apache.causeway.core.metamodel.facets.object.entity;
    exports org.apache.causeway.core.metamodel.facets.object.icon;
    exports org.apache.causeway.core.metamodel.facets.object.mixin;
    exports org.apache.causeway.core.metamodel.facets.object.objectvalidprops;
    exports org.apache.causeway.core.metamodel.facets.object.publish.entitychange;
    exports org.apache.causeway.core.metamodel.facets.object.title;
    exports org.apache.causeway.core.metamodel.facets.object.value;
    exports org.apache.causeway.core.metamodel.facets.object.viewmodel;
    exports org.apache.causeway.core.metamodel.facets.objectvalue.digits;
    exports org.apache.causeway.core.metamodel.facets.objectvalue.labelat;
    exports org.apache.causeway.core.metamodel.facets.objectvalue.mandatory;
    exports org.apache.causeway.core.metamodel.facets.objectvalue.maxlen;

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
    exports org.apache.causeway.core.metamodel.interactions.managed;
    exports org.apache.causeway.core.metamodel.interactions.managed.nonscalar;

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
    exports org.apache.causeway.core.metamodel.spec.feature.memento
        //TODO don't expose impl. details
        to org.apache.causeway.viewer.wicket.model;

    exports org.apache.causeway.core.metamodel.specloader
        to org.apache.causeway.core.runtimeservices,
        //TODO probably don't expose SpecificationLoader to persistence
        org.apache.causeway.persistence.jdo.metamodel,
        //TODO probably don't expose SpecificationLoader to viewers
        org.apache.causeway.viewer.restfulobjects.rendering,
        org.apache.causeway.viewer.restfulobjects.viewer,
        org.apache.causeway.viewer.wicket.model,
        org.apache.causeway.viewer.wicket.ui,
        org.apache.causeway.incubator.viewer.graphql.viewer;

    exports org.apache.causeway.core.metamodel.specloader.validator;

    exports org.apache.causeway.core.metamodel.util;
    exports org.apache.causeway.core.metamodel.util.pchain;
    exports org.apache.causeway.core.metamodel.util.snapshot;

    exports org.apache.causeway.core.metamodel.valuesemantics;
    exports org.apache.causeway.core.metamodel.valuesemantics.temporal;
    exports org.apache.causeway.core.metamodel.valuesemantics.temporal.legacy;

    exports org.apache.causeway.core.metamodel.valuetypes;

    requires jakarta.activation;
    requires java.annotation;
    requires java.desktop;
    requires java.sql;
    requires java.validation;
    requires java.xml;
    requires java.xml.bind;
    requires java.inject;
    requires lombok;
    requires transitive org.apache.causeway.applib;
    requires transitive org.apache.causeway.commons;
    requires transitive org.apache.causeway.core.config;
    requires transitive org.apache.causeway.schema;
    requires org.apache.causeway.security.api;
    requires org.apache.logging.log4j;
    requires org.joda.time;
    requires spring.beans;
    requires spring.context;
    requires spring.core;
    requires org.apache.causeway.core.privileged;

//JUnit testing stuff, not required as long this module is an 'open' one
//    opens org.apache.causeway.core.metamodel.services to spring.core;
//    opens org.apache.causeway.core.metamodel.services.registry to spring.core;
//    opens org.apache.causeway.core.metamodel.services.grid to java.xml.bind;
//    opens org.apache.causeway.core.metamodel.services.metamodel to java.xml.bind;
}