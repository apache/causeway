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
package org.apache.causeway.viewer.restfulobjects.viewer.resources;

import java.util.concurrent.atomic.LongAdder;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.commons.functional.Railway;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteraction.Result;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteraction.SemanticConstraint;
import org.apache.causeway.core.metamodel.interactions.managed.InteractionVeto;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedMember;
import org.apache.causeway.core.metamodel.interactions.managed.MemberInteraction.AccessIntent;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.rendering.domainobjects.ActionResultReprRenderer;
import org.apache.causeway.viewer.restfulobjects.rendering.domainobjects.DomainObjectLinkTo;
import org.apache.causeway.viewer.restfulobjects.rendering.domainobjects.DomainServiceLinkTo;
import org.apache.causeway.viewer.restfulobjects.rendering.domainobjects.ObjectAndActionInvocation;
import org.apache.causeway.viewer.restfulobjects.rendering.service.RepresentationService;
import org.apache.causeway.viewer.restfulobjects.viewer.context.ResourceContext;

import org.jspecify.annotations.NonNull;

import org.springframework.http.ResponseEntity;

record _DomainResourceHelper(
    ResourceContext resourceContext,
    RepresentationService representationService,
    TransactionService transactionService,
    ManagedObject objectAdapter) {

    // -- FACTORIES

    public static _DomainResourceHelper ofObjectResource(
            final ResourceContext resourceContext,
            final ManagedObject objectAdapter) {
        _Assert.assertTrue(resourceContext.objectAdapterLinkTo() instanceof DomainObjectLinkTo);
        return new _DomainResourceHelper(resourceContext, objectAdapter);
    }

    public static _DomainResourceHelper ofServiceResource(
            final ResourceContext resourceContext,
            final String serviceIdOrAlias) {
        _Assert.assertTrue(resourceContext.objectAdapterLinkTo() instanceof DomainServiceLinkTo);
        return new _DomainResourceHelper(resourceContext,
            resourceContext.lookupServiceAdapterElseFail(serviceIdOrAlias));
    }

    // -- NON CANONICAL CONSTRUCTOR

    private _DomainResourceHelper(
            final ResourceContext resourceContext,
            final ManagedObject objectAdapter) {

        this(
            resourceContext,
            resourceContext.lookupServiceElseFail(RepresentationService.class),
            resourceContext.lookupServiceElseFail(TransactionService.class),
            objectAdapter);

        resourceContext.objectAdapterLinkTo()
            .usingUrlBase(this.resourceContext)
            .with(this.objectAdapter);
    }

    // resource delegate here ...

    /**
     * Simply delegates to the {@link org.apache.causeway.viewer.restfulobjects.rendering.service.RepresentationService} to
     * render a representation of the object.
     */
    public ResponseEntity<Object> objectRepresentation() {
        transactionService.flushTransaction();
        return representationService
                .objectRepresentation(resourceContext, objectAdapter);
    }

    /**
     * Obtains the property (checking it is visible) of the object and then delegates to the
     * {@link org.apache.causeway.viewer.restfulobjects.rendering.service.RepresentationService} to render a representation
     * of that property.
     */
    public ResponseEntity<Object> propertyDetails(
            final String propertyId,
            final ManagedMember.RepresentationMode representationMode) {

        var property = ObjectAdapterAccessHelper.of(resourceContext, objectAdapter)
                .getPropertyThatIsVisibleForIntent(propertyId, AccessIntent.ACCESS);
        property.setRepresentationMode(representationMode);

        transactionService.flushTransaction();
        return representationService.propertyDetails(resourceContext, property);
    }

    /**
     * Obtains the collection (checking it is visible) of the object and then delegates to the
     * {@link org.apache.causeway.viewer.restfulobjects.rendering.service.RepresentationService} to render a representation
     * of that collection.
     */
    public ResponseEntity<Object> collectionDetails(
            final String collectionId,
            final ManagedMember.RepresentationMode representationMode) {

        var collection = ObjectAdapterAccessHelper.of(resourceContext, objectAdapter)
                .getCollectionThatIsVisibleForIntent(collectionId, AccessIntent.ACCESS);
        collection.setRepresentationMode(representationMode);

        transactionService.flushTransaction();
        return representationService.collectionDetails(resourceContext, collection);
    }

    /**
     * Obtains the action details (arguments etc), checking it is visible, of the object and then delegates to the
     * {@link org.apache.causeway.viewer.restfulobjects.rendering.service.RepresentationService} to render a representation
     * of that object's action (arguments).
     */
    public ResponseEntity<Object> actionPrompt(final String actionId) {

        var action = ObjectAdapterAccessHelper.of(resourceContext, objectAdapter)
                .getObjectActionThatIsVisibleForIntentAndSemanticConstraint(
                        actionId, AccessIntent.ACCESS, SemanticConstraint.NONE);

        transactionService.flushTransaction();
        return representationService.actionPrompt(resourceContext, action);
    }

    /**
     * Invokes the action for the object  (checking it is visible) and then delegates to the
     * {@link org.apache.causeway.viewer.restfulobjects.rendering.service.RepresentationService} to render a representation
     * of the result of that action.
     *
     * <p>
     *     The action must have {@link SemanticsOf#isSafeInNature()} safe/request-cacheable}  semantics
     *     otherwise an error response is thrown.
     * </p>
     */
    public ResponseEntity<Object> invokeActionQueryOnly(final String actionId, final JsonRepresentation arguments) {

        return invokeAction(
                actionId, AccessIntent.MUTATE, SemanticConstraint.SAFE,
                arguments, ActionResultReprRenderer.SelfLink.EXCLUDED);
    }

    /**
     * Invokes the action for the object  (checking it is visible) and then delegates to the
     * {@link org.apache.causeway.viewer.restfulobjects.rendering.service.RepresentationService} to render a representation
     * of the result of that action.
     *
     * <p>
     *     The action must have {@link SemanticsOf#IDEMPOTENT idempotent}
     *     semantics otherwise an error response is thrown.
     * </p>
     */
    public ResponseEntity<Object> invokeActionIdempotent(final String actionId, final JsonRepresentation arguments) {

        return invokeAction(
                actionId, AccessIntent.MUTATE, SemanticConstraint.IDEMPOTENT,
                arguments, ActionResultReprRenderer.SelfLink.EXCLUDED);
    }

    /**
     * Invokes the action for the object  (checking it is visible) and then delegates to the
     * {@link org.apache.causeway.viewer.restfulobjects.rendering.service.RepresentationService} to render a representation
     * of the result of that action.
     */
    public ResponseEntity<Object> invokeAction(final String actionId, final JsonRepresentation arguments) {

        return invokeAction(
                actionId, AccessIntent.MUTATE, SemanticConstraint.NONE,
                arguments, ActionResultReprRenderer.SelfLink.EXCLUDED);
    }

    // -- HELPER

    private ResponseEntity<Object> invokeAction(
            final @NonNull String actionId,
            final @NonNull AccessIntent intent,
            final @NonNull SemanticConstraint semanticConstraint,
            final @NonNull JsonRepresentation arguments,
            final ActionResultReprRenderer.@NonNull SelfLink selfLink) {

        var where = resourceContext.where();

        // lombok issue, needs explicit cast here
        var actionInteraction = ActionInteraction.start(objectAdapter, actionId, where)
        .checkVisibility()
        .checkUsability(intent)
        .checkSemanticConstraint(semanticConstraint);

        var pendingArgs = actionInteraction.startParameterNegotiation().orElse(null);

        if(pendingArgs==null) {
            // no such action or not visible or not usable
            throw InteractionFailureHandler.onFailure(actionInteraction
                    .getInteractionVeto()
                    .orElseGet(()->InteractionVeto.notFound(Identifier.Type.ACTION, actionId))); // unexpected code reach
        }

        var hasParams = pendingArgs.getParamCount()>0;

        if(hasParams) {

            // parse parameters ...

            var vetoCount = new LongAdder();

            var paramsOrVetos = ObjectActionArgHelper
                    .parseArguments(resourceContext, pendingArgs.act(), arguments);

            pendingArgs.getParamModels().zip(paramsOrVetos, (managedParam, paramOrVeto)->{

                paramOrVeto.ifFailure(veto->{
                    InteractionFailureHandler
                        .collectParameterInvalid(managedParam.getMetaModel(), veto, arguments);
                    vetoCount.increment();
                });

            });

            if(vetoCount.intValue()>0) {
                throw InteractionFailureHandler.onParameterListInvalid(
                        InteractionVeto.actionParamInvalid("error parsing arguments"), arguments);
            }

            var argAdapters = paramsOrVetos.map(Railway::getSuccessElseFail);
            pendingArgs.setParamValues(argAdapters);

            // validate parameters ...

            var individualParamConsents = pendingArgs.validateParameterSetForParameters();

            pendingArgs.getParamModels().zip(individualParamConsents, (managedParam, consent)->{
                if(consent.isVetoed()) {
                    var veto = InteractionVeto.actionParamInvalid(consent);
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

        var actionConsent = pendingArgs.validateParameterSetForAction();
        if(actionConsent.isVetoed()) {
            throw InteractionFailureHandler.onParameterListInvalid(
                    InteractionVeto.invalid(actionConsent),
                    arguments);
        }

        if(resourceContext.isValidateOnly()) {
            return ResponseEntity.noContent().build(); // do not progress any further
        }

        var resultOrVeto = actionInteraction.invokeWith(pendingArgs);

        resultOrVeto.ifFailure(veto->{
            throw InteractionFailureHandler.onFailure(veto);
        });

        var actionInteractionResult = new Result(
                actionInteraction.getManagedAction().orElse(null),
                pendingArgs.getParamValues(),
                resultOrVeto.getSuccessElseFail());

        var objectAndActionInvocation = ObjectAndActionInvocation.of(actionInteractionResult, arguments, selfLink);

        // response
        transactionService.flushTransaction();
        return representationService.actionResult(resourceContext, objectAndActionInvocation);
    }

}
