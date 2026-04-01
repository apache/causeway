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
package org.apache.causeway.core.runtimeservices.executor;

import java.util.Optional;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

import org.jspecify.annotations.NonNull;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.iactn.ActionInvocation;
import org.apache.causeway.applib.services.iactn.Execution;
import org.apache.causeway.applib.services.iactn.Interaction;
import org.apache.causeway.applib.services.iactn.PropertyEdit;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration.ObservationProvider;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.MessageTemplate;
import org.apache.causeway.core.metamodel.commons.CanonicalInvoker;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.execution.ActionExecutor;
import org.apache.causeway.core.metamodel.execution.InteractionCarrier;
import org.apache.causeway.core.metamodel.execution.InteractionLayerTracker;
import org.apache.causeway.core.metamodel.execution.MemberExecutorService;
import org.apache.causeway.core.metamodel.execution.PropertyModifier;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.members.publish.execution.ExecutionPublishingFacet;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmEntityUtils;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtils;
import org.apache.causeway.core.metamodel.object.MmVisibilityUtils;
import org.apache.causeway.core.metamodel.object.PackedManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.services.ixn.InteractionDtoFactory;
import org.apache.causeway.core.metamodel.services.publishing.CommandPublisher;
import org.apache.causeway.core.metamodel.services.publishing.ExecutionPublisher;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.schema.ixn.v2.ActionInvocationDto;

import static org.apache.causeway.core.metamodel.facets.members.publish.command.CommandPublishingFacet.isPublishingEnabled;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation of {@link MemberExecutorService}.
 *
 * @since 2.0 {@index}
 */
