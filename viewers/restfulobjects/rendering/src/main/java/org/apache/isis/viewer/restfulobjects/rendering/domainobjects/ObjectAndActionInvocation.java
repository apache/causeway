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
package org.apache.isis.viewer.restfulobjects.rendering.domainobjects;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.facets.collections.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ActionResultRepresentation;

import lombok.NonNull;

public class ObjectAndActionInvocation {

    public static ObjectAndActionInvocation of(
            @NonNull ActionInteraction.Result actionInteractionResult, 
            @NonNull JsonRepresentation argsJsonRepr,
            @NonNull ActionResultReprRenderer.SelfLink selfLink) {
        return new ObjectAndActionInvocation(
                actionInteractionResult.getManagedAction().getOwner(), 
                actionInteractionResult.getManagedAction().getAction(),
                argsJsonRepr,
                actionInteractionResult.getParameterList(),
                actionInteractionResult.getActionReturnedObject(), 
                selfLink);
    }
    
    private final ManagedObject objectAdapter;
    private final ObjectAction action;
    private final JsonRepresentation arguments;
    private final Can<ManagedObject> argAdapters;
    private final ManagedObject returnedAdapter;
    private final ActionResultReprRenderer.SelfLink selfLink;

    public ObjectAndActionInvocation(
            final ManagedObject objectAdapter,
            final ObjectAction action,
            final JsonRepresentation arguments,
            final Can<ManagedObject> argAdapters,
            final ManagedObject returnedAdapter,
            final ActionResultReprRenderer.SelfLink selfLink) {
        
        this.objectAdapter = objectAdapter;
        this.action = action;
        this.arguments = arguments;
        this.argAdapters = argAdapters;
        this.returnedAdapter = returnedAdapter;
        this.selfLink = selfLink;
    }

    public ManagedObject getObjectAdapter() {
        return objectAdapter;
    }

    public ObjectAction getAction() {
        return action;
    }

    public JsonRepresentation getArguments() {
        return arguments;
    }

    public Can<ManagedObject> getArgAdapters() {
        return argAdapters;
    }

    public ManagedObject getReturnedAdapter() {
        return returnedAdapter;
    }

    public ActionResultReprRenderer.SelfLink getSelfLink() {
        return selfLink;
    }


    /**
     * not API
     */
    public ActionResultRepresentation.ResultType determineResultType() {

        final ObjectSpecification returnType = this.action.getReturnType();

        if (returnType.getCorrespondingClass() == void.class) {
            return ActionResultRepresentation.ResultType.VOID;
        }

        final CollectionFacet collectionFacet = returnType.getFacet(CollectionFacet.class);
        if (collectionFacet != null) {
            return ActionResultRepresentation.ResultType.LIST;
        }

        final EncodableFacet encodableFacet = returnType.getFacet(EncodableFacet.class);
        if (encodableFacet != null) {
            return ActionResultRepresentation.ResultType.SCALAR_VALUE;
        }

        // else
        return ActionResultRepresentation.ResultType.DOMAIN_OBJECT;
    }



}