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
import java.util.List;

import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;

import lombok.val;

class CollectionInvocationHandler<T, C extends Collection<?>>
extends NonScalarInvocationHandlerAbstract<T, C> {

    public CollectionInvocationHandler(
            final C collectionToProxy,
            final DomainObjectInvocationHandler<T> handler,
            final OneToManyAssociation otma) {

        super(collectionToProxy, handler, otma);

        _Assert.assertTrue(collectionToProxy.getClass().isAssignableFrom(Collection.class),
                ()->String.format("Cannot use %s for type %s, these are not compatible.",
                        this.getClass().getName(),
                        collectionToProxy.getClass()));

        val methodSets = (collectionToProxy instanceof List)
                ? ProgrammingModelConstants.WrapperFactoryProxy.LIST
                : ProgrammingModelConstants.WrapperFactoryProxy.COLLECTION;

        methodSets.getIntercepted().forEach(this::intercept);
        methodSets.getVetoed().forEach(this::veto);

    }

}
