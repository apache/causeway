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


package org.apache.isis.runtime.testsystem;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.apache.isis.commons.ensure.Assert;
import org.apache.isis.metamodel.adapter.Instance;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ObjectList;
import org.apache.isis.metamodel.adapter.ResolveState;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.adapter.version.Version;
import org.apache.isis.metamodel.commons.exceptions.NotYetImplementedException;
import org.apache.isis.metamodel.facets.Facet;
import org.apache.isis.metamodel.facets.FacetHolder;
import org.apache.isis.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.Specification;
import org.apache.isis.runtime.persistence.adaptermanager.ObjectToAdapterTransformer;


public class TestProxyCollectionAdapter implements ObjectAdapter {
    
    private Vector collection = new Vector();
    private Oid oid;
    private ResolveState resolveState = ResolveState.GHOST;
    private ObjectSpecification specification;
    private Version version;
    private String removeValidMessage;
    private String addValidMessgae;

    public TestProxyCollectionAdapter() {}

    public TestProxyCollectionAdapter(final Object wrappedCollection) {
        if (wrappedCollection instanceof Collection) {
            collection = new Vector((Collection) wrappedCollection);
        } else if (wrappedCollection.getClass().isArray()) {
            final Object[] array = (Object[]) wrappedCollection;
            collection = new Vector(array.length);
            for (int i = 0; i < array.length; i++) {
                collection.add(array[i]);
            }
        } else if (wrappedCollection instanceof ObjectList) {
            final Enumeration elements = ((ObjectList) wrappedCollection).elements();
            collection = new Vector();
            while (elements.hasMoreElements()) {
                collection.add(elements.nextElement());
            }
        }
    }

    public void checkLock(final Version version) {}

    boolean contains(final ObjectAdapter object) {
        return collection.contains(object);
    }

    public Enumeration elements() {
        return collection.elements();
    }

    public ObjectAdapter firstElement() {
        if (collection.size() == 0) {
            return null;
        } else {
            return (ObjectAdapter) collection.elementAt(0);
        }
    }

    public ObjectSpecification getElementSpecification() {
        return null;
    }

    public String getIconName() {
        return null;
    }

    public Object getObject() {
        return collection;
    }

    public Oid getOid() {
        return oid;
    }

    public ResolveState getResolveState() {
        return resolveState;
    }

	public boolean isPersistent() {
		return getResolveState().isPersistent();
	}

	public boolean isTransient() {
		return getResolveState().isTransient();
	}

    public ObjectSpecification getSpecification() {
        return specification;
    }

    public Version getVersion() {
        return version;
    }

    public void init(final ObjectAdapter[] initElements) {
        Assert.assertEquals("Collection not empty", 0, this.collection.size());
        for (int i = 0; i < initElements.length; i++) {
            collection.addElement(initElements[i]);
        }
    }

    public void replacePojo(final Object pojo) {
        throw new NotYetImplementedException();
    }

    public void setOptimisticLock(final Version version) {}

    public void setupResolveState(final ResolveState resolveState) {
        this.resolveState = resolveState;
    }

    public void setupSpecification(final ObjectSpecification specification) {
        this.specification = specification;
    }

    public void setupOid(final Oid oid) {
        this.oid = oid;
    }

    public void setupElement(final ObjectAdapter element) {
        collection.addElement(element);
    }

    public int size() {
        return collection.size();
    }

    public String titleString() {
        return "title";
    }

    public void setupVersion(final Version version) {
        this.version = version;
    }

    public void changeState(final ResolveState newState) {}

    public void add(final ObjectAdapter element) {
        collection.add(element);
    }

    public void clear() {
        collection.clear();
    }

    public String isAddValid(final ObjectAdapter element) {
        return addValidMessgae;
    }

    public String isRemoveValid(final ObjectAdapter element) {
        return removeValidMessage;
    }

    public void remove(final ObjectAdapter element) {
        collection.remove(element);
    }

    public void setupAddValidMessage(final String addValidMessage) {
        this.addValidMessgae = addValidMessage;
    }

    public void setupRemoveValidMessage(final String removeValidMessage) {
        this.removeValidMessage = removeValidMessage;
    }

    public void fireChangedEvent() {}

    public void setTypeOfFacet(final TypeOfFacet typeOfFacet) {}

    public TypeOfFacet getTypeOfFacet() {
        return null;
    }

    public ObjectAdapter getOwner() {
        return null;
    }

    public Instance getInstance(Specification specification) {
        return null;
    }

    public boolean isAggregated() {
        return true;
    }

}

class TestProxyCollectionFacet implements CollectionFacet {

    private TestProxyCollectionAdapter collectionDowncasted(final ObjectAdapter collection) {
        final TestProxyCollectionAdapter coll = (TestProxyCollectionAdapter) collection;
        return coll;
    }

    public boolean contains(final ObjectAdapter collection, final ObjectAdapter element) {
        return collectionDowncasted(collection).contains(element);
    }

    public Enumeration elements(final ObjectAdapter collection) {
        TestProxyCollectionAdapter collectionDowncasted = collectionDowncasted(collection);
        List list = EnumerationUtils.toList(collectionDowncasted.elements());
        Collection transformedCollection = CollectionUtils.collect(list, new ObjectToAdapterTransformer());
        return new IteratorEnumeration(transformedCollection.iterator());
    }

    public ObjectAdapter firstElement(final ObjectAdapter collection) {
        return collectionDowncasted(collection).firstElement();
    }

    public void init(final ObjectAdapter collection, final ObjectAdapter[] initData) {}

    public int size(final ObjectAdapter collection) {
        return collectionDowncasted(collection).size();
    }

    public Class<? extends Facet> facetType() {
        return CollectionFacet.class;
    }

    public void setFacetHolder(final FacetHolder facetHolder) {}

    public boolean alwaysReplace() {
        return false;
    }
    
    public boolean isDerived() {
    	return false;
    }

    public boolean isNoop() {
        return false;
    }

    public FacetHolder getFacetHolder() {
        throw new NotYetImplementedException();
    }

    public TypeOfFacet getTypeOfFacet() {
        throw new NotYetImplementedException();
    }

    public Iterator<ObjectAdapter> iterator(ObjectAdapter wrappedCollection) {
        throw new NotYetImplementedException();
    }

    public Collection<ObjectAdapter> collection(ObjectAdapter wrappedCollection) {
        throw new NotYetImplementedException();
    }

    public Iterable<ObjectAdapter> iterable(ObjectAdapter collectionAdapter) {
        throw new NotYetImplementedException();
    }

	public Facet getUnderlyingFacet() {
		return null;
	}
	public void setUnderlyingFacet(Facet underlyingFacet) {
		throw new UnsupportedOperationException();
	}

}
