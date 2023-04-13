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
module org.apache.causeway.incubator.viewer.javafx.viewer {

    exports org.apache.causeway.incubator.viewer.javafx.viewer;

    requires transitive javafx.base;
    requires transitive javafx.graphics;

    requires transitive org.apache.causeway.commons;
    requires transitive org.apache.causeway.core.config;
    requires transitive org.apache.causeway.incubator.viewer.javafx.headless;
    requires transitive org.apache.causeway.incubator.viewer.javafx.model;
    requires transitive org.apache.causeway.incubator.viewer.javafx.ui;
    requires transitive org.apache.causeway.viewer.commons.services;
    requires transitive spring.beans;
    requires transitive spring.boot;
    requires transitive spring.context;
    requires transitive spring.core;

    requires static lombok;
}