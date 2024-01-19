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

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import graphql.Scalars;
import graphql.schema.DataFetcher;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLObjectType;
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

        graphQLTypeRegistry.addTypeIfNotAlreadyPresent(gqlvObjectSpec.getMetaField().getType());


        // create input type
        GraphQLInputType inputType = gqlvObjectSpec.getGqlInputObjectType();
        graphQLTypeRegistry.addTypeIfNotAlreadyPresent(inputType);

        gqlvObjectSpec.addPropertiesAsFields();

        gqlvObjectSpec.addCollectionsAsLists();

        // add actions
        MutatorsDataForEntity mutatorsDataForEntity = gqlvObjectSpec.addActions(graphQLTypeRegistry);
        if(gqlvObjectSpec.hasMutators()) {
            gqlvObjectSpec.getMutatorsTypeIfAny().ifPresent(graphQLTypeRegistry::addTypeIfNotAlreadyPresent);
        }

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


    @Data
    @AllArgsConstructor
    static class MutatorsDataForEntity {

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
        String mutatorsTypeName = logicalTypeNameSanitized + "__DomainObject_mutators";
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

        GraphQLObjectType metaType = gqlvObjectSpec.getMetaType();
        gqlvObjectSpec.getMetaField().getType();
        codeRegistryBuilder.dataFetcher(
                FieldCoordinates.coordinates(metaType, Fields.id),
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
