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


package org.apache.isis.extensions.html.context;

import java.util.Enumeration;
import java.util.Vector;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.util.CollectionFacetUtils;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.core.runtime.persistence.PersistenceSession;
import org.apache.isis.core.runtime.persistence.adaptermanager.AdapterManager;


/**
 * Has value semantics based on the value semantics of the underlying list that it wraps.
 */
public class CollectionMapping {
    private final Vector list = new Vector();
    private final ObjectSpecification elementSpecification;

    public CollectionMapping(final Context context, final ObjectAdapter collection) {
        final TypeOfFacet typeOfFacet = collection.getSpecification().getFacet(TypeOfFacet.class);
        elementSpecification = typeOfFacet.valueSpec();

        final CollectionFacet collectionFacet = CollectionFacetUtils.getCollectionFacetFromSpec(collection);
        final Enumeration elements = collectionFacet.elements(collection);
        while (elements.hasMoreElements()) {
            final ObjectAdapter element = (ObjectAdapter) elements.nextElement();
            list.add(context.mapObject(element));
        }
    }

    public ObjectAdapter getCollection(final Context context) {
        final Vector elements = new Vector();
        final Enumeration e = list.elements();
        while (e.hasMoreElements()) {
            final String elementId = (String) e.nextElement();
            final ObjectAdapter adapter = context.getMappedObject(elementId);
            elements.add(adapter.getObject());
        }
        return getAdapterManager().adapterFor(elements);
    }

    public ObjectSpecification getElementSpecification() {
        return elementSpecification;
    }

    public void debug(final DebugString debug) {
        debug.indent();
        final Enumeration e = list.elements();
        while (e.hasMoreElements()) {
            final String elementId = (String) e.nextElement();
            debug.appendln(elementId);
        }
        debug.unindent();
    }

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
        return equals((CollectionMapping)other);
    }

    public boolean equals(final CollectionMapping other) {
    	return this.list.equals(other.list);
    }

    public boolean contains(final String id) {
        final Enumeration e = list.elements();
        while (e.hasMoreElements()) {
            final String elementId = (String) e.nextElement();
            if (elementId.equals(id)) {
                return true;
            }
        }
        return false;
    }

    public Enumeration elements() {
        return list.elements();
    }
    
    public void remove(String existingId) {
        for (Object entry : list) {
            if (entry.equals(existingId)) {
                list.remove(existingId);
                break;
            }
        }
    }
    
    

    
    ////////////////////////////////////////////////////////
    // Dependencies (from context)
    ////////////////////////////////////////////////////////

    private static AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    private static PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

}

