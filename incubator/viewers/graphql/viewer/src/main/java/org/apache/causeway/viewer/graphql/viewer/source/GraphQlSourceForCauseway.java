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
package org.apache.causeway.viewer.graphql.viewer.source;

import static graphql.schema.FieldCoordinates.coordinates;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.graphql.execution.GraphQlSource;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.config.metamodel.specloader.IntrospectionMode;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import graphql.GraphQL;
import graphql.Scalars;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import lombok.RequiredArgsConstructor;
import lombok.val;

@Service()
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class GraphQlSourceForCauseway implements GraphQlSource {

    private final ServiceRegistry serviceRegistry;
    private final SpecificationLoader specificationLoader;
    private final CausewayConfiguration causewayConfiguration;
    private final CausewaySystemEnvironment causewaySystemEnvironment;
    private final ExecutionStrategyResolvingWithinInteraction executionStrategy;
    private final ObjectTypeFactory objectTypeFactory;
    private final QueryFieldFactory queryFieldFactory;

    @PostConstruct
    public void init() {
        boolean fullyIntrospect = IntrospectionMode.isFullIntrospect(causewayConfiguration, causewaySystemEnvironment);
        if (!fullyIntrospect) {
            throw new IllegalStateException("GraphQL requires full introspection mode");
        }
    }

    @Override
    public GraphQL graphQl() {
        return GraphQL.newGraphQL(schema())
//                .instrumentation(new TracingInstrumentation())
                .queryExecutionStrategy(executionStrategy)
                .build();
    }

    @Override
    public GraphQLSchema schema() {

        val fullyIntrospected = specificationLoader.isMetamodelFullyIntrospected();
        if (!fullyIntrospected) {
            throw new IllegalStateException("Metamodel is not fully introspected");
        }

        val queryBuilder = newObject().name("Query");
        GraphQLCodeRegistry.Builder codeRegistryBuilder = GraphQLCodeRegistry.newCodeRegistry();

        Set<GraphQLType> graphQLObjectTypes = new HashSet<>();

        specificationLoader.snapshotSpecifications()
        .distinct((a, b) -> a.getLogicalTypeName().equals(b.getLogicalTypeName()))
        .forEach(objectSpecification -> {

            val logicalTypeName = objectSpecification.getLogicalTypeName();
            String logicalTypeNameSanitized = _Utils.logicalTypeNameSanitized(logicalTypeName);

            switch (objectSpecification.getBeanSort()) {

                case ABSTRACT:
                case VIEW_MODEL: // @DomainObject(nature=VIEW_MODEL)
                case ENTITY:    // @DomainObject(nature=ENTITY)

                    // TODO: App interface should mapp to gql interfaces?
                    objectTypeFactory
                        .objectTypeFromObjectSpecification(objectSpecification, graphQLObjectTypes, codeRegistryBuilder);

                    break;

                case MANAGED_BEAN_CONTRIBUTING: //@DomainService

                    queryFieldFactory
                        .queryFieldFromObjectSpecification(queryBuilder, codeRegistryBuilder, objectSpecification);
                    break;

                case MANAGED_BEAN_NOT_CONTRIBUTING: // a @Service or @Component ... ignore
                case MIXIN:
                case VALUE:
                case COLLECTION:
                case VETOED:
                case UNKNOWN:
                    break;
            }
        });

        val query_numServices = newFieldDefinition()
                .name("numServices")
                .type(Scalars.GraphQLInt)
                .build();

        GraphQLObjectType query = queryBuilder
                .field(query_numServices)
                .build();


        val codeRegistry = codeRegistryBuilder
                .dataFetcher(coordinates(query.getName(), query_numServices.getName()),
                        (DataFetcher<Object>) environment -> this.serviceRegistry.streamRegisteredBeans().count())
                .build();


        return GraphQLSchema.newSchema()
                .query(query)
                .additionalTypes(graphQLObjectTypes)
                .codeRegistry(codeRegistry)
                .build();
    }

}
