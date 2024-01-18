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
import lombok.val;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ObjectTypeFactory {

    final static String GQL_INPUTTYPE_PREFIX = "_gql_input__";
    final static String GQL_MUTATTIONS_FIELDNAME = "_gql_mutations";
    private final BookmarkService bookmarkService;
    private final SpecificationLoader specificationLoader;
    private final ObjectManager objectManager;

    static String mutatorsTypeName(final String logicalTypeNameSanitized){
        return logicalTypeNameSanitized + "__DomainObject_mutators";
    }

    static String metaTypeName(final String logicalTypeNameSanitized){
        return logicalTypeNameSanitized + "__DomainObject_meta";
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
            final Set<GraphQLType> graphQLObjectTypes,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder) {

        val gqlvObjectSpec = GqlvObjectSpec.gqlv(objectSpec);

        // create meta field type
        BeanSort objectSpecificationBeanSort = objectSpec.getBeanSort();
        final String logicalTypeNameSanitized = gqlvObjectSpec.getLogicalTypeNameSanitized();

        GraphQLObjectType metaType = _GraphQLObjectType.create(logicalTypeNameSanitized, objectSpecificationBeanSort);

        addTypeIfNotAlreadyPresent(graphQLObjectTypes, metaType, logicalTypeNameSanitized);

        // add meta field
        val _gql_meta_Field = newFieldDefinition().name("_gql_meta").type(metaType).build();
        gqlvObjectSpec.getObjectTypeBuilder().field(_gql_meta_Field);

        // create input type
        String inputTypeName = GQL_INPUTTYPE_PREFIX + gqlvObjectSpec.getLogicalTypeNameSanitized();
        GraphQLInputObjectType.Builder inputTypeBuilder = newInputObject().name(inputTypeName);
        inputTypeBuilder
            .field(GraphQLInputObjectField.newInputObjectField()
                .name("id")
                .type(nonNull(Scalars.GraphQLID))
                .build());
        GraphQLInputType inputType = inputTypeBuilder.build();
        addTypeIfNotAlreadyPresent(graphQLObjectTypes, inputType, inputTypeName);

        // add fields
        gqlvObjectSpec.addFields();

        // add collections
        gqlvObjectSpec.addCollections();

        // add actions
        MutatorsDataForEntity mutatorsDataForEntity =
                addActions(gqlvObjectSpec.getLogicalTypeNameSanitized(), objectSpec, gqlvObjectSpec.getObjectTypeBuilder(), graphQLObjectTypes);

        // build and register object type
        GraphQLObjectType graphQLObjectType = gqlvObjectSpec.getObjectTypeBuilder().build();
        addTypeIfNotAlreadyPresent(graphQLObjectTypes, graphQLObjectType, gqlvObjectSpec.getLogicalTypeNameSanitized());

        // create and register data fetchers
        createAndRegisterDataFetchersForMetaData(
                codeRegistryBuilder, objectSpecificationBeanSort, metaType, _gql_meta_Field, graphQLObjectType);
        if (mutatorsDataForEntity!=null) {
            createAndRegisterDataFetchersForMutators(
                    codeRegistryBuilder, objectSpecificationBeanSort, mutatorsDataForEntity, graphQLObjectType);
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

    void addTypeIfNotAlreadyPresent(
            final Set<GraphQLType> graphQLObjectTypes,
            final GraphQLType typeToAdd,
            final String logicalTypeName){

        boolean present;
        if (typeToAdd.getClass().isAssignableFrom(GraphQLObjectType.class)){
            GraphQLObjectType typeToAdd1 = (GraphQLObjectType) typeToAdd;
            present = graphQLObjectTypes.stream()
                    .filter(o -> o.getClass().isAssignableFrom(GraphQLObjectType.class))
                    .map(GraphQLObjectType.class::cast)
                    .filter(ot -> ot.getName().equals(typeToAdd1.getName()))
                    .findFirst().isPresent();
        } else {
            // must be input type
            GraphQLInputObjectType typeToAdd1 = (GraphQLInputObjectType) typeToAdd;
            present = graphQLObjectTypes.stream()
                    .filter(o -> o.getClass().isAssignableFrom(GraphQLInputObjectType.class))
                    .map(GraphQLInputObjectType.class::cast)
                    .filter(ot -> ot.getName().equals(typeToAdd1.getName()))
                    .findFirst().isPresent();
        }
        if (present){
            // For now we just log and skip
            System.out.println("==== DOUBLE ====");
            System.out.println(logicalTypeName);

        } else {
            graphQLObjectTypes.add(typeToAdd);
        }
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
            final GraphQLObjectType.Builder objectTypeBuilder,
            final Set<GraphQLType> graphQLObjectTypes) {

        MutatorManager result = mutatorManager(logicalTypeNameSanitized);

        objectSpecification.streamActions(ActionScope.PRODUCTION, MixedIn.INCLUDED)
                .forEach(objectAction ->
                        addAction(objectAction, objectTypeBuilder, result.mutatorsTypeBuilder, result.mutatorsTypeFields)
                );

        if (!result.mutatorsTypeFields.isEmpty()){
            GraphQLObjectType mutatorsType = result.mutatorsTypeBuilder.build();
            addTypeIfNotAlreadyPresent(graphQLObjectTypes, mutatorsType, result.mutatorsTypeName);
            GraphQLFieldDefinition gql_mutations = newFieldDefinition()
                    .name(GQL_MUTATTIONS_FIELDNAME)
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
//                    return new GqlMutations(bookmark, bookmarkService, mutatorsTypeFields);
//                }
//            });
//
//            // for each field something like
//            codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(mutatorsType, idField), new DataFetcher<Object>() {
//                @Override
//                public Object get(DataFetchingEnvironment environment) throws Exception {
//
//                    GqlMeta gqlMeta = environment.getSource();
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
            final BeanSort objectSpecificationBeanSort,
            final GraphQLObjectType metaType,
            final GraphQLFieldDefinition gql_meta,
            final GraphQLObjectType graphQLObjectType) {

        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(graphQLObjectType, gql_meta), (DataFetcher<Object>) environment -> {
            return bookmarkService.bookmarkFor(environment.getSource())
                    .map(bookmark -> new GqlMeta(bookmark, bookmarkService, objectManager))
                    .orElse(null); //TODO: is this correct ?
        });

        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(metaType, Fields.id), (DataFetcher<Object>) environment -> {
            GqlMeta gqlMeta = environment.getSource();
            return gqlMeta.id();
        });

        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(metaType, Fields.logicalTypeName), (DataFetcher<Object>) environment -> {
            GqlMeta gqlMeta = environment.getSource();
            return gqlMeta.logicalTypeName();
        });

        if (objectSpecificationBeanSort == BeanSort.ENTITY) {
            codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(metaType, Fields.version), (DataFetcher<Object>) environment -> {
                GqlMeta gqlMeta = environment.getSource();
                return gqlMeta.version();
            });

        }
    }

}