@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".MemberExecutorServiceDefault")
@Priority(PriorityPrecedence.EARLY)
@Qualifier("Default")
@Slf4j
public class MemberExecutorServiceDefault
implements MemberExecutorService {

    private final InteractionLayerTracker interactionLayerTracker;
    private final CausewayConfiguration configuration;
    private final ObjectManager objectManager;
    private final InteractionDtoFactory interactionDtoFactory;
    private final Provider<ExecutionPublisher> executionPublisherProvider;
    private final TransactionService transactionService;
    private final Provider<CommandPublisher> commandPublisherProvider;
    private final ObservationProvider observationProvider;

    @Inject
    MemberExecutorServiceDefault(final InteractionLayerTracker interactionLayerTracker,
            final CausewayConfiguration configuration,
            final ObjectManager objectManager,
            final InteractionDtoFactory interactionDtoFactory,
            final Provider<ExecutionPublisher> executionPublisherProvider,
            final TransactionService transactionService,
            final Provider<CommandPublisher> commandPublisherProvider,
            final CausewayObservationIntegration observationIntegration) {
        this.interactionLayerTracker = interactionLayerTracker;
        this.configuration = configuration;
        this.objectManager = objectManager;
        this.interactionDtoFactory = interactionDtoFactory;
        this.executionPublisherProvider = executionPublisherProvider;
        this.transactionService = transactionService;
        this.commandPublisherProvider = commandPublisherProvider;
        this.observationProvider = observationIntegration.provider(getClass(),
                CausewayObservationIntegration.withModuleName(CausewayModuleCoreRuntimeServices.NAMESPACE));
    }

    @Override
    public ManagedObject invokeAction(
            final @NonNull ActionExecutor actionExecutor) {

        var executionResult = observationProvider.get("Action Invocation (%s)"
                .formatted(actionExecutor.owningAction().getFeatureIdentifier()))
                .lowCardinalityKeyValue("causeway.execution.initiatedBy", actionExecutor.interactionInitiatedBy().name())
                //could also add action's args as tags (but potentially sensitive)
                //(we do this with Xray, but that is local for debugging only)
            .observe(()->
                actionExecutor.interactionInitiatedBy().isPassThrough()
                ? Try.call(()->
                    invokeActionInternally(actionExecutor))
                : transactionService.callWithinCurrentTransactionElseCreateNew(()->
                    invokeActionInternally(actionExecutor)));

        return executionResult
                .valueAsNullableElseFail();
    }

    private ManagedObject invokeActionInternally(
            final ActionExecutor actionExecutor) {

        final ObjectAction owningAction = actionExecutor.owningAction();
        final InteractionHead head = actionExecutor.head();

        final Can<ManagedObject> argumentAdapters = actionExecutor.arguments();
        final InteractionInitiatedBy interactionInitiatedBy = actionExecutor.interactionInitiatedBy();
        final FacetHolder facetHolder = actionExecutor.facetHolder();

        if(interactionInitiatedBy.isPassThrough()) {
            var resultPojo = invokeMethodPassThrough(actionExecutor.method(), head, argumentAdapters);
            return facetHolder.getObjectManager().adapt(resultPojo);
        }

        var interactionCarrier = interactionCarrierElseFail();

        prepareCommandForPublishing(interactionCarrier.command(), head, owningAction, facetHolder);

        var xrayHandle = _Xray.enterActionInvocation(interactionLayerTracker, interactionCarrier, owningAction, head, argumentAdapters);

        var actionId = owningAction.getFeatureIdentifier();
        log.debug("about to invoke action {}", actionId);

        var targetAdapter = head.target();
        var targetPojo = MmUnwrapUtils.single(targetAdapter);

        var argumentPojos = argumentAdapters.stream()
                .map(MmUnwrapUtils::single)
                .collect(_Lists.toUnmodifiable());

        var actionInvocation = new ActionInvocation(
        		interactionCarrier.interaction(), actionId, targetPojo, argumentPojos);

        // sets up startedAt and completedAt on the execution, also manages the execution call graph
        execute(interactionCarrier, actionExecutor, actionInvocation);

        final var priorExecution = interactionCarrier.interaction().getPriorExecution();

        // throws if there was any exception in prior execution
        var executionExceptionIfAny = priorExecution.getThrew();
        if(executionExceptionIfAny != null) {
        	actionInvocation.setThrew(executionExceptionIfAny);
			throw executionExceptionIfAny instanceof RuntimeException rex
                ? rex
                : new RuntimeException(executionExceptionIfAny);
        }

        var returnedPojo = priorExecution.getReturned();
        var returnedAdapter = objectManager.adapt(
                returnedPojo, owningAction::getElementType);

        // assert has bookmark, unless non-scalar
        ManagedObjects.asScalarNonEmpty(returnedAdapter)
            .filter(scalarNonEmpty->!scalarNonEmpty.specialization().isOther()) // don't care
            // if its a transient entity, flush the current transaction, so we get an OID
            .filter(scalarNonEmpty->{
                MmEntityUtils.ifHasNoOidThenFlush(scalarNonEmpty);
                return true;
            })
            .ifPresent(scalarNonEmpty->{
                _Assert.assertTrue(scalarNonEmpty.getBookmark().isPresent(), ()->{
                    var returnTypeSpec = scalarNonEmpty.objSpec();
                    var violation = returnTypeSpec.isEntity()
                            ? MessageTemplate.ACTION_METHOD_RETURNING_TRANSIENT_ENTITY_NOT_ALLOWED
                            : MessageTemplate.ACTION_METHOD_RETURNING_NON_BOOKMARKABLE_OBJECT_NOT_ALLOWED;
                    return violation.builder()
                        .addVariablesFor(actionId)
                        .addVariable("returnTypeSpec", returnTypeSpec.toString())
                        .buildMessage();
                });
            });

        // sync DTO with result
        interactionDtoFactory
            .updateResult((ActionInvocationDto)priorExecution.getDto(), owningAction, returnedAdapter);

        // update Command (if required)
        setCommandResultIfEntity(interactionCarrier.command(), returnedAdapter);

        // publish (if not a contributed association, query-only mixin)
        if (ExecutionPublishingFacet.isPublishingEnabled(facetHolder)) {
            executionPublisher().publishActionInvocation(priorExecution);
        }

        var result = resultFilteredHonoringVisibility(returnedAdapter, interactionInitiatedBy);
        _Xray.exitInvocation(xrayHandle);
        return result;
    }

	@Override
    public ManagedObject setOrClearProperty(
            final @NonNull PropertyModifier propertyExecutor) {

        var executionResult = observationProvider.get("Property Update (%s)"
                .formatted(propertyExecutor.owningProperty().getFeatureIdentifier()))
                .lowCardinalityKeyValue("causeway.execution.initiatedBy", propertyExecutor.interactionInitiatedBy().name())
                //could also add property's old and new value as tags (but potentially sensitive)
                //(we do this with Xray, but that is local for debugging only)
            .observe(()->
                propertyExecutor.interactionInitiatedBy().isPassThrough()
                ? Try.call(()->
                    setOrClearPropertyInternally(propertyExecutor))
                : transactionService
                    .callWithinCurrentTransactionElseCreateNew(() ->
                        setOrClearPropertyInternally(propertyExecutor)));

        return executionResult
                .valueAsNullableElseFail();
    }

    private ManagedObject setOrClearPropertyInternally(
            final @NonNull PropertyModifier propertyModifier) {

        final InteractionHead head = propertyModifier.head();

        var domainObject = head.target();

        if(propertyModifier.interactionInitiatedBy().isPassThrough()) {
            /* directly access property setter to prevent triggering of domain events
             * or change tracking, eg. when called in the context of serialization */
            propertyModifier.executeClearOrSetWithoutEvents(propertyModifier.newValue());
            return domainObject;
        }

        var interactionCarrier = interactionCarrierElseFail();
        var command = interactionCarrier.command();

        var owningProperty = propertyModifier.owningProperty();

        prepareCommandForPublishing(command, head, owningProperty, propertyModifier.facetHolder());

        var xrayHandle = _Xray.enterPropertyEdit(interactionLayerTracker, interactionCarrier, owningProperty, head, propertyModifier.newValue());

        var propertyId = owningProperty.getFeatureIdentifier();

        var targetManagedObject = head.target();
        var target = MmUnwrapUtils.single(targetManagedObject);
        var argValuePojo = MmUnwrapUtils.single(propertyModifier.newValue());

        var propertyEdit = new PropertyEdit(interactionCarrier.interaction(), propertyId, target, argValuePojo);

        // sets up startedAt and completedAt on the execution, also manages the execution call graph
        var targetPojo = execute(interactionCarrier, propertyModifier, propertyEdit);

        // handle any exceptions
        var priorExecution = interactionCarrier.interaction().getPriorExecution();
        var executionExceptionIfAny = priorExecution.getThrew();
        if(executionExceptionIfAny != null)
            throw executionExceptionIfAny instanceof RuntimeException rex
                ? rex
                : new RuntimeException(executionExceptionIfAny);

        // publish (if not a contributed association, query-only mixin)
        if (ExecutionPublishingFacet.isPublishingEnabled(propertyModifier.facetHolder())) {
            executionPublisher().publishPropertyEdit(priorExecution);
        }

        var result = objectManager.adapt(targetPojo);
        _Xray.exitInvocation(xrayHandle);
        return result;
    }

    // -- HELPER

    /**
     * Use the provided {@link ActionExecutor} to invoke an action, with the provided
     * {@link ActionInvocation} capturing the details of said action.
     *
     * <p> Because this both pushes an {@link Execution} to
     * represent the action invocation and then pops it, that completed
     * execution is accessible at {@link Interaction#getPriorExecution()}.
     */
    private void execute(
    		final InteractionCarrier carrier,
    		final ActionExecutor actionExecutor,
    		final ActionInvocation actionInvocation) {
        observationProvider.get("Execute Action Invocation")
  	      .observe(()->
  	          carrier.execute(actionInvocation, ()->
  	              actionExecutor.executeWithExecutingEvents(
  	            		  carrier.nextExecutionSequence(),
  	            		  actionInvocation)));
    }

    /**
     * Use the provided {@link PropertyModifier} to edit a property, with the provided
     * {@link PropertyEdit} capturing the details of said property edit.
     *
     * <p> Because this both pushes an {@link Execution} to
     * represent the property edit and then pops it, that completed
     * execution is accessible at {@link Interaction#getPriorExecution()}.
     */
    private Object execute(
    		final InteractionCarrier carrier,
    		final PropertyModifier propertyModifier,
			final PropertyEdit propertyEdit) {

      return observationProvider.get("Execute Property Edit")
	      .observe(()->
	      	  carrier.execute(propertyEdit, ()->
	          	propertyModifier.executeWithExecutingEvents(
	          			carrier.nextExecutionSequence(),
	          			propertyEdit)));
	}

	@SneakyThrows
    private Object invokeMethodPassThrough(
            final MethodFacade methodFacade,
            final InteractionHead head,
            final Can<ManagedObject> arguments) {

        final Object[] executionParameters = MmUnwrapUtils.multipleAsArray(arguments);
        final Object targetPojo = MmUnwrapUtils.single(head.target());
        return CanonicalInvoker.invoke(methodFacade, targetPojo, executionParameters);
    }

    private void setCommandResultIfEntity(
            final Command command,
            final ManagedObject resultAdapter) {
        if(command.getResult() != null)
            // don't trample over any existing result, eg subsequent mixins.
            return;
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(resultAdapter))
            return;
        var entityState = resultAdapter.getEntityState();
        if(!entityState.isPersistable())
            return;
        if(entityState.isHollow()
                || entityState.isDetached()) {
            // ensure that any still-to-be-persisted adapters get persisted to DB.
            transactionService.flushTransaction();
        }
        // re-evaluate
        if(!resultAdapter.getEntityState().hasOid()) {
            log.warn("was unable to get a bookmark for the command result, "
                    + "which is an entity: {}", resultAdapter);
            return;
        }
        var bookmark = ManagedObjects.bookmarkElseFail(resultAdapter);
        command.updater().setResult(Try.success(bookmark));
    }

    private ManagedObject resultFilteredHonoringVisibility(
            final ManagedObject resultAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(resultAdapter))
            return resultAdapter;

        if (!configuration.core().metaModel().filterVisibility()
                || resultAdapter instanceof PackedManagedObject)
            return resultAdapter;

        return MmVisibilityUtils.isVisible(resultAdapter, interactionInitiatedBy)
                ? resultAdapter
                : null;
    }

    /**
     * Will set the command's CommandPublishingPhase to READY,
     * if command and objectMember have a matching member-id
     * and if the facetHolder has a CommandPublishingFacet (has commandPublishing=ENABLED).
     */
    void prepareCommandForPublishing(
            final @NonNull Command command,
            final @NonNull InteractionHead interactionHead,
            final @NonNull ObjectMember objectMember,
            final @NonNull FacetHolder facetHolder) {

        if(interactionHead.isCommandForMember(command, objectMember)
                && isPublishingEnabled(facetHolder)) {
            command.updater().setPublishingPhase(Command.CommandPublishingPhase.READY);
        }

        commandPublisher().ready(command);
    }

    private CommandPublisher commandPublisher() {
    	return commandPublisherProvider.get();
    }

    private ExecutionPublisher executionPublisher() {
    	return executionPublisherProvider.get();
    }

    private Optional<InteractionCarrier> interactionCarrier() {
    	return interactionLayerTracker.currentInteractionCarrier();
    }

    private InteractionCarrier interactionCarrierElseFail() {
    	return interactionCarrier().orElseThrow(()->_Exceptions
    			.unrecoverable("needs an InteractionSession on current thread"));
    }
}
