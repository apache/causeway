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
import org.apache.isis.core.metamodel.spec.feature.*;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.metamodel.BeanSort;
import org.apache.isis.core.metamodel.object.ManagedObject;
import org.apache.isis.core.metamodel.spec.ActionScope;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.val;

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

    // _gql_meta fields
    private static GraphQLFieldDefinition structureField = newFieldDefinition()
            .name("structure").type(nonNull(GraphQLTypeReference.typeRef(GQL_DOMAINOBJECT_STRUCTURE_TYPENAME))).build();

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
            final Set<GraphQLType> graphQLObjectTypes,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder) {
        ObjectTypeDataCollector objectTypeDataCollector = new ObjectTypeDataCollector(); //may be removed or slimmed down later
        objectTypeDataCollector.setObjectSpecification(objectSpecification);

        String logicalTypeNameSanitized = _Utils.logicalTypeNameSanitized(objectSpecification.getLogicalTypeName());
        GraphQLObjectType.Builder objectTypeBuilder = newObject().name(logicalTypeNameSanitized);
        objectTypeDataCollector.setGqlObjectTypeName(logicalTypeNameSanitized);

        // create meta field type
        GraphQLObjectType.Builder metaMutationsTypeBuilder = null;
        GraphQLObjectType.Builder metaFieldsTypeBuilder = null;
        if (objectTypeDataCollector.objectHasMutations()) {
            metaMutationsTypeBuilder = newObject().name(metaMutationsTypeName(logicalTypeNameSanitized));
            objectTypeDataCollector.setMetaMutationsTypeName(metaMutationsTypeName(logicalTypeNameSanitized));
        }
        if (objectTypeDataCollector.objectHasFields()) {
            metaFieldsTypeBuilder = newObject().name(metaFieldsTypeName(logicalTypeNameSanitized));
            objectTypeDataCollector.setMetaFieldsTypeName(metaFieldsTypeName(logicalTypeNameSanitized));
        }

//        // TODO: make all dynamic
//        GraphQLObjectType paramMetaDataType = newObject().name(_Utils.SINGLE_PARAM_META_DATA_TYPENAME)
//                .field(newFieldDefinition().name("optionality").type(Scalars.GraphQLBoolean).build())
//                .field(newFieldDefinition().name("default").type(Scalars.GraphQLString).build()) // for now
//                .field(newFieldDefinition().name("choices").type(Scalars.GraphQLString).build()) // for now
//                .field(newFieldDefinition().name("autocomplete").argument(GraphQLArgument.newArgument().name("we_call_search_for_now").type(Scalars.GraphQLString).build()).type(Scalars.GraphQLBoolean).build()) // for now
//                .field(newFieldDefinition().name("validate").argument(GraphQLArgument.newArgument().name("we_call_value_for_now").type(Scalars.GraphQLString).build()).type(Scalars.GraphQLString).build())
//                .build();
//        graphQLObjectTypes.add(paramMetaDataType);

//        GraphQLObjectType mutatorMetaDataType = newObject().name(_Utils.MUTATOR_META_DATA_TYPENAME)
//                .field(newFieldDefinition().name("params").type(paramsMetaDataType).build())
//                .field(newFieldDefinition().name("validate").type(Scalars.GraphQLString).build())
//                .field(newFieldDefinition().name("hide").type(Scalars.GraphQLBoolean).build())
//                .field(newFieldDefinition().name("disable").type(Scalars.GraphQLString).build())
//                .build();
//        graphQLObjectTypes.add(mutatorMetaDataType);

        BeanSort objectSpecificationBeanSort = objectSpecification.getBeanSort();
//        GraphQLObjectType metaType =
//                createAndRegisterMetaType(logicalTypeNameSanitized, objectSpecificationBeanSort, graphQLObjectTypes, fields, mutations);

        String metaTypeName = metaTypeName(logicalTypeNameSanitized);
        GraphQLObjectType.Builder metaTypeBuilder = newObject().name(metaTypeName);
        objectTypeDataCollector.setMetaTypeName(metaTypeName);
        metaTypeBuilder.field(structureField);
        metaTypeBuilder.field(idField);
        metaTypeBuilder.field(logicalTypeNameField);
        if (objectSpecificationBeanSort == BeanSort.ENTITY) {
            metaTypeBuilder.field(versionField);
        }
        metaTypeBuilder.field(titleField);
        metaTypeBuilder.field(iconNameField);

        // add meta field
        GraphQLFieldDefinition gql_meta = newFieldDefinition().name("_gql_meta").type(GraphQLTypeReference.typeRef(metaTypeName)).build();
        objectTypeBuilder.field(gql_meta);

        // create input type
        String inputTypeName = _Utils.GQL_INPUTTYPE_PREFIX + logicalTypeNameSanitized;
        GraphQLInputObjectType.Builder inputTypeBuilder = newInputObject().name(inputTypeName);
        objectTypeDataCollector.setInputTypeName(inputTypeName);
        inputTypeBuilder
                .field(GraphQLInputObjectField.newInputObjectField()
                        .name("id")
                        .type(nonNull(Scalars.GraphQLID))
                        .build());
        GraphQLInputType inputType = inputTypeBuilder.build();
        addTypeIfNotAlreadyPresent(graphQLObjectTypes, inputType, inputTypeName);

        // add fields
        if (metaFieldsTypeBuilder != null) {
            addFields(objectSpecification, objectTypeBuilder, metaTypeBuilder, metaFieldsTypeBuilder, objectTypeDataCollector);
        }

        // add collections
        if (metaFieldsTypeBuilder != null) {
            addCollections(objectSpecification, objectTypeBuilder, metaTypeBuilder, metaFieldsTypeBuilder, objectTypeDataCollector);
        }

        // add actions TODO: maybe split into 2: safe actions and non-safe actions?
        MutatorsDataForEntity mutatorsDataForEntity =
                addActions(logicalTypeNameSanitized, objectSpecification, objectTypeBuilder, graphQLObjectTypes, metaTypeBuilder, metaFieldsTypeBuilder, metaMutationsTypeBuilder);

        // adds types for meta
        if (metaFieldsTypeBuilder != null) {
            graphQLObjectTypes.add(metaFieldsTypeBuilder.build());
        }
        if (metaMutationsTypeBuilder != null) {
            graphQLObjectTypes.add(metaMutationsTypeBuilder.build());
        }

        // build and register object type
        GraphQLObjectType graphQLObjectType = objectTypeBuilder.build();
        addTypeIfNotAlreadyPresent(graphQLObjectTypes, graphQLObjectType, logicalTypeNameSanitized);

        // build and regoster meta type
        GraphQLObjectType metaType = metaTypeBuilder.build();
        addTypeIfNotAlreadyPresent(graphQLObjectTypes, metaType, logicalTypeNameSanitized);

        // create and register data fetchers
        createAndRegisterDataFetchersForMetaData(
                codeRegistryBuilder, objectSpecificationBeanSort, metaType, gql_meta, graphQLObjectType);
        if (mutatorsDataForEntity != null) createAndRegisterDataFetchersForMutators(
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

        List<String> mutatorsTypeFields = mutatorsDataForEntity.getMutatorsTypeFields().stream().map(f -> f.getName()).collect(Collectors.toList());

        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(graphQLObjectType, _Utils.GQL_MUTATTIONS_FIELDNAME), new DataFetcher<Object>() {

            @Override
            public Object get(DataFetchingEnvironment environment) throws Exception {

                Bookmark bookmark = bookmarkService.bookmarkFor(environment.getSource()).orElse(null);
                if (bookmark == null) return null;
                return new GQLMutations(bookmark, bookmarkService, mutatorsTypeFields);

            }

        });

        GraphQLObjectType mutatorsType = mutatorsDataForEntity.getMutatorsType();
        ObjectSpecification objectSpecification = mutatorsDataForEntity.getObjectSpecification();
        mutatorsDataForEntity.getMutatorsTypeFields().forEach(
                mf -> {
                    Optional<ObjectAction> action = objectSpecification.getAction(mf.getName());
                    if (action.isPresent()) {
                        ObjectAction objectAction = action.get();
                        codeRegistryBuilder
                                .dataFetcher(
                                        FieldCoordinates.coordinates(mutatorsType, mf.getName()),
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

                                                ManagedObject owner = ManagedObject.of(specification, domainObject);

                                                ActionInteractionHead actionInteractionHead = objectAction.interactionHead(owner);

                                                Map<String, Object> arguments = dataFetchingEnvironment.getArguments();
                                                Can<ObjectActionParameter> parameters = objectAction.getParameters();
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
                                                            return ManagedObject.of(elementType, argumentValue);

                                                        default:
                                                            throw new RuntimeException("Not yet implemented");
                                                    }


                                                }).collect(Can.toCan());

                                                ManagedObject managedObject = objectAction
                                                        .execute(actionInteractionHead, canOfParams, InteractionInitiatedBy.USER);

                                                return managedObject.getPojo();
                                            }

                                        });

                    }
                }
        );

    }

    private ManagedObject getManagedObjectFromInputType(ObjectSpecification elementType, Object argumentValue) {
        LinkedHashMap map = (LinkedHashMap) argumentValue;
        String identifier = (String) map.get("id");
        Bookmark bookmark = Bookmark.forLogicalTypeNameAndIdentifier(elementType.getLogicalTypeName(), identifier);
        return bookmarkService.lookup(bookmark).map(p->ManagedObject.of(elementType, p)).orElse(ManagedObject.empty(elementType));
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

    void addFieldsFieldOnMetaTypeIfNotAlready(final GraphQLObjectType.Builder metaTypeBuilder, final String logicalTypeNameSanitized){
        GraphQLFieldDefinition fields =
                newFieldDefinition().name("fields").type(GraphQLTypeReference.typeRef(metaFieldsTypeName(logicalTypeNameSanitized))).build();
        if (!metaTypeBuilder.hasField("fields")){
            metaTypeBuilder.field(fields);
        }
    }

    void addMutationsOnMetaTypeIfNotAlready(GraphQLObjectType.Builder metaTypeBuilder, final String logicalTypeNameSanitized){
        if (metaTypeBuilder==null) metaTypeBuilder = newObject().name(metaMutationsTypeName(logicalTypeNameSanitized));
        GraphQLFieldDefinition mutations =
                newFieldDefinition().name("mutations").type(GraphQLTypeReference.typeRef(metaMutationsTypeName(logicalTypeNameSanitized))).build();
        if (!metaTypeBuilder.hasField("mutations")) {
            metaTypeBuilder.field(mutations);
        }
    }

    void addFields(
            final ObjectSpecification objectSpecification,
            final GraphQLObjectType.Builder objectTypeBuilder,
            final GraphQLObjectType.Builder metaTypeBuilder,
            final GraphQLObjectType.Builder metaTypeFieldsBuilder,
            ObjectTypeDataCollector objectTypeDataCollector) {

        objectSpecification.streamProperties(MixedIn.INCLUDED)
                .forEach(otoa -> {

                    ObjectSpecification fieldObjectSpecification = otoa.getElementType();
                    BeanSort beanSort = fieldObjectSpecification.getBeanSort();
                    String logicalTypeNameSanitized = logicalTypeNameSanitized(objectSpecification.getLogicalTypeName());
                    switch (beanSort) {

                        case VIEW_MODEL:
                        case ENTITY:

                            // _gql_meta 'maintenance'
                            addFieldsFieldOnMetaTypeIfNotAlready(metaTypeBuilder, logicalTypeNameSanitized);
                            metaTypeFieldsBuilder.field(newFieldDefinition().name(otoa.getId()).type(GraphQLTypeReference.typeRef(FIELD_META_DATA_TYPENAME)).build());
                            // END _gql_meta 'maintenance'

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

                            // _gql_meta 'maintenance'
                            addFieldsFieldOnMetaTypeIfNotAlready(metaTypeBuilder, logicalTypeNameSanitized);
                            metaTypeFieldsBuilder.field(newFieldDefinition().name(otoa.getId()).type(GraphQLTypeReference.typeRef(FIELD_META_DATA_TYPENAME)).build());
                            // END _gql_meta 'maintenance'

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
            final GraphQLObjectType.Builder objectTypeBuilder,
            final GraphQLObjectType.Builder metaTypeBuilder,
            final GraphQLObjectType.Builder metaTypeFieldsBuilder,
            ObjectTypeDataCollector objectTypeDataCollector) {

        objectSpecification.streamCollections(MixedIn.INCLUDED).forEach(otom -> {

            ObjectSpecification elementType = otom.getElementType();
            BeanSort beanSort = elementType.getBeanSort();
            switch (beanSort) {

                case VIEW_MODEL:
                case ENTITY:

                    // _gql_meta 'maintenance'
                    addFieldsFieldOnMetaTypeIfNotAlready(metaTypeBuilder, logicalTypeNameSanitized(objectSpecification.getLogicalTypeName()));
                    metaTypeFieldsBuilder.field(newFieldDefinition().name(otom.getId()).type(GraphQLTypeReference.typeRef(FIELD_META_DATA_TYPENAME)).build());
                    // END _gql_meta 'maintenance'

                    String logicalTypeNameOfField = elementType.getLogicalTypeName();
                    GraphQLFieldDefinition.Builder fieldBuilder = newFieldDefinition()
                            .name(otom.getId())
                            .type(GraphQLList.list(GraphQLTypeReference.typeRef(
                                    _Utils.logicalTypeNameSanitized(logicalTypeNameOfField))));
                    objectTypeBuilder.field(fieldBuilder);

                    break;

                case VALUE:

                    // _gql_meta 'maintenance'
                    addFieldsFieldOnMetaTypeIfNotAlready(metaTypeBuilder, logicalTypeNameSanitized(objectSpecification.getLogicalTypeName()));
                    metaTypeFieldsBuilder.field(newFieldDefinition().name(otom.getId()).type(GraphQLTypeReference.typeRef(FIELD_META_DATA_TYPENAME)).build());
                    // END _gql_meta 'maintenance'

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
            final Set<GraphQLType> graphQLObjectTypes,
            final GraphQLObjectType.Builder metaTypeBuilder,
            final GraphQLObjectType.Builder metaTypeFieldsBuilder,
            final GraphQLObjectType.Builder metaMutationsTypeBuilder) {

        String mutatorsTypeName = mutationsTypeName(logicalTypeNameSanitized);
        GraphQLObjectType.Builder mutatorsTypeBuilder = newObject().name(mutatorsTypeName);
        final List<GraphQLFieldDefinition> mutatorsTypeFields = new ArrayList<>();

        objectSpecification.streamActions(ActionScope.PRODUCTION, MixedIn.INCLUDED)
                .forEach(objectAction -> {

                    if (objectAction.getSemantics().isSafeInNature()) {

                        // _gql_meta 'maintenance' TODO: bring to separate method --
                        addFieldsFieldOnMetaTypeIfNotAlready(metaTypeBuilder, logicalTypeNameSanitized(objectSpecification.getLogicalTypeName()));

                        if (objectAction.getParameters().isNotEmpty()) {
                            // treat as parameterized field
                            GraphQLObjectType.Builder parameterizedFieldMetaDataTypeNameBuilder = newObject().name(parameterizedFieldMetaDataTypeName(logicalTypeNameSanitized, objectAction.getId()));
                            String parametersMetaDataTypeName = parametersMetaDataTypeName(logicalTypeNameSanitized, objectAction.getId());
                            parameterizedFieldMetaDataTypeNameBuilder.field(newFieldDefinition().name("params").type(GraphQLTypeReference.typeRef(parametersMetaDataTypeName)).build());
                            parameterizedFieldMetaDataTypeNameBuilder.field(newFieldDefinition().name("validate").type(Scalars.GraphQLString).build());
                            parameterizedFieldMetaDataTypeNameBuilder.field(newFieldDefinition().name("hide").type(Scalars.GraphQLBoolean).build());
                            parameterizedFieldMetaDataTypeNameBuilder.field(newFieldDefinition().name("disable").type(Scalars.GraphQLString).build());

                            GraphQLFieldDefinition metaTypeFieldDefinition = newFieldDefinition().name(objectAction.getId()).type(parameterizedFieldMetaDataTypeNameBuilder).build();
                            metaTypeFieldsBuilder.field(metaTypeFieldDefinition);

                            GraphQLObjectType.Builder parametersMetaDataTypeNameBuilder = newObject().name(parametersMetaDataTypeName);
                            objectAction.getParameters().forEach(p->{

                                GraphQLObjectType.Builder parameterMetaDataType = newObject().name(parameterMetaDataTypeName(logicalTypeNameSanitized, objectAction.getId(), p.getId()));
                                parameterMetaDataType.field(newFieldDefinition().name("optionality").type(Scalars.GraphQLBoolean).build());
                                parameterMetaDataType.field(newFieldDefinition().name("default").type(Scalars.GraphQLString).build()); // for now
                                parameterMetaDataType.field(newFieldDefinition().name("choices").type(Scalars.GraphQLString).build()); // for now
                                parameterMetaDataType.field(newFieldDefinition().name("autocomplete").argument(GraphQLArgument.newArgument().name("we_call_search_for_now").type(Scalars.GraphQLString).build()).type(Scalars.GraphQLBoolean).build()); // for now
                                parameterMetaDataType.field(newFieldDefinition().name("validate").argument(GraphQLArgument.newArgument().name("we_call_value_for_now").type(Scalars.GraphQLString).build()).type(Scalars.GraphQLString).build());

                                parametersMetaDataTypeNameBuilder.field(newFieldDefinition().name(p.getId()).type(parameterMetaDataType));

                            });
                            graphQLObjectTypes.add(parametersMetaDataTypeNameBuilder.build());

                        } else {
                            // treat as simple field
                            metaTypeFieldsBuilder.field(newFieldDefinition().name(objectAction.getId()).type(GraphQLTypeReference.typeRef(FIELD_META_DATA_TYPENAME)).build());
                        }
                        // END _gql_meta 'maintenance'

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

                        addMutationsOnMetaTypeIfNotAlready(metaTypeBuilder, logicalTypeNameSanitized(objectSpecification.getLogicalTypeName()));

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

                        GraphQLFieldDefinition metaTypeMutatorDefinition = newFieldDefinition().name(objectAction.getId()).type(GraphQLTypeReference.typeRef(MUTATOR_META_DATA_TYPENAME)).build();
                        metaMutationsTypeBuilder.field(metaTypeMutatorDefinition);

                    }


                });

        if (!mutatorsTypeFields.isEmpty()) {
            GraphQLObjectType mutatorsType = mutatorsTypeBuilder.build();
            addTypeIfNotAlreadyPresent(graphQLObjectTypes, mutatorsType, mutatorsTypeName);
            GraphQLFieldDefinition gql_mutations = newFieldDefinition()
                    .name(_Utils.GQL_MUTATTIONS_FIELDNAME)
                    .type(mutatorsType)
                    .build();
            objectTypeBuilder.field(gql_mutations);

            return new MutatorsDataForEntity(objectSpecification, mutatorsType, mutatorsTypeFields);

        }

        return null;

    }

    @Data
    @AllArgsConstructor
    class MutatorsDataForEntity {

        private ObjectSpecification objectSpecification;

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

                                    return managedObject != null ? managedObject.getPojo() : null;

                                });


                break;

        }
    }

    void createAndRegisterDataFetchersForMetaData(
            final GraphQLCodeRegistry.Builder codeRegistryBuilder,
            final BeanSort objectSpecificationBeanSort,
            final GraphQLObjectType metaType,
            final GraphQLFieldDefinition gql_meta,
            final GraphQLObjectType graphQLObjectType) {

        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(graphQLObjectType, gql_meta), new DataFetcher<Object>() {
            @Override
            public Object get(final DataFetchingEnvironment environment) throws Exception {

                Bookmark bookmark = bookmarkService.bookmarkFor(environment.getSource()).orElse(null);
                if (bookmark == null) return null; //TODO: is this correct ?
                return new GQLMeta(bookmark, bookmarkService);
            }
        });

        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(metaType, idField), new DataFetcher<Object>() {
            @Override
            public Object get(final DataFetchingEnvironment environment) throws Exception {

                GQLMeta gqlMeta = environment.getSource();

                return gqlMeta.id();
            }
        });

        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(metaType, logicalTypeNameField), new DataFetcher<Object>() {
            @Override
            public Object get(final DataFetchingEnvironment environment) throws Exception {

                GQLMeta gqlMeta = environment.getSource();

                return gqlMeta.logicalTypeName();
            }
        });

        if (objectSpecificationBeanSort == BeanSort.ENTITY) {
            codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(metaType, versionField), new DataFetcher<Object>() {
                @Override
                public Object get(final DataFetchingEnvironment environment) throws Exception {

                    GQLMeta gqlMeta = environment.getSource();

                    return gqlMeta.version();
                }
            });

        }

        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(metaType, iconNameField), new DataFetcher<Object>() {
            @Override
            public Object get(final DataFetchingEnvironment environment) throws Exception {

                GQLMeta gqlMeta = environment.getSource();

                return gqlMeta.version();
            }
        });
    }

}
