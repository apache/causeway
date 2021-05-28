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
package org.apache.isis.core.runtimeservices.executor;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.iactn.ActionInvocation;
import org.apache.isis.applib.services.iactn.Execution;
import org.apache.isis.applib.services.iactn.PropertyEdit;
import org.apache.isis.applib.services.metrics.MetricsService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.functional.Result;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.interaction.session.InteractionTracker;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.execution.InteractionInternal;
import org.apache.isis.core.metamodel.execution.MemberExecutorService;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.CommandUtil;
import org.apache.isis.core.metamodel.facets.members.publish.command.CommandPublishingFacet;
import org.apache.isis.core.metamodel.facets.members.publish.execution.ExecutionPublishingFacet;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertySetterOrClearFacetForDomainEventAbstract.EditingVariant;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.services.events.MetamodelEventService;
import org.apache.isis.core.metamodel.services.ixn.InteractionDtoFactory;
import org.apache.isis.core.metamodel.services.publishing.ExecutionPublisher;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ManagedObjects.UnwrapUtil;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.schema.ixn.v2.ActionInvocationDto;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Named("isis.runtimeservices.MemberExecutorServiceDefault")
@Order(OrderPrecedence.EARLY)
@Primary
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class MemberExecutorServiceDefault
implements MemberExecutorService {

    private final @Getter InteractionTracker interactionTracker;
    private final @Getter IsisConfiguration configuration;
    private final @Getter ObjectManager objectManager;
    private final @Getter ClockService clockService;
    private final @Getter Provider<MetricsService> metricsService;
    private final @Getter InteractionDtoFactory interactionDtoFactory;
    private final @Getter Provider<ExecutionPublisher> executionPublisher;
    private final @Getter MetamodelEventService metamodelEventService;
    private final @Getter TransactionService transactionService;

    @Override
    public Optional<InteractionInternal> getInteraction() {
        return interactionTracker.currentInteraction()
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
            final @NonNull FacetHolder facetHolder,
            final @NonNull IdentifiedHolder identifiedHolder) {

        _Assert.assertEquals(owningAction.getParameterCount(), argumentAdapters.size(),
                "action's parameter count and provided argument count must match");

        val interaction = getInteractionElseFail();
        val command = interaction.getCommand();

        CommandPublishingFacet.ifPublishingEnabledForCommand(
                command, owningAction, facetHolder, ()->command.updater().setPublishingEnabled(true));

        val xrayHandle = _Xray.enterActionInvocation(interactionTracker, interaction, owningAction, head, argumentAdapters);

        val actionId = owningAction.getIdentifier();
        log.debug("about to invoke action {}", actionId);

        val targetAdapter = head.getTarget();
        val mixedInAdapter = head.getMixedIn().orElse(null);

        val targetPojo = UnwrapUtil.single(targetAdapter);

        val argumentPojos = argumentAdapters.stream()
                .map(UnwrapUtil::single)
                .collect(_Lists.toUnmodifiable());

        val targetMemberName = targetNameFor(owningAction, mixedInAdapter);
        val targetClass = CommandUtil.targetClassNameFor(targetAdapter);

        val actionInvocation =
                new ActionInvocation(
                        interaction, actionId, targetPojo, argumentPojos, targetMemberName,
                        targetClass);
        final InteractionInternal.MemberExecutor<ActionInvocation> memberExecution =
                actionExecutorFactory.createExecutor(
                        owningAction, head, argumentAdapters);

        // sets up startedAt and completedAt on the execution, also manages the execution call graph
        interaction.execute(memberExecution, actionInvocation, clockService, metricsService.get(), command);

        // handle any exceptions
        final Execution<ActionInvocationDto, ?> priorExecution =
                _Casts.uncheckedCast(interaction.getPriorExecution());

        val executionExceptionIfAny = priorExecution.getThrew();

        // TODO: should also sync DTO's 'threw' attribute here...?

        if(executionExceptionIfAny != null) {
            throw executionExceptionIfAny instanceof RuntimeException
                ? ((RuntimeException)executionExceptionIfAny)
                : new RuntimeException(executionExceptionIfAny);
        }

        val returnedPojo = priorExecution.getReturned();
        val returnedAdapter = objectManager.adapt(returnedPojo);

        // sync DTO with result
        interactionDtoFactory
        .updateResult(priorExecution.getDto(), owningAction, returnedPojo);

        // update Command (if required)
        setCommandResultIfEntity(command, returnedAdapter);

        // publish (if not a contributed association, query-only mixin)
        if (ExecutionPublishingFacet.isPublishingEnabled(identifiedHolder)) {
            executionPublisher.get().publishActionInvocation(priorExecution);
        }

        val result = filteredIfRequired(method, returnedAdapter, interactionInitiatedBy);
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
            final @NonNull IdentifiedHolder identifiedHolder,
            final @NonNull EditingVariant editingVariant) {

        val interaction = getInteractionElseFail();
        val command = interaction.getCommand();
        if( command==null ) {
            return head.getTarget();
        }

        CommandPublishingFacet.ifPublishingEnabledForCommand(
                command, owningProperty, facetHolder, ()->command.updater().setPublishingEnabled(true));

        val xrayHandle = _Xray.enterPropertyEdit(interactionTracker, interaction, owningProperty, head, newValueAdapter);

        val propertyId = owningProperty.getIdentifier();

        val targetManagedObject = head.getTarget();
        val target = UnwrapUtil.single(targetManagedObject);
        val argValue = UnwrapUtil.single(newValueAdapter);

        val targetMemberName = CommandUtil.targetMemberNameFor(owningProperty);
        val targetClass = CommandUtil.targetClassNameFor(targetManagedObject);

        val propertyEdit = new PropertyEdit(interaction, propertyId, target, argValue, targetMemberName, targetClass);
        val executor = propertyExecutorFactory
                .createExecutor(owningProperty, head, newValueAdapter,
                        interactionInitiatedBy, editingVariant);

        // sets up startedAt and completedAt on the execution, also manages the execution call graph
        val targetPojo = interaction.execute(executor, propertyEdit, clockService, metricsService.get(), command);

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
        val publishedPropertyFacet = identifiedHolder.getFacet(ExecutionPublishingFacet.class);
        if (publishedPropertyFacet != null) {
            executionPublisher.get().publishPropertyEdit(priorExecution);
        }

        val result = getObjectManager().adapt(targetPojo);
        _Xray.exitInvocation(xrayHandle);
        return result;

    }

    // -- HELPER

    private static String targetNameFor(ObjectAction owningAction, ManagedObject mixedInAdapter) {
        return ObjectAction.Util.targetNameFor(owningAction, mixedInAdapter)
                .orElseGet(()->CommandUtil.targetMemberNameFor(owningAction));
    }

    private void setCommandResultIfEntity(final Command command, final ManagedObject resultAdapter) {
        if(command.getResult() != null) {
            // don't trample over any existing result, eg subsequent mixins.
            return;
        }
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(resultAdapter)) {
            return;
        }

        val entityState = ManagedObjects.EntityUtil.getEntityState(resultAdapter);
        if(entityState.isDetached())   {
            // ensure that any still-to-be-persisted adapters get persisted to DB.
            getTransactionService().flushTransaction();
        }
        if(entityState.isAttached()) {
            resultAdapter.getBookmark()
            .ifPresent(bookmark->
                command.updater().setResult(Result.success(bookmark))
            );
        } else {
            if(entityState.isPersistable()) {
                log.warn("was unable to get a bookmark for the command result, "
                        + "which is an entity: {}", resultAdapter);
            }
        }

        // ignore all other sorts of objects

    }

    private ManagedObject filteredIfRequired(
            final Method method,
            final ManagedObject resultAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(resultAdapter)) {
            return null;
        }

        final boolean filterForVisibility = getConfiguration().getCore().getMetaModel().isFilterVisibility();
        if (!filterForVisibility) {
            return resultAdapter;
        }

        final Object result = resultAdapter.getPojo();

        if(result instanceof Collection || result.getClass().isArray()) {

            val requiredContainerType = method.getReturnType();

            val autofittedObjectContainer = ManagedObjects.VisibilityUtil
                    .visiblePojosAutofit(resultAdapter, interactionInitiatedBy, requiredContainerType);

            if (autofittedObjectContainer != null) {
                return getObjectManager().adapt(autofittedObjectContainer);
            }

            // would be null if unable to take a copy (unrecognized return type)
            // fallback to returning the original adapter, without filtering for visibility

            return resultAdapter;

        } else {
            boolean visible = ManagedObjects.VisibilityUtil.isVisible(resultAdapter, interactionInitiatedBy);
            return visible ? resultAdapter : null;
        }
    }



}
