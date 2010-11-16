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


package org.apache.isis.progmodel.wrapper.metamodel.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.isis.applib.events.InteractionEvent;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.persistence.container.DomainObjectContainerObjectChanged;
import org.apache.isis.core.runtime.persistence.container.DomainObjectContainerResolve;
import org.apache.isis.progmodel.wrapper.applib.WrapperFactory;
import org.apache.isis.progmodel.wrapper.applib.WrapperFactory.ExecutionMode;



public class DelegatingInvocationHandlerDefault<T> implements DelegatingInvocationHandler<T> {

    private final T delegate;
    protected final WrapperFactory headlessViewer;
    private final ExecutionMode executionMode;
    private final RuntimeContext runtimeContext;

    protected final Method equalsMethod;
    protected final Method hashCodeMethod;
    protected final Method toStringMethod;

	private DomainObjectContainerObjectChanged domainObjectContainerObjectChanged;
	private DomainObjectContainerResolve domainObjectContainerResolve;

	private boolean resolveObjectChangedEnabled;
	
    public DelegatingInvocationHandlerDefault(
            final T delegate,
            final WrapperFactory headlessViewer,
            final ExecutionMode executionMode,
            final RuntimeContext runtimeContext) {
        if (delegate == null) {
            throw new IllegalArgumentException("delegate must not be null");
        }
        this.delegate = delegate;
        this.headlessViewer = headlessViewer;
        this.executionMode = executionMode;
        this.runtimeContext = runtimeContext;

        this.domainObjectContainerResolve = new DomainObjectContainerResolve();
        this.domainObjectContainerObjectChanged = new DomainObjectContainerObjectChanged();

        try {
            equalsMethod = delegate.getClass().getMethod("equals", new Class[] { Object.class });
            hashCodeMethod = delegate.getClass().getMethod("hashCode", new Class[] {});
            toStringMethod = delegate.getClass().getMethod("toString", new Class[] {});
        } catch (final NoSuchMethodException e) {
            // ///CLOVER:OFF
            throw new RuntimeException("An Object method could not be found: " + e.getMessage());
            // ///CLOVER:ON
        }
    }

	public boolean isResolveObjectChangedEnabled() {
		return resolveObjectChangedEnabled;
	}
    public void setResolveObjectChangedEnabled(boolean resolveObjectChangedEnabled) {
    	this.resolveObjectChangedEnabled = resolveObjectChangedEnabled;
    }


	protected void resolveIfRequired(final ObjectAdapter targetAdapter) {
		resolveIfRequired(targetAdapter.getObject());
	}
	protected void resolveIfRequired(final Object domainObject) {
		if (resolveObjectChangedEnabled) {
			domainObjectContainerResolve.resolve(domainObject);
		}
	}
	protected void objectChangedIfRequired(final ObjectAdapter targetAdapter) {
		objectChangedIfRequired(targetAdapter.getObject());
	}

	protected void objectChangedIfRequired(Object domainObject) {
		if(resolveObjectChangedEnabled) {
			domainObjectContainerObjectChanged.objectChanged(domainObject);
		}
	}



    public WrapperFactory getHeadlessViewer() {
        return headlessViewer;
    }

    public T getDelegate() {
        return delegate;
    }

    public ExecutionMode getExecutionMode() {
		return executionMode;
	}

    protected Object delegate(final Method method, final Object[] args) throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {

    	
        // commented out, hoping isn't needed...
//        if (method.equals(getHandlerMethod)) {
//            return this;
//        }

        return method.invoke(getDelegate(), args);
    }

    protected boolean isObjectMethod(final Method method) {
        return toStringMethod.equals(method) || hashCodeMethod.equals(method) || equalsMethod.equals(method);
    }

    // commented out, hoping isn't needed...
//    protected boolean isObjectAdapterTestViewHandlerMethod(final Method method) {
//        return getHandlerMethod.equals(method);
//    }

    public Object invoke(final Object object, final Method method, final Object[] args) throws Throwable {

        return method.invoke(object, args);
    }

    protected InteractionEvent notifyListeners(final InteractionEvent interactionEvent) {
        headlessViewer.notifyListeners(interactionEvent);
        return interactionEvent;
    }

    
    // /////////////////////////////////////////////////////////////////
    // Dependencies (from constructor)
    // /////////////////////////////////////////////////////////////////

    protected RuntimeContext getRuntimeContext() {
		return runtimeContext;
	}
    

    // /////////////////////////////////////////////////////////////////
    // Dependencies (from runtime context)
    // /////////////////////////////////////////////////////////////////

    protected SpecificationLoader getSpecificationLoader() {
        return runtimeContext.getSpecificationLoader();
    }

    protected AuthenticationSession getAuthenticationSession() {
        return runtimeContext.getAuthenticationSession();
    }
    
}
