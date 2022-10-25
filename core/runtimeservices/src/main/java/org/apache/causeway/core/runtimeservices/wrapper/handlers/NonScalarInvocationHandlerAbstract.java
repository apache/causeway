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
import java.util.List;
import java.util.Map;

import org.apache.causeway.applib.services.wrapper.events.CollectionMethodEvent;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;

import lombok.val;

/**
 * Base class in support of non-scalar types to be proxied up.
 *
 * @param <T> Domain Object type
 * @param <P> non-scalar type (eg. {@link Collection} or {@link Map}) to be proxied
 */
abstract class NonScalarInvocationHandlerAbstract<T, P>
extends DelegatingInvocationHandlerDefault<P> {

    private final List<Method> interceptedMethods = _Lists.newArrayList();
    private final List<Method> vetoedMethods = _Lists.newArrayList();

    private final OneToManyAssociation oneToManyAssociation;
    private final T domainObject;

    protected NonScalarInvocationHandlerAbstract(
            final P collectionOrMapToProxy,
            final DomainObjectInvocationHandler<T> handler,
            final OneToManyAssociation otma) {

        super(otma.getMetaModelContext(),
                collectionOrMapToProxy,
                handler.getSyncControl());

        this.oneToManyAssociation = otma;
        this.domainObject = handler.getDelegate();
    }

    /**
     * Adds given method to the list of intercepted methods,
     * those which will trigger {@link CollectionMethodEvent}(s)
     * on invocation.
     */
    protected Method intercept(final Method method) {
        this.interceptedMethods.add(method);
        return method;
    }

    /**
     * Adds given method to the list of vetoed methods,
     * those which will cause an {@link UnsupportedOperationException}
     * on invocation.
     */
    protected Method veto(final Method method) {
        this.vetoedMethods.add(method);
        return method;
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
        final Object returnValueObj = delegate(method, args);

        if (interceptedMethods.contains(method)) {

            resolveIfRequired(domainObject);

            val event =
                    new CollectionMethodEvent(
                            getDelegate(),
                            getCollection().getFeatureIdentifier(),
                            getDomainObject(),
                            method.getName(),
                            args,
                            returnValueObj);
            notifyListeners(event);
            return returnValueObj;
        }

        if (vetoedMethods.contains(method)) {
            throw new UnsupportedOperationException(
                    String.format("Method '%s' may not be called directly.", method.getName()));
        }

        return returnValueObj;
    }

}
