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

package org.apache.isis.progmodel.wrapper.metamodel.internal;

import static org.apache.isis.core.commons.lang.MethodUtils.getMethod;

import java.util.Map;

import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;

class MapInvocationHandler<T, C> extends AbstractCollectionInvocationHandler<T, C> {

    public MapInvocationHandler(final C collectionToProxy, final String collectionName, final DomainObjectInvocationHandler<T> handler, final OneToManyAssociation otma) {
        super(collectionToProxy, collectionName, handler, otma);

        try {
            intercept(getMethod(collectionToProxy, "containsKey", Object.class));
            intercept(getMethod(collectionToProxy, "containsValue", Object.class));
            intercept(getMethod(collectionToProxy, "size"));
            intercept(getMethod(collectionToProxy, "isEmpty"));
            veto(getMethod(collectionToProxy, "put", Object.class, Object.class));
            veto(getMethod(collectionToProxy, "remove", Object.class));
            veto(getMethod(collectionToProxy, "putAll", Map.class));
            veto(getMethod(collectionToProxy, "clear"));
        } catch (final NoSuchMethodException e) {
            // ///CLOVER:OFF
            throw new RuntimeException("A Collection method could not be found: " + e.getMessage());
            // ///CLOVER:ON
        }
    }

}
