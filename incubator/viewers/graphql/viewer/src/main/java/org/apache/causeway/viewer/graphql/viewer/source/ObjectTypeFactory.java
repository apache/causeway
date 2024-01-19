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

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectType.newInputObject;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLObjectType.newObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;

import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import graphql.Scalars;
import graphql.schema.DataFetcher;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import lombok.val;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class ObjectTypeFactory {

    final static String GQL_INPUTTYPE_PREFIX = "_gql_input__";
    final static String GQL_MUTATIONS_FIELDNAME = "_gql_mutations";

    private final BookmarkService bookmarkService;
    private final SpecificationLoader specificationLoader;
    private final ObjectManager objectManager;
    private final GraphQLTypeRegistry graphQLTypeRegistry;

    static String mutatorsTypeName(final String logicalTypeNameSanitized){
        return logicalTypeNameSanitized + "__DomainObject_mutators";
    }

    @UtilityClass
    static class Fields {
        static GraphQLFieldDefinition id =
                newFieldDefinition()
                    .name("id")
                    .type(nonNull(Scalars.GraphQLString))
                    .build();
        static GraphQLFieldDefinition logicalTypeName =
                newFieldDefinition()
                    .name("logicalTypeName")
                    .type(nonNull(Scalars.GraphQLString))
                    .build();
        static GraphQLFieldDefinition version =
                newFieldDefinition()
                    .name("version")
                    .type(Scalars.GraphQLString).build();
    }

    public void objectTypeFromObjectSpecification(
            final ObjectSpecification objectSpec,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder) {

        val gqlvObjectSpec = new GqlvObjectSpec(objectSpec);

        graphQLTypeRegistry.addTypeIfNotAlreadyPresent(gqlvObjectSpec.getMetaType());


        // create input type
//        String inputTypeName = GQL_INPUTTYPE_PREFIX + gqlvObjectSpec.getLogicalTypeNameSanitized();
//        GraphQLInputObjectType.Builder inputTypeBuilder = newInputObject().name(inputTypeName);
//        inputTypeBuilder
//            .field(GraphQLInputObjectField.newInputObjectField()
//                .name("id")
//                .type(nonNull(Scalars.GraphQLID))
//                .build());
//        GraphQLInputType inputType = inputTypeBuilder.build();
        GraphQLInputType inputType = gqlvObjectSpec.getInputType();
        graphQLTypeRegistry.addTypeIfNotAlreadyPresent(inputType);

        // add fields
        gqlvObjectSpec.addFields();

        // add collections
        gqlvObjectSpec.addCollections();

        // add actions
        MutatorsDataForEntity mutatorsDataForEntity =
                addActions(gqlvObjectSpec.getLogicalTypeNameSanitized(), objectSpec, gqlvObjectSpec.getGqlObjectTypeBuilder());

        // build and register object type
        GraphQLObjectType graphQLObjectType = gqlvObjectSpec.buildGqlObjectType();
        graphQLTypeRegistry.addTypeIfNotAlreadyPresent(graphQLObjectType);


        // create and register data fetchers
        createAndRegisterDataFetchersForMetaData(codeRegistryBuilder, gqlvObjectSpec);
        if (mutatorsDataForEntity!=null) {
            createAndRegisterDataFetchersForMutators(
                    codeRegistryBuilder, gqlvObjectSpec.getBeanSort(), mutatorsDataForEntity, gqlvObjectSpec.getGqlObjectType());
        }
        createAndRegisterDataFetchersForField(objectSpec, codeRegistryBuilder, graphQLObjectType);
        createAndRegisterDataFetchersForCollection(objectSpec, codeRegistryBuilder, graphQLObjectType);
    }

    private void createAndRegisterDataFetchersForMutators(
            final GraphQLCodeRegistry.Builder codeRegistryBuilder,
            final BeanSort objectSpecificationBeanSort,
            final MutatorsDataForEntity mutatorsDataForEntity,
            final GraphQLObjectType graphQLObjectType) {

    }

    void createAndRegisterDataFetchersForField(
            final ObjectSpecification objectSpecification,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder,
            final GraphQLObjectType graphQLObjectType) {
        objectSpecification.streamProperties(MixedIn.INCLUDED)
        .forEach(otoa -> {

            createAndRegisterDataFetcherForObjectAssociation(codeRegistryBuilder, graphQLObjectType, otoa);

        });
    }

    void createAndRegisterDataFetchersForCollection(
            final ObjectSpecification objectSpecification,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder,
            final GraphQLObjectType graphQLObjectType) {

        objectSpecification.streamCollections(MixedIn.INCLUDED)
                .forEach(otom -> {
                    createAndRegisterDataFetcherForObjectAssociation(codeRegistryBuilder, graphQLObjectType, otom);
                });
    }

    MutatorsDataForEntity addActions(
            final String logicalTypeNameSanitized,
            final ObjectSpecification objectSpecification,
            final GraphQLObjectType.Builder objectTypeBuilder) {

        MutatorManager result = mutatorManager(logicalTypeNameSanitized);

        objectSpecification.streamActions(ActionScope.PRODUCTION, MixedIn.INCLUDED)
                .forEach(objectAction ->
                        addAction(objectAction, objectTypeBuilder, result.mutatorsTypeBuilder, result.mutatorsTypeFields)
                );

        if (!result.mutatorsTypeFields.isEmpty()){
            GraphQLObjectType mutatorsType = result.mutatorsTypeBuilder.build();
            graphQLTypeRegistry.addTypeIfNotAlreadyPresent(mutatorsType, result.mutatorsTypeName);
            GraphQLFieldDefinition gql_mutations = newFieldDefinition()
                    .name(GQL_MUTATIONS_FIELDNAME)
                    .type(mutatorsType)
                    .build();
            objectTypeBuilder.field(gql_mutations);

            return new MutatorsDataForEntity(mutatorsType, result.mutatorsTypeFields);

//            // I think we have to create and register data fetcher for mutations here, but we can't since we have no objectTypeYet
//            codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(graphQLTypeReference, gql_mutations), new DataFetcher<Object>() {
//                @Override
//                public Object get(DataFetchingEnvironment environment) throws Exception {
//
//                    Bookmark bookmark = bookmarkService.bookmarkFor(environment.getSource()).orElse(null);
//                    if (bookmark == null) return null; //TODO: is this correct ?
//                    return new GqlvMutations(bookmark, bookmarkService, mutatorsTypeFields);
//                }
//            });
//
//            // for each field something like
//            codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(mutatorsType, idField), new DataFetcher<Object>() {
//                @Override
//                public Object get(DataFetchingEnvironment environment) throws Exception {
//
//                    GqlvMeta gqlMeta = environment.getSource();
//
//                    return gqlMeta.id();
//                }
//            });
        }

        return null;

    }

    private static MutatorManager mutatorManager(String logicalTypeNameSanitized) {
        val mutatorsTypeName = mutatorsTypeName(logicalTypeNameSanitized);
        GraphQLObjectType.Builder mutatorsTypeBuilder = newObject().name(mutatorsTypeName);
        final List<GraphQLFieldDefinition> mutatorsTypeFields = new ArrayList<>();
        MutatorManager result = new MutatorManager(mutatorsTypeName, mutatorsTypeBuilder, mutatorsTypeFields);
        return result;
    }

    private static class MutatorManager {
        public final String mutatorsTypeName;
        public final GraphQLObjectType.Builder mutatorsTypeBuilder;
        public final List<GraphQLFieldDefinition> mutatorsTypeFields;

        public MutatorManager(String mutatorsTypeName, GraphQLObjectType.Builder mutatorsTypeBuilder, List<GraphQLFieldDefinition> mutatorsTypeFields) {
            this.mutatorsTypeName = mutatorsTypeName;
            this.mutatorsTypeBuilder = mutatorsTypeBuilder;
            this.mutatorsTypeFields = mutatorsTypeFields;
        }
    }

    private static void addAction(ObjectAction objectAction, GraphQLObjectType.Builder objectTypeBuilder, GraphQLObjectType.Builder mutatorsTypeBuilder, List<GraphQLFieldDefinition> mutatorsTypeFields) {
        if (objectAction.getSemantics().isSafeInNature()) {

            String fieldName = objectAction.getId();
            GraphQLFieldDefinition.Builder builder = newFieldDefinition()
                    .name(fieldName)
                    .type((GraphQLOutputType) TypeMapper.typeForObjectAction(objectAction));
            if (objectAction.getParameters().isNotEmpty()) {
                builder.arguments(objectAction.getParameters().stream()
                        .map(objectActionParameter -> GraphQLArgument.newArgument()
                                .name(objectActionParameter.getId())
                                .type(objectActionParameter.isOptional()
                                        ? TypeMapper.inputTypeFor(objectActionParameter)
                                        : nonNull(TypeMapper.inputTypeFor(objectActionParameter)))
                                .build())
                        .collect(Collectors.toList()));
            }
            objectTypeBuilder.field(builder);

        } else {

            String fieldName = objectAction.getId();
            GraphQLFieldDefinition.Builder builder = newFieldDefinition()
                    .name(fieldName)
                    .type((GraphQLOutputType) TypeMapper.typeForObjectAction(objectAction));
            if (objectAction.getParameters().isNotEmpty()) {
                builder.arguments(objectAction.getParameters().stream()
                        .map(objectActionParameter -> GraphQLArgument.newArgument()
                                .name(objectActionParameter.getId())
                                .type(objectActionParameter.isOptional()
                                        ? TypeMapper.inputTypeFor(objectActionParameter)
                                        : nonNull(TypeMapper.inputTypeFor(objectActionParameter)))
                                .build())
                        .collect(Collectors.toList()));
            }

            GraphQLFieldDefinition fieldDefinition = builder.build();
            mutatorsTypeBuilder.field(fieldDefinition);
            mutatorsTypeFields.add(fieldDefinition);

        }
    }

    @Data
    @AllArgsConstructor
    class MutatorsDataForEntity {

        private GraphQLObjectType mutatorsType;

        private List<GraphQLFieldDefinition> mutatorsTypeFields;

    }

    private void createAndRegisterDataFetcherForObjectAssociation(
            final GraphQLCodeRegistry.Builder codeRegistryBuilder,
            final GraphQLObjectType graphQLObjectType,
            final ObjectAssociation otom) {

        ObjectSpecification fieldObjectSpecification = otom.getElementType();
        BeanSort beanSort = fieldObjectSpecification.getBeanSort();
        switch (beanSort) {

            case VALUE: //TODO: does this work for values as well?

            case VIEW_MODEL:

            case ENTITY:

                codeRegistryBuilder
                .dataFetcher(
                    FieldCoordinates.coordinates(graphQLObjectType, otom.getId()),
                    (DataFetcher<Object>) environment -> {

                        Object domainObjectInstance = environment.getSource();

                        Class<?> domainObjectInstanceClass = domainObjectInstance.getClass();
                        ObjectSpecification specification = specificationLoader.loadSpecification(domainObjectInstanceClass);

                        ManagedObject owner = ManagedObject.adaptSingular(specification, domainObjectInstance);

                        ManagedObject managedObject = otom.get(owner);

                        return managedObject!=null ? managedObject.getPojo() : null;

                    });


                break;

        }
    }


    GraphQLObjectType createAndRegisterMutatorsType(
            final String logicalTypeNameSanitized,
            final BeanSort objectSpecificationBeanSort,
            final Set<GraphQLType> graphQLObjectTypes) {

        //TODO: this is not going to work, because we need to dynamically add fields
        String mutatorsTypeName = mutatorsTypeName(logicalTypeNameSanitized);
        GraphQLObjectType.Builder mutatorsTypeBuilder = newObject().name(mutatorsTypeName);
        GraphQLObjectType mutatorsType = mutatorsTypeBuilder.build();
        graphQLObjectTypes.add(mutatorsType);
        return mutatorsType;
    }

    void createAndRegisterDataFetchersForMetaData(
            final GraphQLCodeRegistry.Builder codeRegistryBuilder,
            final GqlvObjectSpec gqlvObjectSpec) {

        codeRegistryBuilder.dataFetcher(
                FieldCoordinates.coordinates(gqlvObjectSpec.getGqlObjectType(), gqlvObjectSpec.getMetaField()),
                (DataFetcher<Object>) environment -> {
                    return bookmarkService.bookmarkFor(environment.getSource())
                            .map(bookmark -> new GqlvMeta(bookmark, bookmarkService, objectManager))
                            .orElse(null); //TODO: is this correct ?
                });

        codeRegistryBuilder.dataFetcher(
                FieldCoordinates.coordinates(gqlvObjectSpec.getMetaType(), Fields.id),
                (DataFetcher<Object>) environment -> {
                    GqlvMeta gqlvMeta = environment.getSource();
                    return gqlvMeta.id();
                });

        codeRegistryBuilder.dataFetcher(
                FieldCoordinates.coordinates(gqlvObjectSpec.getMetaType(), Fields.logicalTypeName),
                (DataFetcher<Object>) environment -> {
                    GqlvMeta gqlvMeta = environment.getSource();
                    return gqlvMeta.logicalTypeName();
                });

        if (gqlvObjectSpec.getBeanSort() == BeanSort.ENTITY) {
            codeRegistryBuilder.dataFetcher(
                    FieldCoordinates.coordinates(gqlvObjectSpec.getMetaType(), Fields.version),
                    (DataFetcher<Object>) environment -> {
                        GqlvMeta gqlvMeta = environment.getSource();
                        return gqlvMeta.version();
                    });
        }
    }

}
