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

import java.util.Collection;
import java.util.Map;

import org.apache.isis.applib.services.wrapper.control.SyncControl;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.runtimeservices.wrapper.proxy.ProxyCreator;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class ProxyContextHandler {

    @NonNull private final ProxyCreator proxyCreator;

    public <T> T proxy(
            final T domainObject,
            final ManagedObject adapter,
            final SyncControl syncControl) {

        val invocationHandler = new DomainObjectInvocationHandler<T>(
                domainObject,
                null, // mixeeAdapter ignored
                adapter,
                syncControl,
                this);

        return proxyCreator.instantiateProxy(invocationHandler);
    }

    public <T> T mixinProxy(
            final T mixin,
            final ManagedObject mixeeAdapter,
            final ManagedObject mixinAdapter,
            final SyncControl syncControl) {

        val invocationHandler = new DomainObjectInvocationHandler<T>(
                mixin,
                mixeeAdapter,
                mixinAdapter,
                syncControl,
                this);

        return proxyCreator.instantiateProxy(invocationHandler);
    }


    /**
     * Whether to execute or not will be picked up from the supplied parent
     * handler.
     */
    public <T, E> Collection<E> proxy(
            final Collection<E> collectionToProxy,
            final DomainObjectInvocationHandler<T> handler,
            final OneToManyAssociation otma) {

        val collectionInvocationHandler = new CollectionInvocationHandler<T, Collection<E>>(
                        collectionToProxy, handler, otma);
        collectionInvocationHandler.setResolveObjectChangedEnabled(
                handler.isResolveObjectChangedEnabled());

        return proxyCreator.instantiateProxy(collectionInvocationHandler);
    }

    /**
     * Whether to execute or not will be picked up from the supplied parent
     * handler.
     */
    public <T, P, Q> Map<P, Q> proxy(
            final Map<P, Q> collectionToProxy,
            final DomainObjectInvocationHandler<T> handler,
            final OneToManyAssociation otma) {

        val mapInvocationHandler = new MapInvocationHandler<T, Map<P, Q>>(
                collectionToProxy, handler, otma);
        mapInvocationHandler.setResolveObjectChangedEnabled(handler.isResolveObjectChangedEnabled());

        return proxyCreator.instantiateProxy(mapInvocationHandler);
    }



}
