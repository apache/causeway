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
module org.apache.causeway.core.config {
    exports org.apache.causeway.core.config;
    exports org.apache.causeway.core.config.applib;
    exports org.apache.causeway.core.config.beans;
    exports org.apache.causeway.core.config.converters;
    exports org.apache.causeway.core.config.datasources;
    exports org.apache.causeway.core.config.environment;
    exports org.apache.causeway.core.config.messages;
    exports org.apache.causeway.core.config.metamodel.facets;
    exports org.apache.causeway.core.config.metamodel.services;
    exports org.apache.causeway.core.config.metamodel.specloader;
    exports org.apache.causeway.core.config.presets;
    exports org.apache.causeway.core.config.progmodel;
    exports org.apache.causeway.core.config.util;
    exports org.apache.causeway.core.config.validators;
    exports org.apache.causeway.core.config.viewer.web;

    requires static lombok;

    requires transitive org.apache.causeway.applib;
    requires transitive org.apache.causeway.commons;
    requires jakarta.activation;
    requires jakarta.annotation;
    requires jakarta.persistence;
    requires java.sql;
    requires jakarta.validation;
    requires jakarta.inject;
    requires org.hibernate.validator;
    requires spring.aop;
    requires spring.beans;
    requires transitive spring.boot;
    requires spring.context;
    requires spring.core;
    requires spring.tx;
    requires org.slf4j;

    opens org.apache.causeway.core.config to spring.core, org.hibernate.validator;
    opens org.apache.causeway.core.config.environment to spring.core;
}