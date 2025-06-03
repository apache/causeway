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
module org.apache.causeway.viewer.wicket.ui {
    exports org.apache.causeway.viewer.wicket.ui;
    exports org.apache.causeway.viewer.wicket.ui.app.logout;
    exports org.apache.causeway.viewer.wicket.ui.app.registry;
    exports org.apache.causeway.viewer.wicket.ui.components;
    exports org.apache.causeway.viewer.wicket.ui.components.about;
    exports org.apache.causeway.viewer.wicket.ui.components.actioninfo;
    exports org.apache.causeway.viewer.wicket.ui.components.actionlinks.entityactions;
    exports org.apache.causeway.viewer.wicket.ui.components.actionlinks.serviceactions;
    exports org.apache.causeway.viewer.wicket.ui.components.actionlinks;
    exports org.apache.causeway.viewer.wicket.ui.components.actionprompt;
    exports org.apache.causeway.viewer.wicket.ui.components.actionpromptsb;
    exports org.apache.causeway.viewer.wicket.ui.components.actions;
    exports org.apache.causeway.viewer.wicket.ui.components.attributes;
    exports org.apache.causeway.viewer.wicket.ui.components.attributes.blobclob;
    exports org.apache.causeway.viewer.wicket.ui.components.attributes.bool;
    exports org.apache.causeway.viewer.wicket.ui.components.attributes.choices;
    exports org.apache.causeway.viewer.wicket.ui.components.attributes.image;
    exports org.apache.causeway.viewer.wicket.ui.components.attributes.markup;
    exports org.apache.causeway.viewer.wicket.ui.components.attributes.passwd;
    exports org.apache.causeway.viewer.wicket.ui.components.attributes.string;
    exports org.apache.causeway.viewer.wicket.ui.components.attributes.temporal;
    exports org.apache.causeway.viewer.wicket.ui.components.attributes.value;
    exports org.apache.causeway.viewer.wicket.ui.components.bookmarkedpages;
    exports org.apache.causeway.viewer.wicket.ui.components.collection.count;
    exports org.apache.causeway.viewer.wicket.ui.components.collection.selector;
    exports org.apache.causeway.viewer.wicket.ui.components.collection;
    exports org.apache.causeway.viewer.wicket.ui.components.collection.present.ajaxtable.columns;
    exports org.apache.causeway.viewer.wicket.ui.components.collection.present.ajaxtable;
    exports org.apache.causeway.viewer.wicket.ui.components.collection.present.icons;
    exports org.apache.causeway.viewer.wicket.ui.components.collection.present.multiple;
    exports org.apache.causeway.viewer.wicket.ui.components.collection.present.summary;
    exports org.apache.causeway.viewer.wicket.ui.components.collection.present.unresolved;
    exports org.apache.causeway.viewer.wicket.ui.components.empty;
    exports org.apache.causeway.viewer.wicket.ui.components.collection.parented;
    exports org.apache.causeway.viewer.wicket.ui.components.object.fieldset;
    exports org.apache.causeway.viewer.wicket.ui.components.object.header;
    exports org.apache.causeway.viewer.wicket.ui.components.object.icontitle;
    exports org.apache.causeway.viewer.wicket.ui.components.object;
    exports org.apache.causeway.viewer.wicket.ui.components.footer;
    exports org.apache.causeway.viewer.wicket.ui.components.header;
    exports org.apache.causeway.viewer.wicket.ui.components.layout.bs.clearfix;
    exports org.apache.causeway.viewer.wicket.ui.components.layout.bs.col;
    exports org.apache.causeway.viewer.wicket.ui.components.layout.bs.row;
    exports org.apache.causeway.viewer.wicket.ui.components.layout.bs.tabs;
    exports org.apache.causeway.viewer.wicket.ui.components.layout.bs;
    exports org.apache.causeway.viewer.wicket.ui.components.property;
    exports org.apache.causeway.viewer.wicket.ui.components.propertyheader;
    exports org.apache.causeway.viewer.wicket.ui.components.collection.standalone;
    exports org.apache.causeway.viewer.wicket.ui.components.tree.themes.bootstrap;
    exports org.apache.causeway.viewer.wicket.ui.components.tree.themes;
    exports org.apache.causeway.viewer.wicket.ui.components.tree;
    exports org.apache.causeway.viewer.wicket.ui.components.unknown;
    exports org.apache.causeway.viewer.wicket.ui.components.value;
    exports org.apache.causeway.viewer.wicket.ui.components.voidreturn;
    exports org.apache.causeway.viewer.wicket.ui.components.welcome;
    exports org.apache.causeway.viewer.wicket.ui.components.widgets.actionlink;
    exports org.apache.causeway.viewer.wicket.ui.components.widgets.bootstrap;
    exports org.apache.causeway.viewer.wicket.ui.components.widgets.breadcrumbs;
    exports org.apache.causeway.viewer.wicket.ui.components.widgets.buttons;
    exports org.apache.causeway.viewer.wicket.ui.components.widgets.checkbox;
    exports org.apache.causeway.viewer.wicket.ui.components.widgets.objectsimplelink;
    exports org.apache.causeway.viewer.wicket.ui.components.widgets.formcomponent;
    exports org.apache.causeway.viewer.wicket.ui.components.widgets.links;
    exports org.apache.causeway.viewer.wicket.ui.components.widgets.navbar;
    exports org.apache.causeway.viewer.wicket.ui.components.widgets.select2;
    exports org.apache.causeway.viewer.wicket.ui.components.widgets.themepicker;
    exports org.apache.causeway.viewer.wicket.ui.components.widgets.zclip;
    exports org.apache.causeway.viewer.wicket.ui.errors;
    exports org.apache.causeway.viewer.wicket.ui.exec;
    exports org.apache.causeway.viewer.wicket.ui.pages;
    exports org.apache.causeway.viewer.wicket.ui.pages.about;
    exports org.apache.causeway.viewer.wicket.ui.pages.accmngt.password_reset;
    exports org.apache.causeway.viewer.wicket.ui.pages.accmngt.register;
    exports org.apache.causeway.viewer.wicket.ui.pages.accmngt.signup;
    exports org.apache.causeway.viewer.wicket.ui.pages.accmngt;
    exports org.apache.causeway.viewer.wicket.ui.pages.common.bootstrap.css;
    exports org.apache.causeway.viewer.wicket.ui.pages.common.datatables;
    exports org.apache.causeway.viewer.wicket.ui.pages.common.fontawesome;
    exports org.apache.causeway.viewer.wicket.ui.pages.common.serversentevents.js;
    exports org.apache.causeway.viewer.wicket.ui.pages.common.sidebar.css;
    exports org.apache.causeway.viewer.wicket.ui.pages.common.viewer.js;
    exports org.apache.causeway.viewer.wicket.ui.pages.obj;
    exports org.apache.causeway.viewer.wicket.ui.pages.error;
    exports org.apache.causeway.viewer.wicket.ui.pages.home;
    exports org.apache.causeway.viewer.wicket.ui.pages.login;
    exports org.apache.causeway.viewer.wicket.ui.pages.mmverror;
    exports org.apache.causeway.viewer.wicket.ui.pages.standalonecollection;
    exports org.apache.causeway.viewer.wicket.ui.pages.value;
    exports org.apache.causeway.viewer.wicket.ui.pages.voidreturn;
    exports org.apache.causeway.viewer.wicket.ui.panels;
    exports org.apache.causeway.viewer.wicket.ui.util;
    exports org.apache.causeway.viewer.wicket.ui.validation;

