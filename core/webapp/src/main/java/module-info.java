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
module org.apache.isis.core.webapp {
    exports org.apache.isis.core.webapp;
    exports org.apache.isis.core.webapp.confmenu;
    exports org.apache.isis.core.webapp.health;
    exports org.apache.isis.core.webapp.keyvaluestore;
    exports org.apache.isis.core.webapp.modules;
    exports org.apache.isis.core.webapp.modules.logonlog;
    exports org.apache.isis.core.webapp.modules.templresources;
    exports org.apache.isis.core.webapp.routing;
    exports org.apache.isis.core.webapp.webappctx;

    requires java.annotation;
    requires java.inject;
    requires javax.servlet.api;
    requires lombok;
    requires org.apache.isis.applib;
    requires org.apache.isis.commons;
    requires org.apache.isis.core.config;
    requires org.apache.isis.core.interaction;
    requires org.apache.isis.core.metamodel;
    requires org.apache.isis.core.runtime;
    requires org.apache.isis.security.api;
    requires org.apache.logging.log4j;
    requires spring.beans;
    requires spring.boot;
    requires spring.boot.actuator;
    requires spring.context;
    requires spring.core;
    requires spring.web;
}