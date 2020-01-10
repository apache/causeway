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

package org.apache.isis.runtimeservices.wrapper.handlers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumSet;

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.WrapperFactory.ExecutionMode;
import org.apache.isis.applib.services.wrapper.events.InteractionEvent;
import org.apache.isis.core.commons.internal._Constants;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.objectmanager.load.ObjectLoader;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

public class DelegatingInvocationHandlerDefault<T> implements DelegatingInvocationHandler<T> {

    private ObjectManager objectManager;
    
    // getter is API
    @Getter(onMethod = @__(@Override)) private final T delegate;
    @Getter protected final WrapperFactory wrapperFactory;
    @Getter private final EnumSet<ExecutionMode> executionMode;

    protected final Method equalsMethod;
    protected final Method hashCodeMethod;
    protected final Method toStringMethod;

    // getter and setter are API
    @Getter(onMethod = @__(@Override)) @Setter(onMethod = @__(@Override))
    private boolean resolveObjectChangedEnabled;

    public DelegatingInvocationHandlerDefault(
            final ServiceRegistry serviceRegistry,
            final T delegate,
            final EnumSet<ExecutionMode> executionMode) {

        if (delegate == null) {
            throw new IllegalArgumentException("delegate must not be null");
        }
        this.delegate = delegate;
        this.wrapperFactory = serviceRegistry.lookupServiceElseFail(WrapperFactory.class);
        this.objectManager = serviceRegistry.lookupServiceElseFail(ObjectManager.class);
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

    
    
    protected void resolveIfRequired(final ManagedObject adapter) {

        if(!resolveObjectChangedEnabled) {
            return;
        }
        if(adapter==null) {
            return;
        }
        if(!ManagedObject.isEntity(adapter)) {
            return;
        }
        
        val rootOid = objectManager.identifyObject(adapter);
        
        val loadRequest = ObjectLoader.Request.of(adapter.getSpecification(), rootOid.getIdentifier());
        
        objectManager.loadObject(loadRequest);
        
        //legacy of 
        //getPersistenceSession().refreshRootInTransaction(domainObject);
    }
    
    protected void resolveIfRequired(final Object domainObject) {
        resolveIfRequired(objectManager.adapt(domainObject));
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

}
