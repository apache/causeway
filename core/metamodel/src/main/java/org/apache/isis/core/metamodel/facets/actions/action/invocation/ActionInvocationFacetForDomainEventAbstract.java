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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.events.domain.AbstractDomainEvent;
import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.services.iactn.Interaction.ActionInvocation;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.core.metamodel.commons.CanonicalParameterUtil;
import org.apache.isis.core.metamodel.commons.ThrowableExtensions;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.execution.InternalInteraction;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.DomainEventHelper;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.services.ixn.InteractionDtoServiceInternal;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects.UnwrapUtil;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

import lombok.Getter;
import lombok.NonNull;
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
            final InteractionHead head,
            final Can<ManagedObject> argumentAdapters,
            final InteractionInitiatedBy interactionInitiatedBy) {

        val executionResult = 
                getTransactionService().executeWithinTransaction(()->
                    doInvoke(owningAction, head, argumentAdapters, interactionInitiatedBy));

        //PersistableTypeGuard.instate(executionResult);

        return executionResult
                .nullableOrElseFail();
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
        
        return getMemberExecutor().invokeAction(
                owningAction, 
                head, 
                argumentAdapters, 
                interactionInitiatedBy, 
                method,
                DomainEventMemberExecutor::new, 
                getFacetHolder(), 
                getIdentified());
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
    
    private QueryResultsCache getQueryResultsCache() {
        return serviceRegistry.lookupServiceElseFail(QueryResultsCache.class);
    }

    private InteractionDtoServiceInternal getInteractionDtoServiceInternal() {
        return serviceRegistry.lookupServiceElseFail(InteractionDtoServiceInternal.class);
    }
    
    @RequiredArgsConstructor
    private final class DomainEventMemberExecutor 
            implements InternalInteraction.MemberExecutor<ActionInvocation> {
        
        private final Can<ManagedObject> argumentAdapters;
        private final ManagedObject targetAdapter;
        private final ObjectAction owningAction;
        private final ManagedObject mixinElseRegularAdapter;
        private final ManagedObject mixedInAdapter;

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

                return ThrowableExtensions.handleInvocationException(e, method.toString(), recovery);
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
