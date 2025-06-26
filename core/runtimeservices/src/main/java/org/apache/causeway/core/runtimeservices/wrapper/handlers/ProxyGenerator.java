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

import org.apache.causeway.applib.services.wrapper.control.SyncControl;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.context._Context;
import org.apache.causeway.commons.internal.proxy.ProxyFactoryService;
import org.apache.causeway.commons.semantics.CollectionSemantics;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.runtime.wrap.WrapperInvocationHandler;
import org.apache.causeway.core.runtime.wrap.WrappingObject;
import org.apache.causeway.core.runtimeservices.session.InteractionIdGenerator;

public record ProxyGenerator(
        @NonNull ProxyFactoryService proxyFactoryService,
        @NonNull CommandRecordFactory commandRecordFactory) {

    public ProxyGenerator(ProxyFactoryService proxyFactoryService, InteractionIdGenerator interactionIdGenerator) {
        this(proxyFactoryService, new CommandRecordFactory(interactionIdGenerator));
    }

    @SuppressWarnings("unchecked")
    public <T> T objectProxy(
            final T domainObject,
            final ObjectSpecification domainObjectSpec,
            final SyncControl syncControl) {
        return (T) instantiateProxy(domainObjectSpec, new WrappingObject.Origin(domainObject, syncControl));
    }

    @SuppressWarnings("unchecked")
    public <T> T mixinProxy(
            final T mixin,
            final ManagedObject managedMixee,
            final ObjectSpecification mixinSpec,
            final SyncControl syncControl) {
        return (T) instantiateProxy(mixinSpec, new WrappingObject.Origin(mixin, managedMixee, syncControl));
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

    /**
     * Creates a proxy, using given {@code targetSpec} type as the proxy's base.
     */
    private Object instantiateProxy(final ObjectSpecification targetSpec, WrappingObject.Origin origin) {
        var proxyClass = proxyFactoryService
            .proxyClass(handler(targetSpec),
                    targetSpec.getCorrespondingClass(), WrappingObject.class, WrappingObject.ADDITIONAL_FIELDS);
        var proxy = proxyFactoryService
                .factory(proxyClass)
                .createInstance(false);
        return WrappingObject.withOrigin(proxy, origin);
    }

    private <T, P> P instantiatePluralProxy(final Class<T> base, final PluralInvocationHandler<T, P> pluralInvocationHandler) {
        var proxyWithoutFields = Proxy.newProxyInstance(
                _Context.getDefaultClassLoader(),
                new Class<?>[] {base},
                pluralInvocationHandler);
        return _Casts.uncheckedCast(proxyWithoutFields);
    }

    public WrapperInvocationHandler handler(ObjectSpecification targetSpec) {
        return new DomainObjectInvocationHandler(
                targetSpec,
                this, commandRecordFactory);
    }

}
