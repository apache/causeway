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
import java.util.function.Function;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.events.domain.AbstractDomainEvent;
import org.apache.causeway.applib.events.domain.ActionDomainEvent;
import org.apache.causeway.applib.services.iactn.ActionInvocation;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.collections._Arrays;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration.ObservationProvider;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;
import org.apache.causeway.core.metamodel.commons.CanonicalInvoker;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetAbstract;
import org.apache.causeway.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtils;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.schema.ixn.v2.ActionInvocationDto;

import static org.apache.causeway.commons.internal.base._Casts.uncheckedCast;

import lombok.SneakyThrows;

public record ActionExecutor(
	    ExecutionContext executionContext,
	    FacetHolder facetHolder,
	    InteractionInitiatedBy interactionInitiatedBy,
	    ObjectAction owningAction,
	    MethodFacade method,
	    InteractionHead head,
	    Can<ManagedObject> arguments,
	    ActionInvocationFacetAbstract actionInvocationFacetAbstract,
	    ObservationProvider observationProvider)
implements Function<ActionInvocation, Object>,
    HasMetaModelContext {

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
        var executionContext = facetHolder.lookupServiceElseFail(ExecutionContext.class);
        return new ActionExecutor(
        		executionContext, 
        		facetHolder, interactionInitiatedBy, owningAction, 
        		method, head, 
        		argumentAdapters, actionInvocationFacetAbstract,
        		executionContext.observationProvider(ActionExecutor.class, CausewayModuleCoreMetamodel.NAMESPACE));
    }

    @Override public MetaModelContext getMetaModelContext() { return facetHolder.getMetaModelContext(); }
    
    @Override
	@SneakyThrows
    public Object apply(final ActionInvocation currentExecution) {

    	observationProvider.get("Setting Execution DTO (Attempt)")
    		.observe(()->{
    			// update the current execution with the DTO (memento)
    			// but ... no point in attempting this if no bookmark is yet available.
    			// this logic is for symmetry with PropertyModifier, which has a scenario where this might occur.
		        ManagedObjects.bookmark(head.owner())
		        	.ifPresent(ownerBookmark->currentExecution.setDto(executionDto()));
    		});

        if(!isPostable()) {
        	// don't emit domain events
        	return executeWithoutEvents(arguments);
        }

        // ... post the EXECUTING event
        final ActionDomainEvent<?> event = observationProvider.get("Domain Event EXECUTING")
    		//.highCardinalityKeyValue(CausewayObservationIntegration.interactionMethod(method))
    		.observe(()->
        		executionContext.domainEventHelper()
		    		.postEventForAction(
		                AbstractDomainEvent.Phase.EXECUTING,
		                getEventType(),
		                owningAction, owningAction,
		                head, arguments,
		                null)
    		);

        final Can<ManagedObject> argsForInvocation = event != null
            // the event handlers may have updated the argument themselves
            ? updateArguments(
                    owningAction.getParameters(),
                    arguments,
                    event.getArguments())
            : arguments;

        // set event onto the execution
        currentExecution.setEvent(event);

        // invoke method
        var resultPojo = executeWithoutEvents(argsForInvocation);

        if (event != null) {
        	return observationProvider.get("Domain Event EXECUTED")
        		//.highCardinalityKeyValue(CausewayObservationIntegration.interactionMethod(method))
        		.observe(()->{
		            // ... post the EXECUTED event
		        	executionContext.domainEventHelper()
		        		.postEventForAction(
		                    AbstractDomainEvent.Phase.EXECUTED,
		                    event,
		                    owningAction, owningAction, head, argsForInvocation,
		                    resultPojo);
		
		        	return event.getReturnValue() == resultPojo
		        			? resultPojo
							: getServiceInjector().injectServicesInto(event.getReturnValue());
		        });
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
    
    private boolean isPostable() {
    	// when mixed-in prop/coll always returns false
    	return actionInvocationFacetAbstract.isPostable();
    }
    
    private ActionInvocationDto executionDto() {
    	return executionContext.interactionDtoFactory()
    			.asActionInvocationDto(owningAction, head, arguments);
    }
    
    private final Class<? extends ActionDomainEvent<?>> getEventType() {
        //TODO[CAUSEWAY-3409] when mixed-in prop/coll we need to ask the prop/coll facet instead
        return uncheckedCast(actionInvocationFacetAbstract.getEventType());
    }

    private Object invokeMethodElseFromCache(
            final MethodFacade method,
            final InteractionHead head,
            final Can<ManagedObject> arguments)
                    throws IllegalAccessException, InvocationTargetException {

    	final Object targetPojo = targetPojo();
        final Object[] executionParameters = MmUnwrapUtils.multipleAsArray(arguments);

        return isSafeAndRequestCacheable()
    		? observationProvider.get("Consulting QueryResultsCache")
				.observe(()->executionContext.queryResultsCache()
	        		.execute(
                        ()->invoke(method, targetPojo, executionParameters),
                        targetPojo.getClass(), 
                        method.getName(), 
                        _Arrays.combineWithExplicitType(Object.class, executionParameters, targetPojo)))
        	: invoke(method, targetPojo, executionParameters);
    }
    
    public Object invoke(
            final MethodFacade methodFacade, 
            final Object targetPojo, 
            final Object ... executionParameters) {
    	return observationProvider.get("Invoke Action Method")
			.observe(()->CanonicalInvoker.invoke(method, targetPojo, executionParameters));
    } 	

    private Object targetPojo() {
    	return Objects.requireNonNull(
                MmUnwrapUtils.single(head.target()),
                ()->"Could not extract pojo, that this invocation is targeted at.");
    }
    
	private boolean isSafeAndRequestCacheable() {
		final boolean cacheable = facetHolder.lookupFacet(ActionSemanticsFacet.class)
        		.map(ActionSemanticsFacet::value)
        		.map(SemanticsOf::isSafeAndRequestCacheable)
        		.orElse(false);
		return cacheable;
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
