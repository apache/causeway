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

package org.apache.isis.runtimes.dflt.runtime.testsystem;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.spec.ElementSpecificationProvider;
import org.apache.isis.core.metamodel.spec.Instance;
import org.apache.isis.core.metamodel.spec.ObjectList;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.Specification;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.ObjectToAdapterTransformer;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManager;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;

public class TestProxyCollectionAdapter implements ObjectAdapter {

    private Vector collection = new Vector();
    private Oid oid;
    private ResolveState resolveState = ResolveState.GHOST;
    private ObjectSpecification specification;
    private Version version;
    private String removeValidMessage;
    private String addValidMessgae;

    public TestProxyCollectionAdapter() {
    }

    public TestProxyCollectionAdapter(final Object wrappedCollection) {
        if (wrappedCollection instanceof Collection) {
            collection = new Vector((Collection) wrappedCollection);
        } else if (wrappedCollection.getClass().isArray()) {
            final Object[] array = (Object[]) wrappedCollection;
            collection = new Vector(array.length);
            for (final Object element : array) {
                collection.add(element);
            }
        } else if (wrappedCollection instanceof ObjectList) {
            final Enumeration elements = ((ObjectList) wrappedCollection).elements();
            collection = new Vector();
            while (elements.hasMoreElements()) {
                collection.add(elements.nextElement());
            }
        }
    }

    @Override
    public void checkLock(final Version version) {
    }

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

    @Override
    public String getIconName() {
        return null;
    }

    @Override
    public Object getObject() {
        return collection;
    }

    @Override
    public Oid getOid() {
        return oid;
    }

    @Override
    public ResolveState getResolveState() {
        return resolveState;
    }

    @Override
    public boolean isPersistent() {
        return getResolveState().isPersistent();
    }

    @Override
    public boolean isTransient() {
        return getResolveState().isTransient();
    }

    @Override
    public ObjectSpecification getSpecification() {
        return specification;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    public void init(final ObjectAdapter[] initElements) {
        Assert.assertEquals("Collection not empty", 0, this.collection.size());
        for (final ObjectAdapter initElement : initElements) {
            collection.addElement(initElement);
        }
    }

    @Override
    public void replacePojo(final Object pojo) {
        throw new NotYetImplementedException();
    }

    @Override
    public void setOptimisticLock(final Version version) {
    }

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

    @Override
    public String titleString() {
        return "title";
    }

    public void setupVersion(final Version version) {
        this.version = version;
    }

    @Override
    public void changeState(final ResolveState newState) {
    }

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

    @Override
    public void fireChangedEvent() {
    }

    @Override
    public ObjectSpecification getElementSpecification() {
        return null;
    }

    @Override
    public ObjectAdapter getOwner() {
        return null;
    }

    @Override
    public Instance getInstance(final Specification specification) {
        return null;
    }

    @Override
    public boolean isAggregated() {
        return true;
    }

    @Override
    public ObjectAdapter getAggregateRoot() {
        final Oid parentOid = ((AggregatedOid) this.getOid()).getParentOid();
        return getAdapterManager().getAdapterFor(parentOid);
    }

    // ////////////////////////////////////////////////////////////////
    // Dependencies (from context)
    // ////////////////////////////////////////////////////////////////

    protected AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.isis.core.metamodel.spec.ObjectMetaModel#setElementTypeProvider(org.apache.isis.core.metamodel.spec
     * .ElementTypeProvider)
     */
    @Override
    public void setElementSpecificationProvider(final ElementSpecificationProvider elementSpecificationProvider) {
        // TODO Auto-generated method stub

    }

}

class TestProxyCollectionFacet implements CollectionFacet {

    private TestProxyCollectionAdapter collectionDowncasted(final ObjectAdapter collection) {
        final TestProxyCollectionAdapter coll = (TestProxyCollectionAdapter) collection;
        return coll;
    }

    @Override
    public boolean contains(final ObjectAdapter collection, final ObjectAdapter element) {
        return collectionDowncasted(collection).contains(element);
    }

    @Override
    public Enumeration elements(final ObjectAdapter collection) {
        final TestProxyCollectionAdapter collectionDowncasted = collectionDowncasted(collection);
        final List list = EnumerationUtils.toList(collectionDowncasted.elements());
        final Collection transformedCollection = CollectionUtils.collect(list, new ObjectToAdapterTransformer());
        return new IteratorEnumeration(transformedCollection.iterator());
    }

    @Override
    public ObjectAdapter firstElement(final ObjectAdapter collection) {
        return collectionDowncasted(collection).firstElement();
    }

    @Override
    public void init(final ObjectAdapter collection, final ObjectAdapter[] initData) {
    }

    @Override
    public int size(final ObjectAdapter collection) {
        return collectionDowncasted(collection).size();
    }

    @Override
    public Class<? extends Facet> facetType() {
        return CollectionFacet.class;
    }

    @Override
    public void setFacetHolder(final FacetHolder facetHolder) {
    }

    @Override
    public boolean alwaysReplace() {
        return false;
    }

    @Override
    public boolean isDerived() {
        return false;
    }

    @Override
    public boolean isNoop() {
        return false;
    }

    @Override
    public FacetHolder getFacetHolder() {
        throw new NotYetImplementedException();
    }

    @Override
    public TypeOfFacet getTypeOfFacet() {
        throw new NotYetImplementedException();
    }

    @Override
    public Iterator<ObjectAdapter> iterator(final ObjectAdapter wrappedCollection) {
        throw new NotYetImplementedException();
    }

    @Override
    public Collection<ObjectAdapter> collection(final ObjectAdapter wrappedCollection) {
        throw new NotYetImplementedException();
    }

    @Override
    public Iterable<ObjectAdapter> iterable(final ObjectAdapter collectionAdapter) {
        throw new NotYetImplementedException();
    }

    @Override
    public Facet getUnderlyingFacet() {
        return null;
    }

    @Override
    public void setUnderlyingFacet(final Facet underlyingFacet) {
        throw new UnsupportedOperationException();
    }

}
