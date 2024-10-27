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

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.causeway.core.metamodel.services.deadlock.DeadlockRecognizer;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.iactn.ActionInvocation;
import org.apache.causeway.applib.services.iactn.Execution;
import org.apache.causeway.applib.services.iactn.PropertyEdit;
import org.apache.causeway.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.applib.services.metrics.MetricsService;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.MessageTemplate;
import org.apache.causeway.core.metamodel.commons.CanonicalInvoker;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.execution.ActionExecutor;
import org.apache.causeway.core.metamodel.execution.InteractionInternal;
import org.apache.causeway.core.metamodel.execution.MemberExecutorService;
import org.apache.causeway.core.metamodel.execution.PropertyModifier;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.IdentifierUtil;
import org.apache.causeway.core.metamodel.facets.members.publish.execution.ExecutionPublishingFacet;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmEntityUtils;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtils;
import org.apache.causeway.core.metamodel.object.MmVisibilityUtils;
import org.apache.causeway.core.metamodel.object.PackedManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.services.events.MetamodelEventService;
import org.apache.causeway.core.metamodel.services.ixn.InteractionDtoFactory;
import org.apache.causeway.core.metamodel.services.publishing.CommandPublisher;
import org.apache.causeway.core.metamodel.services.publishing.ExecutionPublisher;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.schema.ixn.v2.ActionInvocationDto;

import static org.apache.causeway.core.metamodel.facets.members.publish.command.CommandPublishingFacet.isPublishingEnabled;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

/**
 * Default implementation of {@link MemberExecutorService}.
 *
 * @since 2.0 {@index}
 */
