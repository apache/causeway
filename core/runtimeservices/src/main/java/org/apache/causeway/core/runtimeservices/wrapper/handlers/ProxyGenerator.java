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
import org.apache.causeway.commons.internal.context._Context;
import org.apache.causeway.commons.internal.proxy._ProxyFactoryService;
import org.apache.causeway.commons.semantics.CollectionSemantics;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.runtime.wrap.WrapperInvocationHandler;

public record ProxyGenerator(@NonNull _ProxyFactoryService proxyFactoryService) {

    public <T> T objectProxy(
        final T domainObject,
        final ObjectSpecification domainObjectSpec,
        final SyncControl syncControl) {

        var invocationHandler = handlerForRegular(domainObjectSpec);
        return instantiateProxy(invocationHandler, new WrappingObject.Origin(domainObject, syncControl));
    }

    public <T> T mixinProxy(
            final T mixin,
            final ManagedObject mixeeAdapter,
            final ObjectSpecification mixinSpec,
            final SyncControl syncControl) {
    
        var invocationHandler = handlerForMixin(mixeeAdapter, mixinSpec);
        return instantiateProxy(invocationHandler, new WrappingObject.Origin(mixin, syncControl));
    }
    
    /**
     * Whether to execute or not will be picked up from the supplied parent
     * handler.
     */
    public <T, E> Collection<E> collectionProxy(
            final Collection<E> collectionToBeProxied,
            final OneToManyAssociation otma) {
    
        var collectionInvocationHandler = PluralInvocationHandler
            .forCollection(collectionToBeProxied, otma);
    
        var proxyBase = CollectionSemantics
            .valueOfElseFail(collectionToBeProxied.getClass())
            .getContainerType();
    
        return instantiatePluralProxy(_Casts.uncheckedCast(proxyBase), 
                collectionInvocationHandler);
    }
    
    /**
     * Whether to execute or not will be picked up from the supplied parent
     * handler.
     */
    public <T, P, Q> Map<P, Q> mapProxy(
            final Map<P, Q> mapToBeProxied,
            final OneToManyAssociation otma) {
    
        var proxyBase = Map.class;
    
        return instantiatePluralProxy(_Casts.uncheckedCast(proxyBase), 
                PluralInvocationHandler.forMap(mapToBeProxied, otma));
    }
    
    // -- HELPER

    <T> T instantiateProxy(final WrapperInvocationHandler handler, WrappingObject.Origin origin) {
        return _Casts.uncheckedCast(instantiateProxy(handler.classMetaData().pojoClass(), handler, origin));
    }

    /**
     * Creates a proxy, using given {@code base} type as the proxy's base.
     * @implNote introduced to circumvent access issues on cases,
     *      where {@code handler.getDelegate().getClass()} is not visible
     *      (eg. nested private type)
     */
    private <T> T instantiateProxy(final Class<T> base, final WrapperInvocationHandler handler, WrappingObject.Origin origin) {
        T proxy = proxyFactoryService
                .factory(base, WrappingObject.class, WrappingObject.ADDITIONAL_FIELDS)
                .createInstance(handler, false);
        return WrappingObject.withOrigin(proxy, origin);
    }
    
    private <T, P> P instantiatePluralProxy(final Class<T> base, final PluralInvocationHandler<T, P> pluralInvocationHandler) {
        var proxyWithoutFields = Proxy.newProxyInstance(
                _Context.getDefaultClassLoader(),
                new Class<?>[] {base},
                pluralInvocationHandler); 
        return _Casts.uncheckedCast(proxyWithoutFields);
    }

    public WrapperInvocationHandler handlerForRegular(ObjectSpecification targetSpec) {
        return new DomainObjectInvocationHandler(
                null, // mixeeAdapter ignored
                targetSpec,
                this);
    }

    public WrapperInvocationHandler handlerForMixin(ManagedObject mixeeAdapter, ObjectSpecification mixinSpec) {
        return new DomainObjectInvocationHandler(
                mixeeAdapter,
                mixinSpec,
                this);
    }
    
}
