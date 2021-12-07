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
package org.apache.isis.core.runtimeservices.wrapper.handlers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.control.SyncControl;
import org.apache.isis.applib.services.wrapper.events.InteractionEvent;
import org.apache.isis.commons.internal._Constants;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.objectmanager.load.ObjectLoader;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

public class DelegatingInvocationHandlerDefault<T> implements DelegatingInvocationHandler<T> {

    private ObjectManager objectManager;

    // getter is API
    @Getter(onMethod = @__(@Override)) private final T delegate;
    @Getter protected final WrapperFactory wrapperFactory;
    @Getter private final SyncControl syncControl;

    protected final Method equalsMethod;
    protected final Method hashCodeMethod;
    protected final Method toStringMethod;

    // getter and setter are API
    @Getter(onMethod = @__(@Override)) @Setter(onMethod = @__(@Override))
    private boolean resolveObjectChangedEnabled;

    public DelegatingInvocationHandlerDefault(
            final @NonNull MetaModelContext metaModelContext,
            final @NonNull T delegate,
            final SyncControl syncControl) {

        this.delegate = delegate;
        this.objectManager = metaModelContext.getObjectManager();
        this.wrapperFactory = metaModelContext.getWrapperFactory();
        this.syncControl = syncControl;

        try {
            equalsMethod = delegate.getClass().getMethod("equals", _Constants.classesOfObject);
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
        if(!ManagedObjects.isEntity(adapter)) {
            return;
        }

        val bookmark = objectManager.bookmarkObject(adapter);

        val loadRequest = ObjectLoader.Request.of(adapter.getSpecification(), bookmark);

        objectManager.loadObject(loadRequest);
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
