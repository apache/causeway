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
package org.apache.causeway.viewer.graphql.model.domain;

import java.util.Map;

import graphql.GraphQLContext;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;

public interface Environment {

    Map<String, Object> getArguments();

    default <T> T getArgument(String name) {
        return (T)getArguments().get(name);
    }

    GraphQLContext getGraphQlContext();

    @RequiredArgsConstructor
    class For implements Environment {

        private final DataFetchingEnvironment dataFetchingEnvironment;

        @Override
        public Map<String, Object> getArguments() {
            return dataFetchingEnvironment.getArguments();
        }

        @Override
        public GraphQLContext getGraphQlContext() {
            return dataFetchingEnvironment.getGraphQlContext();
        }
    }

    @RequiredArgsConstructor
    class ForTunnelled implements Environment {

        private final DataFetchingEnvironment dataFetchingEnvironment;

        @Override
        public Map<String, Object> getArguments() {
            return dataFetchingEnvironment.getGraphQlContext().get("arguments");
        }

        @Override
        public GraphQLContext getGraphQlContext() {
            return dataFetchingEnvironment.getGraphQlContext();
        }
    }
}
