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
/**
 * Do NOT release incubator projects, unstable module name!
 */
module org.apache.causeway.incubator.viewer.javafx.ui {
    exports org.apache.causeway.incubator.viewer.javafx.ui.decorator.icon;
    exports org.apache.causeway.incubator.viewer.javafx.ui.components.markup;
    exports org.apache.causeway.incubator.viewer.javafx.ui;
    exports org.apache.causeway.incubator.viewer.javafx.ui.main;
    exports org.apache.causeway.incubator.viewer.javafx.ui.components;
    exports org.apache.causeway.incubator.viewer.javafx.ui.components.collections;
    exports org.apache.causeway.incubator.viewer.javafx.ui.components.number;
    exports org.apache.causeway.incubator.viewer.javafx.ui.components.other;
    exports org.apache.causeway.incubator.viewer.javafx.ui.decorator.disabling;
    exports org.apache.causeway.incubator.viewer.javafx.ui.components.dialog;
    exports org.apache.causeway.incubator.viewer.javafx.ui.components.object;
    exports org.apache.causeway.incubator.viewer.javafx.ui.components.panel;
    exports org.apache.causeway.incubator.viewer.javafx.ui.components.form;
    exports org.apache.causeway.incubator.viewer.javafx.ui.components.form.field;
    exports org.apache.causeway.incubator.viewer.javafx.ui.components.text;
    exports org.apache.causeway.incubator.viewer.javafx.ui.components.temporal;
    exports org.apache.causeway.incubator.viewer.javafx.ui.components.objectref;
    exports org.apache.causeway.incubator.viewer.javafx.ui.decorator.prototyping;

    requires jakarta.annotation;
    requires jakarta.inject;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;

    requires org.apache.causeway.applib;
    requires org.apache.causeway.commons;
    requires org.apache.causeway.core.config;
    requires org.apache.causeway.core.interaction;
    requires org.apache.causeway.core.metamodel;
    requires org.apache.causeway.incubator.viewer.javafx.model;
    requires org.apache.causeway.security.api;
    requires org.apache.causeway.viewer.commons.applib;
    requires org.apache.causeway.viewer.commons.model;
    requires org.apache.logging.log4j;
    requires spring.beans;
    requires spring.context;
    requires spring.core;

    requires static lombok;
    requires org.apache.causeway.incubator.viewer.javafx.headless;
}