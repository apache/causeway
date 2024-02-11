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

import java.util.Comparator;
import java.util.LinkedHashMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.viewer.graphql.viewer.toplevel.GqlvTopLevelMutation;

import org.springframework.graphql.execution.GraphQlSource;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.id.HasLogicalType;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.config.metamodel.specloader.IntrospectionMode;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvDomainObject;
import org.apache.causeway.viewer.graphql.model.registry.GraphQLTypeRegistry;
import org.apache.causeway.viewer.graphql.viewer.toplevel.GqlvTopLevelQuery;

import lombok.RequiredArgsConstructor;
import lombok.val;

import graphql.GraphQL;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLSchema;

@Service()
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class GraphQlSourceForCauseway implements GraphQlSource {

    private final CausewayConfiguration causewayConfiguration;
    private final CausewaySystemEnvironment causewaySystemEnvironment;
    private final SpecificationLoader specificationLoader;
    private final ServiceRegistry serviceRegistry;
    private final ObjectManager objectManager;
    private final BookmarkService bookmarkService;
    private final GraphQLTypeRegistry graphQLTypeRegistry;
    private final TypeMapper typeMapper;
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

        val codeRegistryBuilder = GraphQLCodeRegistry.newCodeRegistry();
        val context = new Context(codeRegistryBuilder, bookmarkService, specificationLoader, typeMapper, serviceRegistry, causewayConfiguration, causewaySystemEnvironment);

        // domain objects
        val domainObjects = new LinkedHashMap<ObjectSpecification, GqlvDomainObject>();
        context.objectSpecifications().forEach(objectSpec -> {
            switch (objectSpec.getBeanSort()) {

                case ABSTRACT:
                case VIEW_MODEL: // @DomainObject(nature=VIEW_MODEL)
                case ENTITY:     // @DomainObject(nature=ENTITY)

                    val domainObject = new GqlvDomainObject(objectSpec, context, objectManager, graphQLTypeRegistry);
                    domainObject.addTypesInto(graphQLTypeRegistry);
                    domainObject.addDataFetchers();

                    domainObjects.put(objectSpec, domainObject);

                    break;
            }
        });

        // top-level query type and (dependent on configuration) the top-level mutation type
        val topLevelQuery = new GqlvTopLevelQuery(context, domainObjects);


        val topLevelMutation =
                causewayConfiguration.getViewer().getGraphql().getApiVariant() == CausewayConfiguration.Viewer.Graphql.ApiVariant.QUERY_AND_MUTATIONS ?
                        new GqlvTopLevelMutation(context)
                        : null;


        // finalize the fetcher/mutator code that's been registered
        val codeRegistry = codeRegistryBuilder.build();

        // build the schema
        val schemaBuilder = GraphQLSchema.newSchema()
                .query(topLevelQuery.getQueryType())
                .additionalTypes(graphQLTypeRegistry.getGraphQLTypes())
                .codeRegistry(codeRegistry);
        if (topLevelMutation != null) {
            schemaBuilder.mutation(topLevelMutation.getGqlObjectType());
        }
        return schemaBuilder
                .build();
    }


}
