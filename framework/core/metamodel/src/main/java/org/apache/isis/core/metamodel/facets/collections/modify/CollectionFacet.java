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

package org.apache.isis.core.metamodel.facets.collections.modify;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

/**
 * Attached to {@link ObjectSpecification}s that represent a collection.
 * 
 * <p>
 * Factories of (implementations of this) facet should ensure that a
 * {@link TypeOfFacet} is also attached to the same facet holder. The
 * {@link #getTypeOfFacet()} is a convenience for this.
 */
public interface CollectionFacet extends Facet {

    int size(ObjectAdapter collection);

    /**
     * @deprecated - use {@link #iterator(ObjectAdapter)} or
     *             {@link #iterable(ObjectAdapter)}.
     */
    @Deprecated
    Enumeration<ObjectAdapter> elements(ObjectAdapter collectionAdapter);

    Iterable<ObjectAdapter> iterable(ObjectAdapter collectionAdapter);

    Iterator<ObjectAdapter> iterator(ObjectAdapter collectionAdapter);

    /**
     * Returns an unmodifiable {@link Collection} of {@link ObjectAdapter}s.
     */
    Collection<ObjectAdapter> collection(ObjectAdapter collectionAdapter);

    ObjectAdapter firstElement(ObjectAdapter collectionAdapter);

    boolean contains(ObjectAdapter collectionAdapter, ObjectAdapter element);

    /**
     * Set the contents of this collection.
     */
    void init(ObjectAdapter collectionAdapter, ObjectAdapter[] elements);

    /**
     * Convenience method that returns the {@link TypeOfFacet} on this facet's
     * {@link #getFacetHolder() holder}.
     */
    TypeOfFacet getTypeOfFacet();

}
