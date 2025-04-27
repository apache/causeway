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

import java.util.Map;

import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.semantics.CollectionSemantics;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;

class MapInvocationHandler<T, M extends Map<?,?>>
extends PluralInvocationHandlerAbstract<T, M> {

    public MapInvocationHandler(
            final Object proxyObject,
            final M mapToBeProxied,
            final DomainObjectInvocationHandler<T> handler,
            final OneToManyAssociation otma) {

        super(proxyObject, mapToBeProxied, handler, otma,
                CollectionSemantics.MAP);

        _Assert.assertTrue(Map.class.isAssignableFrom(mapToBeProxied.getClass()),
                ()->String.format("Cannot use %s for type %s, these are not compatible.",
                        this.getClass().getName(),
                        mapToBeProxied.getClass()));
    }

}
