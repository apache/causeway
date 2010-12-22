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


package org.apache.isis.core.progmodel.facets.collections;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.isis.core.metamodel.adapter.AdapterMap;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.collections.CollectionFacetAbstract;


public class JavaArrayFacet extends CollectionFacetAbstract {

    private final AdapterMap adapterMap;

    public JavaArrayFacet(final FacetHolder holder, final AdapterMap adapterManager) {
        super(holder);
        this.adapterMap = adapterManager;
    }

    /**
     * Expected to be called with a {@link ObjectAdapter} wrapping an array.
     */
    @Override
    public void init(final ObjectAdapter collectionAdapter, final ObjectAdapter[] initData) {
        int length = initData.length;
        Object[] array = new Object[length];
        for (int i = 0; i < length; i++) {
            array[i] = initData[i].getObject();
        }
        collectionAdapter.replacePojo(array);
    }

    /**
     * Expected to be called with a {@link ObjectAdapter} wrapping an array.
     */
    @Override
    public Collection<ObjectAdapter> collection(ObjectAdapter collectionAdapter) {
        final Object[] array = array(collectionAdapter);
        ArrayList<ObjectAdapter> objectCollection = new ArrayList<ObjectAdapter>(array.length);
        for (int i = 0; i < array.length; i++) {
            ObjectAdapter element = getAdapterMap().getAdapterFor(array[i]);
            objectCollection.add(element);
        }
        return objectCollection;
    }

    /**
     * Expected to be called with a {@link ObjectAdapter} wrapping an array.
     */
    @Override
    public ObjectAdapter firstElement(final ObjectAdapter collectionAdapter) {
        final Object[] array = array(collectionAdapter);
        return array.length > 0 ? getAdapterMap().getAdapterFor(array[0]) : null;
    }

    /**
     * Expected to be called with a {@link ObjectAdapter} wrapping an array.
     */
    @Override
    public int size(final ObjectAdapter collectionAdapter) {
        return array(collectionAdapter).length;
    }

    private Object[] array(final ObjectAdapter collectionAdapter) {
        return (Object[]) collectionAdapter.getObject();
    }

    // /////////////////////////////////////////////////////
    // Dependencies (from constructor)
    // /////////////////////////////////////////////////////

    private AdapterMap getAdapterMap() {
        return adapterMap;
    }

}

