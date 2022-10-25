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
package org.apache.causeway.core.metamodel.facets.actions.action.invocation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.apache.causeway.applib.events.domain.AbstractDomainEvent;
import org.apache.causeway.applib.events.domain.ActionDomainEvent;
import org.apache.causeway.applib.services.iactn.ActionInvocation;
import org.apache.causeway.applib.services.queryresultscache.QueryResultsCache;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.collections._Arrays;
import org.apache.causeway.core.metamodel.commons.CanonicalInvoker;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.execution.InteractionInternal;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.DomainEventHelper;
import org.apache.causeway.core.metamodel.facets.ImperativeFacet;
import org.apache.causeway.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtil;
import org.apache.causeway.core.metamodel.services.ixn.InteractionDtoFactory;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

public abstract class ActionInvocationFacetForDomainEventAbstract
extends ActionInvocationFacetAbstract
implements ImperativeFacet {

    @Getter private final Class<? extends ActionDomainEvent<?>> eventType;
    @Getter(onMethod_ = {@Override}) private final @NonNull Can<Method> methods;
    @Getter(onMethod = @__(@Override)) private final ObjectSpecification declaringType;
    @Getter(onMethod = @__(@Override)) private final ObjectSpecification returnType;
    private final ServiceRegistry serviceRegistry;
    private final DomainEventHelper domainEventHelper;

    public ActionInvocationFacetForDomainEventAbstract(
            final Class<? extends ActionDomainEvent<?>> eventType,
            final Method method,
            final ObjectSpecification declaringType,
            final ObjectSpecification returnType,
            final FacetHolder holder) {

        super(holder);
        this.eventType = eventType;
        this.methods = ImperativeFacet.singleMethod(method);
        this.declaringType = declaringType;
        this.returnType = returnType;
        this.serviceRegistry = getServiceRegistry();
        this.domainEventHelper = DomainEventHelper.ofServiceRegistry(serviceRegistry);
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

        val executionResult = interactionInitiatedBy.isPassThrough()
                ? Try.call(()->
                    doInvoke(owningAction, head, argumentAdapters, interactionInitiatedBy))
                : getTransactionService().callWithinCurrentTransactionElseCreateNew(()->
                    doInvoke(owningAction, head, argumentAdapters, interactionInitiatedBy));

        //PersistableTypeGuard.instate(executionResult);

        return executionResult
                .ifFailureFail()
                .getValue().orElse(null);
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        ImperativeFacet.visitAttributes(this, visitor);
        visitor.accept("declaringType", declaringType);
        visitor.accept("returnType", returnType);
        visitor.accept("eventType", eventType);
    }

    // -- HELPER

    private ManagedObject doInvoke(
            final ObjectAction owningAction,
            final InteractionHead head,
            final Can<ManagedObject> argumentAdapters,
            final InteractionInitiatedBy interactionInitiatedBy) {

        _Assert.assertEquals(owningAction.getParameterCount(), argumentAdapters.size(),
                "action's parameter count and provided argument count must match");

        val method = methods.getFirstOrFail();

        val resultAdapter = getMemberExecutor()
                .invokeAction(
                    owningAction,
                    head,
                    argumentAdapters,
                    interactionInitiatedBy,
                    method,
                    DomainEventMemberExecutor::new,
                    getFacetHolder());
        return resultAdapter;
    }

    private Object invokeMethodElseFromCache(
            final InteractionHead head,
            final Can<ManagedObject> arguments)
                    throws IllegalAccessException, InvocationTargetException {

        val method = methods.getFirstOrFail();

        final Object[] executionParameters = MmUnwrapUtil.multipleAsArray(arguments);
        final Object targetPojo = MmUnwrapUtil.single(head.getTarget());

        final ActionSemanticsFacet semanticsFacet = getFacetHolder().getFacet(ActionSemanticsFacet.class);
        final boolean cacheable = semanticsFacet != null && semanticsFacet.value().isSafeAndRequestCacheable();
        if(cacheable) {
            final QueryResultsCache queryResultsCache = queryResultsCache();
            final Object[] targetPojoPlusExecutionParameters = _Arrays.combine(executionParameters, targetPojo);
            return queryResultsCache.execute(
                    ()->CanonicalInvoker.invoke(method, targetPojo, executionParameters),
                    targetPojo.getClass(), method.getName(), targetPojoPlusExecutionParameters);

        } else {
            return CanonicalInvoker.invoke(method, targetPojo, executionParameters);
        }
    }

    private QueryResultsCache queryResultsCache() {
        return serviceRegistry.lookupServiceElseFail(QueryResultsCache.class);
    }

    private InteractionDtoFactory interactionDtoFactory() {
        return serviceRegistry.lookupServiceElseFail(InteractionDtoFactory.class);
    }

    @RequiredArgsConstructor
    private final class DomainEventMemberExecutor
            implements InteractionInternal.MemberExecutor<ActionInvocation> {

        private final ObjectAction owningAction;
        private final InteractionHead head;
        private final Can<ManagedObject> initialArgs;

        @SneakyThrows
        @Override
        public Object execute(final ActionInvocation currentExecution) {

            // update the current execution with the DTO (memento)
            val invocationDto = interactionDtoFactory()
            .asActionInvocationDto(owningAction, head, initialArgs);

            currentExecution.setDto(invocationDto);

            // ... post the executing event
            final ActionDomainEvent<?> actionDomainEvent = domainEventHelper.postEventForAction(
                    AbstractDomainEvent.Phase.EXECUTING,
                    getEventType(),
                    owningAction, owningAction,
                    head, initialArgs,
                    null);

            // the event handlers may have updated the argument themselves
            val argsAfterEventPolling = updateArguments(
                    owningAction.getParameters(),
                    initialArgs,
                    actionDomainEvent.getArguments());

            // set event onto the execution
            currentExecution.setEvent(actionDomainEvent);

            // invoke method
            val resultPojo = invokeMethodElseFromCache(head, argsAfterEventPolling);
            getServiceInjector().injectServicesInto(resultPojo);

            // ... post the executed event

            domainEventHelper.postEventForAction(
                    AbstractDomainEvent.Phase.EXECUTED,
                    actionDomainEvent,
                    owningAction, owningAction, head, argsAfterEventPolling,
                    resultPojo);

            // probably superfluous, but does no harm...
            Object actualReturnValue = actionDomainEvent.getReturnValue();  // usually the same as resultPojo
            getServiceInjector().injectServicesInto(actualReturnValue);

            return actualReturnValue;
        }

    }

    private static Can<ManagedObject> updateArguments(
            final @NonNull Can<ObjectActionParameter> params,
            final @NonNull Can<ManagedObject> argumentAdapters,
            final @NonNull List<Object> newArgumentPojos) {

        // zip in the newArgumentPojos from right
        // element wise: update adapter if new-argument pojo differs from original adapter pojo
        return params.zipMap(newArgumentPojos, (param, newPojo)->{
            final int paramIndex = param.getParameterIndex();
            val originalAdapter = argumentAdapters.getElseFail(paramIndex);
            val originalPojo = originalAdapter.getPojo(); // the original
            return Objects.equals(originalPojo, newPojo)
                    ? originalAdapter
                    : ManagedObject.adaptParameter(param, newPojo);
        });
    }

}
