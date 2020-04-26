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
package org.apache.isis.viewer.restfulobjects.viewer.resources;

import javax.ws.rs.core.Response;

import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.interaction.ActionInteraction.SemanticConstraint;
import org.apache.isis.core.metamodel.spec.interaction.ManagedAction;
import org.apache.isis.core.metamodel.spec.interaction.ManagedMember;
import org.apache.isis.core.metamodel.spec.interaction.MemberInteraction.AccessIntent;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ActionResultReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.DomainObjectLinkTo;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.DomainServiceLinkTo;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAdapterLinkTo;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndActionInvocation;
import org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService;
import org.apache.isis.viewer.restfulobjects.viewer.context.ResourceContext;

import lombok.val;

class DomainResourceHelper {

    private final IResourceContext resourceContext;
    private final RepresentationService representationService;
    private final TransactionService transactionService;

    public static DomainResourceHelper ofObjectResource(
            IResourceContext resourceContext,
            ManagedObject objectAdapter) {
        return new DomainResourceHelper(resourceContext, objectAdapter, new DomainObjectLinkTo());
    }
    
    public static DomainResourceHelper ofServiceResource(
            IResourceContext resourceContext,
            ManagedObject objectAdapter) {
        return new DomainResourceHelper(resourceContext, objectAdapter, new DomainServiceLinkTo());
    }
    
    private DomainResourceHelper(
            final IResourceContext resourceContext,
            final ManagedObject objectAdapter,
            final ObjectAdapterLinkTo adapterLinkTo) {

        ((ResourceContext)resourceContext).setObjectAdapterLinkTo(adapterLinkTo);
        
        this.resourceContext = resourceContext;
        this.objectAdapter = objectAdapter;

        adapterLinkTo.usingUrlBase(this.resourceContext)
        .with(this.objectAdapter);

        representationService = lookupService(RepresentationService.class);
        transactionService = lookupService(TransactionService.class);
    }

    private final ManagedObject objectAdapter;


    // //////////////////////////////////////
    // Helpers (resource delegate here)
    // //////////////////////////////////////

    /**
     * Simply delegates to the {@link org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService} to
     * render a representation of the object.
     */
    public Response objectRepresentation() {
        transactionService.flushTransaction();
        return representationService
                .objectRepresentation(resourceContext, objectAdapter);
    }

    /**
     * Obtains the property (checking it is visible) of the object and then delegates to the
     * {@link org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService} to render a representation
     * of that property.
     */
    public Response propertyDetails(
            final String propertyId,
            final ManagedMember.RepresentationMode representationMode) {

        val property = ObjectAdapterAccessHelper.of(resourceContext, objectAdapter)
                .getPropertyThatIsVisibleForIntent(propertyId, AccessIntent.ACCESS);
        property.setRepresentationMode(representationMode);

        transactionService.flushTransaction();
        return representationService.propertyDetails(resourceContext, property);
    }


    /**
     * Obtains the collection (checking it is visible) of the object and then delegates to the
     * {@link org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService} to render a representation
     * of that collection.
     */
    public Response collectionDetails(
            final String collectionId,
            final ManagedMember.RepresentationMode representationMode) {

        val collection = ObjectAdapterAccessHelper.of(resourceContext, objectAdapter)
                .getCollectionThatIsVisibleForIntent(collectionId, AccessIntent.ACCESS);
        collection.setRepresentationMode(representationMode);

        transactionService.flushTransaction();
        return representationService.collectionDetails(resourceContext, collection);
    }


    /**
     * Obtains the action details (arguments etc), checking it is visible, of the object and then delegates to the
     * {@link org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService} to render a representation
     * of that object's action (arguments).
     */
    public Response actionPrompt(final String actionId) {

        val action = ObjectAdapterAccessHelper.of(resourceContext, objectAdapter)
                .getObjectActionThatIsVisibleForIntentAndSemanticConstraint(
                        actionId, AccessIntent.ACCESS, SemanticConstraint.NONE);

        transactionService.flushTransaction();
        return representationService.actionPrompt(resourceContext, action);
    }

    /**
     * Invokes the action for the object  (checking it is visible) and then delegates to the
     * {@link org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService} to render a representation
     * of the result of that action.
     *
     * <p>
     *     The action must have {@link SemanticsOf#isSafeInNature()} safe/request-cacheable}  semantics
     *     otherwise an error response is thrown.
     * </p>
     */
    public Response invokeActionQueryOnly(final String actionId, final JsonRepresentation arguments) {

        val action = ObjectAdapterAccessHelper.of(resourceContext, objectAdapter)
                .getObjectActionThatIsVisibleForIntentAndSemanticConstraint(
                        actionId, AccessIntent.MUTATE, SemanticConstraint.SAFE);

        return invokeActionUsingAdapters(action, arguments, ActionResultReprRenderer.SelfLink.INCLUDED);
    }

    /**
     * Invokes the action for the object  (checking it is visible) and then delegates to the
     * {@link org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService} to render a representation
     * of the result of that action.
     *
     * <p>
     *     The action must have {@link SemanticsOf#IDEMPOTENT idempotent}
     *     semantics otherwise an error response is thrown.
     * </p>
     */
    public Response invokeActionIdempotent(final String actionId, final JsonRepresentation arguments) {

        val action = ObjectAdapterAccessHelper.of(resourceContext, objectAdapter)
                .getObjectActionThatIsVisibleForIntentAndSemanticConstraint(
                        actionId, AccessIntent.MUTATE, SemanticConstraint.IDEMPOTENT);

        return invokeActionUsingAdapters(action, arguments, ActionResultReprRenderer.SelfLink.EXCLUDED);
    }

    /**
     * Invokes the action for the object  (checking it is visible) and then delegates to the
     * {@link org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService} to render a representation
     * of the result of that action.
     */
    public Response invokeAction(final String actionId, final JsonRepresentation arguments) {

        val action = ObjectAdapterAccessHelper.of(resourceContext, objectAdapter)
                .getObjectActionThatIsVisibleForIntentAndSemanticConstraint(
                        actionId, AccessIntent.MUTATE, SemanticConstraint.NONE);

        return invokeActionUsingAdapters(action, arguments, ActionResultReprRenderer.SelfLink.EXCLUDED);
    }


    private Response invokeActionUsingAdapters(
            final ManagedAction managedAction,
            final JsonRepresentation arguments,
            final ActionResultReprRenderer.SelfLink selfLink) {

        val action = managedAction.getAction();
        val objectAdapter = this.objectAdapter;
        val argHelper = new ObjectActionArgHelper(resourceContext, objectAdapter, action);
        val argAdapters = argHelper.parseAndValidateArguments(arguments);

        if(resourceContext.isValidateOnly()) {
            // nothing more to do.
            // if there had been a validation error, then an exception would have been thrown above.
            return Response.noContent().build();
        }

        // invoke
        final ManagedObject mixedInAdapter = null; // action will automatically fill in if a mixin
        final ManagedObject returnedAdapter = action.execute(
                objectAdapter,  mixedInAdapter, argAdapters,
                InteractionInitiatedBy.USER);

        final ObjectAndActionInvocation objectAndActionInvocation =
                new ObjectAndActionInvocation(objectAdapter, action, arguments, argAdapters, returnedAdapter, selfLink);

        // response
        transactionService.flushTransaction();
        return representationService.actionResult(resourceContext, objectAndActionInvocation, selfLink);
    }


    // //////////////////////////////////////
    // dependencies (from context)
    // //////////////////////////////////////

    private <T> T lookupService(Class<T> serviceType) {
        return resourceContext.getServiceRegistry().lookupServiceElseFail(serviceType);
    }

}

