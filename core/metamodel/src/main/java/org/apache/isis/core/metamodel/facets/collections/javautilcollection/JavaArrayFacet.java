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

import static org.apache.isis.commons.internal.base._NullSafe.isEmpty;
import static org.apache.isis.commons.internal.collections._Arrays.toArray;

import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.collections.CollectionFacetAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public class JavaArrayFacet extends CollectionFacetAbstract {

    private final ObjectAdapterProvider adapterProvider;

    public JavaArrayFacet(final FacetHolder holder, final ObjectAdapterProvider adapterProvider) {
        super(holder);
        this.adapterProvider = adapterProvider;
    }

    @Override
    public <T extends ManagedObject> Object populatePojo(
            Supplier<Object> emptyCollectionPojoFactory, 
            ObjectSpecification collectionSpec,
            Stream<T> initData, 
            int elementCount) {
        
        final Object[] array = initData
                .map(ManagedObject::getPojo)
                .collect(toArray(Object.class, elementCount));
        return array;
    }

    @Override
    public <T extends ManagedObject> Stream<T> stream(T arrayAdapter) {
        final Object[] array = pojoArray(arrayAdapter);
        if(isEmpty(array)) {
            return Stream.of();
        }
        return Stream.of(array)
                .map(getObjectAdapterProvider()::adapterFor) //FIXME[ISIS-1976] we always generate an OA here
                .map(x->(T)x);
    }
    
    /**
     * Expected to be called with a {@link ObjectAdapter} wrapping an array.
     */
    @Override
    public int size(final ManagedObject arrayAdapter) {
        return pojoArray(arrayAdapter).length;
    }

    // -- HELPER
    
    private Object[] pojoArray(final ManagedObject arrayAdapter) {
        return (Object[]) arrayAdapter.getPojo();
    }

    // /////////////////////////////////////////////////////
    // Dependencies (from constructor)
    // /////////////////////////////////////////////////////

    private ObjectAdapterProvider getObjectAdapterProvider() {
        return adapterProvider;
    }







}
