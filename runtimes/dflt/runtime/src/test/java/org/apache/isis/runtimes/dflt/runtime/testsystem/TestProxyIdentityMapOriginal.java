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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterFactory;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.AdapterManagerExtended;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.OidGenerator;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;

public class TestProxyIdentityMapOriginal implements AdapterManagerExtended {

    private final Hashtable<Oid, ObjectAdapter> identities = new Hashtable<Oid, ObjectAdapter>();
    private final Hashtable<Object, ObjectAdapter> objectAdapters = new Hashtable<Object, ObjectAdapter>();
    private final Hashtable<Object, ObjectAdapter> collectionAdapters = new Hashtable<Object, ObjectAdapter>();
    private final Hashtable<Oid, ObjectAdapter> recreatedPersistent = new Hashtable<Oid, ObjectAdapter>();
    private final Vector recreatedTransient = new Vector();
    private final Hashtable valueAdapters = new Hashtable();

    public void addAdapter(final Object object, final ObjectAdapter adapter) {
        if (adapter.getSpecification().isCollection()) {
            collectionAdapters.put(object, adapter);
        } else {
            objectAdapters.put(object, adapter);
        }
    }

    public void addIdentity(final Oid oid, final ObjectAdapter adapter) {
        identities.put(oid, adapter);
    }

    public void addRecreated(final Oid oid, final ObjectAdapter adapter) {
        recreatedPersistent.put(oid, adapter);
    }

    public void addRecreatedTransient(final ObjectAdapter adapter) {
        recreatedTransient.addElement(adapter);
    }

    @Override
    public ObjectAdapter getAdapterFor(final Object object) {
        return objectAdapters.get(object);
    }

    @Override
    public ObjectAdapter getAdapterFor(final Oid oid) {
        final ObjectAdapter no = identities.get(oid);
        return no;
    }

    @Override
    public Iterator<ObjectAdapter> iterator() {
        throw new IsisException();
    }

    @Override
    public void open() {
    }

    public void initDomainObject(final Object domainObject) {
    }

    public boolean isIdentityKnown(final Oid oid) {
        return identities.containsKey(oid);
    }

    @Override
    public void remapAsPersistent(final ObjectAdapter adapter) {
        final Oid oid = adapter.getOid();
        getOidGenerator().convertTransientToPersistentOid(oid);
        remapUpdated(oid);
        adapter.changeState(ResolveState.RESOLVED);
    }

    @Override
    public void remapUpdated(final Oid oid) {
        identities.remove(oid);
        final Oid previousOid = oid.getPrevious();
        final ObjectAdapter object = identities.get(previousOid);
        if (object == null) {
            return;
        }
        identities.remove(previousOid);
        final Oid oidFromObject = object.getOid();
        oidFromObject.copyFrom(oid);
        identities.put(oidFromObject, object);
    }

    @Override
    public void reset() {
        // collectionAdapters.clear();
        identities.clear();
        objectAdapters.clear();
        recreatedPersistent.clear();
        recreatedTransient.clear();
        valueAdapters.clear();
    }

    @Override
    public void close() {
    }

    public void unloaded(final ObjectAdapter object) {
        throw new IsisException();
    }

    public ObjectAdapter addAdapter(final ObjectAdapter adapter) {
        identities.put(adapter.getOid(), adapter);
        objectAdapters.put(adapter.getObject(), adapter);

        return adapter;
    }

    public ObjectAdapter createAdapter(final Object pojo, final Oid oid) {
        throw new NotYetImplementedException();
    }

    @Override
    public ObjectAdapter adapterFor(final Object pojo, final ObjectAdapter ownerAdapter,
        final IdentifiedHolder identifiedHolder) {
        throw new NotYetImplementedException();
    }

    @Override
    public ObjectAdapter adapterFor(final Object pojo) {
        throw new NotYetImplementedException();
    }

    @Override
    public ObjectAdapter adapterForAggregated(final Object domainObject, final ObjectAdapter parent) {
        throw new NotYetImplementedException();
    }

    @Override
    public void removeAdapter(final ObjectAdapter objectToDispose) {
        throw new NotYetImplementedException();
    }

    public void setAdapterFactory(final ObjectAdapterFactory adapterFactory) {
        throw new NotYetImplementedException();
    }

    public void setSpecificationLoader(final SpecificationLoader specificationLoader) {
        throw new NotYetImplementedException();
    }

    public void setOidGenerator(final OidGenerator oidGenerator) {
        throw new NotYetImplementedException();
    }

    @Override
    public ObjectAdapter addExistingAdapter(final ObjectAdapter adapter) {
        return addAdapter(adapter);
    }

    public ObjectAdapter testCreateAdapterFor(final Object pojo, final Oid oid) {
        return recreateRootAdapter(oid, pojo);
    }

    @Override
    public ObjectAdapter recreateRootAdapter(final Oid oid, final Object pojo) {
        throw new NotYetImplementedException();
    }

    @Override
    public ObjectAdapter testCreateTransient(final Object pojo, final Oid oid) {
        throw new NotYetImplementedException();
    }

    @Override
    public void injectInto(final Object candidate) {
        throw new NotYetImplementedException();
    }

    @Override
    public String debugTitle() {
        return "Test Proxy Identity Map";
    }

    @Override
    public void debugData(final DebugBuilder debug) {
    }

    // //////////////////////////////////////////////////////////////
    // Dependencies (from context)
    // //////////////////////////////////////////////////////////////

    private PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    private OidGenerator getOidGenerator() {
        return getPersistenceSession().getOidGenerator();
    }

    @Override
    public void removeAdapter(final Oid oid) {
        final ObjectAdapter adapter = getAdapterFor(oid);
        if (adapter != null) {
            removeAdapter(adapter);
        }
    }

}
