package org.apache.causeway.viewer.graphql.model.domain;

import graphql.GraphQLContext;
import graphql.schema.DataFetchingEnvironment;

import lombok.RequiredArgsConstructor;

import java.util.Map;

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
