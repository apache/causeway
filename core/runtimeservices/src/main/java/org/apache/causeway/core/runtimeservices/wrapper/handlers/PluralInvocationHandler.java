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
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.semantics.CollectionSemantics;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.runtime.wrap.WrapperInvocationHandler;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Base class in support of non-scalar types to be proxied up.
 *
 * @param <T> Domain Object type
 * @param <P> non-scalar type (eg. {@link Collection} or {@link Map}) to be proxied
 */
final class PluralInvocationHandler<T, P>
implements WrapperInvocationHandler {

    // -- FACTORIES
    
   static <T, C extends Collection<?>> PluralInvocationHandler<T, C> forCollection(
           final C collectionToBeProxied,
           final DomainObjectInvocationHandler<T> handler,
           final OneToManyAssociation otma) {
       
       _Assert.assertTrue(Collection.class.isAssignableFrom(collectionToBeProxied.getClass()),
               ()->String.format("Cannot use %s for type %s, these are not compatible.",
                       PluralInvocationHandler.class.getName() + ".forCollection(..)",
                       collectionToBeProxied.getClass()));
       
       return new PluralInvocationHandler<>(collectionToBeProxied, handler, otma,
                CollectionSemantics
                    .valueOfElseFail(collectionToBeProxied.getClass()));
    }
    
   static <T, M extends Map<?,?>> PluralInvocationHandler<T, M> forMap(
           final M mapToBeProxied,
           final DomainObjectInvocationHandler<T> handler,
           final OneToManyAssociation otma) {

       _Assert.assertTrue(Map.class.isAssignableFrom(mapToBeProxied.getClass()),
               ()->String.format("Cannot use %s for type %s, these are not compatible.",
                       PluralInvocationHandler.class.getName() + ".forMap(..)",
                       mapToBeProxied.getClass()));
       
       return new PluralInvocationHandler<>(mapToBeProxied, handler, otma,
               CollectionSemantics.MAP);
   }
   
    // -- CONSTRUCTION
    
    @Getter(onMethod_ = {@Override}) @Accessors(fluent=true) 
    private final WrapperInvocationHandler.Context context;
    
    private final OneToManyAssociation oneToManyAssociation;
    private final CollectionSemantics collectionSemantics;
    
    protected PluralInvocationHandler(
            final P collectionOrMapToBeProxied,
            final DomainObjectInvocationHandler<T> handler,
            final OneToManyAssociation otma,
            final CollectionSemantics collectionSemantics) {

        this.context = WrapperInvocationHandler.Context.of(otma.getMetaModelContext(), 
                collectionOrMapToBeProxied, handler.context().syncControl());

        this.oneToManyAssociation = otma;
        this.collectionSemantics = collectionSemantics;
    }

    public OneToManyAssociation getCollection() {
        return oneToManyAssociation;
    }

    public T getDomainObject() {
        return (T) context().delegate();
    }

    @Override
    public Object invoke(final Object collectionObject, final Method method, final Object[] args) throws Throwable {

        final Object returnValueObj = context().invoke(method, args);

        var policy = collectionSemantics.getInvocationHandlingPolicy();

        if (policy.intercepts(method)) {

            var event =
                    new CollectionMethodEvent(
                            context().delegate(),
                            getCollection().getFeatureIdentifier(),
                            getDomainObject(),
                            method.getName(),
                            args,
                            returnValueObj);
            context().notifyListeners(event);
            return returnValueObj;
        }

        if (policy.vetoes(method)) {
            throw new UnsupportedOperationException(
                    String.format("Method '%s' may not be called directly.", method.getName()));
        }

        return returnValueObj;
    }

}
