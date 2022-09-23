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
import java.util.stream.Collectors;

import javax.inject.Inject;

import graphql.schema.*;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.isis.core.metamodel.object.ManagedObject;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.spec.feature.*;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.metamodel.BeanSort;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import lombok.RequiredArgsConstructor;

import graphql.Scalars;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectType.newInputObject;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLObjectType.newObject;
import static org.apache.isis.viewer.graphql.viewer.source._Utils.*;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ObjectTypeFactory {

    private final BookmarkService bookmarkService;
    private final SpecificationLoader specificationLoader;
    private final ObjectManager objectManager;

    // _gql_meta fields
    private static GraphQLFieldDefinition structureField = newFieldDefinition()
            .name("structure").type(nonNull(GraphQLTypeReference.typeRef(GQL_GENERIC_STRUCTURE_TYPENAME))).build();

    private static GraphQLFieldDefinition idField = newFieldDefinition()
            .name("id").type(nonNull(Scalars.GraphQLID)).build();

    private static GraphQLFieldDefinition logicalTypeNameField = newFieldDefinition()
            .name("logicalTypeName").type(nonNull(Scalars.GraphQLString)).build();

    private static GraphQLFieldDefinition versionField = newFieldDefinition()
            .name("version").type(Scalars.GraphQLString).build();

    private static GraphQLFieldDefinition titleField = newFieldDefinition()
            .name("title").type(Scalars.GraphQLString).build();

    private static GraphQLFieldDefinition iconNameField = newFieldDefinition()
            .name("iconName").type(Scalars.GraphQLString).build();

    public void objectTypeFromObjectSpecification(
            final ObjectSpecification objectSpecification,
            final Set<GraphQLType> graphQLTypes,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder) {
        ObjectTypeConstructionHelper constructionHelper = new ObjectTypeConstructionHelper(bookmarkService, objectManager);
        constructionHelper.setObjectSpecification(objectSpecification);

        GraphQLObjectType.Builder objectTypeBuilder = newObject().name(constructionHelper.gqlObjectTypeName());

        // construct meta type (builder)
        BeanSort objectSpecificationBeanSort = objectSpecification.getBeanSort();
        GraphQLObjectType.Builder genericTypeBuilder = newObject().name(constructionHelper.genericTypeName());
        genericTypeBuilder.field(structureField);
        genericTypeBuilder.field(idField);
        genericTypeBuilder.field(logicalTypeNameField);
        if (objectSpecificationBeanSort == BeanSort.ENTITY) {
            genericTypeBuilder.field(versionField);
        }
        genericTypeBuilder.field(titleField);
        genericTypeBuilder.field(iconNameField);

        // add meta field to object type (builder)
        GraphQLFieldDefinition gql_generic = newFieldDefinition().name(GQL_GENERIC_FIELDNAME).type(GraphQLTypeReference.typeRef(constructionHelper.genericTypeName())).build();
        objectTypeBuilder.field(gql_generic);

        // create input type
        GraphQLInputObjectType.Builder inputTypeBuilder = newInputObject().name(constructionHelper.inputTypeName());
        inputTypeBuilder
                .field(GraphQLInputObjectField.newInputObjectField()
                        .name("id")
                        .type(nonNull(Scalars.GraphQLID))
                        .build());
        GraphQLInputType inputType = inputTypeBuilder.build();
        addTypeIfNotAlreadyPresent(graphQLTypes, inputType, constructionHelper.inputTypeName());

        GraphQLObjectType.Builder genericPropertiesTypeBuilder = null;
        if (constructionHelper.objectHasProperties()) {
            genericPropertiesTypeBuilder = newObject().name(constructionHelper.genericFieldsTypeName());
        }
        // add fields
        addOneToOneAssociationsAsFields(objectTypeBuilder, genericTypeBuilder, genericPropertiesTypeBuilder, constructionHelper);

        GraphQLObjectType.Builder genericCollectionsTypeBuilder = null;
        if (constructionHelper.objectHasCollections()) {
            genericCollectionsTypeBuilder = newObject().name(constructionHelper.genericCollectionsTypeName());
        }

        // add collections
        addOneToManyAssociationsAsFields(objectTypeBuilder, genericTypeBuilder, genericCollectionsTypeBuilder, constructionHelper);

        // add actions
        GraphQLObjectType.Builder genericActionsTypeBuilder = null;
        if (constructionHelper.objectHasActions()) {
            genericActionsTypeBuilder = newObject().name(constructionHelper.genericActionsTypename());
        }
        addActions(objectTypeBuilder, graphQLTypes, genericTypeBuilder, genericActionsTypeBuilder, constructionHelper);

        // adds types for meta
        if (genericPropertiesTypeBuilder != null) {
            graphQLTypes.add(genericPropertiesTypeBuilder.build());
        }
        if (genericCollectionsTypeBuilder != null) {
            graphQLTypes.add(genericCollectionsTypeBuilder.build());
        }
        if (genericActionsTypeBuilder != null) {
            graphQLTypes.add(genericActionsTypeBuilder.build());
        }

        // build and register object type
        GraphQLObjectType graphQLObjectType = objectTypeBuilder.build();
        addTypeIfNotAlreadyPresent(graphQLTypes, graphQLObjectType, constructionHelper.logicalTypeNameSanitized());

        // build and register meta type
        GraphQLObjectType metaType = genericTypeBuilder.build();
        addTypeIfNotAlreadyPresent(graphQLTypes, metaType, constructionHelper.logicalTypeNameSanitized());

        // create and register data fetchers
        createAndRegisterDataFetchersForMetaData(
                codeRegistryBuilder, objectSpecification, metaType, gql_generic, graphQLObjectType, graphQLTypes, constructionHelper);
        if (!constructionHelper.mutatorActions().isEmpty()) createAndRegisterDataFetchersForMutators(
                codeRegistryBuilder, graphQLObjectType, constructionHelper, graphQLTypes);
        createAndRegisterDataFetchersForField(objectSpecification, codeRegistryBuilder, graphQLObjectType);
        createAndRegisterDataFetchersForCollection(objectSpecification, codeRegistryBuilder, graphQLObjectType);

        return;
    }

    private void createAndRegisterDataFetchersForMutators(
            final GraphQLCodeRegistry.Builder codeRegistryBuilder,
            final GraphQLObjectType graphQLObjectType,
            final ObjectTypeConstructionHelper dataCollector,
            final Set<GraphQLType> graphQLTypes) {

        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(graphQLObjectType, _Utils.GQL_MUTATTIONS_FIELDNAME), new DataFetcher<Object>() {

            @Override
            public Object get(DataFetchingEnvironment environment) throws Exception {

                Bookmark bookmark = bookmarkService.bookmarkFor(environment.getSource()).orElse(null);
                if (bookmark == null) return null;
                return new GQLMutations(bookmark, bookmarkService, dataCollector);

            }

        });

        GraphQLObjectType mutatorsType = ObjectTypeConstructionHelper.getObjectTypeFor(dataCollector.mutationsTypeName(), graphQLTypes);
        dataCollector.mutatorActions().forEach(
            action -> {
                codeRegistryBuilder
                    .dataFetcher(
                        FieldCoordinates.coordinates(mutatorsType, action.getId()),
                        new DataFetcher<Object>() {

                            @Override
                            public Object get(final DataFetchingEnvironment dataFetchingEnvironment) throws Exception {

                                GQLMutations gqlMutations = dataFetchingEnvironment.getSource();

                                Optional<Object> optionalDomainObject = bookmarkService.lookup(gqlMutations.getBookmark());

                                if (!optionalDomainObject.isPresent()) return null;
                                Object domainObject = optionalDomainObject.get();

                                Class<?> domainObjectInstanceClass = domainObject.getClass();
                                ObjectSpecification specification = specificationLoader
                                        .loadSpecification(domainObjectInstanceClass);

                                ManagedObject owner = ManagedObject.adaptSingular(specification, domainObject);

                                ActionInteractionHead actionInteractionHead = action.interactionHead(owner);

                                Map<String, Object> arguments = dataFetchingEnvironment.getArguments();
                                Can<ObjectActionParameter> parameters = action.getParameters();
                                Can<ManagedObject> canOfParams = parameters.stream().map(oap -> {
                                    Object argumentValue = arguments.get(oap.getId());
                                    ObjectSpecification elementType = oap.getElementType();

                                    if (argumentValue == null)
                                        return ManagedObject.empty(elementType);
                                    switch (elementType.getBeanSort()){
                                        case ENTITY:
                                            return getManagedObjectFromInputType(elementType, argumentValue);

                                        case COLLECTION:
                                            /* TODO */
                                            throw new RuntimeException("Not yet implemented");

                                        case VALUE:
                                            return ManagedObject.adaptSingular(elementType, argumentValue);

                                        default:
                                            throw new RuntimeException("Not yet implemented");
                                    }


                                }).collect(Can.toCan());

                                ManagedObject managedObject = action
                                        .execute(actionInteractionHead, canOfParams, InteractionInitiatedBy.USER);

                                return managedObject.getPojo();
                            }

                        });

            }
        );

    }

    private ManagedObject getManagedObjectFromInputType(ObjectSpecification elementType, Object argumentValue) {
        LinkedHashMap map = (LinkedHashMap) argumentValue;
        String identifier = (String) map.get("id");
        Bookmark bookmark = Bookmark.forLogicalTypeNameAndIdentifier(elementType.getLogicalTypeName(), identifier);
        return bookmarkService.lookup(bookmark).map(p->ManagedObject.adaptSingular(elementType, p)).orElse(ManagedObject.empty(elementType));
    }

    void addTypeIfNotAlreadyPresent(
            final Set<GraphQLType> graphQLObjectTypes,
            final GraphQLType typeToAdd,
            final String logicalTypeName) {

        boolean present;
        if (typeToAdd.getClass().isAssignableFrom(GraphQLObjectType.class)) {
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
        if (present) {
            // For now we just log and skip
            System.out.println("==== DOUBLE ====");
            System.out.println(logicalTypeName);

        } else {
            graphQLObjectTypes.add(typeToAdd);
        }
    }

    void addFieldsFieldOnGenericTypeIfNotAlready(final GraphQLObjectType.Builder genericTypeBuilder, final String logicalTypeNameSanitized){
        GraphQLFieldDefinition fields =
                newFieldDefinition().name(GQL_GENERIC_PROPERTIES_FIELDNAME).type(GraphQLTypeReference.typeRef(genericPropertiesTypeName(logicalTypeNameSanitized))).build();
        if (!genericTypeBuilder.hasField(GQL_GENERIC_PROPERTIES_FIELDNAME)){
            genericTypeBuilder.field(fields);
        }
    }

    void addCollectionsFieldOnGenericTypeIfNotAlready(final GraphQLObjectType.Builder genericTypeBuilder, final String logicalTypeNameSanitized){
        GraphQLFieldDefinition field =
                newFieldDefinition().name(GQL_GENERIC_COLLECTIONS_FIELDNAME).type(GraphQLTypeReference.typeRef(genericCollectionsTypeName(logicalTypeNameSanitized))).build();
        if (!genericTypeBuilder.hasField(GQL_GENERIC_COLLECTIONS_FIELDNAME)){
            genericTypeBuilder.field(field);
        }
    }

    void addActionsFieldOnGenericTypeIfNotAlready(GraphQLObjectType.Builder genericTypeBuilder, final String logicalTypeNameSanitized){
        if (genericTypeBuilder==null) genericTypeBuilder = newObject().name(genericActionsTypeName(logicalTypeNameSanitized));
        GraphQLFieldDefinition mutations =
                newFieldDefinition().name(GQL_GENERIC_ACTIONS_FIELDNAME).type(GraphQLTypeReference.typeRef(genericActionsTypeName(logicalTypeNameSanitized))).build();
        if (!genericTypeBuilder.hasField(GQL_GENERIC_ACTIONS_FIELDNAME)) {
            genericTypeBuilder.field(mutations);
        }
    }

    void addOneToOneAssociationsAsFields(
            final GraphQLObjectType.Builder objectTypeBuilder,
            final GraphQLObjectType.Builder genericTypeBuilder,
            final GraphQLObjectType.Builder metaFieldsTypeBuilder,
            ObjectTypeConstructionHelper constructionHelper) {

        if (constructionHelper.oneToOneAssociations().isEmpty()) return;

        ObjectSpecification objectSpecification = constructionHelper.getObjectSpecification();
        constructionHelper.oneToOneAssociations()
                .forEach(otoa -> {

                    ObjectSpecification fieldObjectSpecification = otoa.getElementType();
                    BeanSort beanSort = fieldObjectSpecification.getBeanSort();
                    String logicalTypeNameSanitized = logicalTypeNameSanitized(objectSpecification.getLogicalTypeName());
                    switch (beanSort) {

                        case VIEW_MODEL:
                        case ENTITY:

                            // _gql_generic 'maintenance'
                            addFieldsFieldOnGenericTypeIfNotAlready(genericTypeBuilder, logicalTypeNameSanitized);
                            metaFieldsTypeBuilder.field(newFieldDefinition().name(otoa.getId()).type(GraphQLTypeReference.typeRef(GQL_GENERIC_PROPERTY_TYPENAME)).build());
                            // END _gql_generic 'maintenance'

                            String logicalTypeNameOfField = fieldObjectSpecification.getLogicalTypeName();
                            String logicalTypeNameOfFieldSanitized = logicalTypeNameSanitized(logicalTypeNameOfField);

                            GraphQLFieldDefinition.Builder fieldBuilder = newFieldDefinition()
                                    .name(otoa.getId())
                                    .type(otoa.isOptional()
                                            ? GraphQLTypeReference.typeRef(
                                            logicalTypeNameOfFieldSanitized)
                                            : nonNull(GraphQLTypeReference.typeRef(
                                            logicalTypeNameOfFieldSanitized)));
                            objectTypeBuilder.field(fieldBuilder);

                            break;

                        case VALUE:

                            // _gql_generic 'maintenance'
                            addFieldsFieldOnGenericTypeIfNotAlready(genericTypeBuilder, logicalTypeNameSanitized);
                            metaFieldsTypeBuilder.field(newFieldDefinition().name(otoa.getId()).type(GraphQLTypeReference.typeRef(GQL_GENERIC_PROPERTY_TYPENAME)).build());
                            // END _gql_generic 'maintenance'

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

    void addOneToManyAssociationsAsFields(
            final GraphQLObjectType.Builder objectTypeBuilder,
            final GraphQLObjectType.Builder genericTypeBuilder,
            final GraphQLObjectType.Builder genericCollectionsTypeBuilder,
            ObjectTypeConstructionHelper constructionHelper) {

        if (constructionHelper.oneToManyAssociations().isEmpty()) return;

        ObjectSpecification objectSpecification = constructionHelper.getObjectSpecification();
        constructionHelper.oneToManyAssociations().forEach(otom -> {

            ObjectSpecification elementType = otom.getElementType();
            BeanSort beanSort = elementType.getBeanSort();
            switch (beanSort) {

                case VIEW_MODEL:
                case ENTITY:

                    // _gql_generic 'maintenance'
                    addCollectionsFieldOnGenericTypeIfNotAlready(genericTypeBuilder, logicalTypeNameSanitized(objectSpecification.getLogicalTypeName()));
                    genericCollectionsTypeBuilder.field(newFieldDefinition().name(otom.getId()).type(GraphQLTypeReference.typeRef(GQL_GENERIC_COLLECTION_TYPENAME)).build());
                    // END _gql_generic 'maintenance'

                    String logicalTypeNameOfField = elementType.getLogicalTypeName();
                    GraphQLFieldDefinition.Builder fieldBuilder = newFieldDefinition()
                            .name(otom.getId())
                            .type(GraphQLList.list(GraphQLTypeReference.typeRef(
                                    _Utils.logicalTypeNameSanitized(logicalTypeNameOfField))));
                    objectTypeBuilder.field(fieldBuilder);

                    break;

                case VALUE:

                    // _gql_generic 'maintenance'
                    addCollectionsFieldOnGenericTypeIfNotAlready(genericTypeBuilder, logicalTypeNameSanitized(objectSpecification.getLogicalTypeName()));
                    genericCollectionsTypeBuilder.field(newFieldDefinition().name(otom.getId()).type(GraphQLTypeReference.typeRef(GQL_GENERIC_COLLECTION_TYPENAME)).build());
                    // END _gql_generic 'maintenance'

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

    void addActions(
            final GraphQLObjectType.Builder objectTypeBuilder,
            final Set<GraphQLType> graphQLObjectTypes,
            final GraphQLObjectType.Builder genericTypeBuilder,
            final GraphQLObjectType.Builder genericActionsTypeBuilder,
            final ObjectTypeConstructionHelper constructionHelper) {

        GraphQLObjectType.Builder mutationsTypeBuilder = null;
        if (constructionHelper.objectHasMutations()) {
            mutationsTypeBuilder = newObject().name(constructionHelper.mutationsTypeName());
        }
        List<GraphQLFieldDefinition> mutatorFieldDefinitions = new ArrayList<>();

        constructionHelper.allActions()
            .forEach(objectAction -> {

                // _gql_generic 'maintenance' TODO: bring to separate method --
                addActionsFieldOnGenericTypeIfNotAlready(genericTypeBuilder, logicalTypeNameSanitized(constructionHelper.getObjectSpecification().getLogicalTypeName()));

                Can<ObjectActionParameter> parameters = objectAction.getParameters();
                String actionsGenericTypeName = constructionHelper.actionGenericTypeName(objectAction.getId());
                GraphQLObjectType.Builder actionsGenericTypeBuilder = newObject().name(actionsGenericTypeName);
                if (parameters.isNotEmpty()) {
                    actionsGenericTypeBuilder.field(newFieldDefinition().name("params").type(GraphQLTypeReference.typeRef(constructionHelper.objectActionGenericParamsTypeName(objectAction.getId()))).build());
                }
                actionsGenericTypeBuilder.field(newFieldDefinition().name("validate").type(Scalars.GraphQLString).build());
                actionsGenericTypeBuilder.field(newFieldDefinition().name("hide").type(Scalars.GraphQLBoolean).build());
                actionsGenericTypeBuilder.field(newFieldDefinition().name("disable").type(Scalars.GraphQLString).build());
                actionsGenericTypeBuilder.field(newFieldDefinition().name("semantics").type(GraphQLTypeReference.typeRef(GQL_SEMANTICS_TYPENAME)).build()); //TODO: should be GQLEnum I think, but I do not know how to fetch yet ...

                GraphQLFieldDefinition metaTypeFieldDefinition = newFieldDefinition().name(objectAction.getId()).type(GraphQLTypeReference.typeRef(actionsGenericTypeName)).build();
                genericActionsTypeBuilder.field(metaTypeFieldDefinition);

                if (parameters.isNotEmpty()) {
                    GraphQLObjectType.Builder parametersMetaDataTypeNameBuilder = newObject().name(constructionHelper.objectActionGenericParamsTypeName(objectAction.getId()));
                    parameters.forEach(p -> {

                        String objectActionParameterGenericTypeName = constructionHelper.objectActionParameterGenericTypeName(objectAction.getId(), p.getId());
                        GraphQLObjectType.Builder parameterMetaDataTypeBuilder = newObject().name(objectActionParameterGenericTypeName);
                        parameterMetaDataTypeBuilder.field(newFieldDefinition().name("optionality").type(Scalars.GraphQLBoolean).build());
                        GraphQLOutputType graphQLType = (GraphQLOutputType) TypeMapper.outputTypeFor(p.getElementType());
                        parameterMetaDataTypeBuilder.field(newFieldDefinition().name("default").type(graphQLType).build());
                        parameterMetaDataTypeBuilder.field(newFieldDefinition().name("choices").type(Scalars.GraphQLString).build()); // for now
                        parameterMetaDataTypeBuilder.field(newFieldDefinition().name("autocomplete").argument(GraphQLArgument.newArgument().name("we_call_search_for_now").type(Scalars.GraphQLString).build()).type(Scalars.GraphQLBoolean).build()); // for now
                        parameterMetaDataTypeBuilder.field(newFieldDefinition().name("validate").argument(GraphQLArgument.newArgument().name("we_call_value_for_now").type(Scalars.GraphQLString).build()).type(Scalars.GraphQLString).build());

                        GraphQLObjectType parameterMetaDataType = parameterMetaDataTypeBuilder.build();
                        addTypeIfNotAlreadyPresent(graphQLObjectTypes, parameterMetaDataType, objectActionParameterGenericTypeName);
                        parametersMetaDataTypeNameBuilder.field(newFieldDefinition().name(p.getId()).type(parameterMetaDataType));

                    });
                    graphQLObjectTypes.add(parametersMetaDataTypeNameBuilder.build());
                }
                addTypeIfNotAlreadyPresent(graphQLObjectTypes, actionsGenericTypeBuilder.build(), actionsGenericTypeName);
                // END _gql_generic 'maintenance'

                if (objectAction.getSemantics().isSafeInNature()) {

                    String fieldName = objectAction.getId();
                    GraphQLFieldDefinition.Builder builder = newFieldDefinition()
                            .name(fieldName)
                            .type((GraphQLOutputType) TypeMapper.typeForObjectAction(objectAction));
                    if (parameters.isNotEmpty()) {
                        builder.arguments(parameters.stream()
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
                    mutatorFieldDefinitions.add(fieldDefinition);
                }

            });

        if (!mutatorFieldDefinitions.isEmpty()) {
            for (GraphQLFieldDefinition mutatorFieldDefinition : mutatorFieldDefinitions) {
                mutationsTypeBuilder.field(mutatorFieldDefinition);
            }
            GraphQLObjectType mutationsType = mutationsTypeBuilder.build();
            addTypeIfNotAlreadyPresent(graphQLObjectTypes, mutationsType, constructionHelper.mutationsTypeName());
            GraphQLFieldDefinition gql_mutations = newFieldDefinition()
                    .name(GQL_MUTATTIONS_FIELDNAME)
                    .type(mutationsType)
                    .build();
            objectTypeBuilder.field(gql_mutations);
        }

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

                                    return managedObject != null ? managedObject.getPojo() : null;

                                });


                break;

        }
    }

    void createAndRegisterDataFetchersForMetaData(
            final GraphQLCodeRegistry.Builder codeRegistryBuilder,
            final ObjectSpecification objectSpecification,
            final GraphQLObjectType metaType,
            final GraphQLFieldDefinition gql_generic,
            final GraphQLObjectType graphQLObjectType,
            final Set<GraphQLType> types,
            final ObjectTypeConstructionHelper constructionHelper) {

        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(graphQLObjectType, gql_generic), new DataFetcher<Object>() {
            @Override
            public Object get(final DataFetchingEnvironment environment) throws Exception {

                Bookmark bookmark = bookmarkService.bookmarkFor(environment.getSource()).orElse(null);
                if (bookmark == null) return null; //TODO: is this correct ?
                return new GQLGeneric(bookmark, constructionHelper);
            }
        });

        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(metaType, idField), new DataFetcher<Object>() {
            @Override
            public Object get(final DataFetchingEnvironment environment) throws Exception {

                GQLGeneric gqlGeneric = environment.getSource();

                return gqlGeneric.id();
            }
        });

        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(metaType, logicalTypeNameField), new DataFetcher<Object>() {
            @Override
            public Object get(final DataFetchingEnvironment environment) throws Exception {

                GQLGeneric gqlGeneric = environment.getSource();

                return gqlGeneric.logicalTypeName();
            }
        });

        if (objectSpecification.getBeanSort() == BeanSort.ENTITY) {
            codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(metaType, versionField), new DataFetcher<Object>() {
                @Override
                public Object get(final DataFetchingEnvironment environment) throws Exception {

                    GQLGeneric gqlGeneric = environment.getSource();

                    return gqlGeneric.version();
                }
            });

        }

        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(metaType, iconNameField), new DataFetcher<Object>() {
            @Override
            public Object get(final DataFetchingEnvironment environment) throws Exception {

                GQLGeneric gqlGeneric = environment.getSource();

                return gqlGeneric.iconName();
            }
        });

        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(metaType, titleField), new DataFetcher<Object>() {
            @Override
            public Object get(final DataFetchingEnvironment environment) throws Exception {

                GQLGeneric gqlGeneric = environment.getSource();

                return gqlGeneric.title();
            }
        });

        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(metaType, structureField), new DataFetcher<Object>() {
            @Override
            public Object get(final DataFetchingEnvironment environment) throws Exception {

                GQLGeneric gqlGeneric = environment.getSource();

                return gqlGeneric.structure();
            }
        });

        GraphQLObjectType structureType = ObjectTypeConstructionHelper.getObjectTypeFor(GQL_GENERIC_STRUCTURE_TYPENAME, types);

        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(structureType, "properties"), new DataFetcher<Object>() {
            @Override
            public Object get(final DataFetchingEnvironment environment) throws Exception {

                GQLGenericStructure gqlStructure = environment.getSource();

                return gqlStructure.properties();
            }
        });

        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(structureType, "collections"), new DataFetcher<Object>() {
            @Override
            public Object get(final DataFetchingEnvironment environment) throws Exception {

                GQLGenericStructure gqlStructure = environment.getSource();

                return gqlStructure.collections();
            }
        });

        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(structureType, "actions"), new DataFetcher<Object>() {
            @Override
            public Object get(final DataFetchingEnvironment environment) throws Exception {

                GQLGenericStructure gqlStructure = environment.getSource();

                return gqlStructure.actions();
            }
        });

        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(structureType, "layoutXml"), new DataFetcher<Object>() {
            @Override
            public Object get(final DataFetchingEnvironment environment) throws Exception {

                GQLGenericStructure gqlGenericStructure = environment.getSource();

                return gqlGenericStructure.layoutXml();
            }
        });


        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(metaType, GQL_GENERIC_PROPERTIES_FIELDNAME), new DataFetcher<Object>() {
            @Override
            public Object get(final DataFetchingEnvironment environment) throws Exception {
                GQLGeneric source = environment.getSource();
                return new GQLGenericFieldsAndCollections(constructionHelper, source.getBookmark());
            }

        });

        GraphQLObjectType fieldGenericDataTypeName = ObjectTypeConstructionHelper.getObjectTypeFor(constructionHelper.genericFieldsTypeName(), types);

        for (OneToOneAssociation oneToOneAssociation : constructionHelper.oneToOneAssociations()) {

            codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(fieldGenericDataTypeName, oneToOneAssociation.getId()), new DataFetcher<Object>() {
                @Override
                public Object get(final DataFetchingEnvironment environment) throws Exception {
                    GQLGenericFieldsAndCollections source = environment.getSource();
                    return new GQLFieldOrCollectionHideDisable(source.hideOTOA(oneToOneAssociation), source.disableOTOA(oneToOneAssociation));
                }

            });

            codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(GQL_GENERIC_PROPERTY_TYPENAME, "hide"), new DataFetcher<Object>() {
                @Override
                public Object get(final DataFetchingEnvironment environment) throws Exception {
                    GQLFieldOrCollectionHideDisable source = environment.getSource();
                    return source.isHide();
                }

            });

            codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(GQL_GENERIC_PROPERTY_TYPENAME, "disable"), new DataFetcher<Object>() {
                @Override
                public Object get(final DataFetchingEnvironment environment) throws Exception {
                    GQLFieldOrCollectionHideDisable source = environment.getSource();
                    return source.getDisable();
                }

            });

        }

        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(metaType, GQL_GENERIC_COLLECTIONS_FIELDNAME), new DataFetcher<Object>() {
            @Override
            public Object get(final DataFetchingEnvironment environment) throws Exception {
                GQLGeneric source = environment.getSource();
                return new GQLGenericFieldsAndCollections(constructionHelper, source.getBookmark());
            }

        });

        GraphQLObjectType genericCollectionsTypeName = ObjectTypeConstructionHelper.getObjectTypeFor(constructionHelper.genericCollectionsTypeName(), types);
        GraphQLEnumType semanticsEnumType = ObjectTypeConstructionHelper.getEnumTypeFor(GQL_SEMANTICS_TYPENAME, types);

        for (OneToManyAssociation oneToManyAssociation : constructionHelper.oneToManyAssociations()) {

            codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(genericCollectionsTypeName, oneToManyAssociation.getId()), new DataFetcher<Object>() {
                @Override
                public Object get(final DataFetchingEnvironment environment) throws Exception {
                    GQLGenericFieldsAndCollections source = environment.getSource();
                    return new GQLFieldOrCollectionHideDisable(source.hideOTMA(oneToManyAssociation), source.disableOTMA(oneToManyAssociation));
                }

            });

            codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(GQL_GENERIC_COLLECTION_TYPENAME, "hide"), new DataFetcher<Object>() {
                @Override
                public Object get(final DataFetchingEnvironment environment) throws Exception {
                    GQLFieldOrCollectionHideDisable source = environment.getSource();
                    return source.isHide();
                }

            });

            codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(GQL_GENERIC_COLLECTION_TYPENAME, "disable"), new DataFetcher<Object>() {
                @Override
                public Object get(final DataFetchingEnvironment environment) throws Exception {
                    GQLFieldOrCollectionHideDisable source = environment.getSource();
                    return source.getDisable();
                }

            });

        }

        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(metaType, GQL_GENERIC_ACTIONS_FIELDNAME), new DataFetcher<Object>() {
            @Override
            public Object get(final DataFetchingEnvironment environment) throws Exception {
                GQLGeneric source = environment.getSource();
                return new GQLGenericActions(constructionHelper, source.getBookmark());
            }

        });

        String genericActionsTypename = constructionHelper.genericActionsTypename();

        for (ObjectAction objectAction : constructionHelper.allActions()) {

            codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(genericActionsTypename, objectAction.getId()), new DataFetcher<Object>() {
                @Override
                public Object get(final DataFetchingEnvironment environment) throws Exception {
                    GQLGenericActions source = environment.getSource();
                    return source;
                }

            });

            GraphQLObjectType actionGenericType = ObjectTypeConstructionHelper.getObjectTypeFor(constructionHelper.actionGenericTypeName(objectAction.getId()), types);

            codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(actionGenericType, "hide"), new DataFetcher<Object>() {
                @Override
                public Object get(final DataFetchingEnvironment environment) throws Exception {
                    GQLGenericActions source = environment.getSource();
                    return source.hideAction(objectAction);
                }

            });

            codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(actionGenericType , "disable"), new DataFetcher<Object>() {
                @Override
                public Object get(final DataFetchingEnvironment environment) throws Exception {
                    GQLGenericActions source = environment.getSource();
                    return source.disableAction(objectAction);
                }

            });

            codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(actionGenericType , "validate"), new DataFetcher<Object>() {
                @Override
                public Object get(final DataFetchingEnvironment environment) throws Exception {
                    GQLGenericActions source = environment.getSource();
                    return source.validateAction(objectAction);
                }

            });

            codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(actionGenericType , "semantics"), new DataFetcher<Object>() {
                @Override
                public Object get(final DataFetchingEnvironment environment) throws Exception {
                    GQLGenericActions source = environment.getSource();
                    return semanticsEnumType.getValue(source.semanticsOf(objectAction)).getValue();
                }
            });

            codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(actionGenericType , "params"), new DataFetcher<Object>() {
                @Override
                public Object get(final DataFetchingEnvironment environment) throws Exception {
                    GQLGenericActions source = environment.getSource();
                    return source;
                }
            });

            String objectActionGenericParamsTypeName = constructionHelper.objectActionGenericParamsTypeName(objectAction.getId());
            GraphQLObjectType objectActionGenericParamsType = ObjectTypeConstructionHelper.getObjectTypeFor(objectActionGenericParamsTypeName, types);

            for (ObjectActionParameter parameter : objectAction.getParameters()) {

                String typeName = constructionHelper.objectActionParameterGenericTypeName(objectAction.getId(), parameter.getId());
                GraphQLObjectType actionParamGenericType = ObjectTypeConstructionHelper.getObjectTypeFor(typeName, types);

                codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(objectActionGenericParamsType, parameter.getId()), new DataFetcher<Object>() {
                    @Override
                    public Object get(DataFetchingEnvironment environment) throws Exception {
                        GQLGenericActions source = environment.getSource();
                        return source;
                    }
                });

                codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(actionParamGenericType, "optionality"), new DataFetcher<Object>() {
                    @Override
                    public Object get(DataFetchingEnvironment environment) throws Exception {
                        return parameter.isOptional();
                    }
                });

                codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(actionParamGenericType, "default"), new DataFetcher<Object>() {
                    @Override
                    public Object get(DataFetchingEnvironment environment) throws Exception {
                        GQLGenericActions source = environment.getSource();
                        return source.defaultValueFor(objectAction, parameter);
                    }
                });

            }
        }

    }

}
