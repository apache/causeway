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
package org.apache.causeway.viewer.graphql.viewer.integration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.causeway.viewer.graphql.model.domain.GqlvDomainObject;

import org.apache.causeway.viewer.graphql.model.domain.TypeNames;

import org.springframework.graphql.execution.GraphQlSource;
import org.springframework.stereotype.Service;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.config.metamodel.specloader.IntrospectionMode;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.registry.GraphQLTypeRegistry;
import org.apache.causeway.viewer.graphql.model.toplevel.GqlvTopLevelMutation;
import org.apache.causeway.viewer.graphql.model.toplevel.GqlvTopLevelQuery;

import graphql.GraphQL;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLEnumValueDefinition;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLSchema;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static graphql.schema.GraphQLEnumType.newEnum;
import static graphql.schema.GraphQLEnumValueDefinition.*;

@Service()
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class GraphQlSourceForCauseway implements GraphQlSource {

    private final CausewayConfiguration causewayConfiguration;
    private final CausewaySystemEnvironment causewaySystemEnvironment;
    private final SpecificationLoader specificationLoader;
    private final GraphQLTypeRegistry graphQLTypeRegistry;
    private final Context context;

    private final AsyncExecutionStrategyResolvingWithinInteraction executionStrategy;

    @PostConstruct
    public void init() {
        boolean fullyIntrospect = IntrospectionMode.isFullIntrospect(causewayConfiguration, causewaySystemEnvironment);
        if (!fullyIntrospect) {
            throw new IllegalStateException("GraphQL requires full introspection mode");
        }
    }

    GraphQL graphQL;

    @Override
    public GraphQL graphQl() {
        if (graphQL == null) {
            graphQL = GraphQL.newGraphQL(schema())
                    .defaultDataFetcherExceptionHandler(new DataFetcherExceptionHandler() {
                        @Override
                        public DataFetcherExceptionHandlerResult onException(DataFetcherExceptionHandlerParameters handlerParameters) {
                            return DataFetcherExceptionHandler.super.onException(handlerParameters);
                        }
                    })
                    .queryExecutionStrategy(executionStrategy)
                    .mutationExecutionStrategy(executionStrategy)
                    .build();
        }
        return graphQL;
    }

    @Override
    public GraphQLSchema schema() {

        val fullyIntrospected = specificationLoader.isMetamodelFullyIntrospected();
        if (!fullyIntrospected) {
            throw new IllegalStateException("Metamodel is not fully introspected");
        }

        // top-level query and mutation type
        val topLevelQuery = new GqlvTopLevelQuery(context);
        val topLevelMutation = new GqlvTopLevelMutation(context);

        // add the data fetchers
        topLevelQuery.addDataFetchers();
        topLevelMutation.addDataFetchers();

        // finalize the fetcher/mutator code that's been added
        val codeRegistry = context.codeRegistryBuilder.build();

        // build the schema
        return GraphQLSchema.newSchema()
                .query(topLevelQuery.getGqlObjectType())
                .mutation(topLevelMutation.getGqlObjectType())
                .additionalTypes(graphQLTypeRegistry.getGraphQLTypes())
                .codeRegistry(codeRegistry)
                .build();
    }


}
