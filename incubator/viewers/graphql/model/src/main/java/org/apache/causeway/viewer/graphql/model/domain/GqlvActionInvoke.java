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

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.viewer.graphql.model.context.Context;

import org.apache.causeway.viewer.graphql.model.exceptions.DisabledException;
import org.apache.causeway.viewer.graphql.model.exceptions.HiddenException;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectActionProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectSpecificationProvider;

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
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

@Log4j2
public class GqlvActionInvoke {

    private final Holder holder;
    private final Context context;
    private final GraphQLFieldDefinition field;

    public GqlvActionInvoke(
            final Holder holder,
            final Context context) {
        this.holder = holder;
        this.context = context;
        this.field = fieldDefinition(holder);
    }

    private static GraphQLFieldDefinition fieldDefinition(final Holder holder) {

        val objectAction = holder.getObjectAction();

        GraphQLFieldDefinition fieldDefinition = null;
        GraphQLOutputType type = typeFor(objectAction);
        if (type != null) {
            val fieldBuilder = newFieldDefinition()
                    .name(fieldNameForSemanticsOf(objectAction))
                    .type(type);
            GqlvAction.addGqlArguments(objectAction, fieldBuilder, TypeMapper.InputContext.INVOKE, objectAction.getParameterCount());
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
        context.codeRegistryBuilder.dataFetcher(
                holder.coordinatesFor(field),
                this::invoke
        );
    }

    private Object invoke(final DataFetchingEnvironment dataFetchingEnvironment) {

        val sourcePojo = BookmarkedPojo.sourceFrom(dataFetchingEnvironment);

        val objectSpecification = context.specificationLoader.loadSpecification(sourcePojo.getClass());
        if (objectSpecification == null) {
            return null;
        }

        val objectAction = holder.getObjectAction();
        val managedObject = ManagedObject.adaptSingular(objectSpecification, sourcePojo);
        val actionInteractionHead = objectAction.interactionHead(managedObject);

        val argumentManagedObjects = GqlvAction.argumentManagedObjectsFor(dataFetchingEnvironment, objectAction, context.bookmarkService);


        val visibleConsent = objectAction.isVisible(managedObject, InteractionInitiatedBy.USER, Where.ANYWHERE);
        if (visibleConsent.isVetoed()) {
            throw new HiddenException(objectAction.getFeatureIdentifier());
        }

        val usableConsent = objectAction.isUsable(managedObject, InteractionInitiatedBy.USER, Where.ANYWHERE);
        if (usableConsent.isVetoed()) {
            throw new DisabledException(objectAction.getFeatureIdentifier());
        }

        val validityConsent = objectAction.isArgumentSetValid(actionInteractionHead, argumentManagedObjects, InteractionInitiatedBy.USER);
        if (validityConsent.isVetoed()) {
            throw new IllegalArgumentException(validityConsent.getReasonAsString().orElse("Invalid"));
        }

        val resultManagedObject = objectAction.execute(actionInteractionHead, argumentManagedObjects, InteractionInitiatedBy.USER);
        return resultManagedObject.getPojo();
    }

    public interface Holder
            extends GqlvHolder,
            ObjectSpecificationProvider,
            ObjectActionProvider {

    }
}
