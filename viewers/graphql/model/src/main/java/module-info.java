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
module org.apache.causeway.incubator.viewer.graphql.model {
    exports org.apache.causeway.viewer.graphql.model;
    exports org.apache.causeway.viewer.graphql.model.context;
    exports org.apache.causeway.viewer.graphql.model.domain;
    exports org.apache.causeway.viewer.graphql.model.domain.common;
    exports org.apache.causeway.viewer.graphql.model.domain.common.interactors;
    exports org.apache.causeway.viewer.graphql.model.domain.common.query;
    exports org.apache.causeway.viewer.graphql.model.domain.common.query.meta;
    exports org.apache.causeway.viewer.graphql.model.domain.rich;
    exports org.apache.causeway.viewer.graphql.model.domain.rich.query;
    exports org.apache.causeway.viewer.graphql.model.domain.rich.mutation;
    exports org.apache.causeway.viewer.graphql.model.domain.simple;
    exports org.apache.causeway.viewer.graphql.model.domain.simple.query;
    exports org.apache.causeway.viewer.graphql.model.domain.simple.mutation;
    exports org.apache.causeway.viewer.graphql.model.exceptions;
    exports org.apache.causeway.viewer.graphql.model.fetcher;
    exports org.apache.causeway.viewer.graphql.model.mmproviders;
    exports org.apache.causeway.viewer.graphql.model.registry;
    exports org.apache.causeway.viewer.graphql.model.toplevel;
    exports org.apache.causeway.viewer.graphql.model.types;
    exports org.apache.causeway.viewer.graphql.model.domain.rich.scenario;

    requires org.apache.causeway.core.config;
    requires org.apache.causeway.core.metamodel;
    requires org.apache.causeway.incubator.viewer.graphql.applib;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires com.graphqljava;
    requires com.graphqljava.extendedscalars;
}