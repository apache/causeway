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
module org.apache.causeway.security.keycloak {
    exports org.apache.causeway.security.keycloak;
    exports org.apache.causeway.security.keycloak.handler;
    exports org.apache.causeway.security.keycloak.services;

    requires org.apache.causeway.core.webapp;
    requires org.apache.causeway.security.spring;
    requires lombok;
    requires org.apache.causeway.core.config;
    requires org.apache.causeway.core.runtimeservices;
    requires org.apache.causeway.security.api;
    requires org.slf4j;
    requires spring.beans;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.core;
    requires spring.security.config;
    requires spring.security.core;
    requires spring.security.oauth2.client;
    requires spring.security.oauth2.core;
    requires spring.security.oauth2.jose;
    requires spring.security.web;
    requires spring.web;
    requires org.apache.causeway.commons;
}