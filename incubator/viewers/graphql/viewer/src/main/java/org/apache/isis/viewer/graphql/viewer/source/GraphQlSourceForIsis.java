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
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.core.metamodel.spec.ManagedObject;
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

        GraphQLObjectType.Builder queryLookupTypeBuilder = newObject().name("_gql_Query_lookup");
        final List<ObjectSpecification> entityObjectSpecs = new ArrayList<>();

        specificationLoader.forEach(objectSpecification -> {

            val logicalTypeName = objectSpecification.getLogicalTypeName();
            String logicalTypeNameSanitized = _Utils.logicalTypeNameSanitized(logicalTypeName);

            switch (objectSpecification.getBeanSort()) {

                case ABSTRACT:
                case VIEW_MODEL: // @DomainObject(nature=VIEW_MODEL)

                    // TODO: App interface should mapp to gql interfaces?
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
        }, false);

        // TODO: make all dynamic
        GraphQLObjectType paramMetaDataType = newObject().name(_Utils.SINGLE_PARAM_META_DATA_TYPENAME)
                .field(newFieldDefinition().name("optionality").type(Scalars.GraphQLBoolean).build())
                .field(newFieldDefinition().name("default").type(Scalars.GraphQLString).build()) // for now
                .field(newFieldDefinition().name("choices").type(Scalars.GraphQLString).build()) // for now
                .field(newFieldDefinition().name("autocomplete").argument(GraphQLArgument.newArgument().name("we_call_search_for_now").type(Scalars.GraphQLString).build()).type(Scalars.GraphQLBoolean).build()) // for now
                .field(newFieldDefinition().name("validate").argument(GraphQLArgument.newArgument().name("we_call_value_for_now").type(Scalars.GraphQLString).build()).type(Scalars.GraphQLString).build())
                .build();
        graphQLObjectTypes.add(paramMetaDataType);
        GraphQLObjectType paramsMetaDataType = newObject().name(_Utils.PARAMS_META_DATA_TYPENAME)
                .field(newFieldDefinition().name("object_action_name").type(paramMetaDataType).build())
                .build();
        graphQLObjectTypes.add(paramsMetaDataType);
        GraphQLObjectType fieldMetaDataType = newObject().name(_Utils.FIELD_META_DATA_TYPENAME)
                .field(newFieldDefinition().name("params").type(paramsMetaDataType).build())
                .field(newFieldDefinition().name("validate").type(Scalars.GraphQLString).build())
                .field(newFieldDefinition().name("hide").type(Scalars.GraphQLBoolean).build())
                .field(newFieldDefinition().name("disable").type(Scalars.GraphQLString).build())
                .build();
        graphQLObjectTypes.add(fieldMetaDataType);
        GraphQLObjectType mutatorMetaDataType = newObject().name(_Utils.MUTATOR_META_DATA_TYPENAME)
                .field(newFieldDefinition().name("params").type(paramsMetaDataType).build())
                .field(newFieldDefinition().name("validate").type(Scalars.GraphQLString).build())
                .field(newFieldDefinition().name("hide").type(Scalars.GraphQLBoolean).build())
                .field(newFieldDefinition().name("disable").type(Scalars.GraphQLString).build())
                .build();
        graphQLObjectTypes.add(mutatorMetaDataType);

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
                    return bookmarkService.lookup(bookmark).map(p->ManagedObject.of(entity, p).getPojo()).orElse(null);
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
