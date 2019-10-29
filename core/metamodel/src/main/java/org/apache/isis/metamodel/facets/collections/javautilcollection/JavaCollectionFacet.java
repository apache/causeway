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

package org.apache.isis.metamodel.facets.collections.javautilcollection;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.collections.CollectionFacetAbstract;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;

import lombok.val;

public class JavaCollectionFacet extends CollectionFacetAbstract {

    public JavaCollectionFacet(final FacetHolder holder) {
        super(holder);
    }

    @Override
    public <T extends ManagedObject> Object populatePojo(
            Supplier<Object> emptyCollectionPojoFactory, 
            ObjectSpecification collectionSpec,
            Stream<T> initData, 
            int elementCount) {

        final Collection<? super Object> pojoCollection = _Casts.uncheckedCast(emptyCollectionPojoFactory.get());
        pojoCollection.clear(); // just in case
        initData.forEach(pojoCollection::add);
        return pojoCollection;
    }

    @Override
    public int size(final ManagedObject collection) {
        return pojoCollection(collection).size();
    }

    @Override
    public <T extends ManagedObject> Stream<T> stream(T collectionAdapter) {

        val objectManager = super.getObjectManager();

        return pojoCollection(collectionAdapter)
                .stream()
                .map(objectManager::adapt)
                .map(x->_Casts.<T>uncheckedCast(x));
    }

    /**
     * The underlying collection of objects (not {@link ObjectAdapter}s).
     */
    private Collection<?> pojoCollection(final ManagedObject collectionAdapter) {
        return (Collection<?>) collectionAdapter.getPojo();
    }



}
