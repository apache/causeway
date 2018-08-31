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
import java.util.Iterator;

import com.google.common.collect.Collections2;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.collections.CollectionFacetAbstract;

public class JavaCollectionFacet extends CollectionFacetAbstract {

    private final AdapterManager adapterManager;

    public JavaCollectionFacet(final FacetHolder holder, final AdapterManager adapterManager) {
        super(holder);
        this.adapterManager = adapterManager;
    }

    @Override
    public Collection<ObjectAdapter> collection(final ObjectAdapter wrappedCollection) {
        final Collection<?> collectionOfUnderlying = collectionOfUnderlying(wrappedCollection);

        //TODO [ahuber] java doc states, this is a live view, don't know if this is needed, 
        // or if a copy is sufficient
        return Collections2.transform(collectionOfUnderlying,
                ObjectAdapter.Functions.adapter_ForUsing(getAdapterManager()));
    }

    @Override
    public ObjectAdapter firstElement(final ObjectAdapter collection) {
        final Iterator<ObjectAdapter> iterator = iterator(collection);
        return iterator.hasNext() ? iterator.next() : null;
    }

    @Override
    public int size(final ObjectAdapter collection) {
        return collectionOfUnderlying(collection).size();
    }

    @Override
    public void init(final ObjectAdapter collection, final ObjectAdapter[] initData) {
        final Collection<? super Object> javaCollection = collectionOfUnderlying(collection);
        javaCollection.clear();
        for (final ObjectAdapter element : initData) {
            final Object pojo = element.getObject();
            javaCollection.add(pojo);
        }
    }

    /**
     * The underlying collection of objects (not {@link ObjectAdapter}s).
     */
    @SuppressWarnings("unchecked")
    private Collection<? super Object> collectionOfUnderlying(final ObjectAdapter wrappedCollection) {
        return (Collection<? super Object>) wrappedCollection.getObject();
    }

    // //////////////////////////////////////////////////////////////////////
    // Dependencies (from constructor)
    // //////////////////////////////////////////////////////////////////////

    private AdapterManager getAdapterManager() {
        return adapterManager;
    }

}
