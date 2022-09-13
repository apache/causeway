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
module org.apache.isis.viewer.commons.model {
    exports org.apache.isis.viewer.commons.model;
    exports org.apache.isis.viewer.commons.model.action.decorator;
    exports org.apache.isis.viewer.commons.model.layout;
    exports org.apache.isis.viewer.commons.model.components;
    exports org.apache.isis.viewer.commons.model.binding;
    exports org.apache.isis.viewer.commons.model.mixin;
    exports org.apache.isis.viewer.commons.model.mock;
    exports org.apache.isis.viewer.commons.model.object;
    exports org.apache.isis.viewer.commons.model.action;
    exports org.apache.isis.viewer.commons.model.decorators;
    exports org.apache.isis.viewer.commons.model.scalar;

    requires lombok;
    requires org.apache.isis.applib;
    requires org.apache.isis.commons;
    requires org.apache.isis.core.config;
    requires org.apache.isis.core.metamodel;
    requires spring.core;
}