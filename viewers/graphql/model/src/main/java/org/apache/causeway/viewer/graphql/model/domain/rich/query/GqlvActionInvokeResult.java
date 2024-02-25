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
package org.apache.causeway.viewer.graphql.model.domain.rich.query;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Environment;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstract;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.ActionInteractor;
import org.apache.causeway.viewer.graphql.model.exceptions.DisabledException;
import org.apache.causeway.viewer.graphql.model.exceptions.HiddenException;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GqlvActionInvokeResult extends GqlvAbstract {

    private final ActionInteractor actionInteractor;

    public GqlvActionInvokeResult(
            final ActionInteractor actionInteractor,
            final Context context) {
        super(context);

        this.actionInteractor = actionInteractor;

        val objectAction = actionInteractor.getObjectMember();

        val graphQLOutputType = typeFor(objectAction);
        if (graphQLOutputType != null) {
            val fieldBuilder = newFieldDefinition()
                    .name("results")
                    .type(graphQLOutputType);
            setField(fieldBuilder.build());
        } else {
            setField(null);
        }
    }

    @Nullable
    private GraphQLOutputType typeFor(final ObjectAction objectAction){
        val objectSpecification = objectAction.getReturnType();
        switch (objectSpecification.getBeanSort()){

            case COLLECTION:

                TypeOfFacet facet = objectAction.getFacet(TypeOfFacet.class);
                if (facet == null) {
                    log.warn("Unable to locate TypeOfFacet for {}", objectAction.getFeatureIdentifier().getFullIdentityString());
                    return null;
                }
                val objectSpecificationOfCollectionElement = facet.elementSpec();
                GraphQLType wrappedType = context.typeMapper.outputTypeFor(objectSpecificationOfCollectionElement, actionInteractor.getSchemaType());
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
                return context.typeMapper.outputTypeFor(objectSpecification, actionInteractor.getSchemaType());

        }
    }

    @Override
    protected Object fetchData(final DataFetchingEnvironment dataFetchingEnvironment) {

        val sourcePojo = BookmarkedPojo.sourceFrom(dataFetchingEnvironment);

        val environment = new Environment.ForTunnelled(dataFetchingEnvironment);

        val objectSpecification = context.specificationLoader.loadSpecification(sourcePojo.getClass());
        if (objectSpecification == null) {
            return null;
        }

        val objectAction = actionInteractor.getObjectMember();
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
        val argumentManagedObjects = actionInteractor.argumentManagedObjectsFor(environment, objectAction, context.bookmarkService);

        val validityConsent = objectAction.isArgumentSetValid(head, argumentManagedObjects, InteractionInitiatedBy.USER);
        if (validityConsent.isVetoed()) {
            throw new IllegalArgumentException(validityConsent.getReasonAsString().orElse("Invalid"));
        }

        val resultManagedObject = objectAction.execute(head, argumentManagedObjects, InteractionInitiatedBy.USER);
        return resultManagedObject.getPojo();
    }


}
