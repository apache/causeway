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

package org.apache.isis.core.objectstore.internal;

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
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.version.SerialNumberVersion;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.objectstore.InMemoryObjectStore;
import org.apache.isis.core.runtime.persistence.query.PersistenceQueryBuiltIn;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;

/*
 * The objects need to store in a repeatable sequence so the elements and instances method return the same data for any repeated
 * call, and so that one subset of instances follows on the previous. This is done by keeping the objects in the order that they
 * where created.
 */
public class ObjectStoreInstances {

    private final Map<Oid, Object> pojoByOidMap = new HashMap<Oid, Object>();
    private final Map<Oid, Version> versionByOidMap = new HashMap<Oid, Version>();

    @SuppressWarnings("unused")
    private final ObjectSpecId spec;

    // ///////////////////////////////////////////////////////
    // Constructors
    // ///////////////////////////////////////////////////////

    public ObjectStoreInstances(final ObjectSpecId spec) {
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

        final Version version = versionByOidMap.get(adapter.getOid());
        final Version nextVersion = nextVersion(version);
        versionByOidMap.put(adapter.getOid(), nextVersion);
        adapter.setVersion(nextVersion);
    }

    private synchronized Version nextVersion(final Version version) {
        final long sequence = (version != null ? version.getSequence() : 0) + 1;
        return SerialNumberVersion.create(sequence, getAuthenticationSession().getUserName(), new Date(Clock.getTime()));
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
     * {@link ObjectAdapter adapter} from the {@link org.apache.isis.core.runtime.persistence.adaptermanager.AdapterManagerDefault}, and only
     * if none found does it recreates a new {@link ObjectAdapter adapter}.
     */
    public ObjectAdapter getObjectAndMapIfRequired(final Oid oid) {
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
        return getPersistenceSession().getAdapterManager().mapRecreatedPojo(oid, pojo);
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
            v.add(getObjectAndMapIfRequired(oid));
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
            final ObjectAdapter objectAdapter = getObjectAndMapIfRequired(oid);
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
    protected AuthenticationSession getAuthenticationSession() {
        return IsisContext.getAuthenticationSession();
    }

}
