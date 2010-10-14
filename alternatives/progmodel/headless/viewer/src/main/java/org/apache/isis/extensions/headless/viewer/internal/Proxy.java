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


package org.apache.isis.extensions.headless.viewer.internal;

import java.util.Collection;
import java.util.Map;

import org.apache.isis.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.extensions.headless.applib.HeadlessViewer;
import org.apache.isis.extensions.headless.applib.HeadlessViewer.ExecutionMode;


public class Proxy {

    public static <T> T proxy(
            final T toProxy,
            final HeadlessViewer embeddedViewer,
            final ExecutionMode mode,
            final RuntimeContext runtimeContext) {
        final DomainObjectInvocationHandler<T> invocationHandler = 
        	new DomainObjectInvocationHandler<T>(toProxy, embeddedViewer, mode, runtimeContext);

        final CgLibProxy<T> cglibProxy = new CgLibProxy<T>(invocationHandler);
        return cglibProxy.proxy();
    }

    /**
     * Whether to execute or not will be picked up from the supplied parent handler.
     */
    public static <T, E> Collection<E> proxy(
            final Collection<E> collectionToProxy,
            final String collectionName,
            final DomainObjectInvocationHandler<T> handler,
            final RuntimeContext runtimeContext,
            final OneToManyAssociation otma) {

        final CollectionInvocationHandler<T, Collection<E>> collectionInvocationHandler = 
        	new CollectionInvocationHandler<T, Collection<E>>(
                collectionToProxy, collectionName, handler, runtimeContext, otma);
        collectionInvocationHandler.setResolveObjectChangedEnabled(handler.isResolveObjectChangedEnabled());

        final CgLibProxy<Collection<E>> cglibProxy = new CgLibProxy<Collection<E>>(collectionInvocationHandler);
        return cglibProxy.proxy();
    }

    /**
     * Whether to execute or not will be picked up from the supplied parent handler.
     */
    public static <T, P, Q> Map<P, Q> proxy(
            final Map<P, Q> collectionToProxy,
            final String collectionName,
            final DomainObjectInvocationHandler<T> handler,
            final RuntimeContext runtimeContext,
            final OneToManyAssociation otma) {

        final MapInvocationHandler<T, Map<P, Q>> mapInvocationHandler = new MapInvocationHandler<T, Map<P, Q>>(collectionToProxy,
                collectionName, handler, runtimeContext, otma);
        mapInvocationHandler.setResolveObjectChangedEnabled(handler.isResolveObjectChangedEnabled());

        final CgLibProxy<Map<P, Q>> cglibProxy = new CgLibProxy<Map<P, Q>>(mapInvocationHandler);
        return cglibProxy.proxy();
    }

}
