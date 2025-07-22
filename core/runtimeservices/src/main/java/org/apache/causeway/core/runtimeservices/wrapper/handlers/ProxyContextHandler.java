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

import java.util.Collection;
import java.util.Map;

import org.apache.causeway.applib.services.wrapper.control.SyncControl;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.semantics.CollectionSemantics;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.runtimeservices.wrapper.proxy.ProxyCreator;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@RequiredArgsConstructor
public class ProxyContextHandler {

    @NonNull private final ProxyCreator proxyCreator;

    @SneakyThrows
    public <T> T proxy(
            final MetaModelContext metaModelContext,
            final ObjectSpecification targetSpecification,
            final T targetPojo,
            final SyncControl syncControl
    ) {
        val invocationHandler = new DomainObjectInvocationHandler<T>(
                metaModelContext,
                this,
                targetSpecification
        );

        T proxyObject = proxyCreator.instantiateProxy(invocationHandler);
        WrapperInvocationContext.set(proxyObject, new WrapperInvocationContext(targetPojo, null, syncControl, null));

        return proxyObject;
    }

    @SneakyThrows
    public <T> T mixinProxy(
            final MetaModelContext metaModelContext,
            final ObjectSpecification targetSpecification,
            final T targetMixinPojo,
            final Object mixeePojo,
            final SyncControl syncControl) {

        val invocationHandler = new DomainObjectInvocationHandler<T>(
                metaModelContext,
                this, targetSpecification
        );

        T proxyObject = proxyCreator.instantiateProxy(invocationHandler);
        WrapperInvocationContext.set(proxyObject, new WrapperInvocationContext(targetMixinPojo, mixeePojo, syncControl, null));

        return proxyObject;
    }


    /**
     * Whether to execute or not will be picked up from the supplied parent
     * handler.
     */
    public <T, E> Collection<E> proxy(
            final Object proxyObject,
            final Collection<E> collectionToBeProxied,
            final DomainObjectInvocationHandler<T> handler,
            final OneToManyAssociation otma) {

        // TODO: to introduce caching of proxy classes, we'd need to pull collectionToBeProxied
        //  out of the handler's state, and move into a variant of WrapperInvocationContext, set into the proxyCollection
        val collectionInvocationHandler = new CollectionInvocationHandler<T, Collection<E>>(
                proxyObject, collectionToBeProxied, handler, otma);

        val proxyBase = CollectionSemantics
                .valueOfElseFail(collectionToBeProxied.getClass())
                .getContainerType();

        final var proxyCollection = proxyCreator.instantiateProxy(_Casts.uncheckedCast(proxyBase), collectionInvocationHandler);
        return proxyCollection;
    }

    /**
     * Whether to execute or not will be picked up from the supplied parent
     * handler.
     */
    public <T, P, Q> Map<P, Q> proxy(
            final Object proxyObject,
            final Map<P, Q> mapToBeProxied,
            final DomainObjectInvocationHandler<T> handler,
            final OneToManyAssociation otma) {

        // TODO: to introduce caching of proxy classes, we'd need to pull mapToBeProxied
        //  out of the handler's state, and move into a variant of WrapperInvocationContext, set into the proxyMap
        val mapInvocationHandler = new MapInvocationHandler<T, Map<P, Q>>(
                proxyObject, mapToBeProxied, handler, otma);

        val proxyBase = Map.class;

        final var proxyMap = proxyCreator.instantiateProxy(_Casts.uncheckedCast(proxyBase), mapInvocationHandler);
        return proxyMap;
    }



}
