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

package org.apache.isis.viewer.html.context;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacetUtils;
import org.apache.isis.core.metamodel.facets.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;

/**
 * Has value semantics based on the value semantics of the underlying list that
 * it wraps.
 */
public class CollectionMapping implements Iterable<String> {
    
    private final List<String> list = Lists.newArrayList();
    private final ObjectSpecification elementSpecification;

    public CollectionMapping(final Context context, final ObjectAdapter collection) {
        final TypeOfFacet typeOfFacet = collection.getSpecification().getFacet(TypeOfFacet.class);
        elementSpecification = typeOfFacet.valueSpec();

        final CollectionFacet collectionFacet = CollectionFacetUtils.getCollectionFacetFromSpec(collection);
        
        for (ObjectAdapter element : collectionFacet.iterable(collection)) {
            final String objectId = context.mapObject(element);
            list.add(objectId);
        }
    }

    public ObjectAdapter getCollection(final Context context) {
        final List<Object> elementPojos = Lists.newArrayList();
        
        for (String elementId : list) {
            final ObjectAdapter adapter = context.getMappedObject(elementId);
            final Object pojo = adapter.getObject();
            elementPojos.add(pojo);
        }
        return getAdapterManager().adapterFor(elementPojos);
    }

    public ObjectSpecification getElementSpecification() {
        return elementSpecification;
    }

    
    public Iterator<String> iterator() {
        return list.iterator();
    }

    public boolean contains(final String id) {
        for (String elementId : list) {
            if (elementId.equals(id)) {
                return true;
            }
        }
        return false;
    }

    public void remove(final String existingId) {
        for (final String elementId: list) {
            if (elementId.equals(existingId)) {
                list.remove(existingId);
                break;
            }
        }
    }


    // //////////////////////////////////////////////////////
    // debugging
    // //////////////////////////////////////////////////////

    public void debug(final DebugBuilder debug) {
        debug.indent();
        for (String elementId : list) {
            debug.appendln(elementId);
        }
        debug.unindent();
    }



    // //////////////////////////////////////////////////////
    // equals, hashCode
    // //////////////////////////////////////////////////////

    /**
     * Value semantics based on the identity of the underlying list that this
     * wraps.
     */
    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (other.getClass() != this.getClass()) {
            return false;
        }
        return equals((CollectionMapping) other);
    }

    public boolean equals(final CollectionMapping other) {
        return this.list.equals(other.list);
    }

    // //////////////////////////////////////////////////////
    // Dependencies (from context)
    // //////////////////////////////////////////////////////

    private static AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    private static PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

}
