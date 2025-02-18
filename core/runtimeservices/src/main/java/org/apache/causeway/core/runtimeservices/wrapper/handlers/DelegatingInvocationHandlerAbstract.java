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
package org.apache.causeway.core.runtimeservices.wrapper.handlers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.applib.services.wrapper.control.SyncControl;
import org.apache.causeway.applib.services.wrapper.events.InteractionEvent;
import org.apache.causeway.commons.internal._Constants;
import org.apache.causeway.commons.internal.base._Blackhole;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;

import lombok.Getter;
import lombok.Setter;

/**
 * @param <T> type of delegate
 */
abstract class DelegatingInvocationHandlerAbstract<T> implements DelegatingInvocationHandler<T> {

    private ObjectManager objectManager;

    // getter is API
    @Getter(onMethod_ = {@Override}) private final T delegate;
    @Getter protected final WrapperFactory wrapperFactory;
    @Getter private final SyncControl syncControl;

    protected final Method equalsMethod;
    protected final Method hashCodeMethod;
    protected final Method toStringMethod;

    @Getter(onMethod_ = {@Override}) @Setter(onMethod_ = {@Override})
    private boolean isResolveObjectChangedEnabled = false;

    protected DelegatingInvocationHandlerAbstract(
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
        if(adapter==null) return;
        if(!isResolveObjectChangedEnabled) return;
        if(!ManagedObjects.isEntity(adapter)) return;

        _Blackhole.consume(adapter.getPojo()); // has side effects
    }

    protected void resolveIfRequired(final Object domainObject) {
        resolveIfRequired(objectManager.adapt(domainObject));
    }

    protected Object delegate(final Method method, final Object[] args)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
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