@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".MemberExecutorServiceDefault")
@Priority(PriorityPrecedence.EARLY)
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class MemberExecutorServiceDefault
implements MemberExecutorService {

    private final @Getter InteractionLayerTracker interactionLayerTracker;
    private final @Getter CausewayConfiguration configuration;
    private final @Getter ObjectManager objectManager;
    private final @Getter ClockService clockService;
    private final @Getter DeadlockRecognizer deadlockRecognizer;
    private final @Getter ServiceInjector serviceInjector;
    private final @Getter Provider<MetricsService> metricsServiceProvider;
    private final @Getter InteractionDtoFactory interactionDtoFactory;
    private final @Getter Provider<ExecutionPublisher> executionPublisherProvider;
    private final @Getter MetamodelEventService metamodelEventService;
    private final @Getter TransactionService transactionService;
    private final Provider<CommandPublisher> commandPublisherProvider;

    private MetricsService metricsService() {
        return metricsServiceProvider.get();
    }

    private ExecutionPublisher executionPublisher() {
        return executionPublisherProvider.get();
    }

    @Override
    public Optional<InteractionInternal> getInteraction() {
        return interactionLayerTracker.currentInteraction()
                .map(InteractionInternal.class::cast);
    }

    @Override
    public ManagedObject invokeAction(
            final @NonNull ActionExecutor actionExecutor) {

        var executionResult = actionExecutor.getInteractionInitiatedBy().isPassThrough()
                ? Try.call(()->
                    invokeActionInternally(actionExecutor))
                : getTransactionService().callWithinCurrentTransactionElseCreateNew(()->
                    invokeActionInternally(actionExecutor));

        return executionResult
                .valueAsNullableElseFail();
    }

    private ManagedObject invokeActionInternally(
            final ActionExecutor actionExecutor) {

        final ObjectAction owningAction = actionExecutor.getOwningAction();
        final InteractionHead head = actionExecutor.getHead();
        final Can<ManagedObject> argumentAdapters = actionExecutor.getArguments();
        final InteractionInitiatedBy interactionInitiatedBy = actionExecutor.getInteractionInitiatedBy();
        //            final MethodFacade methodFacade,
        //            final ActionExecutorFactory actionExecutorFactory,
        final FacetHolder facetHolder = actionExecutor.getFacetHolder();

        if(interactionInitiatedBy.isPassThrough()) {
            var resultPojo = invokeMethodPassThrough(actionExecutor.getMethod(), head, argumentAdapters);
            return facetHolder.getObjectManager().adapt(resultPojo);
        }

        var interaction = getInteractionElseFail();

        prepareCommandForPublishing(interaction.getCommand(), head, owningAction, facetHolder);

        var xrayHandle = _Xray.enterActionInvocation(interactionLayerTracker, interaction, owningAction, head, argumentAdapters);

        var actionId = owningAction.getFeatureIdentifier();
        log.debug("about to invoke action {}", actionId);

        var targetAdapter = head.getTarget();
        var targetPojo = MmUnwrapUtils.single(targetAdapter);

        var argumentPojos = argumentAdapters.stream()
                .map(MmUnwrapUtils::single)
                .collect(_Lists.toUnmodifiable());

        var actionInvocation = new ActionInvocation(
                        interaction, actionId, targetPojo, argumentPojos);

        // sets up startedAt and completedAt on the execution, also manages the execution call graph
        interaction.execute(actionExecutor, actionInvocation, InteractionInternal.Context.of(clockService, metricsService(), commandPublisherProvider.get(), deadlockRecognizer));

        // handle any exceptions
        var priorExecution = interaction.getPriorExecutionOrThrowIfAnyException(actionInvocation);

        var returnedPojo = priorExecution.getReturned();
        var returnedAdapter = objectManager.adapt(
                returnedPojo, owningAction::getElementType);

        // assert has bookmark, unless non-scalar
        ManagedObjects.asScalarNonEmpty(returnedAdapter)
        .filter(scalarNonEmpty->!scalarNonEmpty.getSpecialization().isOther()) // don't care
        // if its a transient entity, flush the current transaction, so we get an OID
        .filter(scalarNonEmpty->{
            MmEntityUtils.ifHasNoOidThenFlush(scalarNonEmpty);
            return true;
        })
        .ifPresent(scalarNonEmpty->{
            _Assert.assertTrue(scalarNonEmpty.getBookmark().isPresent(), ()->{
                var returnTypeSpec = scalarNonEmpty.getSpecification();
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
        setCommandResultIfEntity(interaction.getCommand(), returnedAdapter);

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

        var executionResult = propertyExecutor.getInteractionInitiatedBy().isPassThrough()
                ? Try.call(()->
                    setOrClearPropertyInternally(propertyExecutor))
                : getTransactionService()
                    .callWithinCurrentTransactionElseCreateNew(() ->
                        setOrClearPropertyInternally(propertyExecutor));

        return executionResult
                .valueAsNullableElseFail();
    }

    private ManagedObject setOrClearPropertyInternally(
            final @NonNull PropertyModifier propertyModifier) {

        final InteractionHead head = propertyModifier.getHead();

        var domainObject = head.getTarget();

        if(propertyModifier.getInteractionInitiatedBy().isPassThrough()) {
            /* directly access property setter to prevent triggering of domain events
             * or change tracking, eg. when called in the context of serialization */
            propertyModifier.executeClearOrSetWithoutEvents(propertyModifier.getNewValue());
            return domainObject;
        }

        var interaction = getInteractionElseFail();
        var command = interaction.getCommand();
        if( command==null ) {
            return domainObject;
        }

        var owningProperty = propertyModifier.getOwningProperty();

        prepareCommandForPublishing(command, head, owningProperty, propertyModifier.getFacetHolder());

        var xrayHandle = _Xray.enterPropertyEdit(interactionLayerTracker, interaction, owningProperty, head, propertyModifier.getNewValue());

        var propertyId = owningProperty.getFeatureIdentifier();

        var targetManagedObject = head.getTarget();
        var target = MmUnwrapUtils.single(targetManagedObject);
        var argValuePojo = MmUnwrapUtils.single(propertyModifier.getNewValue());

        var propertyEdit = new PropertyEdit(interaction, propertyId, target, argValuePojo);

        // sets up startedAt and completedAt on the execution, also manages the execution call graph
        var targetPojo = interaction.execute(propertyModifier, propertyEdit,
                InteractionInternal.Context.of(clockService, metricsService(), commandPublisherProvider.get(), deadlockRecognizer));

        // handle any exceptions
        final Execution<?, ?> priorExecution = interaction.getPriorExecution();

        // TODO: should also sync DTO's 'threw' attribute here...?

        var executionExceptionIfAny = priorExecution.getThrew();
        if(executionExceptionIfAny != null) {
            throw executionExceptionIfAny instanceof RuntimeException
                ? ((RuntimeException)executionExceptionIfAny)
                : new RuntimeException(executionExceptionIfAny);
        }

        // publish (if not a contributed association, query-only mixin)
        if (ExecutionPublishingFacet.isPublishingEnabled(propertyModifier.getFacetHolder())) {
            executionPublisher().publishPropertyEdit(priorExecution);
        }

        var result = getObjectManager().adapt(targetPojo);
        _Xray.exitInvocation(xrayHandle);
        return result;

    }

    // -- HELPER

    @SneakyThrows
    private Object invokeMethodPassThrough(
            final MethodFacade methodFacade,
            final InteractionHead head,
            final Can<ManagedObject> arguments) {

        final Object[] executionParameters = MmUnwrapUtils.multipleAsArray(arguments);
        final Object targetPojo = MmUnwrapUtils.single(head.getTarget());
        return CanonicalInvoker.invoke(methodFacade, targetPojo, executionParameters);
    }

    private void setCommandResultIfEntity(
            final Command command,
            final ManagedObject resultAdapter) {
        if(command.getResult() != null) {
            // don't trample over any existing result, eg subsequent mixins.
            return;
        }
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(resultAdapter)) {
            return;
        }
        var entityState = resultAdapter.getEntityState();
        if(!entityState.isPersistable()) {
            return;
        }
        if(entityState.isHollow()
                || entityState.isDetached()) {
            // ensure that any still-to-be-persisted adapters get persisted to DB.
            getTransactionService().flushTransaction();
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

        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(resultAdapter)) {
            return resultAdapter;
        }

        if (!getConfiguration().getCore().getMetaModel().isFilterVisibility()
                || resultAdapter instanceof PackedManagedObject) {
            return resultAdapter;
        }

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

        if(IdentifierUtil.isCommandForMember(command, interactionHead, objectMember)
                && isPublishingEnabled(facetHolder)) {
            command.updater().setPublishingPhase(Command.CommandPublishingPhase.READY);
        }

        commandPublisherProvider.get().ready(command);
    }

}
