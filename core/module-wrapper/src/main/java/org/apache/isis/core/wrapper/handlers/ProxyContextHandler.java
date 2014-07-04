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

package org.apache.isis.core.wrapper.handlers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

import java.util.Collection;
import java.util.Map;

import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.WrapperFactory.ExecutionMode;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.ensure.Ensure;
import org.apache.isis.core.metamodel.adapter.ObjectPersistor;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.wrapper.proxy.ProxyInstantiator;

public class ProxyContextHandler {

    private final ProxyInstantiator proxyInstantiator;
    
    public ProxyContextHandler(final ProxyInstantiator proxyInstantiator) {
        this.proxyInstantiator = proxyInstantiator;
    }
    
    public <T> T proxy(final T domainObject, final WrapperFactory wrapperFactory, final ExecutionMode mode, final AuthenticationSessionProvider authenticationSessionProvider, final SpecificationLoader specificationLookup, final AdapterManager adapterManager, final ObjectPersistor objectPersistor) {

        Ensure.ensureThatArg(wrapperFactory, is(not(nullValue())));
        Ensure.ensureThatArg(authenticationSessionProvider, is(not(nullValue())));
        Ensure.ensureThatArg(specificationLookup, is(not(nullValue())));
        Ensure.ensureThatArg(adapterManager, is(not(nullValue())));
        Ensure.ensureThatArg(objectPersistor, is(not(nullValue())));

        final DomainObjectInvocationHandler<T> invocationHandler = new DomainObjectInvocationHandler<T>(domainObject, wrapperFactory, mode, authenticationSessionProvider, specificationLookup, adapterManager, objectPersistor, this);

        return proxyInstantiator.instantiateProxy(invocationHandler);
    }

    /**
     * Whether to execute or not will be picked up from the supplied parent
     * handler.
     */
    public <T, E> Collection<E> proxy(final Collection<E> collectionToProxy, final String collectionName, final DomainObjectInvocationHandler<T> handler, final OneToManyAssociation otma) {

        final CollectionInvocationHandler<T, Collection<E>> collectionInvocationHandler = new CollectionInvocationHandler<T, Collection<E>>(collectionToProxy, collectionName, handler, otma);
        collectionInvocationHandler.setResolveObjectChangedEnabled(handler.isResolveObjectChangedEnabled());

        return proxyInstantiator.instantiateProxy(collectionInvocationHandler);
    }

    /**
     * Whether to execute or not will be picked up from the supplied parent
     * handler.
     */
    public <T, P, Q> Map<P, Q> proxy(final Map<P, Q> collectionToProxy, final String collectionName, final DomainObjectInvocationHandler<T> handler, final OneToManyAssociation otma) {

        final MapInvocationHandler<T, Map<P, Q>> mapInvocationHandler = new MapInvocationHandler<T, Map<P, Q>>(collectionToProxy, collectionName, handler, otma);
        mapInvocationHandler.setResolveObjectChangedEnabled(handler.isResolveObjectChangedEnabled());

        return proxyInstantiator.instantiateProxy(mapInvocationHandler);
    }

}
