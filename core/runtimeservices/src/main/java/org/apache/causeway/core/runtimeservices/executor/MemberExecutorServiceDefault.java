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

import java.lang.reflect.Method;
import java.util.Optional;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

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
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.commons.CanonicalInvoker;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.execution.InteractionInternal;
import org.apache.causeway.core.metamodel.execution.MemberExecutorService;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.members.publish.command.CommandPublishingFacet;
import org.apache.causeway.core.metamodel.facets.members.publish.execution.ExecutionPublishingFacet;
import org.apache.causeway.core.metamodel.facets.properties.property.modify.PropertySetterOrClearFacetForDomainEventAbstract.EditingVariant;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmEntityUtil;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtil;
import org.apache.causeway.core.metamodel.object.MmVisibilityUtil;
import org.apache.causeway.core.metamodel.object.PackedManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.services.events.MetamodelEventService;
import org.apache.causeway.core.metamodel.services.ixn.InteractionDtoFactory;
import org.apache.causeway.core.metamodel.services.publishing.ExecutionPublisher;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.schema.ixn.v2.ActionInvocationDto;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.log4j.Log4j2;

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
    private final @Getter ServiceInjector serviceInjector;
    private final @Getter Provider<MetricsService> metricsServiceProvider;
    private final @Getter InteractionDtoFactory interactionDtoFactory;
    private final @Getter Provider<ExecutionPublisher> executionPublisherProvider;
    private final @Getter MetamodelEventService metamodelEventService;
    private final @Getter TransactionService transactionService;

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
            final @NonNull ObjectAction owningAction,
            final @NonNull InteractionHead head,
            final @NonNull Can<ManagedObject> argumentAdapters,
            final @NonNull InteractionInitiatedBy interactionInitiatedBy,
            final @NonNull Method method,
            final @NonNull ActionExecutorFactory actionExecutorFactory,
            final @NonNull FacetHolder facetHolder) {

        _Assert.assertEquals(owningAction.getParameterCount(), argumentAdapters.size(),
                "action's parameter count and provided argument count must match");

        // guard against malformed initialArgs
        argumentAdapters.forEach(arg->{if(!ManagedObjects.isSpecified(arg)) {
            throw _Exceptions.illegalArgument("arguments must be specified for action %s", owningAction);
        }});

        if(interactionInitiatedBy.isPassThrough()) {
            val resultPojo = invokeMethodPassThrough(method, head, argumentAdapters);
            return facetHolder.getObjectManager().adapt(resultPojo);
        }

        val interaction = getInteractionElseFail();
        val command = interaction.getCommand();

        CommandPublishingFacet.prepareCommandForPublishing(command, head, owningAction, facetHolder);

        val xrayHandle = _Xray.enterActionInvocation(interactionLayerTracker, interaction, owningAction, head, argumentAdapters);

        val actionId = owningAction.getFeatureIdentifier();
        log.debug("about to invoke action {}", actionId);

        val targetAdapter = head.getTarget();
        val targetPojo = MmUnwrapUtil.single(targetAdapter);

        val argumentPojos = argumentAdapters.stream()
                .map(MmUnwrapUtil::single)
                .collect(_Lists.toUnmodifiable());

        val actionInvocation = new ActionInvocation(
                        interaction, actionId, targetPojo, argumentPojos);
        val memberExecutor = actionExecutorFactory.createExecutor(owningAction, head, argumentAdapters);

        // sets up startedAt and completedAt on the execution, also manages the execution call graph
        interaction.execute(memberExecutor, actionInvocation, clockService, metricsService(), command);

        // handle any exceptions
        val priorExecution = interaction.getPriorExecutionOrThrowIfAnyException(actionInvocation);

        val returnedPojo = priorExecution.getReturned();
        val returnedAdapter = objectManager.adapt(
                returnedPojo, owningAction::getElementType);

        // assert has bookmark, unless non-scalar
        ManagedObjects.asScalarNonEmpty(returnedAdapter)
        .filter(scalarNonEmpty->!scalarNonEmpty.getSpecialization().isOther()) // don't care
        // if its a transient entity, flush the current transaction, so we get an OID
        .filter(scalarNonEmpty->{
            MmEntityUtil.ifHasNoOidThenFlush(scalarNonEmpty);
            return true;
        })
        .ifPresent(scalarNonEmpty->{
            _Assert.assertTrue(scalarNonEmpty.getBookmark().isPresent(), ()->String.format(
                    "bookmark required for non-empty scalars %s", scalarNonEmpty.getSpecification()));
        });

        // sync DTO with result
        interactionDtoFactory
        .updateResult((ActionInvocationDto)priorExecution.getDto(), owningAction, returnedAdapter);

        // update Command (if required)
        setCommandResultIfEntity(command, returnedAdapter);

        // publish (if not a contributed association, query-only mixin)
        if (ExecutionPublishingFacet.isPublishingEnabled(facetHolder)) {
            executionPublisher().publishActionInvocation(priorExecution);
        }

        val result = resultFilteredHonoringVisibility(method, returnedAdapter, interactionInitiatedBy);
        _Xray.exitInvocation(xrayHandle);
        return result;
    }

    @Override
    public ManagedObject setOrClearProperty(
            final @NonNull OneToOneAssociation owningProperty,
            final @NonNull InteractionHead head,
            final @NonNull ManagedObject newValueAdapter,
            final @NonNull InteractionInitiatedBy interactionInitiatedBy,
            final @NonNull PropertyExecutorFactory propertyExecutorFactory,
            final @NonNull FacetHolder facetHolder,
            final @NonNull EditingVariant editingVariant) {

        val interaction = getInteractionElseFail();
        val command = interaction.getCommand();
        if( command==null ) {
            return head.getTarget();
        }

        CommandPublishingFacet.prepareCommandForPublishing(command, head, owningProperty, facetHolder);

        val xrayHandle = _Xray.enterPropertyEdit(interactionLayerTracker, interaction, owningProperty, head, newValueAdapter);

        val propertyId = owningProperty.getFeatureIdentifier();

        val targetManagedObject = head.getTarget();
        val target = MmUnwrapUtil.single(targetManagedObject);
        val argValue = MmUnwrapUtil.single(newValueAdapter);

        val propertyEdit = new PropertyEdit(interaction, propertyId, target, argValue);
        val executor = propertyExecutorFactory
                .createExecutor(owningProperty, head, newValueAdapter,
                        interactionInitiatedBy, editingVariant);

        // sets up startedAt and completedAt on the execution, also manages the execution call graph
        val targetPojo = interaction.execute(executor, propertyEdit, clockService, metricsService(), command);

        // handle any exceptions
        final Execution<?, ?> priorExecution = interaction.getPriorExecution();

        // TODO: should also sync DTO's 'threw' attribute here...?

        val executionExceptionIfAny = priorExecution.getThrew();
        if(executionExceptionIfAny != null) {
            throw executionExceptionIfAny instanceof RuntimeException
                ? ((RuntimeException)executionExceptionIfAny)
                : new RuntimeException(executionExceptionIfAny);
        }

        // publish (if not a contributed association, query-only mixin)
        val publishedPropertyFacet = facetHolder.getFacet(ExecutionPublishingFacet.class);
        if (publishedPropertyFacet != null) {
            executionPublisher().publishPropertyEdit(priorExecution);
        }

        val result = getObjectManager().adapt(targetPojo);
        _Xray.exitInvocation(xrayHandle);
        return result;

    }

    // -- HELPER

    @SneakyThrows
    private Object invokeMethodPassThrough(
            final Method method,
            final InteractionHead head,
            final Can<ManagedObject> arguments) {

        final Object[] executionParameters = MmUnwrapUtil.multipleAsArray(arguments);
        final Object targetPojo = MmUnwrapUtil.single(head.getTarget());
        return CanonicalInvoker.invoke(method, targetPojo, executionParameters);
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
        val entityState = resultAdapter.getEntityState();
        if(!entityState.isPersistable()) {
            return;
        }
        if(entityState.isDetached()
                || entityState.isSpecicalJpaDetachedWithOid()) {
            // ensure that any still-to-be-persisted adapters get persisted to DB.
            getTransactionService().flushTransaction();
        }
        val entityState2 = resultAdapter.getEntityState();
        if(entityState2.hasOid()) {
            val bookmark = ManagedObjects.bookmarkElseFail(resultAdapter);
            command.updater().setResult(Try.success(bookmark));
            return;
        }
        log.warn("was unable to get a bookmark for the command result, "
                + "which is an entity: {}", resultAdapter);
    }

    private ManagedObject resultFilteredHonoringVisibility(
            final Method method,
            final ManagedObject resultAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(resultAdapter)) {
            return resultAdapter;
        }

        if (!getConfiguration().getCore().getMetaModel().isFilterVisibility()
                || resultAdapter instanceof PackedManagedObject) {
            return resultAdapter;
        }

        return MmVisibilityUtil.isVisible(resultAdapter, interactionInitiatedBy)
                ? resultAdapter
                : null;
    }

}
