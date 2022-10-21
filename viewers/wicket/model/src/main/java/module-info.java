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
module org.apache.causeway.viewer.wicket.model {
    exports org.apache.causeway.viewer.wicket.model.value;
    exports org.apache.causeway.viewer.wicket.model.models.interaction.prop;
    exports org.apache.causeway.viewer.wicket.model.modelhelpers;
    exports org.apache.causeway.viewer.wicket.model.hints;
    exports org.apache.causeway.viewer.wicket.model.models;
    exports org.apache.causeway.viewer.wicket.model.models.binding;
    exports org.apache.causeway.viewer.wicket.model;
    exports org.apache.causeway.viewer.wicket.model.links;
    exports org.apache.causeway.viewer.wicket.model.models.interaction.act;
    exports org.apache.causeway.viewer.wicket.model.models.interaction;
    exports org.apache.causeway.viewer.wicket.model.util;
    exports org.apache.causeway.viewer.wicket.model.mementos;
    exports org.apache.causeway.viewer.wicket.model.models.interaction.coll;
    exports org.apache.causeway.viewer.wicket.model.causeway;

    requires jakarta.activation;
    requires lombok;
    requires transitive org.apache.causeway.applib;
    requires transitive org.apache.causeway.commons;
    requires transitive org.apache.causeway.core.config;
    requires transitive org.apache.causeway.core.metamodel;
    requires org.apache.causeway.core.webapp;
    requires transitive org.apache.causeway.viewer.commons.applib;
    requires transitive org.apache.causeway.viewer.commons.model;
    requires org.apache.logging.log4j;
    requires org.danekja.jdk.serializable.functional;
    requires org.slf4j;
    requires spring.context;
    requires spring.core;
    //as of 9.11.0 only works when used as automatic named modules ...
    requires org.apache.wicket.core;
    requires org.apache.wicket.request;
    requires org.apache.wicket.util;
}
