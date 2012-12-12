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
package org.apache.isis.viewer.bdd.common.components;

import java.util.Map;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.facets.object.cached.CachedFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.objectstore.internal.ObjectStoreInstances;
import org.apache.isis.core.objectstore.internal.ObjectStorePersistedObjects;
import org.apache.isis.core.runtime.system.persistence.IdentifierGeneratorDefault.Memento;

/**
 * Stores instances in one of two maps, based on whether have their
 * specification has the {@link CachedFacet} (represents cached or reference
 * data) or not (represents operational data).
 * <p>
 * Those that are cached are stored in a <tt>static</tt> map that is never
 * {@link #clear()}ed down. Those that are operational go in a regular instance
 * cache and can be {@link #clear()}ed.
 */
public class BddObjectStorePersistedObjects implements ObjectStorePersistedObjects {

    private static final Map<ObjectSpecification, ObjectStoreInstances> cachedInstancesBySpecMap = Maps.newHashMap();

    private final Map<ObjectSpecification, ObjectStoreInstances> operationalInstancesBySpecMap = Maps.newHashMap();
    private final Map<ObjectSpecId, Oid> serviceOidByIdMap = Maps.newHashMap();
    
    private Memento oidGeneratorMemento;

    @Override
    public Memento getOidGeneratorMemento() {
        return oidGeneratorMemento;
    }

    @Override
    public void saveOidGeneratorMemento(final Memento memento) {
        this.oidGeneratorMemento = memento;
    }

    @Override
    public Oid getService(final ObjectSpecId objectSpecId) {
        return serviceOidByIdMap.get(objectSpecId);
    }

    @Override
    public void registerService(final ObjectSpecId objectSpecId, final Oid oid) {
        final Oid oidLookedUpByName = serviceOidByIdMap.get(objectSpecId);
        if (oidLookedUpByName != null) {
            if (!oidLookedUpByName.equals(oid)) {
                throw new IsisException("Already another service registered as name: " + objectSpecId + " (existing Oid: " + oidLookedUpByName + ", " + "intended: " + oid + ")");
            }
        } else {
            serviceOidByIdMap.put(objectSpecId, oid);
        }
    }

    @Override
    public Iterable<ObjectSpecification> specifications() {
        return Iterables.concat(BddObjectStorePersistedObjects.cachedInstancesBySpecMap.keySet(), operationalInstancesBySpecMap.keySet());
    }

    @Override
    public Iterable<ObjectStoreInstances> instances() {
        return Iterables.concat(BddObjectStorePersistedObjects.cachedInstancesBySpecMap.values(), operationalInstancesBySpecMap.values());
    }

    @Override
    public ObjectStoreInstances instancesFor(final ObjectSpecification spec) {
        if (isCached(spec)) {
            return getFromMap(spec, BddObjectStorePersistedObjects.cachedInstancesBySpecMap);
        } else {
            return getFromMap(spec, operationalInstancesBySpecMap);
        }
    }

    // //////////////////////////////////////////////////////////////////
    // Helpers
    // //////////////////////////////////////////////////////////////////

    private ObjectStoreInstances getFromMap(final ObjectSpecification spec, final Map<ObjectSpecification, ObjectStoreInstances> map) {
        ObjectStoreInstances ins = map.get(spec);
        if (ins == null) {
            ins = new ObjectStoreInstances(spec);
            map.put(spec, ins);
        }
        return ins;
    }

    private boolean isCached(final ObjectSpecification spec) {
        return spec.containsFacet(CachedFacet.class);
    }

    /**
     * Only clears the operational instances, not the cached instances.
     */
    @Override
    public void clear() {
        operationalInstancesBySpecMap.clear();
    }

}
