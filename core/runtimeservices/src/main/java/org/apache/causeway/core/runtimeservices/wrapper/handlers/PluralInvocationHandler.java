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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import org.apache.causeway.applib.services.wrapper.events.CollectionMethodEvent;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.semantics.CollectionSemantics;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.runtime.wrap.WrapperInvocationHandler;

/**
 * InvocationHandler in support of non-scalar types to be proxied up.
 *
 * @param <T> Domain Object type
 * @param <P> non-scalar type (eg. {@link Collection} or {@link Map}) to be proxied
 */
record PluralInvocationHandler<T, P>(
        P collectionOrMapToBeProxied,
        WrapperInvocationHandler.ClassMetaData classMetaData,
        OneToManyAssociation oneToManyAssociation,
        CollectionSemantics collectionSemantics
        ) implements InvocationHandler {

    // -- FACTORIES
    
   static <T, C extends Collection<?>> PluralInvocationHandler<T, C> forCollection(
           final C collectionToBeProxied,
           final OneToManyAssociation otma) {
       
       _Assert.assertTrue(Collection.class.isAssignableFrom(collectionToBeProxied.getClass()),
               ()->String.format("Cannot use %s for type %s, these are not compatible.",
                       PluralInvocationHandler.class.getName() + ".forCollection(..)",
                       collectionToBeProxied.getClass()));
       
       return new PluralInvocationHandler<>(collectionToBeProxied, otma,
                CollectionSemantics
                    .valueOfElseFail(collectionToBeProxied.getClass()));
    }
    
   static <T, M extends Map<?,?>> PluralInvocationHandler<T, M> forMap(
           final M mapToBeProxied,
           final OneToManyAssociation otma) {

       _Assert.assertTrue(Map.class.isAssignableFrom(mapToBeProxied.getClass()),
               ()->String.format("Cannot use %s for type %s, these are not compatible.",
                       PluralInvocationHandler.class.getName() + ".forMap(..)",
                       mapToBeProxied.getClass()));
       
       return new PluralInvocationHandler<>(mapToBeProxied, otma,
               CollectionSemantics.MAP);
   }
   
    // -- NON CANONICAL CONSTRUCTOR
    
    private PluralInvocationHandler(
            final P collectionOrMapToBeProxied,
            final OneToManyAssociation otma,
            final CollectionSemantics collectionSemantics) {
        
        this(collectionOrMapToBeProxied, 
                WrapperInvocationHandler.ClassMetaData.of(collectionOrMapToBeProxied), 
                otma, collectionSemantics);
    }

    @Override
    public Object invoke(final Object collectionObject, final Method method, final Object[] args) throws Throwable {

        var policy = collectionSemantics.getInvocationHandlingPolicy();
        if (policy.intercepts(method)) {

            final Object returnValueObj = method.invoke(collectionOrMapToBeProxied, args);
            
            var event =
                    new CollectionMethodEvent(
                            collectionOrMapToBeProxied,
                            oneToManyAssociation().getFeatureIdentifier(),
                            collectionOrMapToBeProxied,
                            method.getName(),
                            args,
                            returnValueObj);
            oneToManyAssociation().getWrapperFactory().notifyListeners(event);
            return returnValueObj;
        }

        if (policy.vetoes(method)) {
            throw new UnsupportedOperationException(
                    String.format("Method '%s' may not be called directly.", method.getName()));
        }

        return method.invoke(collectionOrMapToBeProxied, args);
    }

}
