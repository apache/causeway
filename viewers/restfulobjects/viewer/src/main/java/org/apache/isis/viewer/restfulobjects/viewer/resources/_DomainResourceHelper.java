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

import java.util.concurrent.atomic.LongAdder;

import javax.ws.rs.core.Response;

import org.apache.isis.applib.annotations.SemanticsOf;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.commons.internal.base._Either;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteraction.Result;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteraction.SemanticConstraint;
import org.apache.isis.core.metamodel.interactions.managed.InteractionVeto;
import org.apache.isis.core.metamodel.interactions.managed.ManagedMember;
import org.apache.isis.core.metamodel.interactions.managed.ManagedMember.MemberType;
import org.apache.isis.core.metamodel.interactions.managed.MemberInteraction.AccessIntent;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ActionResultReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.DomainObjectLinkTo;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.DomainServiceLinkTo;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAdapterLinkTo;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndActionInvocation;
import org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService;
import org.apache.isis.viewer.restfulobjects.viewer.context.ResourceContext;

import lombok.NonNull;
import lombok.val;

class _DomainResourceHelper {

    private final IResourceContext resourceContext;
    private final RepresentationService representationService;
    private final TransactionService transactionService;

    public static _DomainResourceHelper ofObjectResource(
            IResourceContext resourceContext,
            ManagedObject objectAdapter) {
        return new _DomainResourceHelper(resourceContext, objectAdapter, new DomainObjectLinkTo());
    }

    public static _DomainResourceHelper ofServiceResource(
            IResourceContext resourceContext,
            ManagedObject objectAdapter) {
        return new _DomainResourceHelper(resourceContext, objectAdapter, new DomainServiceLinkTo());
    }

    private _DomainResourceHelper(
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

        return invokeAction(
                actionId, AccessIntent.MUTATE, SemanticConstraint.SAFE,
                arguments, ActionResultReprRenderer.SelfLink.EXCLUDED);
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

        return invokeAction(
                actionId, AccessIntent.MUTATE, SemanticConstraint.IDEMPOTENT,
                arguments, ActionResultReprRenderer.SelfLink.EXCLUDED);
    }

    /**
     * Invokes the action for the object  (checking it is visible) and then delegates to the
     * {@link org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService} to render a representation
     * of the result of that action.
     */
    public Response invokeAction(final String actionId, final JsonRepresentation arguments) {

        return invokeAction(
                actionId, AccessIntent.MUTATE, SemanticConstraint.NONE,
                arguments, ActionResultReprRenderer.SelfLink.EXCLUDED);
    }

    private Response invokeAction(
            final @NonNull String actionId,
            final @NonNull AccessIntent intent,
            final @NonNull SemanticConstraint semanticConstraint,
            final @NonNull JsonRepresentation arguments,
            final @NonNull ActionResultReprRenderer.SelfLink selfLink) {

        val where = resourceContext.getWhere();

        // lombok issue, needs explicit cast here
        val actionInteraction = (ActionInteraction) ActionInteraction.start(objectAdapter, actionId, where)
        .checkVisibility()
        .checkUsability(intent)
        .checkSemanticConstraint(semanticConstraint);

        val pendingArgs = actionInteraction.startParameterNegotiation().orElse(null);

        if(pendingArgs==null) {
            // no such action or not visible or not usable
            throw InteractionFailureHandler.onFailure(actionInteraction
                    .getInteractionVeto()
                    .orElseGet(()->InteractionVeto.notFound(MemberType.ACTION, actionId))); // unexpected code reach
        }

        val hasParams = pendingArgs.getParamCount()>0;


        if(hasParams) {

            // parse parameters ...

            val action = pendingArgs.getHead().getMetaModel();
            val vetoCount = new LongAdder();

            val paramsOrVetos = ObjectActionArgHelper
                    .parseArguments(resourceContext, action, arguments);

            pendingArgs.getParamModels().zip(paramsOrVetos, (managedParam, paramOrVeto)->{
                if(paramOrVeto.isRight()) {
                    val veto = paramOrVeto.rightIfAny();
                    InteractionFailureHandler.collectParameterInvalid(managedParam.getMetaModel(), veto, arguments);
                    vetoCount.increment();
                }
            });

            if(vetoCount.intValue()>0) {
                throw InteractionFailureHandler.onParameterListInvalid(
                        InteractionVeto.actionParamInvalid("error parsing arguments"), arguments);
            }

            val argAdapters = paramsOrVetos.map(_Either::leftIfAny);
            pendingArgs.setParamValues(argAdapters);

            // validate parameters ...

            val individualParamConsents = pendingArgs.validateParameterSetForParameters();

            pendingArgs.getParamModels().zip(individualParamConsents, (managedParam, consent)->{
                if(consent.isVetoed()) {
                    val veto = InteractionVeto.actionParamInvalid(consent);
                    InteractionFailureHandler.collectParameterInvalid(managedParam.getMetaModel(), veto, arguments);
                    vetoCount.increment();
                }
            });

            if(vetoCount.intValue()>0) {
                throw InteractionFailureHandler.onParameterListInvalid(
                        InteractionVeto.actionParamInvalid(
                                String.format("%d argument(s) failed validation", vetoCount.intValue())),
                            arguments);
            }

        }

        val actionConsent = pendingArgs.validateParameterSetForAction();
        if(actionConsent.isVetoed()) {
            throw InteractionFailureHandler.onParameterListInvalid(
                    InteractionVeto.invalid(actionConsent),
                    arguments);
        }

        if(resourceContext.isValidateOnly()) {
            return Response.noContent().build(); // do not progress any further
        }

        val resultOrVeto = actionInteraction.invokeWith(pendingArgs);

        if(resultOrVeto.isRight()) {
            throw InteractionFailureHandler.onFailure(resultOrVeto.rightIfAny());
        }

        val actionInteractionResult = Result.of(
                actionInteraction.getManagedAction().orElse(null),
                pendingArgs.getParamValues(),
                resultOrVeto.leftIfAny());

        val objectAndActionInvocation = ObjectAndActionInvocation.of(actionInteractionResult, arguments, selfLink);

        // response
        transactionService.flushTransaction();
        return representationService.actionResult(resourceContext, objectAndActionInvocation);
    }


    // //////////////////////////////////////
    // dependencies (from context)
    // //////////////////////////////////////

    private <T> T lookupService(Class<T> serviceType) {
        return resourceContext.getMetaModelContext().getServiceRegistry().lookupServiceElseFail(serviceType);
    }

}

