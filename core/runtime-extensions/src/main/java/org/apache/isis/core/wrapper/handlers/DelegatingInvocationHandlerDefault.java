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

package org.apache.isis.core.wrapper.handlers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.WrapperFactory.ExecutionMode;
import org.apache.isis.applib.services.wrapper.events.InteractionEvent;
import org.apache.isis.commons.internal._Constants;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;

public class DelegatingInvocationHandlerDefault<T> implements DelegatingInvocationHandler<T> {

    private final T delegate;
    protected final WrapperFactory wrapperFactory;
    private final ExecutionMode executionMode;

    protected final Method equalsMethod;
    protected final Method hashCodeMethod;
    protected final Method toStringMethod;

    private boolean resolveObjectChangedEnabled;

    public DelegatingInvocationHandlerDefault(
            final T delegate,
            final ExecutionMode executionMode) {
        
        if (delegate == null) {
            throw new IllegalArgumentException("delegate must not be null");
        }
        this.delegate = delegate;
        this.wrapperFactory = IsisContext.getServiceRegistry().lookupServiceElseFail(WrapperFactory.class);
        this.executionMode = executionMode;

        try {
            equalsMethod = delegate.getClass().getMethod("equals", new Class[] { Object.class });
            hashCodeMethod = delegate.getClass().getMethod("hashCode", _Constants.emptyClasses);
            toStringMethod = delegate.getClass().getMethod("toString", _Constants.emptyClasses);
        } catch (final NoSuchMethodException e) {
            // ///CLOVER:OFF
            throw new RuntimeException("An Object method could not be found: " + e.getMessage());
            // ///CLOVER:ON
        }
    }

    @Override
    public boolean isResolveObjectChangedEnabled() {
        return resolveObjectChangedEnabled;
    }

    @Override
    public void setResolveObjectChangedEnabled(final boolean resolveObjectChangedEnabled) {
        this.resolveObjectChangedEnabled = resolveObjectChangedEnabled;
    }

    protected void resolveIfRequired(final ObjectAdapter targetAdapter) {
        resolveIfRequired(targetAdapter.getPojo());
    }

    protected void resolveIfRequired(final Object domainObject) {
        if (resolveObjectChangedEnabled) {
            getPersistenceSession().refreshRootInTransaction(domainObject);
        }
    }

    public WrapperFactory getWrapperFactory() {
        return wrapperFactory;
    }

    @Override
    public T getDelegate() {
        return delegate;
    }

    public ExecutionMode getExecutionMode() {
        return executionMode;
    }

    protected Object delegate(final Method method, final Object[] args) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        return method.invoke(getDelegate(), args);
    }

    protected boolean isObjectMethod(final Method method) {
        return toStringMethod.equals(method) || hashCodeMethod.equals(method) || equalsMethod.equals(method);
    }

    @Override
    public Object invoke(final Object object, final Method method, final Object[] args) throws Throwable {
        return method.invoke(object, args);
    }

    protected InteractionEvent notifyListeners(final InteractionEvent interactionEvent) {
        wrapperFactory.notifyListeners(interactionEvent);
        return interactionEvent;
    }


    private PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession().orElse(null);
    }

}
