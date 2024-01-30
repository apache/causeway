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

import graphql.schema.*;

import lombok.extern.log4j.Log4j2;
import lombok.val;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.graphql.applib.types.TypeMapper;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.exceptions.DisabledException;
import org.apache.causeway.viewer.graphql.model.exceptions.HiddenException;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectActionProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectSpecificationProvider;

import org.springframework.lang.Nullable;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

@Log4j2
public class GqlvActionInvokeMutating {

    private final Holder holder;
    private final Context context;
    private final GraphQLFieldDefinition field;

    public GqlvActionInvokeMutating(
            final Holder holder,
            final Context context) {
        this.holder = holder;
        this.context = context;

        val objectAction = holder.getObjectAction();

        GraphQLOutputType type = typeFor(objectAction);
        if (type != null) {
            val fieldBuilder = newFieldDefinition()
                    .name(fieldNameForSemanticsOf(objectAction))
                    .type(type);
            holder.addGqlArguments(fieldBuilder, TypeMapper.InputContext.INVOKE);
            this.field = holder.addField(fieldBuilder.build());
        } else {
            this.field = null;
        }
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
    private GraphQLOutputType typeFor(final ObjectAction objectAction){
        ObjectSpecification objectSpecification = objectAction.getReturnType();
        switch (objectSpecification.getBeanSort()){

            case COLLECTION:

                TypeOfFacet facet = objectAction.getFacet(TypeOfFacet.class);
                if (facet == null) {
                    log.warn("Unable to locate TypeOfFacet for {}", objectAction.getFeatureIdentifier().getFullIdentityString());
                    return null;
                }
                val objectSpecificationOfCollectionElement = facet.elementSpec();
                GraphQLType wrappedType = context.typeMapper.outputTypeFor(objectSpecificationOfCollectionElement);
                if (wrappedType == null) {
                    log.warn("Unable to create wrapped type of for {} for action {}",
                            objectSpecificationOfCollectionElement.getFullIdentifier(),
                            objectAction.getFeatureIdentifier().getFullIdentityString());
                    return null;
                }
                return GraphQLList.list(wrappedType);

            case VALUE:
            case ENTITY:
            case VIEW_MODEL:
            default:
                return context.typeMapper.outputTypeFor(objectSpecification);

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

        val visibleConsent = objectAction.isVisible(managedObject, InteractionInitiatedBy.USER, Where.ANYWHERE);
        if (visibleConsent.isVetoed()) {
            throw new HiddenException(objectAction.getFeatureIdentifier());
        }

        val usableConsent = objectAction.isUsable(managedObject, InteractionInitiatedBy.USER, Where.ANYWHERE);
        if (usableConsent.isVetoed()) {
            throw new DisabledException(objectAction.getFeatureIdentifier());
        }

        val head = objectAction.interactionHead(managedObject);
        val argumentManagedObjects = holder.argumentManagedObjectsFor(dataFetchingEnvironment, objectAction, context.bookmarkService);

        val validityConsent = objectAction.isArgumentSetValid(head, argumentManagedObjects, InteractionInitiatedBy.USER);
        if (validityConsent.isVetoed()) {
            throw new IllegalArgumentException(validityConsent.getReasonAsString().orElse("Invalid"));
        }

        val resultManagedObject = objectAction.execute(head, argumentManagedObjects, InteractionInitiatedBy.USER);
        return resultManagedObject.getPojo();
    }

    public interface Holder
            extends GqlvHolder,
                    ObjectSpecificationProvider,
                    ObjectActionProvider {

        void addGqlArguments(
                final GraphQLFieldDefinition.Builder fieldBuilder,
                final TypeMapper.InputContext inputContext);

        Can<ManagedObject> argumentManagedObjectsFor(
                DataFetchingEnvironment dataFetchingEnvironment,
                ObjectAction objectAction,
                BookmarkService bookmarkService);
    }
}
