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
    exports org.apache.isis.core.metamodel.commons;
    exports org.apache.isis.core.metamodel.consent;
    exports org.apache.isis.core.metamodel.context;
    exports org.apache.isis.core.metamodel.execution;
    exports org.apache.isis.core.metamodel.facetapi;
    exports org.apache.isis.core.metamodel.facets.actcoll.typeof;
    exports org.apache.isis.core.metamodel.facets.actions.action.choicesfrom;
    exports org.apache.isis.core.metamodel.facets.actions.action.explicit;
    exports org.apache.isis.core.metamodel.facets.actions.action.hidden;
    exports org.apache.isis.core.metamodel.facets.actions.action.invocation;
    exports org.apache.isis.core.metamodel.facets.actions.action.prototype;
    exports org.apache.isis.core.metamodel.facets.actions.action.semantics;
    exports org.apache.isis.core.metamodel.facets.actions.action.typeof;
    exports org.apache.isis.core.metamodel.facets.actions.action;
    exports org.apache.isis.core.metamodel.facets.actions.contributing.derived;
    exports org.apache.isis.core.metamodel.facets.actions.contributing;
    exports org.apache.isis.core.metamodel.facets.actions.fileaccept;
    exports org.apache.isis.core.metamodel.facets.actions.homepage.annotation;
    exports org.apache.isis.core.metamodel.facets.actions.homepage;
    exports org.apache.isis.core.metamodel.facets.actions.layout;
    exports org.apache.isis.core.metamodel.facets.actions.notinservicemenu.derived;
    exports org.apache.isis.core.metamodel.facets.actions.notinservicemenu;
    exports org.apache.isis.core.metamodel.facets.actions.position;
    exports org.apache.isis.core.metamodel.facets.actions.prototype;
    exports org.apache.isis.core.metamodel.facets.actions.redirect;
    exports org.apache.isis.core.metamodel.facets.actions.semantics;
    exports org.apache.isis.core.metamodel.facets.actions.validate.method;
    exports org.apache.isis.core.metamodel.facets.actions.validate;
    exports org.apache.isis.core.metamodel.facets.all.described;
    exports org.apache.isis.core.metamodel.facets.all.help;
    exports org.apache.isis.core.metamodel.facets.all.hide;
    exports org.apache.isis.core.metamodel.facets.all.i8n.imperative;
    exports org.apache.isis.core.metamodel.facets.all.i8n.noun;
    exports org.apache.isis.core.metamodel.facets.all.i8n.staatic;
    exports org.apache.isis.core.metamodel.facets.all.i8n;
    exports org.apache.isis.core.metamodel.facets.all.named;
    exports org.apache.isis.core.metamodel.facets.collections.accessor;
    exports org.apache.isis.core.metamodel.facets.collections.collection.defaultview;
    exports org.apache.isis.core.metamodel.facets.collections.collection.hidden;
    exports org.apache.isis.core.metamodel.facets.collections.collection.modify;
    exports org.apache.isis.core.metamodel.facets.collections.collection.typeof;
    exports org.apache.isis.core.metamodel.facets.collections.collection;
    exports org.apache.isis.core.metamodel.facets.collections.javautilcollection;
    exports org.apache.isis.core.metamodel.facets.collections.layout.tabledec;
    exports org.apache.isis.core.metamodel.facets.collections.layout;
    exports org.apache.isis.core.metamodel.facets.collections.parented;
    exports org.apache.isis.core.metamodel.facets.collections.sortedby.annotation;
    exports org.apache.isis.core.metamodel.facets.collections.sortedby;
    exports org.apache.isis.core.metamodel.facets.collections;
    exports org.apache.isis.core.metamodel.facets.fallback;
    exports org.apache.isis.core.metamodel.facets.jaxb;
    exports org.apache.isis.core.metamodel.facets.members.cssclass.annotprop;
    exports org.apache.isis.core.metamodel.facets.members.cssclass;
    exports org.apache.isis.core.metamodel.facets.members.cssclassfa.annotprop;
    exports org.apache.isis.core.metamodel.facets.members.cssclassfa;
    exports org.apache.isis.core.metamodel.facets.members.described.annotprop;
    exports org.apache.isis.core.metamodel.facets.members.described.method;
    exports org.apache.isis.core.metamodel.facets.members.disabled.method;
    exports org.apache.isis.core.metamodel.facets.members.disabled;
    exports org.apache.isis.core.metamodel.facets.members.hidden.method;
    exports org.apache.isis.core.metamodel.facets.members.hidden;
    exports org.apache.isis.core.metamodel.facets.members.layout.group;
    exports org.apache.isis.core.metamodel.facets.members.layout.order;
    exports org.apache.isis.core.metamodel.facets.members.named.method;
    exports org.apache.isis.core.metamodel.facets.members.navigation;
    exports org.apache.isis.core.metamodel.facets.members.publish.command;
    exports org.apache.isis.core.metamodel.facets.members.publish.execution;
    exports org.apache.isis.core.metamodel.facets.members.support;
    exports org.apache.isis.core.metamodel.facets.object.autocomplete;
    exports org.apache.isis.core.metamodel.facets.object.bookmarkpolicy.bookmarkable;
    exports org.apache.isis.core.metamodel.facets.object.bookmarkpolicy;
    exports org.apache.isis.core.metamodel.facets.object.callbacks;
    exports org.apache.isis.core.metamodel.facets.object.choices.enums;
    exports org.apache.isis.core.metamodel.facets.object.choices;
    exports org.apache.isis.core.metamodel.facets.object.cssclass.method;
    exports org.apache.isis.core.metamodel.facets.object.cssclassfa.annotation;
    exports org.apache.isis.core.metamodel.facets.object.defaults;
    exports org.apache.isis.core.metamodel.facets.object.disabled.method;
    exports org.apache.isis.core.metamodel.facets.object.disabled;
    exports org.apache.isis.core.metamodel.facets.object.domainobject.autocomplete;
    exports org.apache.isis.core.metamodel.facets.object.domainobject.choices;
    exports org.apache.isis.core.metamodel.facets.object.domainobject.domainevents;
    exports org.apache.isis.core.metamodel.facets.object.domainobject.editing;
    exports org.apache.isis.core.metamodel.facets.object.domainobject.entitychangepublishing;
    exports org.apache.isis.core.metamodel.facets.object.domainobject.introspection;
    exports org.apache.isis.core.metamodel.facets.object.domainobject;
    exports org.apache.isis.core.metamodel.facets.object.domainobjectlayout.tabledec;
    exports org.apache.isis.core.metamodel.facets.object.domainobjectlayout;
    exports org.apache.isis.core.metamodel.facets.object.domainservice.annotation;
    exports org.apache.isis.core.metamodel.facets.object.domainservice;
    exports org.apache.isis.core.metamodel.facets.object.domainservicelayout.annotation;
    exports org.apache.isis.core.metamodel.facets.object.domainservicelayout;
    exports org.apache.isis.core.metamodel.facets.object.entity;
    exports org.apache.isis.core.metamodel.facets.object.grid;
    exports org.apache.isis.core.metamodel.facets.object.hidden.method;
    exports org.apache.isis.core.metamodel.facets.object.hidden;
    exports org.apache.isis.core.metamodel.facets.object.icon.method;
    exports org.apache.isis.core.metamodel.facets.object.icon;
    exports org.apache.isis.core.metamodel.facets.object.ignore.annotation;
    exports org.apache.isis.core.metamodel.facets.object.ignore.datanucleus;
    exports org.apache.isis.core.metamodel.facets.object.ignore.javalang;
    exports org.apache.isis.core.metamodel.facets.object.ignore.jdo;
    exports org.apache.isis.core.metamodel.facets.object.immutable.immutableannot;
    exports org.apache.isis.core.metamodel.facets.object.immutable;
    exports org.apache.isis.core.metamodel.facets.object.introspection;
    exports org.apache.isis.core.metamodel.facets.object.layout;
    exports org.apache.isis.core.metamodel.facets.object.logicaltype.classname;
    exports org.apache.isis.core.metamodel.facets.object.logicaltype;
    exports org.apache.isis.core.metamodel.facets.object.mixin;
    exports org.apache.isis.core.metamodel.facets.object.navparent.annotation;
    exports org.apache.isis.core.metamodel.facets.object.navparent.method;
    exports org.apache.isis.core.metamodel.facets.object.navparent;
    exports org.apache.isis.core.metamodel.facets.object.objectvalidprops.impl;
    exports org.apache.isis.core.metamodel.facets.object.objectvalidprops;
    exports org.apache.isis.core.metamodel.facets.object.paged;
    exports org.apache.isis.core.metamodel.facets.object.parented;
    exports org.apache.isis.core.metamodel.facets.object.projection.ident;
    exports org.apache.isis.core.metamodel.facets.object.projection;
    exports org.apache.isis.core.metamodel.facets.object.promptStyle;
    exports org.apache.isis.core.metamodel.facets.object.publish.entitychange;
    exports org.apache.isis.core.metamodel.facets.object.support;
    exports org.apache.isis.core.metamodel.facets.object.title.annotation;
    exports org.apache.isis.core.metamodel.facets.object.title.methods;
    exports org.apache.isis.core.metamodel.facets.object.title.parser;
    exports org.apache.isis.core.metamodel.facets.object.title;
    exports org.apache.isis.core.metamodel.facets.object.value.annotcfg;
    exports org.apache.isis.core.metamodel.facets.object.value.vsp;
    exports org.apache.isis.core.metamodel.facets.object.value;
    exports org.apache.isis.core.metamodel.facets.object.viewmodel;
    exports org.apache.isis.core.metamodel.facets.object;
    exports org.apache.isis.core.metamodel.facets.objectvalue.choices;
    exports org.apache.isis.core.metamodel.facets.objectvalue.daterenderedadjust;
    exports org.apache.isis.core.metamodel.facets.objectvalue.digits;
    exports org.apache.isis.core.metamodel.facets.objectvalue.fileaccept;
    exports org.apache.isis.core.metamodel.facets.objectvalue.labelat;
    exports org.apache.isis.core.metamodel.facets.objectvalue.mandatory;
    exports org.apache.isis.core.metamodel.facets.objectvalue.maxlen;
    exports org.apache.isis.core.metamodel.facets.objectvalue.multiline;
    exports org.apache.isis.core.metamodel.facets.objectvalue.mustsatisfyspec;
    exports org.apache.isis.core.metamodel.facets.objectvalue.regex;
    exports org.apache.isis.core.metamodel.facets.objectvalue.temporalformat;
    exports org.apache.isis.core.metamodel.facets.objectvalue.typicallen;
    exports org.apache.isis.core.metamodel.facets.objectvalue.valuesemantics;
    exports org.apache.isis.core.metamodel.facets.param.autocomplete.method;
    exports org.apache.isis.core.metamodel.facets.param.autocomplete;
    exports org.apache.isis.core.metamodel.facets.param.choices.methodnum;
    exports org.apache.isis.core.metamodel.facets.param.choices;
    exports org.apache.isis.core.metamodel.facets.param.defaults.methodnum;
    exports org.apache.isis.core.metamodel.facets.param.defaults;
    exports org.apache.isis.core.metamodel.facets.param.described.annotderived;
    exports org.apache.isis.core.metamodel.facets.param.disable.method;
    exports org.apache.isis.core.metamodel.facets.param.disable;
    exports org.apache.isis.core.metamodel.facets.param.hide.method;
    exports org.apache.isis.core.metamodel.facets.param.hide;
    exports org.apache.isis.core.metamodel.facets.param.layout;
    exports org.apache.isis.core.metamodel.facets.param.mandatory.dflt;
    exports org.apache.isis.core.metamodel.facets.param.name;
    exports org.apache.isis.core.metamodel.facets.param.parameter.depdef;
    exports org.apache.isis.core.metamodel.facets.param.parameter.fileaccept;
    exports org.apache.isis.core.metamodel.facets.param.parameter.mandatory;
    exports org.apache.isis.core.metamodel.facets.param.parameter.maxlen;
    exports org.apache.isis.core.metamodel.facets.param.parameter.mustsatisfy;
    exports org.apache.isis.core.metamodel.facets.param.parameter.regex;
    exports org.apache.isis.core.metamodel.facets.param.parameter;
    exports org.apache.isis.core.metamodel.facets.param.support;
    exports org.apache.isis.core.metamodel.facets.param.typicallen.fromtype;
    exports org.apache.isis.core.metamodel.facets.param.validate.method;
    exports org.apache.isis.core.metamodel.facets.param.validate;
    exports org.apache.isis.core.metamodel.facets.propcoll.accessor;
    exports org.apache.isis.core.metamodel.facets.propcoll.memserexcl;
    exports org.apache.isis.core.metamodel.facets.properties.accessor;
    exports org.apache.isis.core.metamodel.facets.properties.autocomplete.method;
    exports org.apache.isis.core.metamodel.facets.properties.autocomplete;
    exports org.apache.isis.core.metamodel.facets.properties.businesskey;
    exports org.apache.isis.core.metamodel.facets.properties.choices.enums;
    exports org.apache.isis.core.metamodel.facets.properties.choices.method;
    exports org.apache.isis.core.metamodel.facets.properties.choices;
    exports org.apache.isis.core.metamodel.facets.properties.defaults.fromtype;
    exports org.apache.isis.core.metamodel.facets.properties.defaults.method;
    exports org.apache.isis.core.metamodel.facets.properties.defaults;
    exports org.apache.isis.core.metamodel.facets.properties.disabled.fromimmutable;
    exports org.apache.isis.core.metamodel.facets.properties.disabled.inferred;
    exports org.apache.isis.core.metamodel.facets.properties.mandatory.dflt;
    exports org.apache.isis.core.metamodel.facets.properties.projection;
    exports org.apache.isis.core.metamodel.facets.properties.property.disabled;
    exports org.apache.isis.core.metamodel.facets.properties.property.entitychangepublishing;
    exports org.apache.isis.core.metamodel.facets.properties.property.fileaccept;
    exports org.apache.isis.core.metamodel.facets.properties.property.hidden;
    exports org.apache.isis.core.metamodel.facets.properties.property.mandatory;
    exports org.apache.isis.core.metamodel.facets.properties.property.maxlength;
    exports org.apache.isis.core.metamodel.facets.properties.property.modify;
    exports org.apache.isis.core.metamodel.facets.properties.property.mustsatisfy;
    exports org.apache.isis.core.metamodel.facets.properties.property.regex;
    exports org.apache.isis.core.metamodel.facets.properties.property.snapshot;
    exports org.apache.isis.core.metamodel.facets.properties.property;
    exports org.apache.isis.core.metamodel.facets.properties.propertylayout;
    exports org.apache.isis.core.metamodel.facets.properties.renderunchanged;
    exports org.apache.isis.core.metamodel.facets.properties.searchable;
    exports org.apache.isis.core.metamodel.facets.properties.typicallen.fromtype;
    exports org.apache.isis.core.metamodel.facets.properties.update.clear;
    exports org.apache.isis.core.metamodel.facets.properties.update.init;
    exports org.apache.isis.core.metamodel.facets.properties.update.modify;
    exports org.apache.isis.core.metamodel.facets.properties.update;
    exports org.apache.isis.core.metamodel.facets.properties.validating.dflt;
    exports org.apache.isis.core.metamodel.facets.properties.validating.method;
    exports org.apache.isis.core.metamodel.facets.properties.validating;
    exports org.apache.isis.core.metamodel.facets.value.semantics;
    exports org.apache.isis.core.metamodel.facets;
    exports org.apache.isis.core.metamodel.inspect.model;
    exports org.apache.isis.core.metamodel.inspect;
    exports org.apache.isis.core.metamodel.interactions.managed.nonscalar;
    exports org.apache.isis.core.metamodel.interactions.managed;
    exports org.apache.isis.core.metamodel.interactions;
    exports org.apache.isis.core.metamodel.layout.memberorderfacet;
    exports org.apache.isis.core.metamodel.layout;
    exports org.apache.isis.core.metamodel.methods;
    exports org.apache.isis.core.metamodel.object;
    exports org.apache.isis.core.metamodel.objectmanager.memento;
    exports org.apache.isis.core.metamodel.objectmanager;
    exports org.apache.isis.core.metamodel.postprocessors.all.i18n;
    exports org.apache.isis.core.metamodel.postprocessors.all;
    exports org.apache.isis.core.metamodel.postprocessors.allbutparam.authorization;
    exports org.apache.isis.core.metamodel.postprocessors.members.navigation;
    exports org.apache.isis.core.metamodel.postprocessors.members;
    exports org.apache.isis.core.metamodel.postprocessors.object;
    exports org.apache.isis.core.metamodel.postprocessors.param;
    exports org.apache.isis.core.metamodel.postprocessors.properties;
    exports org.apache.isis.core.metamodel.postprocessors;
    exports org.apache.isis.core.metamodel.progmodel;
    exports org.apache.isis.core.metamodel.progmodels.dflt;
    exports org.apache.isis.core.metamodel.render;
    exports org.apache.isis.core.metamodel.services.appfeat;
    exports org.apache.isis.core.metamodel.services.classsubstitutor;
    exports org.apache.isis.core.metamodel.services.command;
    exports org.apache.isis.core.metamodel.services.devutils;
    exports org.apache.isis.core.metamodel.services.events;
    exports org.apache.isis.core.metamodel.services.exceprecog;
    exports org.apache.isis.core.metamodel.services.grid.bootstrap;
    exports org.apache.isis.core.metamodel.services.grid;
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
    exports org.apache.isis.core.metamodel.services;
    exports org.apache.isis.core.metamodel.spec.feature.memento;
    exports org.apache.isis.core.metamodel.spec.feature;
    exports org.apache.isis.core.metamodel.spec;
    exports org.apache.isis.core.metamodel.specloader.facetprocessor;
    exports org.apache.isis.core.metamodel.specloader.postprocessor;
    exports org.apache.isis.core.metamodel.specloader.specimpl.dflt;
    exports org.apache.isis.core.metamodel.specloader.specimpl;
    exports org.apache.isis.core.metamodel.specloader.typeextract;
    exports org.apache.isis.core.metamodel.specloader.validator;
    exports org.apache.isis.core.metamodel.specloader;
    exports org.apache.isis.core.metamodel.util.pchain;
    exports org.apache.isis.core.metamodel.util.snapshot;
    exports org.apache.isis.core.metamodel.util;
    exports org.apache.isis.core.metamodel.valuesemantics.temporal.legacy;
    exports org.apache.isis.core.metamodel.valuesemantics.temporal;
    exports org.apache.isis.core.metamodel.valuesemantics;
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
    requires org.apache.isis.applib;
    requires org.apache.isis.commons;
    requires org.apache.isis.core.config;
    requires org.apache.isis.schema;
    requires org.apache.isis.security.api;
    requires org.apache.logging.log4j;
    requires org.joda.time;
    requires spring.beans;
    requires spring.context;
    requires spring.core;

//   opens org.apache.isis.core.metamodel.services to spring.core;
//   opens org.apache.isis.core.metamodel.services.registry to spring.core;


}