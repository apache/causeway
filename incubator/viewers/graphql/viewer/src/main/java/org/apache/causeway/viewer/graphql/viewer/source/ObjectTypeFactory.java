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

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import static org.apache.causeway.viewer.graphql.viewer.source._Utils.metaTypeName;
import static org.apache.causeway.viewer.graphql.viewer.source._Utils.mutatorsTypeName;

import graphql.Scalars;
import graphql.schema.DataFetcher;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.val;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ObjectTypeFactory {

    private final BookmarkService bookmarkService;
    private final SpecificationLoader specificationLoader;

    private static GraphQLFieldDefinition idField = newFieldDefinition()
            .name("id").type(nonNull(Scalars.GraphQLString)).build();

    private static GraphQLFieldDefinition logicalTypeNameField = newFieldDefinition()
            .name("logicalTypeName").type(nonNull(Scalars.GraphQLString)).build();

    private static GraphQLFieldDefinition versionField = newFieldDefinition()
            .name("version").type(Scalars.GraphQLString).build();

    public void objectTypeFromObjectSpecification(
            final ObjectSpecification objectSpecification,
            final Set<GraphQLType> graphQLObjectTypes,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder) {

        val logicalTypeName = objectSpecification.getLogicalTypeName();
        String logicalTypeNameSanitized = _Utils.logicalTypeNameSanitized(logicalTypeName);

        GraphQLObjectType.Builder objectTypeBuilder = newObject().name(logicalTypeNameSanitized);

        // create meta field type
        BeanSort objectSpecificationBeanSort = objectSpecification.getBeanSort();
        GraphQLObjectType metaType =
                createAndRegisterMetaType(logicalTypeNameSanitized, objectSpecificationBeanSort, graphQLObjectTypes);

        // add meta field
        GraphQLFieldDefinition gql_meta = newFieldDefinition().name("_gql_meta").type(metaType).build();
        objectTypeBuilder.field(gql_meta);

        // create input type
        String inputTypeName = _Utils.GQL_INPUTTYPE_PREFIX + logicalTypeNameSanitized;
        GraphQLInputObjectType.Builder inputTypeBuilder = newInputObject().name(inputTypeName);
        inputTypeBuilder
            .field(GraphQLInputObjectField.newInputObjectField()
                .name("id")
                .type(nonNull(Scalars.GraphQLID))
                .build());
        GraphQLInputType inputType = inputTypeBuilder.build();
        addTypeIfNotAlreadyPresent(graphQLObjectTypes, inputType, inputTypeName);

        // add fields
        addFields(objectSpecification, objectTypeBuilder);

        // add collections
        addCollections(objectSpecification, objectTypeBuilder);

        // add actions
        MutatorsDataForEntity mutatorsDataForEntity =
                addActions(logicalTypeNameSanitized, objectSpecification, objectTypeBuilder, graphQLObjectTypes);

        // build and register object type
        GraphQLObjectType graphQLObjectType = objectTypeBuilder.build();
        addTypeIfNotAlreadyPresent(graphQLObjectTypes, graphQLObjectType, logicalTypeNameSanitized);

        // create and register data fetchers
        createAndRegisterDataFetchersForMetaData(
                codeRegistryBuilder, objectSpecificationBeanSort, metaType, gql_meta, graphQLObjectType);
        if (mutatorsDataForEntity!=null) createAndRegisterDataFetchersForMutators(
                codeRegistryBuilder, objectSpecificationBeanSort, mutatorsDataForEntity, graphQLObjectType);
        createAndRegisterDataFetchersForField(objectSpecification, codeRegistryBuilder, graphQLObjectType);
        createAndRegisterDataFetchersForCollection(objectSpecification, codeRegistryBuilder, graphQLObjectType);

        return;
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

    void addFields(
            final ObjectSpecification objectSpecification,
            final GraphQLObjectType.Builder objectTypeBuilder) {

        objectSpecification.streamProperties(MixedIn.INCLUDED)
        .forEach(otoa -> {

            ObjectSpecification fieldObjectSpecification = otoa.getElementType();
            BeanSort beanSort = fieldObjectSpecification.getBeanSort();
            switch (beanSort) {

                case VIEW_MODEL:
                case ENTITY:

                    String logicalTypeNameOfField = fieldObjectSpecification.getLogicalTypeName();

                    GraphQLFieldDefinition.Builder fieldBuilder = newFieldDefinition()
                        .name(otoa.getId())
                        .type(otoa.isOptional()
                                ? GraphQLTypeReference.typeRef(
                                        _Utils.logicalTypeNameSanitized(logicalTypeNameOfField))
                                : nonNull(GraphQLTypeReference.typeRef(
                                        _Utils.logicalTypeNameSanitized(logicalTypeNameOfField))));
                    objectTypeBuilder.field(fieldBuilder);

                    break;

                case VALUE:

                    // todo: map ...

                    GraphQLFieldDefinition.Builder valueBuilder = newFieldDefinition()
                        .name(otoa.getId())
                        .type(otoa.isOptional()
                                ? Scalars.GraphQLString
                                : nonNull(Scalars.GraphQLString));
                    objectTypeBuilder.field(valueBuilder);

                    break;

            }
        });
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

    void addCollections(
            final ObjectSpecification objectSpecification,
            final GraphQLObjectType.Builder objectTypeBuilder) {

        objectSpecification.streamCollections(MixedIn.INCLUDED).forEach(otom -> {

            ObjectSpecification elementType = otom.getElementType();
            BeanSort beanSort = elementType.getBeanSort();
            switch (beanSort) {

                case VIEW_MODEL:
                case ENTITY:

                    String logicalTypeNameOfField = elementType.getLogicalTypeName();
                    GraphQLFieldDefinition.Builder fieldBuilder = newFieldDefinition()
                        .name(otom.getId())
                        .type(GraphQLList.list(GraphQLTypeReference.typeRef(
                                _Utils.logicalTypeNameSanitized(logicalTypeNameOfField))));
                    objectTypeBuilder.field(fieldBuilder);

                    break;

                case VALUE:

                    GraphQLFieldDefinition.Builder valueBuilder = newFieldDefinition()
                        .name(otom.getId())
                        .type(GraphQLList.list(TypeMapper.typeFor(elementType.getCorrespondingClass())));
                    objectTypeBuilder.field(valueBuilder);

                    break;

            }

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

        String mutatorsTypeName = mutatorsTypeName(logicalTypeNameSanitized);
        GraphQLObjectType.Builder mutatorsTypeBuilder = newObject().name(mutatorsTypeName);
        final List<GraphQLFieldDefinition> mutatorsTypeFields = new ArrayList<>();

        objectSpecification.streamActions(ActionScope.PRODUCTION, MixedIn.INCLUDED)
                .forEach(objectAction -> {

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


                });

        if (!mutatorsTypeFields.isEmpty()){
            GraphQLObjectType mutatorsType = mutatorsTypeBuilder.build();
            addTypeIfNotAlreadyPresent(graphQLObjectTypes, mutatorsType, mutatorsTypeName);
            GraphQLFieldDefinition gql_mutations = newFieldDefinition()
                    .name(_Utils.GQL_MUTATTIONS_FIELDNAME)
                    .type(mutatorsType)
                    .build();
            objectTypeBuilder.field(gql_mutations);

            return new MutatorsDataForEntity(mutatorsType, mutatorsTypeFields);

//            // I think we have to create and register data fetcher for mutations here, but we can't since we have no objectTypeYet
//            codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(graphQLTypeReference, gql_mutations), new DataFetcher<Object>() {
//                @Override
//                public Object get(DataFetchingEnvironment environment) throws Exception {
//
//                    Bookmark bookmark = bookmarkService.bookmarkFor(environment.getSource()).orElse(null);
//                    if (bookmark == null) return null; //TODO: is this correct ?
//                    return new GQLMutations(bookmark, bookmarkService, mutatorsTypeFields);
//                }
//            });
//
//            // for each field something like
//            codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(mutatorsType, idField), new DataFetcher<Object>() {
//                @Override
//                public Object get(DataFetchingEnvironment environment) throws Exception {
//
//                    GQLMeta gqlMeta = environment.getSource();
//
//                    return gqlMeta.id();
//                }
//            });
        }

        return null;

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


    GraphQLObjectType createAndRegisterMetaType(
            final String logicalTypeNameSanitized,
            final BeanSort objectSpecificationBeanSort,
            final Set<GraphQLType> graphQLObjectTypes) {

        String metaTypeName = metaTypeName(logicalTypeNameSanitized);
        GraphQLObjectType.Builder metaTypeBuilder = newObject().name(metaTypeName);
        metaTypeBuilder.field(idField);
        metaTypeBuilder.field(logicalTypeNameField);
        if (objectSpecificationBeanSort == BeanSort.ENTITY) {
            metaTypeBuilder.field(versionField);
        }
        GraphQLObjectType metaType = metaTypeBuilder.build();
        addTypeIfNotAlreadyPresent(graphQLObjectTypes, metaType, logicalTypeNameSanitized);
        return metaType;
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

            Bookmark bookmark = bookmarkService.bookmarkFor(environment.getSource()).orElse(null);
            if (bookmark == null) return null; //TODO: is this correct ?
            return new GQLMeta(bookmark, bookmarkService);
        });

        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(metaType, idField), (DataFetcher<Object>) environment -> {

            GQLMeta gqlMeta = environment.getSource();

            return gqlMeta.id();
        });

        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(metaType, logicalTypeNameField), (DataFetcher<Object>) environment -> {

            GQLMeta gqlMeta = environment.getSource();

            return gqlMeta.logicalTypeName();
        });

        if (objectSpecificationBeanSort == BeanSort.ENTITY) {
            codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(metaType, versionField), (DataFetcher<Object>) environment -> {

                GQLMeta gqlMeta = environment.getSource();

                return gqlMeta.version();
            });

        }
    }

}
