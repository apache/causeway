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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.collections.CollectionFacetAbstract;

public class JavaArrayFacet extends CollectionFacetAbstract {

    private final ObjectAdapterProvider adapterProvider;

    public JavaArrayFacet(final FacetHolder holder, final ObjectAdapterProvider adapterProvider) {
        super(holder);
        this.adapterProvider = adapterProvider;
    }

    /**
     * Expected to be called with a {@link ObjectAdapter} wrapping an array.
     */
    @Override
    public ObjectAdapter init(final ObjectAdapter arrayAdapter, final ObjectAdapter[] initData) {
        final int length = initData.length;
        final Object[] array = new Object[length];
        for (int i = 0; i < length; i++) {
            array[i] = initData[i].getObject();
        }
        return arrayAdapter.withPojo(array);  
    }

    /**
     * Expected to be called with a {@link ObjectAdapter} wrapping an array.
     */
    @Override
    public Collection<ObjectAdapter> collection(final ObjectAdapter arrayAdapter) {
        final Object[] array = pojoArray(arrayAdapter);
        if(isEmpty(array)) {
            return Collections.emptyList();
        }
        final ArrayList<ObjectAdapter> objectCollection = new ArrayList<ObjectAdapter>(array.length);
        for (final Object element : array) {
            final ObjectAdapter elementAdapter = getObjectAdapterProvider().adapterFor(element);
            objectCollection.add(elementAdapter);
        }
        return objectCollection;
    }

    /**
     * Expected to be called with a {@link ObjectAdapter} wrapping an array.
     */
    @Override
    public ObjectAdapter firstElement(final ObjectAdapter arrayAdapter) {
        final Object[] array = pojoArray(arrayAdapter);
        if(isEmpty(array)) {
            return null;
        }
        return array.length > 0 ? getObjectAdapterProvider().adapterFor(array[0]) : null;
    }

    /**
     * Expected to be called with a {@link ObjectAdapter} wrapping an array.
     */
    @Override
    public int size(final ObjectAdapter arrayAdapter) {
        return pojoArray(arrayAdapter).length;
    }

    private Object[] pojoArray(final ObjectAdapter arrayAdapter) {
        return (Object[]) arrayAdapter.getObject();
    }

    // /////////////////////////////////////////////////////
    // Dependencies (from constructor)
    // /////////////////////////////////////////////////////

    private ObjectAdapterProvider getObjectAdapterProvider() {
        return adapterProvider;
    }

}
