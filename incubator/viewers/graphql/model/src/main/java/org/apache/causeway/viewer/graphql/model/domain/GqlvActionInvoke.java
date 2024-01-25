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
package org.apache.causeway.viewer.graphql.model.domain;

import org.apache.causeway.applib.services.bookmark.BookmarkService;

import org.springframework.lang.Nullable;

import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

@Log4j2
public class GqlvActionInvoke {

    private final GqlvActionInvokeHolder holder;
    private final GraphQLCodeRegistry.Builder codeRegistryBuilder;
    private final GraphQLFieldDefinition field;
    private final BookmarkService bookmarkService;

    public GqlvActionInvoke(
            final GqlvActionInvokeHolder holder,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder,
            final BookmarkService bookmarkService) {
        this.holder = holder;
        this.codeRegistryBuilder = codeRegistryBuilder;
        this.field = fieldDefinition(holder);
        this.bookmarkService = bookmarkService;
    }

    private static GraphQLFieldDefinition fieldDefinition(final GqlvActionInvokeHolder holder) {

        val objectAction = holder.getObjectAction();

        GraphQLFieldDefinition fieldDefinition = null;
        GraphQLOutputType type = typeFor(objectAction);
        if (type != null) {
            val fieldBuilder = newFieldDefinition()
                    .name(fieldNameForSemanticsOf(objectAction))
                    .type(type);
            GqlvAction.addGqlArguments(objectAction, fieldBuilder, TypeMapper.InputContext.INVOKE);
            fieldDefinition = fieldBuilder.build();

            holder.addField(fieldDefinition);
        }
        return fieldDefinition;
    }

    private static String fieldNameForSemanticsOf(ObjectAction objectAction) {
        switch (objectAction.getSemantics()) {
            case SAFE_AND_REQUEST_CACHEABLE:
            case SAFE:
                return "invoke";
            case IDEMPOTENT:
            case IDEMPOTENT_ARE_YOU_SURE:
                return "invokeIdempotent";
            case NON_IDEMPOTENT:
            case NON_IDEMPOTENT_ARE_YOU_SURE:
            case NOT_SPECIFIED:
            default:
                return "invokeNonIdempotent";
        }
    }

    @Nullable
    private static GraphQLOutputType typeFor(final ObjectAction objectAction){
        ObjectSpecification objectSpecification = objectAction.getReturnType();
        switch (objectSpecification.getBeanSort()){

            case COLLECTION:

                TypeOfFacet facet = objectAction.getFacet(TypeOfFacet.class);
                if (facet == null) {
                    log.warn("Unable to locate TypeOfFacet for {}", objectAction.getFeatureIdentifier().getFullIdentityString());
                    return null;
                }
                ObjectSpecification objectSpecificationForElementWhenCollection = facet.elementSpec();
                GraphQLType wrappedType = TypeMapper.outputTypeFor(objectSpecificationForElementWhenCollection);
                if (wrappedType == null) {
                    log.warn("Unable to create wrapped type of for {} for action {}",
                            objectSpecificationForElementWhenCollection.getFullIdentifier(),
                            objectAction.getFeatureIdentifier().getFullIdentityString());
                    return null;
                }
                return GraphQLList.list(wrappedType);

            case VALUE:
            case ENTITY:
            case VIEW_MODEL:
            default:
                return TypeMapper.outputTypeFor(objectSpecification);

        }
    }

    public void addDataFetcher() {
        codeRegistryBuilder.dataFetcher(
                holder.coordinatesFor(field),
                this::invoke
        );
    }

    private Object invoke(final DataFetchingEnvironment dataFetchingEnvironment) {

        final ObjectAction objectAction = holder.getObjectAction();

        val sourcePojo = BookmarkedPojo.sourceFrom(dataFetchingEnvironment);

        val sourcePojoClass = sourcePojo.getClass();
        val specificationLoader = objectAction.getSpecificationLoader();
        val objectSpecification = specificationLoader.loadSpecification(sourcePojoClass);
        if (objectSpecification == null) {
            // not expected
            return null;
        }

        val managedObject = ManagedObject.adaptSingular(objectSpecification, sourcePojo);
        val actionInteractionHead = objectAction.interactionHead(managedObject);

        val argumentManagedObjects = GqlvAction.argumentManagedObjectsFor(dataFetchingEnvironment, objectAction, bookmarkService);

        val consent = objectAction.isArgumentSetValid(actionInteractionHead, argumentManagedObjects, InteractionInitiatedBy.USER);
        if (consent.isVetoed()) {
            throw new IllegalArgumentException(consent.getReasonAsString().orElse("Invalid"));
        }

        val resultManagedObject = objectAction
                .execute(actionInteractionHead, argumentManagedObjects, InteractionInitiatedBy.USER);

        return resultManagedObject.getPojo();

    }

}
