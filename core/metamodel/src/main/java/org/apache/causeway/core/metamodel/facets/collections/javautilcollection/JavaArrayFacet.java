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
package org.apache.causeway.core.metamodel.facets.collections.javautilcollection;

import java.util.function.Supplier;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.collections.CollectionFacetAbstract;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import static org.apache.causeway.commons.internal.collections._Arrays.toArray;

import lombok.val;

public class JavaArrayFacet extends CollectionFacetAbstract {

    public JavaArrayFacet(final FacetHolder holder) {
        super(holder);
    }

    @Override
    public Object populatePojo(
            Supplier<Object> emptyCollectionPojoFactory,
            ObjectSpecification collectionSpec,
            Stream<ManagedObject> initData,
            int elementCount) {

        final Object[] array = initData
                .map(ManagedObject::getPojo)
                .collect(toArray(Object.class, elementCount));
        return array;
    }

    /**
     * @param arrayAdapter - {@link ManagedObject} wrapping an array.
     */
    @Override
    public Stream<ManagedObject> stream(final @Nullable ManagedObject arrayAdapter) {
        final Object[] array = pojoArray(arrayAdapter); // might be null
        if(_NullSafe.isEmpty(array)) {
            return Stream.of();
        }

        val objectManager = super.getObjectManager();

        return Stream.of(array)
                .map(objectManager::adapt);
    }

    /**
     * @param arrayAdapter - {@link ManagedObject} wrapping an array.
     */
    @Override
    public int size(final @Nullable ManagedObject arrayAdapter) {
        return _NullSafe.size(pojoArray(arrayAdapter));
    }

    // -- HELPER

    @Nullable
    private Object[] pojoArray(final @Nullable ManagedObject arrayAdapter) {
        return arrayAdapter == null
                ? (Object[]) null
                : (Object[]) arrayAdapter.getPojo();
    }


}
