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
module org.apache.causeway.security.spring {
    exports org.apache.causeway.security.spring;
    exports org.apache.causeway.security.spring.authconverters;
    exports org.apache.causeway.security.spring.authentication;
    exports org.apache.causeway.security.spring.webmodule;

    requires static lombok;
    requires org.slf4j;

    requires jakarta.annotation;
    requires jakarta.inject;
    requires jakarta.servlet;

    requires org.apache.causeway.applib;
    requires org.apache.causeway.commons;
    requires org.apache.causeway.core.config;
    requires org.apache.causeway.core.runtimeservices;
    requires org.apache.causeway.core.webapp;
    requires org.apache.causeway.security.api;

    requires spring.beans;
    requires spring.context;
    requires spring.core;
    requires spring.security.core;
    requires spring.security.web;
    requires spring.web;
}
