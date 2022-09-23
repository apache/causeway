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
package org.apache.isis.viewer.graphql.viewer.source;

import java.util.*;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import graphql.schema.*;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.core.metamodel.object.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.springframework.graphql.execution.GraphQlSource;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.environment.IsisSystemEnvironment;
import org.apache.isis.core.config.metamodel.specloader.IntrospectionMode;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import lombok.RequiredArgsConstructor;
import lombok.val;

import graphql.GraphQL;
import graphql.Scalars;

import static graphql.schema.FieldCoordinates.coordinates;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLObjectType.newObject;

@Service()
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class GraphQlSourceForIsis implements GraphQlSource {

    private final ServiceRegistry serviceRegistry;
    private final SpecificationLoader specificationLoader;
    private final IsisConfiguration isisConfiguration;
    private final IsisSystemEnvironment isisSystemEnvironment;
    private final ExecutionStrategyResolvingWithinInteraction executionStrategy;
    private final ObjectTypeFactory objectTypeFactory;
    private final QueryFieldFactory queryFieldFactory;
    private final BookmarkService bookmarkService;

    @PostConstruct
    public void init() {
        boolean fullyIntrospect = IntrospectionMode.isFullIntrospect(isisConfiguration, isisSystemEnvironment);
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

        GraphQLEnumType semanticsEnumType = GraphQLEnumType.newEnum().name(_Utils.GQL_SEMANTICS_TYPENAME)
                .value(GraphQLEnumValueDefinition.newEnumValueDefinition().name("SAFE").value(SemanticsOf.SAFE).build())
                .value(GraphQLEnumValueDefinition.newEnumValueDefinition().name("SAFE_AND_REQUEST_CACHEABLE").value(SemanticsOf.SAFE_AND_REQUEST_CACHEABLE).build())
                .value(GraphQLEnumValueDefinition.newEnumValueDefinition().name("IDEMPOTENT").value(SemanticsOf.IDEMPOTENT).build())
                .value(GraphQLEnumValueDefinition.newEnumValueDefinition().name("IDEMPOTENT_ARE_YOU_SURE").value(SemanticsOf.IDEMPOTENT_ARE_YOU_SURE).build())
                .value(GraphQLEnumValueDefinition.newEnumValueDefinition().name("NON_IDEMPOTENT").value(SemanticsOf.NON_IDEMPOTENT).build())
                .value(GraphQLEnumValueDefinition.newEnumValueDefinition().name("NON_IDEMPOTENT_ARE_YOU_SURE").value(SemanticsOf.NON_IDEMPOTENT_ARE_YOU_SURE).build())
                .value(GraphQLEnumValueDefinition.newEnumValueDefinition().name("NOT_SPECIFIED").value(SemanticsOf.NOT_SPECIFIED).build())
                .build();
        graphQLObjectTypes.add(semanticsEnumType);

        GraphQLObjectType structureType = newObject().name(_Utils.GQL_GENERIC_STRUCTURE_TYPENAME)
                .field(newFieldDefinition().name("properties").type(GraphQLList.list(Scalars.GraphQLString)))
                .field(newFieldDefinition().name("collections").type(GraphQLList.list(Scalars.GraphQLString)))
                .field(newFieldDefinition().name("actions").type(GraphQLList.list(Scalars.GraphQLString)))
                .field(newFieldDefinition().name("layoutXml").type(Scalars.GraphQLString))
                .build();
        graphQLObjectTypes.add(structureType);

        GraphQLObjectType.Builder queryLookupTypeBuilder = newObject().name("_gql_Query_lookup");
        final List<ObjectSpecification> entityObjectSpecs = new ArrayList<>();

        specificationLoader.forEach(objectSpecification -> {

            val logicalTypeName = objectSpecification.getLogicalTypeName();
            String logicalTypeNameSanitized = _Utils.logicalTypeNameSanitized(logicalTypeName);

            switch (objectSpecification.getBeanSort()) {

                case ABSTRACT:
                case VIEW_MODEL: // @DomainObject(nature=VIEW_MODEL)

                    // TODO: App interface should map to gql interfaces?
                    objectTypeFactory
                            .objectTypeFromObjectSpecification(objectSpecification, graphQLObjectTypes, codeRegistryBuilder);


                    break;

                case ENTITY:    // @DomainObject(nature=ENTITY)

                    GraphQLFieldDefinition fd = newFieldDefinition()
                            .name(logicalTypeNameSanitized)
                            .type(GraphQLTypeReference.typeRef(logicalTypeNameSanitized))
                            .argument(GraphQLArgument.newArgument().name("id").type(nonNull(Scalars.GraphQLID)).build())
                            .build();
                    queryLookupTypeBuilder.field(fd);
                    entityObjectSpecs.add(objectSpecification);

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

        // can remain 'static' for all fields / collections (having no params)
        GraphQLObjectType propertiesGenericType = newObject().name(_Utils.GQL_GENERIC_PROPERTY_TYPENAME)
                .field(newFieldDefinition().name("hide").type(Scalars.GraphQLBoolean).build())
                .field(newFieldDefinition().name("disable").type(Scalars.GraphQLString).build())
                .build();
        graphQLObjectTypes.add(propertiesGenericType);

        GraphQLObjectType collectionsGenericType = newObject().name(_Utils.GQL_GENERIC_COLLECTION_TYPENAME)
                .field(newFieldDefinition().name("hide").type(Scalars.GraphQLBoolean).build())
                .field(newFieldDefinition().name("disable").type(Scalars.GraphQLString).build())
                .build();
        graphQLObjectTypes.add(collectionsGenericType);

        GraphQLObjectType queryLookupType = queryLookupTypeBuilder.build();
        graphQLObjectTypes.add(queryLookupType);
        val gql_query_lookup = newFieldDefinition()
                .name("_gql_Query_lookup")
                .type(queryLookupType)
                .build();

        val query_numServices = newFieldDefinition()
                .name("numServices")
                .type(Scalars.GraphQLInt)
                .build();

        GraphQLObjectType query = queryBuilder
                .field(query_numServices)
                .field(gql_query_lookup)
                .build();

        codeRegistryBuilder.dataFetcher(coordinates(query.getName(), gql_query_lookup.getName()), (DataFetcher<Object>) environment -> {
            return entityObjectSpecs;
        });

        entityObjectSpecs.forEach(entity->{
            String s = _Utils.logicalTypeNameSanitized(entity.getLogicalTypeName());
            codeRegistryBuilder.dataFetcher((coordinates(gql_query_lookup.getName(), s)), new DataFetcher<Object>() {

                @Override
                public Object get(DataFetchingEnvironment environment) throws Exception {

                    Map<String, Object> arguments = environment.getArguments();
                    String objectId = (String) arguments.get("id");
                    Bookmark bookmark = Bookmark.forLogicalTypeNameAndIdentifier(entity.getLogicalTypeName(), objectId);
                    return bookmarkService.lookup(bookmark).map(p->ManagedObject.adaptSingular(entity, p).getPojo()).orElse(null);
                }

            });

        });

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
