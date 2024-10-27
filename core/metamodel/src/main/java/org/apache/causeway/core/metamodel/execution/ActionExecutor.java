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
package org.apache.causeway.core.metamodel.execution;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.causeway.applib.events.domain.AbstractDomainEvent;
import org.apache.causeway.applib.events.domain.ActionDomainEvent;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.iactn.ActionInvocation;
import org.apache.causeway.applib.services.queryresultscache.QueryResultsCache;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.collections._Arrays;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.core.metamodel.commons.CanonicalInvoker;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.DomainEventHelper;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetAbstract;
import org.apache.causeway.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtils;
import org.apache.causeway.core.metamodel.services.ixn.InteractionDtoFactory;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;

import static org.apache.causeway.commons.internal.base._Casts.uncheckedCast;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
//@Log4j2
public final class ActionExecutor
implements
    HasMetaModelContext,
    InteractionInternal.MemberExecutor<ActionInvocation> {

    // -- FACTORIES

    public static ActionExecutor forAction(
            final @NonNull FacetHolder facetHolder,
            final @NonNull InteractionInitiatedBy interactionInitiatedBy,
            final @NonNull InteractionHead head,
            final @NonNull Can<ManagedObject> argumentAdapters,
            final @NonNull ObjectAction owningAction,
            final @NonNull ActionInvocationFacetAbstract actionInvocationFacetAbstract) {

        _Assert.assertEquals(owningAction.getParameterCount(), argumentAdapters.size(),
                "action's parameter count and provided argument count must match");

        // guard against malformed argumentAdapters
        argumentAdapters.forEach(arg->{if(!ManagedObjects.isSpecified(arg)) {
            throw _Exceptions.illegalArgument("arguments must be specified for action %s", owningAction);
        }});

        var method = actionInvocationFacetAbstract.getMethods().getFirstElseFail();

        return new ActionExecutor(
                owningAction.getMetaModelContext(), facetHolder,
                interactionInitiatedBy, owningAction, method, head, argumentAdapters, actionInvocationFacetAbstract);
    }

    // -- CONSTRUCTION

    @Getter(onMethod_={@Override})
    private final @NonNull MetaModelContext metaModelContext;

    @Getter
    private final @NonNull FacetHolder facetHolder;
    @Getter
    private final @NonNull InteractionInitiatedBy interactionInitiatedBy;
    @Getter
    private final @NonNull ObjectAction owningAction;
    @Getter
    private final @NonNull MethodFacade method;
    @Getter
    private final @NonNull InteractionHead head;
    @Getter
    private final @NonNull Can<ManagedObject> arguments;

    private final ActionInvocationFacetAbstract actionInvocationFacetAbstract;

    @Getter(lazy=true)
    private final InteractionDtoFactory interactionDtoServiceInternal =
        getServiceRegistry().lookupServiceElseFail(InteractionDtoFactory.class);

    @Getter(lazy=true)
    private final DomainEventHelper domainEventHelper =
        DomainEventHelper.ofServiceRegistry(getServiceRegistry());

    @Getter(lazy=true)
    private final QueryResultsCache queryResultsCache =
        getServiceRegistry().lookupServiceElseFail(QueryResultsCache.class);

    private boolean isPostable() {
        // when mixed-in prop/coll always returns false
        return actionInvocationFacetAbstract.isPostable();
    }

    @SneakyThrows
    @Override
    public Object execute(final ActionInvocation currentExecution) {

        // update the current execution with the DTO (memento)
        //
        // but ... no point in attempting this if no bookmark is yet available.
        // this logic is for symmetry with PropertyModifier, which has a scenario where this might occur.
        //
        var ownerAdapter = head.getOwner();
        Optional<Bookmark> ownerBookmarkIfAny = ManagedObjects.bookmark(ownerAdapter);
        var ownerHasBookmark = ownerBookmarkIfAny.isPresent();
        if (ownerHasBookmark) {
            var invocationDto =
                    getInteractionDtoServiceInternal().asActionInvocationDto(owningAction, head, arguments);
            currentExecution.setDto(invocationDto);
        }

        if(!isPostable()) {
            // don't emit domain events
            return executeWithoutEvents(arguments);
        }

        // ... post the executing event
        final ActionDomainEvent<?> event = getDomainEventHelper().postEventForAction(
                AbstractDomainEvent.Phase.EXECUTING,
                getEventType(),
                owningAction, owningAction,
                head, arguments,
                null);

        final Can<ManagedObject> argsForInvocation;
        if (event != null) {
            // the event handlers may have updated the argument themselves
            argsForInvocation = updateArguments(
                    owningAction.getParameters(),
                    arguments,
                    event.getArguments());
        } else {
            argsForInvocation = arguments;
        }

        // set event onto the execution
        currentExecution.setEvent(event);

        // invoke method
        var resultPojo = executeWithoutEvents(argsForInvocation);

        if (event != null) {
            // ... post the executed event
            getDomainEventHelper().postEventForAction(
                    AbstractDomainEvent.Phase.EXECUTED,
                    event,
                    owningAction, owningAction, head, argsForInvocation,
                    resultPojo);

            // probably superfluous, but does no harm...
            Object actualReturnValue = event.getReturnValue();  // usually the same as resultPojo
            getServiceInjector().injectServicesInto(actualReturnValue);

            return actualReturnValue;
        } else {
            return resultPojo;
        }
    }

    @SneakyThrows
    private Object executeWithoutEvents(final Can<ManagedObject> arguments) {
        // invoke method
        var resultPojo = invokeMethodElseFromCache(method, head, arguments);
        return getServiceInjector().injectServicesInto(resultPojo);
    }

    // -- HELPER

    private final Class<? extends ActionDomainEvent<?>> getEventType() {
        //TODO[CAUSEWAY-3409] when mixed-in prop/coll we need to ask the prop/coll facet instead
        return uncheckedCast(actionInvocationFacetAbstract.getEventType());
    }

    private Object invokeMethodElseFromCache(
            final MethodFacade method,
            final InteractionHead head,
            final Can<ManagedObject> arguments)
                    throws IllegalAccessException, InvocationTargetException {

        final Object[] executionParameters = MmUnwrapUtils.multipleAsArray(arguments);
        final Object targetPojo = Objects.requireNonNull(
                MmUnwrapUtils.single(head.getTarget()),
                ()->"Could not extract pojo, that this invocation is targeted at.");

        final ActionSemanticsFacet semanticsFacet = getFacetHolder().getFacet(ActionSemanticsFacet.class);
        final boolean cacheable = semanticsFacet != null && semanticsFacet.value().isSafeAndRequestCacheable();
        if(cacheable) {
            final QueryResultsCache queryResultsCache = getQueryResultsCache();
            final Object[] targetPojoPlusExecutionParameters = _Arrays.combine(executionParameters, targetPojo);
            return queryResultsCache.execute(
                    ()->CanonicalInvoker.invoke(method, targetPojo, executionParameters),
                    targetPojo.getClass(), method.getName(), targetPojoPlusExecutionParameters);

        } else {
            return CanonicalInvoker.invoke(method, targetPojo, executionParameters);
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
            var originalAdapter = argumentAdapters.getElseFail(paramIndex);
            var originalPojo = originalAdapter.getPojo(); // the original
            return Objects.equals(originalPojo, newPojo)
                    ? originalAdapter
                    : ManagedObject.adaptParameter(param, newPojo);
        });
    }

}
