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

import java.lang.reflect.Method;
import org.apache.isis.applib.FatalException;
import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.eventbus.ActionInvokedEvent;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.util.AdapterUtils;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.actions.event.PostsActionInvokedEventFacet;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.progmodel.facets.actions.invoke.ActionInvocationFacetViaMethod;

public class PostsActionInvokedEventFacetAnnotation 
        extends ActionInvocationFacetViaMethod 
        implements ImperativeFacet, PostsActionInvokedEventFacet {
	
	private ServicesInjector servicesInjector;
	private Class<? extends ActionInvokedEvent<?>> eventType;

    public PostsActionInvokedEventFacetAnnotation(
            final Method method, 
            final ObjectSpecification onType, 
            final ObjectSpecification returnType, 
            final FacetHolder holder, 
            final RuntimeContext runtimeContext, 
            final AdapterManager adapterManager, 
            final ServicesInjector servicesInjector,
			final Class<? extends ActionInvokedEvent<?>> eventType) {
        super(method, onType, returnType, holder, runtimeContext, adapterManager, servicesInjector);
        
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
    
	@SuppressWarnings({ "rawtypes" })
	private void postEvent(
            final ObjectAction owningAction, 
            final ObjectAdapter targetAdapter, 
            final ObjectAdapter[] argumentAdapters) {
	    
		try {
			final Class type = eventType;
			Identifier actionIdentifier = owningAction.getIdentifier();
	        final Object source = AdapterUtils.unwrap(targetAdapter);
			final Object[] arguments = AdapterUtils.unwrap(argumentAdapters);
            @SuppressWarnings("unchecked")
            final ActionInvokedEvent<?> event = org.apache.isis.core.metamodel.facets.actions.event.PostsActionInvokedEventFacet.Util.newEvent(type, source, actionIdentifier, arguments);
			getEventBusService().post(event);
		} catch (Exception e) {
			throw new FatalException(e);
		}
	}

	private EventBusService getEventBusService() {
        return this.servicesInjector.lookupService(EventBusService.class);
	}
}
