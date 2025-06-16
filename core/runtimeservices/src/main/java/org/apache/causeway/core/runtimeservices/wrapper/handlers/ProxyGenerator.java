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

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Map;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.applib.services.wrapper.WrappingObject;
import org.apache.causeway.applib.services.wrapper.control.SyncControl;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.collections._Arrays;
import org.apache.causeway.commons.internal.context._Context;
import org.apache.causeway.commons.internal.proxy._ProxyFactory;
import org.apache.causeway.commons.internal.proxy._ProxyFactoryService;
import org.apache.causeway.commons.semantics.CollectionSemantics;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.runtime.wrap.WrapperInvocationHandler;

public record ProxyGenerator(@NonNull _ProxyFactoryService proxyFactoryService) {

    public <T> T objectProxy(
        final T domainObject,
        final ManagedObject adapter,
        final SyncControl syncControl) {

        var invocationHandler = new DomainObjectInvocationHandler<T>(
            domainObject,
            null, // mixeeAdapter ignored
            adapter,
            syncControl,
            this);

        return instantiateProxy(invocationHandler);
    }

    public <T> T mixinProxy(
            final T mixin,
            final ManagedObject mixeeAdapter,
            final ManagedObject mixinAdapter,
            final SyncControl syncControl) {
    
        var invocationHandler = new DomainObjectInvocationHandler<T>(
                mixin,
                mixeeAdapter,
                mixinAdapter,
                syncControl,
                this);
    
        return instantiateProxy(invocationHandler);
    }
    
    /**
     * Whether to execute or not will be picked up from the supplied parent
     * handler.
     */
    public <T, E> Collection<E> collectionProxy(
            final Collection<E> collectionToBeProxied,
            final DomainObjectInvocationHandler<T> handler,
            final OneToManyAssociation otma) {
    
        var collectionInvocationHandler = PluralInvocationHandler
            .forCollection(collectionToBeProxied, handler, otma);
    
        var proxyBase = CollectionSemantics
            .valueOfElseFail(collectionToBeProxied.getClass())
            .getContainerType();
    
        return instantiateProxy(_Casts.uncheckedCast(proxyBase), collectionInvocationHandler);
    }
    
    /**
     * Whether to execute or not will be picked up from the supplied parent
     * handler.
     */
    public <T, P, Q> Map<P, Q> mapProxy(
            final Map<P, Q> collectionToBeProxied,
            final DomainObjectInvocationHandler<T> handler,
            final OneToManyAssociation otma) {
    
        var mapInvocationHandler = PluralInvocationHandler
            .forMap(collectionToBeProxied, handler, otma);
    
        var proxyBase = Map.class;
    
        return instantiateProxy(_Casts.uncheckedCast(proxyBase), mapInvocationHandler);
    }
    
    // -- HELPER

    <T> T instantiateProxy(final WrapperInvocationHandler handler) {
        var pojoToBeProxied = handler.context().delegate();
        Class<T> base = _Casts.uncheckedCast(pojoToBeProxied.getClass());
        return instantiateProxy(base, handler);
    }

    /**
     * Creates a proxy, using given {@code base} type as the proxy's base.
     * @implNote introduced to circumvent access issues on cases,
     *      where {@code handler.getDelegate().getClass()} is not visible
     *      (eg. nested private type)
     */
    <T> T instantiateProxy(final Class<T> base, final WrapperInvocationHandler handler) {
        if (base.isInterface()) {
            return _Casts.uncheckedCast(
                    Proxy.newProxyInstance(
                            _Context.getDefaultClassLoader(),
                            _Arrays.combine(base, (Class<?>[]) new Class[]{WrappingObject.class}),
                            handler));
        } else {
            final _ProxyFactory<T> proxyFactory = proxyFactoryService.factory(base, WrappingObject.class);
            return proxyFactory.createInstance(handler, false);
        }
    }
    
}
