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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import org.apache.causeway.applib.services.wrapper.events.CollectionMethodEvent;
import org.apache.causeway.commons.semantics.CollectionSemantics;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;

import lombok.val;

/**
 * Base class in support of non-scalar types to be proxied up.
 *
 * @param <T> Domain Object type
 * @param <P> non-scalar type (eg. {@link Collection} or {@link Map}) to be proxied
 */
abstract class PluralInvocationHandlerAbstract<T, P>
extends DelegatingInvocationHandlerAbstract<P> {

    private final OneToManyAssociation oneToManyAssociation;
    private final T domainObject;
    private final CollectionSemantics collectionSemantics;

    private final Object proxyObject;

    private final P target;

    public P getTarget(Object proxyObject) {
        return target;
    }

    protected PluralInvocationHandlerAbstract(
            final Object proxyObject,
            final P collectionOrMapToBeProxied,
            final DomainObjectInvocationHandler<T> handler,
            final OneToManyAssociation otma,
            final CollectionSemantics collectionSemantics) {

        super(otma.getMetaModelContext(),
                (Class<P>)collectionOrMapToBeProxied.getClass()
        );
        this.proxyObject = proxyObject;

        this.target = collectionOrMapToBeProxied;
        this.oneToManyAssociation = otma;
        this.domainObject = handler.getTarget(proxyObject);
        this.collectionSemantics = collectionSemantics;
    }

    public OneToManyAssociation getCollection() {
        return oneToManyAssociation;
    }

    public T getDomainObject() {
        return domainObject;
    }

    @Override
    public Object invoke(final Object collectionObject, final Method method, final Object[] args) throws Throwable {

        // delegate
        final Object returnValueObj = delegate(proxyObject, method, args);

        val policy = collectionSemantics.getInvocationHandlingPolicy();

        if (policy.getIntercepted().contains(method)) {

            val event =
                    new CollectionMethodEvent(
                            getTarget(proxyObject),
                            getCollection().getFeatureIdentifier(),
                            getDomainObject(),
                            method.getName(),
                            args,
                            returnValueObj);
            notifyListeners(event);
            return returnValueObj;
        }

        if (policy.getVetoed().contains(method)) {
            throw new UnsupportedOperationException(
                    String.format("Method '%s' may not be called directly.", method.getName()));
        }

        return returnValueObj;
    }

}
