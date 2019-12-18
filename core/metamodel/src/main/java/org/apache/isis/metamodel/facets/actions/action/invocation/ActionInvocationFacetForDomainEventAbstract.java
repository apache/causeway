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

package org.apache.isis.metamodel.facets.actions.action.invocation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.events.domain.AbstractDomainEvent;
import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.command.spi.CommandService;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactn.Interaction.ActionInvocation;
import org.apache.isis.applib.services.iactn.InteractionContext;
import org.apache.isis.applib.services.metamodel.MetaModelService;
import org.apache.isis.applib.services.metamodel.MetaModelService.Mode;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.ioc.BeanSort;
import org.apache.isis.metamodel.commons.MethodInvocationPreprocessor;
import org.apache.isis.metamodel.commons.ThrowableExtensions;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.CollectionUtils;
import org.apache.isis.metamodel.facets.DomainEventHelper;
import org.apache.isis.metamodel.facets.ImperativeFacet;
import org.apache.isis.metamodel.facets.actions.publish.PublishedActionFacet;
import org.apache.isis.metamodel.facets.actions.semantics.ActionSemanticsFacet;
import org.apache.isis.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.metamodel.services.ixn.InteractionDtoServiceInternal;
import org.apache.isis.metamodel.services.publishing.PublisherDispatchService;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.schema.ixn.v1.ActionInvocationDto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

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
            final ManagedObject targetAdapter,
            final ManagedObject mixedInAdapter,
            final Can<ManagedObject> argumentAdapters,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final ManagedObject executionResult = 
                getTransactionService().executeWithinTransaction(()->
                    doInvoke(owningAction, targetAdapter, mixedInAdapter, argumentAdapters, 
                            interactionInitiatedBy));

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
            final ManagedObject targetAdapter,
            final ManagedObject mixedInAdapter,
            final Can<ManagedObject> argumentAdapters,
            final InteractionInitiatedBy interactionInitiatedBy) {
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
            final ManagedObject mixinElseRegularAdapter = mixedInAdapter != null ? mixedInAdapter : targetAdapter;

            final Object mixinElseRegularPojo = ManagedObject.unwrapPojo(mixinElseRegularAdapter);

            final List<Object> argumentPojos = argumentAdapters.stream()
                    .map(ManagedObject::unwrapPojo)
                    .collect(_Lists.toUnmodifiable());

            final String targetMember = targetNameFor(owningAction, mixedInAdapter);
            final String targetClass = CommandUtil.targetClassNameFor(mixinElseRegularAdapter);

            final Interaction.ActionInvocation execution =
                    new Interaction.ActionInvocation(
                            interaction, actionId, mixinElseRegularPojo, argumentPojos, targetMember,
                            targetClass);
            final Interaction.MemberExecutor<Interaction.ActionInvocation> callable =
                    new DomainEventMemberExecutor(
                            argumentAdapters, targetAdapter, argumentAdapters, command, owningAction,
                            mixinElseRegularAdapter, mixedInAdapter, execution);

            // sets up startedAt and completedAt on the execution, also manages the execution call graph
            interaction.execute(callable, execution);

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

        final Object[] executionParameters = ManagedObject.unwrapPojoArray(arguments);
        final Object targetPojo = ManagedObject.unwrapPojo(targetAdapter);

        final ActionSemanticsFacet semanticsFacet = getFacetHolder().getFacet(ActionSemanticsFacet.class);
        final boolean cacheable = semanticsFacet != null && semanticsFacet.value().isSafeAndRequestCacheable();
        if(cacheable) {
            final QueryResultsCache queryResultsCache = getQueryResultsCache();
            final Object[] targetPojoPlusExecutionParameters = _Arrays.combine(executionParameters, targetPojo);
            return queryResultsCache.execute(
                    ()->MethodInvocationPreprocessor.invoke(method, targetPojo, executionParameters),
                    targetPojo.getClass(), method.getName(), targetPojoPlusExecutionParameters);

        } else {
            return MethodInvocationPreprocessor.invoke(method, targetPojo, executionParameters);
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
        if (resultAdapter == null) {
            return;
        }

        final Class<?> domainType = resultAdapter.getSpecification().getCorrespondingClass();
        final BeanSort sort = getMetaModelService().sortOf(domainType, Mode.STRICT);
        switch (sort) {
        case ENTITY:
            final Object domainObject = resultAdapter.getPojo();
            // ensure that any still-to-be-persisted adapters get persisted to DB.
            if(!getRepositoryService().isPersistent(domainObject)) {
                getTransactionService().flushTransaction();
            }
            if(getRepositoryService().isPersistent(domainObject)) {
                BookmarkService bookmarkService = getBookmarkService();
                Bookmark bookmark = bookmarkService.bookmarkForElseThrow(domainObject);
                command.internal().setResult(bookmark);
            }
            break;
        default:
            // ignore all other sorts of objects
            break;
        }
    }

    private MetaModelService getMetaModelService() {
        return serviceRegistry.lookupServiceElseFail(MetaModelService.class);
    }

    private BookmarkService getBookmarkService() {
        return serviceRegistry.lookupServiceElseFail(BookmarkService.class);
    }

    private ManagedObject filteredIfRequired(
            final ManagedObject resultAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        if (resultAdapter == null) {
            return null;
        }

        final boolean filterForVisibility = getConfiguration().getReflector().getFacet().isFilterVisibility();
        if (!filterForVisibility) {
            return resultAdapter;
        }

        final Object result = resultAdapter.getPojo();

        if(result instanceof Collection || result.getClass().isArray()) {

            final Stream<ManagedObject> adapters = CollectionFacet.Utils.streamAdapters(resultAdapter);

            final Object visibleObjects =
                    CollectionUtils.copyOf(
                            adapters
                            .filter(ManagedObject.VisibilityUtil.filterOn(interactionInitiatedBy))
                            .map(ManagedObject::unwrapPojo)
                            .collect(Collectors.toList()),
                            method.getReturnType());

            if (visibleObjects != null) {
                return getObjectManager().adapt(visibleObjects);
            }

            // would be null if unable to take a copy (unrecognized return type)
            // fallback to returning the original adapter, without filtering for visibility

            return resultAdapter;

        } else {
            boolean visible = ManagedObject.VisibilityUtil.isVisible(resultAdapter, interactionInitiatedBy);
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
        private final Can<ManagedObject> argumentAdapterList;
        private final Command command;
        private final ObjectAction owningAction;
        private final ManagedObject mixinElseRegularAdapter;
        private final ManagedObject mixedInAdapter;
        private final ActionInvocation execution;

        @Override
        public Object execute(final Interaction.ActionInvocation currentExecution) {

            try {

                // update the current execution with the DTO (memento)
                val invocationDto = getInteractionDtoServiceInternal()
                .asActionInvocationDto(owningAction, mixinElseRegularAdapter, argumentAdapterList);
                
                currentExecution.setDto(invocationDto);


                // set the startedAt (and update command if this is the top-most member execution)
                // (this isn't done within Interaction#execute(...) because it requires the DTO
                // to have been set on the current execution).
                val startedAt = getClockService().nowAsJavaSqlTimestamp();
                execution.setStartedAt(startedAt);
                if(command.getStartedAt() == null) {
                    command.internal().setStartedAt(startedAt);
                }

                // ... post the executing event
                //compiler: cannot use val here, because initializer expression does not have a representable type
                final ActionDomainEvent<?> actionDomainEvent = domainEventHelper.postEventForAction(
                        AbstractDomainEvent.Phase.EXECUTING,
                        getEventType(),
                        owningAction, owningAction,
                        targetAdapter, mixedInAdapter, argumentAdapters,
                        null);

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
                        owningAction, owningAction, targetAdapter, mixedInAdapter, argumentAdapters,
                        resultAdapterPossiblyCloned);

                final Object returnValue = actionDomainEvent.getReturnValue();
                if(returnValue != resultPojo) {
                    resultAdapterPossiblyCloned = 
                            cloneIfViewModelCloneable(returnValue, mixinElseRegularAdapter);
                }
                return ManagedObject.unwrapPojo(resultAdapterPossiblyCloned);

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

}
