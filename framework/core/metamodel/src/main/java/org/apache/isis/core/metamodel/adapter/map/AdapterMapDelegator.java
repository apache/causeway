/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.adapter.map;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;

/**
 * Just delegates to an underlying {@link AdapterMap}.
 * 
 * <p>
 * Provided to allow subclasses to override specific methods if required.
 */
public abstract class AdapterMapDelegator extends AdapterMapAbstract {

    private final AdapterMap underlying;

    public AdapterMapDelegator(final AdapterMap underlying) {
        this.underlying = underlying;
    }

    @Override
    public ObjectAdapter getAdapterFor(final Object pojo) {
        return underlying.getAdapterFor(pojo);
    }

    @Override
    public ObjectAdapter adapterFor(final Object domainObject) {
        return underlying.adapterFor(domainObject);
    }

    @Override
    public ObjectAdapter adapterFor(final Object pojo, final ObjectAdapter ownerAdapter, final OneToManyAssociation collection) {
        return underlying.adapterFor(pojo, ownerAdapter, collection);
    }
}