    requires static lombok;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.github.openjson;
    requires jakarta.activation;
    requires java.desktop;
    requires jakarta.inject;
    requires java.sql;
    requires jakarta.validation;
    requires jakarta.servlet;
    requires org.apache.causeway.applib;
    requires org.apache.causeway.commons;
    requires org.apache.causeway.core.config;
    requires org.apache.causeway.core.interaction;
    requires org.apache.causeway.core.metamodel;
    requires org.apache.causeway.security.api;
    requires org.apache.causeway.viewer.commons.applib;
    requires org.apache.causeway.viewer.commons.model;
    requires org.apache.causeway.viewer.commons.prism;
    requires org.apache.causeway.viewer.commons.services;
    requires transitive org.apache.causeway.viewer.wicket.model;
    requires org.slf4j;
    requires org.apache.wicket.auth.roles;
    requires org.apache.wicket.core;
    requires org.apache.wicket.devutils;
    requires org.apache.wicket.extensions;
    requires org.apache.wicket.request;
    requires org.apache.wicket.util;
    requires org.danekja.jdk.serializable.functional;
    requires spring.beans;
    requires spring.context;
    requires spring.core;
    requires wicket.bootstrap.core;
    requires wicket.bootstrap.extensions;
    requires wicket.bootstrap.themes;
    requires de.agilecoders.wicket.webjars;
    requires org.wicketstuff.select2;
    requires de.agilecoders.wicket.jquery;
    requires org.graalvm.polyglot;

}