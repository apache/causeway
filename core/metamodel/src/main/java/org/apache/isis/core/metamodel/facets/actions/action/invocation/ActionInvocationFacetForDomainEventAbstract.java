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

package org.apache.isis.core.metamodel.facets.actions.action.invocation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.events.domain.AbstractDomainEvent;
import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.command.spi.CommandService;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactn.Interaction.ActionInvocation;
import org.apache.isis.applib.services.iactn.InteractionContext;
import org.apache.isis.applib.services.metrics.MetricsService;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.internal.assertions._Assert;
import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.commons.internal.collections._Arrays;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.commons.CanonicalParameterUtil;
import org.apache.isis.core.metamodel.commons.ThrowableExtensions;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.DomainEventHelper;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.actions.publish.PublishedActionFacet;
import org.apache.isis.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.services.ixn.InteractionDtoServiceInternal;
import org.apache.isis.core.metamodel.services.publishing.PublisherDispatchService;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ManagedObjects.UnwrapUtil;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.schema.ixn.v2.ActionInvocationDto;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class ActionInvocationFacetForDomainEventAbstract
extends ActionInvocationFacetAbstract
implements ImperativeFacet {

    @Getter private final Class<? extends ActionDomainEvent<?>> eventType;
    private final Method method;
    @Getter(onMethod = @__(@Override)) private final ObjectSpecification onType;
    @Getter(onMethod = @__(@Override)) private final ObjectSpecification returnType;
    private final ServiceRegistry serviceRegistry;
    private final DomainEventHelper domainEventHelper;

    public ActionInvocationFacetForDomainEventAbstract(
            final Class<? extends ActionDomainEvent<?>> eventType,
            final Method method,
            final ObjectSpecification onType,
            final ObjectSpecification returnType,
            final FacetHolder holder) {
        
        super(holder);
        this.eventType = eventType;
        this.method = method;
        this.onType = onType;
        this.returnType = returnType;
        this.serviceRegistry = getServiceRegistry();
        this.domainEventHelper = DomainEventHelper.ofServiceRegistry(serviceRegistry);
    }

    /**
     * Returns a singleton list of the {@link java.lang.reflect.Method} provided in the
     * constructor.
     */
    @Override
    public List<Method> getMethods() {
        return Collections.singletonList(method);
    }

    @Override
    public Intent getIntent(final Method method) {
        return Intent.EXECUTE;
    }

    @Override
    public ManagedObject invoke(
            final ObjectAction owningAction,
            final InteractionHead head,
            final Can<ManagedObject> argumentAdapters,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final ManagedObject executionResult = 
                getTransactionService().executeWithinTransaction(()->
                    doInvoke(owningAction, head, argumentAdapters, interactionInitiatedBy));

        //PersistableTypeGuard.instate(executionResult);

        return executionResult;
    }

    @Override 
    public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        ImperativeFacet.Util.appendAttributesTo(this, attributeMap);
        attributeMap.put("onType", onType);
        attributeMap.put("returnType", returnType);
        attributeMap.put("eventType", eventType);
    }
    
    @Override
    protected String toStringValues() {
        return "method=" + method;
    }
    
    // -- HELPER
    
    private ManagedObject doInvoke(
            final ObjectAction owningAction,
            final InteractionHead head,
            final Can<ManagedObject> argumentAdapters,
            final InteractionInitiatedBy interactionInitiatedBy) {
        
        _Assert.assertEquals(owningAction.getParameterCount(), argumentAdapters.size(),
                "action's parameter count and provided argument count must match");
        
        // similar code in PropertySetterOrClearFacetFDEA
        
        final CommandContext commandContext = getCommandContext();
        final Command command = commandContext.getCommand();


        final InteractionContext interactionContext = getInteractionContext();
        final Interaction interaction = interactionContext.getInteraction();

        final String actionId = owningAction.getIdentifier().toClassAndNameIdentityString();

        final ManagedObject returnedAdapter;
        if( command.getExecutor() == Command.Executor.USER &&
                command.getExecuteIn() == org.apache.isis.applib.annotation.CommandExecuteIn.BACKGROUND) {

            // deal with background commands

            // persist command so can it can subsequently be invoked in the 'background'
            final CommandService commandService = getCommandService();
            if (!commandService.persistIfPossible(command)) {
                throw new IsisException(String.format(
                        "Unable to persist command for action '%s'; CommandService does not support persistent commands ",
                        actionId));
            }
            returnedAdapter = getObjectManager().adapt(command);

        } else {
            // otherwise, go ahead and execute action in the 'foreground'
            
            val targetAdapter = head.getTarget();
            val mixedInAdapter = head.getMixedIn().orElse(null);

            final Object targetPojo = UnwrapUtil.single(targetAdapter);

            final List<Object> argumentPojos = argumentAdapters.stream()
                    .map(UnwrapUtil::single)
                    .collect(_Lists.toUnmodifiable());

            final String targetMember = targetNameFor(owningAction, mixedInAdapter);
            final String targetClass = CommandUtil.targetClassNameFor(targetAdapter);

            final Interaction.ActionInvocation execution =
                    new Interaction.ActionInvocation(
                            interaction, actionId, targetPojo, argumentPojos, targetMember,
                            targetClass);
            final Interaction.MemberExecutor<Interaction.ActionInvocation> callable =
                    new DomainEventMemberExecutor(
                            argumentAdapters, targetAdapter, command, owningAction,
                            targetAdapter, mixedInAdapter, execution);

            // sets up startedAt and completedAt on the execution, also manages the execution call graph
            interaction.execute(callable, execution, getClockService(), getMetricsService());

            // handle any exceptions
            final Interaction.Execution<ActionInvocationDto, ?> priorExecution =
                    _Casts.uncheckedCast(interaction.getPriorExecution());

            final Exception executionExceptionIfAny = priorExecution.getThrew();

            // TODO: should also sync DTO's 'threw' attribute here...?

            if(executionExceptionIfAny != null) {
                throw executionExceptionIfAny instanceof RuntimeException
                ? ((RuntimeException)executionExceptionIfAny)
                        : new RuntimeException(executionExceptionIfAny);
            }

            final Object returnedPojo = priorExecution.getReturned();
            returnedAdapter = getObjectManager().adapt(returnedPojo);

            // sync DTO with result
            getInteractionDtoServiceInternal()
            .updateResult(priorExecution.getDto(), owningAction, returnedPojo);

            // update Command (if required)
            setCommandResultIfEntity(command, returnedAdapter);

            // publish (if not a contributed association, query-only mixin)
            final PublishedActionFacet publishedActionFacet = getIdentified().getFacet(PublishedActionFacet.class);
            if (publishedActionFacet != null) {
                getPublishingServiceInternal().publishAction(priorExecution);
            }
        }


        return filteredIfRequired(returnedAdapter, interactionInitiatedBy);
    }

    private static String targetNameFor(ObjectAction owningAction, ManagedObject mixedInAdapter) {
        return ObjectAction.Util.targetNameFor(owningAction, mixedInAdapter)
                .orElseGet(()->CommandUtil.targetMemberNameFor(owningAction));
    }

    private static String trim(String message, final int maxLen) {
        if(!_Strings.isNullOrEmpty(message)) {
            message = message.substring(0, Math.min(message.length(), maxLen));
            if(message.length() == maxLen) {
                message += " ...";
            }
        }
        return message;
    }

    private Object invokeMethodElseFromCache(
            final ManagedObject targetAdapter, 
            final Can<ManagedObject> arguments)
                    throws IllegalAccessException, InvocationTargetException {

        final Object[] executionParameters = UnwrapUtil.multipleAsArray(arguments);
        final Object targetPojo = UnwrapUtil.single(targetAdapter);

        final ActionSemanticsFacet semanticsFacet = getFacetHolder().getFacet(ActionSemanticsFacet.class);
        final boolean cacheable = semanticsFacet != null && semanticsFacet.value().isSafeAndRequestCacheable();
        if(cacheable) {
            final QueryResultsCache queryResultsCache = getQueryResultsCache();
            final Object[] targetPojoPlusExecutionParameters = _Arrays.combine(executionParameters, targetPojo);
            return queryResultsCache.execute(
                    ()->CanonicalParameterUtil.invoke(method, targetPojo, executionParameters),
                    targetPojo.getClass(), method.getName(), targetPojoPlusExecutionParameters);

        } else {
            return CanonicalParameterUtil.invoke(method, targetPojo, executionParameters);
        }
    }

    private ManagedObject cloneIfViewModelCloneable(
            final Object resultPojo,
            final ManagedObject targetAdapter) {

        // to remove boilerplate from the domain, we automatically clone the returned object if it is a view model.

        if (resultPojo != null) {
            final ManagedObject resultAdapter = getObjectManager().adapt(resultPojo);
            return cloneIfViewModelElse(resultAdapter, resultAdapter);
        } else {
            // if void or null, attempt to clone the original target, else return null.
            return cloneIfViewModelElse(targetAdapter, null);
        }
    }

    private ManagedObject cloneIfViewModelElse(final ManagedObject adapter, final ManagedObject dfltAdapter) {

        if (!adapter.getSpecification().isViewModelCloneable(adapter)) {
            return dfltAdapter;
        }

        final ViewModelFacet viewModelFacet = adapter.getSpecification().getFacet(ViewModelFacet.class);
        final Object clone = viewModelFacet.clone(adapter.getPojo());

        final ManagedObject clonedAdapter = getObjectManager().adapt(clone);
        return clonedAdapter;
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
        if(entityState.isDetached()) {
            // ensure that any still-to-be-persisted adapters get persisted to DB.
            getTransactionService().flushTransaction();
        }
        if(entityState.isAttached()) {
            resultAdapter.getRootOid().ifPresent(rootOid->{
                val bookmark = rootOid.asBookmark();
                command.internal().setResult(bookmark);
            });
        } else {
            if(entityState.isPersistable()) {
                log.warn("was unable to get a bookmark for the command result, "
                        + "which is an entity: {}", resultAdapter);
            }
        }
        
        // ignore all other sorts of objects
        
    }

    private ManagedObject filteredIfRequired(
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

    private CommandContext getCommandContext() {
        return serviceRegistry.lookupServiceElseFail(CommandContext.class);
    }
    
    private InteractionContext getInteractionContext() {
        return serviceRegistry.lookupServiceElseFail(InteractionContext.class);
    }

    private QueryResultsCache getQueryResultsCache() {
        return serviceRegistry.lookupServiceElseFail(QueryResultsCache.class);
    }

    private CommandService getCommandService() {
        return serviceRegistry.lookupServiceElseFail(CommandService.class);
    }

    private ClockService getClockService() {
        return serviceRegistry.lookupServiceElseFail(ClockService.class);
    }
    private MetricsService getMetricsService() {
        return serviceRegistry.lookupServiceElseFail(MetricsService.class);
    }

    private PublisherDispatchService getPublishingServiceInternal() {
        return serviceRegistry.lookupServiceElseFail(PublisherDispatchService.class);
    }

    private InteractionDtoServiceInternal getInteractionDtoServiceInternal() {
        return serviceRegistry.lookupServiceElseFail(InteractionDtoServiceInternal.class);
    }
    
    @RequiredArgsConstructor
    private final class DomainEventMemberExecutor 
    implements Interaction.MemberExecutor<Interaction.ActionInvocation> {
        
        private final Can<ManagedObject> argumentAdapters;
        private final ManagedObject targetAdapter;
        private final Command command;
        private final ObjectAction owningAction;
        private final ManagedObject mixinElseRegularAdapter;
        private final ManagedObject mixedInAdapter;
        private final ActionInvocation execution;

        @Override
        public Object execute(final ActionInvocation currentExecution) {

            try {
                // it's possible that an event handler changes these en-route
                // so we take a non-final copy
                Can<ManagedObject> argumentAdapters = this.argumentAdapters;

                // update the current execution with the DTO (memento)
                val invocationDto = getInteractionDtoServiceInternal()
                .asActionInvocationDto(owningAction, mixinElseRegularAdapter, argumentAdapters);
                
                currentExecution.setDto(invocationDto);
                
                val head = InteractionHead.mixedIn(targetAdapter, mixedInAdapter);


                // set the startedAt (and update command if this is the top-most member execution)
                // (this isn't done within Interaction#execute(...) because it requires the DTO
                // to have been set on the current execution).
                val startedAt = execution.start(getClockService(), getMetricsService());
                if(command.getStartedAt() == null) {
                    command.internal().setStartedAt(startedAt);
                }

                // ... post the executing event
                //compiler: cannot use val here, because initializer expression does not have a representable type
                final ActionDomainEvent<?> actionDomainEvent = domainEventHelper.postEventForAction(
                        AbstractDomainEvent.Phase.EXECUTING,
                        getEventType(),
                        owningAction, owningAction,
                        head, argumentAdapters,
                        null);

                // the event handlers may have updated the argument themselves
                argumentAdapters = updateArguments(argumentAdapters, actionDomainEvent.getArguments());

                // set event onto the execution
                currentExecution.setEvent(actionDomainEvent);

                // invoke method
                val resultPojo = invokeMethodElseFromCache(targetAdapter, argumentAdapters);
                ManagedObject resultAdapterPossiblyCloned = 
                        cloneIfViewModelCloneable(resultPojo, mixinElseRegularAdapter);

                // ... post the executed event

                domainEventHelper.postEventForAction(
                        AbstractDomainEvent.Phase.EXECUTED,
                        actionDomainEvent,
                        owningAction, owningAction, head, argumentAdapters,
                        resultAdapterPossiblyCloned);

                final Object returnValue = actionDomainEvent.getReturnValue();
                if(returnValue != resultPojo) {
                    resultAdapterPossiblyCloned = 
                            cloneIfViewModelCloneable(returnValue, mixinElseRegularAdapter);
                }
                return UnwrapUtil.single(resultAdapterPossiblyCloned);

            } catch (Exception e) {

                final Consumer<RecoverableException> recovery = recoverableException->{

                    if (!getTransactionState().canCommit()) {
                        // something severe has happened to the underlying transaction;
                        // so escalate this exception to be non-recoverable
                        final Throwable recoverableExceptionCause = recoverableException.getCause();
                        Throwable nonRecoverableCause = recoverableExceptionCause != null
                                ? recoverableExceptionCause
                                        : recoverableException;

                        // trim to first 300 chars
                        final String message = trim(nonRecoverableCause.getMessage(), 300);

                        throw new NonRecoverableException(message, nonRecoverableCause);
                    }
                };

                return ThrowableExtensions.handleInvocationException(e, method.getName(), recovery);
            }
        }
    }

    private static Can<ManagedObject> updateArguments(
            @NonNull final Can<ManagedObject> argumentAdapters,
            @NonNull final List<Object> newArgumentPojos) {

        // zip in the newArgumentPojos from right
        // element wise: update adapter if new-argument pojo differs from original adapter pojo
        return argumentAdapters.zipMap(newArgumentPojos, (leftAdapter, rightPojo)->{
            val leftPojo = leftAdapter.getPojo(); // the original
            return Objects.equals(leftPojo, rightPojo)
                    ? leftAdapter
                    : ManagedObject.of(leftAdapter.getSpecification(), rightPojo);
        });
    }

}
