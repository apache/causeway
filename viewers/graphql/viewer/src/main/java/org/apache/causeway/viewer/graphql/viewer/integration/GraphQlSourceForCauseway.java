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

import graphql.GraphQL;
import graphql.execution.SimpleDataFetcherExceptionHandler;
import graphql.schema.GraphQLSchema;

import org.springframework.graphql.execution.GraphQlSource;
import org.springframework.stereotype.Service;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.config.metamodel.specloader.IntrospectionMode;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.ElementCustom;
import org.apache.causeway.viewer.graphql.model.domain.rich.mutation.RichTopLevelMutation;
import org.apache.causeway.viewer.graphql.model.domain.rich.query.RichTopLevelQuery;
import org.apache.causeway.viewer.graphql.model.domain.simple.mutation.SimpleTopLevelMutation;
import org.apache.causeway.viewer.graphql.model.domain.simple.query.SimpleTopLevelQuery;
import org.apache.causeway.viewer.graphql.model.registry.GraphQLTypeRegistry;
import org.apache.causeway.viewer.graphql.model.toplevel.BothTopLevelQuery;

@Service()
public class GraphQlSourceForCauseway implements GraphQlSource {

    private final CausewayConfiguration causewayConfiguration;
    private final CausewaySystemEnvironment causewaySystemEnvironment;
    private final SpecificationLoader specificationLoader;
    private final GraphQLTypeRegistry graphQLTypeRegistry;
    private final Context context;
    private final AsyncExecutionStrategyResolvingWithinInteraction executionStrategy;

    private CausewayConfiguration.Viewer.Graphql graphqlConfiguration;

    public GraphQlSourceForCauseway(
            final CausewayConfiguration causewayConfiguration,
            final CausewaySystemEnvironment causewaySystemEnvironment,
            final SpecificationLoader specificationLoader,
            final GraphQLTypeRegistry graphQLTypeRegistry,
            final Context context,
            final AsyncExecutionStrategyResolvingWithinInteraction executionStrategy) {
        this.causewayConfiguration = causewayConfiguration;
        this.causewaySystemEnvironment = causewaySystemEnvironment;
        this.specificationLoader = specificationLoader;
        this.graphQLTypeRegistry = graphQLTypeRegistry;
        this.context = context;
        this.executionStrategy = executionStrategy;

        this.graphqlConfiguration = causewayConfiguration.getViewer().getGraphql();
    }

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
                    .defaultDataFetcherExceptionHandler(new SimpleDataFetcherExceptionHandler())
                    .queryExecutionStrategy(executionStrategy)
                    .mutationExecutionStrategy(executionStrategy)
                    .build();
        }
        return graphQL;
    }

    @Override
    public GraphQLSchema schema() {

        var fullyIntrospected = specificationLoader.isMetamodelFullyIntrospected();
        if (!fullyIntrospected) {
            throw new IllegalStateException("Metamodel is not fully introspected");
        }

        var topLevelQuery = determineTopLevelQueryFrom(graphqlConfiguration.getSchemaStyle());
        var topLevelMutation = determineTopLevelMutationFrom(graphqlConfiguration.getSchemaStyle());

        topLevelQuery.addDataFetchers();
        topLevelMutation.addDataFetchers();

        // finalize the fetcher/mutator code that's been added
        var codeRegistry = context.codeRegistryBuilder.build();

        // build the schema
        return GraphQLSchema.newSchema()
                .query(topLevelQuery.getGqlObjectType())
                .mutation(topLevelMutation.getGqlObjectType())
                .additionalTypes(graphQLTypeRegistry.getGraphQLTypes())
                .additionalType(context.getLogicalTypeNames())
                .codeRegistry(codeRegistry)
                .build();
    }

    private ElementCustom determineTopLevelQueryFrom(
            final CausewayConfiguration.Viewer.Graphql.SchemaStyle schemaStyle) {
        switch (schemaStyle) {
            case SIMPLE_ONLY:
                return new SimpleTopLevelQuery(context);
            case RICH_ONLY:
                return new RichTopLevelQuery(context);
            case SIMPLE_AND_RICH:
            case RICH_AND_SIMPLE:
                return new BothTopLevelQuery(context);
            default:
                // shouldn't happen
                throw new IllegalStateException(String.format(
                        "Configured SchemaStyle '%s' not recognised", schemaStyle));
        }
    }
    private ElementCustom determineTopLevelMutationFrom(
            final CausewayConfiguration.Viewer.Graphql.SchemaStyle schemaStyle) {
        switch (schemaStyle) {
            case SIMPLE_ONLY:
                return new SimpleTopLevelMutation(context);
            case RICH_ONLY:
                return new RichTopLevelMutation(context);
            case SIMPLE_AND_RICH:
                return new SimpleTopLevelMutation(context);
            case RICH_AND_SIMPLE:
                return new RichTopLevelMutation(context);
            default:
                // shouldn't happen
                throw new IllegalStateException(String.format(
                        "Configured SchemaStyle '%s' not recognised", schemaStyle));
        }
    }
}
