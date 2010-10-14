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


package org.apache.isis.metamodel.facets.collections;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.facets.FacetHolder;
import org.apache.isis.metamodel.facets.collections.modify.CollectionFacetAbstract;
import org.apache.isis.metamodel.runtimecontext.RuntimeContext;


public class JavaCollectionFacet extends CollectionFacetAbstract {

    private final RuntimeContext runtimeContext;

	public JavaCollectionFacet(
			final FacetHolder holder, 
			final RuntimeContext runtimeContext) {
        super(holder);
        this.runtimeContext = runtimeContext;
    }

    @SuppressWarnings("unchecked")
    public Collection<ObjectAdapter> collection(ObjectAdapter wrappedCollection) {
        Collection<?> collectionOfUnderlying = collectionOfUnderlying(wrappedCollection);
        return CollectionUtils.collect(collectionOfUnderlying, new ObjectToAdapterTransformer(getRuntimeContext()));
    }

	public ObjectAdapter firstElement(final ObjectAdapter collection) {
        final Iterator<ObjectAdapter> iterator = iterator(collection);
        return iterator.hasNext() ? iterator.next() : null;
    }

    public int size(final ObjectAdapter collection) {
        return collectionOfUnderlying(collection).size();
    }

    @SuppressWarnings("unchecked")
    public void init(final ObjectAdapter collection, final ObjectAdapter[] initData) {
        final Collection javaCollection = collectionOfUnderlying(collection);
        javaCollection.clear();
        for (int i = 0; i < initData.length; i++) {
            javaCollection.add(initData[i].getObject());
        }
    }

    /**
     * The underlying collection of objects (not {@link ObjectAdapter}s).
     */
    private Collection<?> collectionOfUnderlying(final ObjectAdapter wrappedCollection) {
        return (Collection<?>) wrappedCollection.getObject();
    }



    ////////////////////////////////////////////////////////////////////////
    // Dependencies (from constructor)
    ////////////////////////////////////////////////////////////////////////
    
    private RuntimeContext getRuntimeContext() {
		return runtimeContext;
	}


}

