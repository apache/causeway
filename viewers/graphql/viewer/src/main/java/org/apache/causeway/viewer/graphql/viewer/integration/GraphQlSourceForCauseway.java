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

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
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
import org.apache.causeway.viewer.graphql.applib.types.TypeMapper;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvDomainObject;
import org.apache.causeway.viewer.graphql.model.registry.GraphQLTypeRegistry;
import org.apache.causeway.viewer.graphql.viewer.toplevel.GqlvTopLevelQuery;

import lombok.RequiredArgsConstructor;
import lombok.val;

import graphql.GraphQL;
import graphql.execution.SimpleDataFetcherExceptionHandler;
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
                    .defaultDataFetcherExceptionHandler(new SimpleDataFetcherExceptionHandler())
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

        // add to the top-level query type and (dependent on configuration) the top-level mutation type also
        val topLevelQuery = new GqlvTopLevelQuery(serviceRegistry, codeRegistryBuilder);
        val topLevelMutation =
                causewayConfiguration.getViewer().getGraphql().getApiVariant() == CausewayConfiguration.Viewer.Graphql.ApiVariant.QUERY_AND_MUTATIONS ?
                    new GqlvTopLevelMutation(context)
                    : null;


        val objectSpecifications = specificationLoader.snapshotSpecifications()
                .filter(x -> x.getCorrespondingClass().getPackage() != Either.class.getPackage())   // exclude the org.apache_causeway.commons.functional
                .distinct((a, b) -> a.getLogicalTypeName().equals(b.getLogicalTypeName()))
                .filter(x -> x.isEntityOrViewModelOrAbstract() || x.getBeanSort().isManagedBeanContributing())
                .sorted(Comparator.comparing(HasLogicalType::getLogicalTypeName))
                .toList();

        // add to top-level query
        objectSpecifications.forEach(objectSpec -> {
            switch (objectSpec.getBeanSort()) {
                case MANAGED_BEAN_CONTRIBUTING: // @DomainService
                    serviceRegistry.lookupBeanById(objectSpec.getLogicalTypeName())
                        .ifPresent(servicePojo -> topLevelQuery.addDomainServiceTo(objectSpec, servicePojo, context));
                    break;
            }
        });
        topLevelQuery.buildQueryType();

        // add top-level mutation (if application configuration requires it)
        if (topLevelMutation != null) {
            objectSpecifications.forEach(objectSpec -> {
                objectSpec.streamActions(context.getActionScope(), MixedIn.INCLUDED)
                        .filter(x -> ! x.getSemantics().isSafeInNature())
                        .forEach(objectAction -> topLevelMutation.addAction(objectSpec, objectAction));
                objectSpec.streamProperties(MixedIn.INCLUDED)
                        .filter(property -> ! property.isAlwaysHidden())
                        .filter(property -> property.containsFacet(PropertySetterFacet.class))
                        .forEach(property -> topLevelMutation.addProperty(objectSpec, property));

            });
            topLevelMutation.buildMutationType();
            topLevelMutation.addDataFetchers();
        }

        // add remaining domain objects
        objectSpecifications.forEach(objectSpec -> {
            switch (objectSpec.getBeanSort()) {

                case ABSTRACT:
                case VIEW_MODEL: // @DomainObject(nature=VIEW_MODEL)
                case ENTITY:     // @DomainObject(nature=ENTITY)

                    val gqlvDomainObject = new GqlvDomainObject(objectSpec, context, objectManager, graphQLTypeRegistry);
                    gqlvDomainObject.addTypesInto(graphQLTypeRegistry);
                    gqlvDomainObject.addDataFetchers();

                    break;
            }
        });



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
