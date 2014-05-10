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

package org.apache.isis.core.progmodel.facets.actions.invoke.event;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.isis.applib.FatalException;
import org.apache.isis.applib.services.eventbus.ActionInvokedEvent;
import org.apache.isis.applib.services.eventbus.CollectionAddedToEvent;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.progmodel.facets.actions.invoke.ActionInvocationFacetViaMethod;

public class PostsActionInvokedEventFacetViaMethod extends ActionInvocationFacetViaMethod implements ImperativeFacet {
	
	private EventBusService eventBusService;
	private boolean searchedForEventBusService = false;
	private ServicesInjector servicesInjector;
	private Class<? extends CollectionAddedToEvent<?, ?>> eventType;

    public PostsActionInvokedEventFacetViaMethod(
            final Method method, 
            final ObjectSpecification onType, 
            final ObjectSpecification returnType, 
            final FacetHolder holder, 
            final RuntimeContext runtimeContext, 
            final AdapterManager adapterManager, 
            final ServicesInjector servicesInjector,
			final Class<? extends CollectionAddedToEvent<?, ?>> eventType) {
        super(method, onType, returnType, holder, runtimeContext, adapterManager, servicesInjector);
        
        // Also needed here.
        this.servicesInjector = servicesInjector;
        
        this.eventType = eventType;
    }

    @Override
    public ObjectAdapter invoke(
            final ObjectAction owningAction, 
            final ObjectAdapter targetAdapter, 
            final ObjectAdapter[] arguments) {

    	final InvocationResult invocationResult = this.internalInvoke(owningAction, targetAdapter, arguments);
    	
    	// Perhaps the Action was not properly invoked (i.e. an exception was raised).
    	if (invocationResult.getWhetherInvoked()) {
    		// If invoked, then send the ActionInvokedEvent to the EventBus.
    		postEvent(owningAction, targetAdapter, arguments);
    	}
    	
    	return invocationResult.getAdapter();
    	
    }
    
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void postEvent(
            final ObjectAction owningAction, 
            final ObjectAdapter targetAdapter, 
            final ObjectAdapter[] arguments) {
	    
		final Object source = targetAdapter.getObject();
		try {
			final Class type = eventType;
			final ActionInvokedEvent event = newEvent(type, owningAction, source, arguments);
			getEventBusService().post(event);
		} catch (Exception e) {
			throw new FatalException(e);
		}
	}

	static <S> ActionInvokedEvent<S> newEvent(
			final Class<? extends ActionInvokedEvent<S>> type,
		            final ObjectAction owningAction, 
		            final S source, 
		            final ObjectAdapter[] arguments)
			throws InstantiationException, IllegalAccessException,
			NoSuchFieldException {
		final ActionInvokedEvent<S> event = type.newInstance();

		setField("owningAction", event, owningAction);
		setField("targetAdapter", event, source);
		setField("arguments", event, arguments.toString());
		return event;
	}

	private static void setField(final String name,
			final ActionInvokedEvent<?> event, final Object sourceValue)
			throws NoSuchFieldException, IllegalAccessException {
		final Field sourceField = CollectionAddedToEvent.class
				.getDeclaredField(name);
		sourceField.setAccessible(true);
		sourceField.set(event, sourceValue);
	}

	private EventBusService getEventBusService() {
		if (!searchedForEventBusService) {
			eventBusService = this.servicesInjector.lookupService(EventBusService.class);
		}
		searchedForEventBusService = true;
		return eventBusService;
	}

}
