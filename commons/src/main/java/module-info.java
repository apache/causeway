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
module org.apache.causeway.commons {
    exports org.apache.causeway.commons.binding;
    exports org.apache.causeway.commons.collections;
    exports org.apache.causeway.commons.concurrent;
    exports org.apache.causeway.commons.functional;
    exports org.apache.causeway.commons.handler;
    exports org.apache.causeway.commons.having;
    exports org.apache.causeway.commons.resource;
    exports org.apache.causeway.commons.internal;
    exports org.apache.causeway.commons.internal.assertions;
    exports org.apache.causeway.commons.internal.base;
    exports org.apache.causeway.commons.internal.binding;
    exports org.apache.causeway.commons.internal.codec;
    exports org.apache.causeway.commons.internal.collections.snapshot;
    exports org.apache.causeway.commons.internal.collections;
    exports org.apache.causeway.commons.internal.compare;
    exports org.apache.causeway.commons.internal.concurrent;
    exports org.apache.causeway.commons.internal.context;
    exports org.apache.causeway.commons.internal.debug.xray.graphics;
    exports org.apache.causeway.commons.internal.debug.xray;
    exports org.apache.causeway.commons.internal.debug;
    exports org.apache.causeway.commons.internal.delegate;
    exports org.apache.causeway.commons.internal.exceptions;
    exports org.apache.causeway.commons.internal.factory;
    exports org.apache.causeway.commons.internal.functions;
    exports org.apache.causeway.commons.internal.graph;
    exports org.apache.causeway.commons.internal.hardening;
    exports org.apache.causeway.commons.internal.hash;
    exports org.apache.causeway.commons.internal.html;
    exports org.apache.causeway.commons.internal.image;
    exports org.apache.causeway.commons.internal.ioc;
    exports org.apache.causeway.commons.internal.memento;
    exports org.apache.causeway.commons.internal.os;
    exports org.apache.causeway.commons.internal.primitives;
    exports org.apache.causeway.commons.internal.proxy;
    exports org.apache.causeway.commons.internal.reflection;
    exports org.apache.causeway.commons.internal.resources;
    exports org.apache.causeway.commons.internal.testing;

    requires transitive com.fasterxml.jackson.annotation;
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;
    requires transitive com.fasterxml.jackson.module.jaxb;
    requires transitive java.desktop;
    requires transitive java.sql;
    requires transitive java.xml;
    requires transitive java.xml.bind;
    requires transitive lombok;
    requires transitive org.apache.logging.log4j;
    requires transitive org.jdom2;
    requires transitive org.jsoup;
    requires transitive org.yaml.snakeyaml;
    requires transitive spring.beans;
    requires transitive spring.context;
    requires transitive spring.core;
    requires java.inject;
    requires java.annotation;

    // JAXB JUnit test
    opens org.apache.causeway.commons.internal.resources to java.xml.bind;

}