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
package org.apache.isis.core.metamodel.facets.collections.javautilcollection;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.collections.CollectionFacetAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public class JavaCollectionFacet extends CollectionFacetAbstract {

    public JavaCollectionFacet(final FacetHolder holder) {
        super(holder);
    }

    @Override
    public Object populatePojo(
            final Supplier<Object> emptyCollectionPojoFactory,
            final ObjectSpecification collectionSpec,
            final Stream<ManagedObject> initData,
            final int elementCount) {

        final Collection<? super Object> pojoCollection = _Casts.uncheckedCast(emptyCollectionPojoFactory.get());
        pojoCollection.clear(); // just in case
        initData.forEach(pojoCollection::add);
        return pojoCollection;
    }

    /**
     * @param collectionAdapter - {@link ManagedObject} wrapping a collection.
     */
    @Override
    public Stream<ManagedObject> stream(final @Nullable ManagedObject collectionAdapter) {
        return _NullSafe.stream(pojoCollection(collectionAdapter))
                .map(super.getObjectManager()::adapt);
    }

    /**
     * @param collectionAdapter - {@link ManagedObject} wrapping a collection.
     */
    @Override
    public int size(final @Nullable ManagedObject collectionAdapter) {
        return _NullSafe.size(pojoCollection(collectionAdapter));
    }

    /**
     * The underlying collection of objects (not {@link ManagedObject}s).
     */
    @Nullable
    private Collection<?> pojoCollection(final @Nullable ManagedObject collectionAdapter) {
        return collectionAdapter == null
                ? (Collection<?>) null
                : (Collection<?>) collectionAdapter.getPojo();
    }

}
