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

package org.apache.isis.core.metamodel.specloader.specimpl.standalonelist;

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.collections.CollectionFacetAbstract;
import org.apache.isis.core.metamodel.spec.FreeStandingList;

public class CollectionFacetOnStandaloneList extends CollectionFacetAbstract {

    public CollectionFacetOnStandaloneList(final FacetHolder holder) {
        super(holder);
    }

    /**
     * Expected to be called with a {@link ObjectAdapter} wrapping a
     * {@link FreeStandingList}.
     */
    @Override
    public List<ObjectAdapter> collection(final ObjectAdapter wrappedObjectList) {
        return (FreeStandingList) wrappedObjectList.getObject();
    }

    /**
     * Expected to be called with a {@link ObjectAdapter} wrapping a
     * {@link FreeStandingList}.
     */
    @Override
    public ObjectAdapter firstElement(final ObjectAdapter wrappedInstanceCollectionVector) {
        final List<ObjectAdapter> icv = collection(wrappedInstanceCollectionVector);
        return icv.size() > 0 ? icv.get(0) : null;
    }

    /**
     * Expected to be called with a {@link ObjectAdapter} wrapping a
     * {@link FreeStandingList}.
     */
    @Override
    public int size(final ObjectAdapter wrappedInstanceCollectionVector) {
        return collection(wrappedInstanceCollectionVector).size();
    }

    /**
     * Does nothing.
     */
    @Override
    public ObjectAdapter init(final ObjectAdapter collection, final ObjectAdapter[] initData) {
        return collection;
    }

}
