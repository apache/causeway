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

import static graphql.schema.FieldCoordinates.coordinates;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import org.apache.causeway.applib.id.HasLogicalType;

import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvDomainObject;
import org.apache.causeway.viewer.graphql.model.domain.GqlvDomainService;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;
import org.apache.causeway.viewer.graphql.viewer.toplevel.GqlvTopLevelQuery;
import org.apache.causeway.viewer.graphql.model.registry.GraphQLTypeRegistry;

import org.springframework.graphql.execution.GraphQlSource;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.config.metamodel.specloader.IntrospectionMode;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import graphql.GraphQL;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLSchema;

import graphql.schema.GraphQLType;

import lombok.RequiredArgsConstructor;
import lombok.val;

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
//                .instrumentation(new TracingInstrumentation())
                    .defaultDataFetcherExceptionHandler(new DataFetcherExceptionHandler() {
                        @Override
                        public DataFetcherExceptionHandlerResult onException(DataFetcherExceptionHandlerParameters handlerParameters) {
                            return DataFetcherExceptionHandler.super.onException(handlerParameters);
                        }
                    })
                    .queryExecutionStrategy(executionStrategy)
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

        final GraphQLCodeRegistry.Builder codeRegistryBuilder = GraphQLCodeRegistry.newCodeRegistry();

        // add to the top-level query
        // (and also add behaviour to the child types)
        val topLevelQuery = new GqlvTopLevelQuery(serviceRegistry, codeRegistryBuilder);

        List<ObjectSpecification> objectSpecifications = specificationLoader.snapshotSpecifications()
                .distinct((a, b) -> a.getLogicalTypeName().equals(b.getLogicalTypeName()))
                .filter(x -> x.isEntityOrViewModelOrAbstract() || x.getBeanSort().isManagedBeanContributing())
                .sorted(Comparator.comparing(HasLogicalType::getLogicalTypeName))
                .toList();
        objectSpecifications.forEach(objectSpec -> addToSchema(objectSpec, topLevelQuery, codeRegistryBuilder));

        topLevelQuery.buildQueryType();


        topLevelQuery.addFetchers();

        // finalize the fetcher/mutator code that's been registered
        val codeRegistry = codeRegistryBuilder.build();


        // build the schema
        Set<GraphQLType> graphQLTypes = graphQLTypeRegistry.getGraphQLTypes();
        return GraphQLSchema.newSchema()
                .query(topLevelQuery.getQueryType())
                .additionalTypes(graphQLTypes)
                .codeRegistry(codeRegistry)
                .build();
    }

    private void addToSchema(
            final ObjectSpecification objectSpec,
            final GqlvTopLevelQuery gqlvTopLevelQuery,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder) {

        Context context = new Context(codeRegistryBuilder, bookmarkService, specificationLoader, typeMapper, causewayConfiguration, causewaySystemEnvironment);

        switch (objectSpec.getBeanSort()) {

            case MANAGED_BEAN_CONTRIBUTING: // @DomainService

                addDomainServiceToTopLevelQuery(objectSpec, gqlvTopLevelQuery, context);
                break;

            case ABSTRACT:
                // TODO: App interface should map to gql interfaces?
            case VIEW_MODEL: // @DomainObject(nature=VIEW_MODEL)
            case ENTITY:     // @DomainObject(nature=ENTITY)

                new GqlvDomainObject(objectSpec, context, objectManager, graphQLTypeRegistry);

                break;

            case MANAGED_BEAN_NOT_CONTRIBUTING: // a @Service or @Component ... ignore
            case MIXIN:
            case VALUE:
            case COLLECTION:
            case VETOED:
            case UNKNOWN:
                break;
        }
    }

    public void addDomainServiceToTopLevelQuery(
            final ObjectSpecification objectSpec,
            final GqlvTopLevelQuery topLevelQueryStructure,
            final Context context) {

        serviceRegistry.lookupBeanById(objectSpec.getLogicalTypeName())
            .ifPresent(servicePojo ->
                addDomainServiceToTopLevelQuery(servicePojo, objectSpec, topLevelQueryStructure, context));
    }

    private void addDomainServiceToTopLevelQuery(
            final Object servicePojo,
            final ObjectSpecification objectSpec,
            final GqlvTopLevelQuery topLevelQuery,
            final Context context) {

        val domainService = new GqlvDomainService(objectSpec, servicePojo, context);

        boolean actionsAdded = domainService.hasActions();
        if (actionsAdded) {
            topLevelQuery.addFieldFor(domainService, context.codeRegistryBuilder);
        }


    }


}
