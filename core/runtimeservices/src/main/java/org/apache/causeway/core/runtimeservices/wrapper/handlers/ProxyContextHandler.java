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
                this, targetSpecification,
                targetPojo,
                null // mixeePojo ignored
        );

        T proxyObject = proxyCreator.instantiateProxy(invocationHandler);
        capture(proxyObject, new WrapperInvocationContext(targetPojo, null, syncControl, null));

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
                this, targetSpecification,
                targetMixinPojo,
                mixeePojo
        );

        T proxyObject = proxyCreator.instantiateProxy(invocationHandler);
        capture(proxyObject, new WrapperInvocationContext(targetMixinPojo, mixeePojo, syncControl, null));

        return proxyObject;
    }

    private static <T> T capture(T proxyObject, WrapperInvocationContext wic) throws NoSuchFieldException, IllegalAccessException {
        Class<?> proxyObjectClass = proxyObject.getClass();
        final var causewayWrapperInvocationContextField = proxyObjectClass.getDeclaredField("__causeway_wrapperInvocationContext");
        causewayWrapperInvocationContextField.setAccessible(true);
        causewayWrapperInvocationContextField.set(proxyObject, wic);

        return proxyObject;
    }


    /**
     * Whether to execute or not will be picked up from the supplied parent
     * handler.
     */
    public <T, E> Collection<E> proxy(
            final Collection<E> collectionToBeProxied,
            final DomainObjectInvocationHandler<T> handler,
            final OneToManyAssociation otma) {

        val collectionInvocationHandler = new CollectionInvocationHandler<T, Collection<E>>(
                        collectionToBeProxied, handler, otma);

        val proxyBase = CollectionSemantics
                .valueOfElseFail(collectionToBeProxied.getClass())
                .getContainerType();

        Collection<E> proxyCollection = proxyCreator.instantiateProxy(_Casts.uncheckedCast(proxyBase), collectionInvocationHandler);
        return proxyCollection;
    }

    /**
     * Whether to execute or not will be picked up from the supplied parent
     * handler.
     */
    public <T, P, Q> Map<P, Q> proxy(
            final Map<P, Q> collectionToBeProxied,
            final DomainObjectInvocationHandler<T> handler,
            final OneToManyAssociation otma) {

        val mapInvocationHandler = new MapInvocationHandler<T, Map<P, Q>>(
                collectionToBeProxied, handler, otma);

        val proxyBase = Map.class;

        return proxyCreator.instantiateProxy(_Casts.uncheckedCast(proxyBase), mapInvocationHandler);
    }



}
