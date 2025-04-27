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

import org.apache.causeway.applib.services.wrapper.events.InteractionEvent;
import org.apache.causeway.commons.internal._Constants;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

/**
 * @param <T>
 */
public abstract class DelegatingInvocationHandlerAbstract<T> implements DelegatingInvocationHandler<T> {

    // getter is API
    @Getter protected final MetaModelContext metaModelContext;

    @Getter(onMethod_ = {@Override})
    private final Class<T> targetClass;

    protected final Method equalsMethod;
    protected final Method hashCodeMethod;
    protected final Method toStringMethod;

    public DelegatingInvocationHandlerAbstract(
            final @NonNull MetaModelContext metaModelContext,
            final Class<T> targetClass) {
        this.metaModelContext = metaModelContext;
        this.targetClass = targetClass;

        try {
            equalsMethod = this.targetClass.getMethod("equals", _Constants.classesOfObject);
            hashCodeMethod = this.targetClass.getMethod("hashCode", _Constants.emptyClasses);
            toStringMethod = this.targetClass.getMethod("toString", _Constants.emptyClasses);
        } catch (final NoSuchMethodException e) {
            // ///CLOVER:OFF
            throw new RuntimeException("An Object method could not be found: " + e.getMessage());
            // ///CLOVER:ON
        }
    }

    protected ManagedObject adaptAndGuardAgainstWrappingNotSupported(final Object domainObject) {

        if(domainObject == null) {
            return null;
        }
        val adapter = metaModelContext.getObjectManager().adapt(domainObject);
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(adapter)
                || !adapter.getSpecification().getBeanSort().isWrappingSupported()) {
            throw _Exceptions.illegalArgument("Cannot wrap an object of type %s",
                    domainObject.getClass().getName());
        }

        return adapter;
    }

    protected Object delegate(Object proxyObject, final Method method, final Object[] args)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        return method.invoke(getTarget(proxyObject), args);
    }

    protected boolean isObjectMethod(final Method method) {
        return toStringMethod.equals(method) || hashCodeMethod.equals(method) || equalsMethod.equals(method);
    }

    @Override
    public Object invoke(final Object object, final Method method, final Object[] args) throws Throwable {
        return method.invoke(object, args);
    }

    protected InteractionEvent notifyListeners(final InteractionEvent interactionEvent) {
        metaModelContext.getWrapperFactory().notifyListeners(interactionEvent);
        return interactionEvent;
    }

}
