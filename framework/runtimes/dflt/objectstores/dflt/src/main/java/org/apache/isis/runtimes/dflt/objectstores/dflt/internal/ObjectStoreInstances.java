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

package org.apache.isis.runtimes.dflt.objectstores.dflt.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.isis.applib.clock.Clock;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.version.SerialNumberVersion;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.objectstores.dflt.InMemoryObjectStore;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryBuiltIn;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManager;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSessionHydrator;

/*
 * The objects need to store in a repeatable sequence so the elements and instances method return the same data for any repeated
 * call, and so that one subset of instances follows on the previous. This is done by keeping the objects in the order that they
 * where created.
 */
public class ObjectStoreInstances {

    private final Map<Oid, Object> pojoByOidMap = new HashMap<Oid, Object>();
    private final Map<Oid, SerialNumberVersion> versionByOidMap = new HashMap<Oid, SerialNumberVersion>();

    @SuppressWarnings("unused")
    private final ObjectSpecification spec;

    // ///////////////////////////////////////////////////////
    // Constructors
    // ///////////////////////////////////////////////////////

    public ObjectStoreInstances(final ObjectSpecification spec) {
        this.spec = spec;
    }

    // ///////////////////////////////////////////////////////
    // Object Instances
    // ///////////////////////////////////////////////////////

    /**
     * TODO: shouldn't really be exposing this directly.
     */
    public Map<Oid, Object> getObjectInstances() {
        return pojoByOidMap;
    }

    public Set<Oid> getOids() {
        return Collections.unmodifiableSet(pojoByOidMap.keySet());
    }

    public Object getPojo(final Oid oid) {
        return pojoByOidMap.get(oid);
    }

    public Version getVersion(final Oid oid) {
        return versionByOidMap.get(oid);
    }

    // ///////////////////////////////////////////////////////
    // shutdown
    // ///////////////////////////////////////////////////////

    public void shutdown() {
        pojoByOidMap.clear();
        versionByOidMap.clear();
    }

    // ///////////////////////////////////////////////////////
    // save, remove
    // ///////////////////////////////////////////////////////

    public void save(final ObjectAdapter adapter) {
        pojoByOidMap.put(adapter.getOid(), adapter.getObject());

        final SerialNumberVersion version = versionByOidMap.get(adapter.getOid());
        final SerialNumberVersion nextVersion = nextVersion(version);
        versionByOidMap.put(adapter.getOid(), nextVersion);
        adapter.setVersion(nextVersion);
    }

    private synchronized SerialNumberVersion nextVersion(final SerialNumberVersion version) {
        final long sequence = (version != null ? version.getSequence() : 0) + 1;
        return new SerialNumberVersion(sequence, getAuthenticationSession().getUserName(), new Date(Clock.getTime()));
    }

    public void remove(final Oid oid) {
        pojoByOidMap.remove(oid);
        versionByOidMap.remove(oid);
    }

    // ///////////////////////////////////////////////////////
    // retrieveObject
    // ///////////////////////////////////////////////////////

    /**
     * If the pojo exists in the object store, then looks up the
     * {@link ObjectAdapter adapter} from the {@link AdapterManager}, and only
     * if none found does it
     * {@link PersistenceSessionHydrator#recreateAdapter(Oid, Object) recreate}
     * a new {@link ObjectAdapter adapter}.
     */
    public ObjectAdapter retrieveObject(final Oid oid) {
        final Object pojo = getObjectInstances().get(oid);
        if (pojo == null) {
            return null;
        }
        final ObjectAdapter adapterLookedUpByPojo = getAdapterManager().getAdapterFor(pojo);
        if (adapterLookedUpByPojo != null) {
            return adapterLookedUpByPojo;
        }
        final ObjectAdapter adapterLookedUpByOid = getAdapterManager().getAdapterFor(oid);
        if (adapterLookedUpByOid != null) {
            return adapterLookedUpByOid;
        }
        return getHydrator().recreateAdapter(oid, pojo);
    }

    // ///////////////////////////////////////////////////////
    // instances, numberOfInstances, hasInstances
    // ///////////////////////////////////////////////////////

    /**
     * Not API, but <tt>public</tt> so can be called by
     * {@link InMemoryObjectStore}.
     */
    public void findInstancesAndAdd(final PersistenceQueryBuiltIn persistenceQuery, final List<ObjectAdapter> foundInstances) {
        for (final ObjectAdapter element : elements()) {
            if (persistenceQuery.matches(element)) {
                foundInstances.add(element);
            }
        }
    }

    public int numberOfInstances() {
        return getObjectInstances().size();
    }

    public boolean hasInstances() {
        return numberOfInstances() > 0;
    }

    private List<ObjectAdapter> elements() {
        final List<ObjectAdapter> v = new ArrayList<ObjectAdapter>(getObjectInstances().size());
        for (final Oid oid : getObjectInstances().keySet()) {
            v.add(retrieveObject(oid));
        }
        return v;
    }

    // ///////////////////////////////////////////////////////
    // Debugging
    // ///////////////////////////////////////////////////////

    public void debugData(final DebugBuilder debug) {
        debug.indent();
        if (getObjectInstances().size() == 0) {
            debug.appendln("no instances");
        }
        for (final Oid oid : getObjectInstances().keySet()) {
            final ObjectAdapter objectAdapter = retrieveObject(oid);
            final String title = objectAdapter.titleString();
            final Object object = getObjectInstances().get(oid);
            debug.appendln(oid.toString(), object + " (" + title + ")");
        }
        debug.appendln();
        debug.unindent();
    }

    // ///////////////////////////////////////////////////////
    // Dependencies (from context)
    // ///////////////////////////////////////////////////////

    /**
     * Must use {@link IsisContext context}, because although this object is
     * recreated with each {@link PersistenceSession session}, the persisted
     * objects that get
     * {@link #attachPersistedObjects(MemoryObjectStorePersistedObjects)
     * attached} to it span multiple sessions.
     * 
     * <p>
     * The alternative design would be to laboriously inject this object via the
     * {@link InMemoryObjectStore}.
     */
    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    /**
     * Must use {@link IsisContext context}, because although this object is
     * recreated with each {@link PersistenceSession session}, the persisted
     * objects that get
     * {@link #attachPersistedObjects(MemoryObjectStorePersistedObjects)
     * attached} to it span multiple sessions.
     * 
     * <p>
     * The alternative design would be to laboriously inject this object via the
     * {@link InMemoryObjectStore}.
     */
    protected AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    /**
     * Must use {@link IsisContext context}, because although this object is
     * recreated with each {@link PersistenceSession session}, the persisted
     * objects that get
     * {@link #attachPersistedObjects(MemoryObjectStorePersistedObjects)
     * attached} to it span multiple sessions.
     * 
     * <p>
     * The alternative design would be to laboriously inject this object via the
     * {@link InMemoryObjectStore}.
     */
    protected PersistenceSessionHydrator getHydrator() {
        return getPersistenceSession();
    }

    /**
     * Must use {@link IsisContext context}, because although this object is
     * recreated with each {@link PersistenceSession session}, the persisted
     * objects that get
     * {@link #attachPersistedObjects(MemoryObjectStorePersistedObjects)
     * attached} to it span multiple sessions.
     * 
     * <p>
     * The alternative design would be to laboriously inject this object via the
     * {@link InMemoryObjectStore}.
     */
    protected AuthenticationSession getAuthenticationSession() {
        return IsisContext.getAuthenticationSession();
    }

}
