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
module org.apache.causeway.persistence.jpa.eclipselink {
    exports org.apache.causeway.persistence.jpa.eclipselink.inject;
    exports org.apache.causeway.persistence.jpa.eclipselink;
    exports org.apache.causeway.persistence.jpa.eclipselink.config;

    requires static lombok;

    requires java.sql;

    requires jakarta.annotation;
    requires jakarta.inject;
    requires jakarta.persistence;
    requires jakarta.el;
    requires jakarta.cdi;

    requires org.apache.causeway.applib;
    requires org.apache.causeway.commons;
    requires org.apache.causeway.core.config;
    requires org.apache.causeway.core.metamodel;
    requires org.apache.causeway.persistence.jpa.integration;
    requires org.slf4j;
    requires org.eclipse.persistence.core;
    requires spring.beans;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.core;
    requires spring.jdbc;
    requires spring.orm;
    requires spring.tx;
    requires org.eclipse.persistence.jpa;
    requires spring.boot.jpa;

}