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

import java.lang.reflect.Method;
import java.util.List;

import org.apache.isis.commons.internal.collections._Lists;

import org.apache.isis.applib.services.wrapper.events.CollectionMethodEvent;
import org.apache.isis.applib.services.wrapper.events.InteractionEvent;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;

abstract class AbstractCollectionInvocationHandler<T, C> extends DelegatingInvocationHandlerDefault<C> {

    private final List<Method> interceptedMethods = _Lists.newArrayList();
    private final List<Method> vetoedMethods = _Lists.newArrayList();

    private final String collectionName;
    private final OneToManyAssociation oneToManyAssociation;
    private final T domainObject;

    public AbstractCollectionInvocationHandler(
            final C collectionOrMapToProxy,
            final String collectionName,
            final DomainObjectInvocationHandler<T> handler,
            final OneToManyAssociation otma) {
        super(collectionOrMapToProxy, handler.getExecutionMode());
        this.collectionName = collectionName;
        this.oneToManyAssociation = otma;
        this.domainObject = handler.getDelegate();
    }

    protected Method intercept(final Method method) {
        this.interceptedMethods.add(method);
        return method;
    }

    protected Method veto(final Method method) {
        this.vetoedMethods.add(method);
        return method;
    }

    public String getCollectionName() {
        return collectionName;
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

            final InteractionEvent ev = new CollectionMethodEvent(getDelegate(), getCollection().getIdentifier(), getDomainObject(), method.getName(), args, returnValueObj);
            notifyListeners(ev);
            return returnValueObj;
        }

        if (vetoedMethods.contains(method)) {
            throw new UnsupportedOperationException(String.format("Method '%s' may not be called directly.", method.getName()));
        }

        return returnValueObj;
    }

}
