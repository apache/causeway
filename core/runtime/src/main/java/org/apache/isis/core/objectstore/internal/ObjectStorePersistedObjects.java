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

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.system.persistence.IdentifierGeneratorDefault;

/**
 * Represents the persisted objects.
 * 
 * Attached and detached to each session.
 */
public class ObjectStorePersistedObjects {

    private final Map<ObjectSpecId, ObjectStoreInstances> instancesBySpecMap = Maps.newHashMap();
    private final Map<ObjectSpecId, Oid> serviceOidByIdMap = Maps.newHashMap();

    private IdentifierGeneratorDefault.Memento oidGeneratorMemento;


    public IdentifierGeneratorDefault.Memento getOidGeneratorMemento() {
        return oidGeneratorMemento;
    }

    public void saveOidGeneratorMemento(final IdentifierGeneratorDefault.Memento memento) {
        this.oidGeneratorMemento = memento;
    }

    public Oid getService(final ObjectSpecId objectSpecId) {
        return serviceOidByIdMap.get(objectSpecId);
    }

    public void registerService(final ObjectSpecId objectSpecId, final Oid oid) {
        final Oid oidLookedUpByName = serviceOidByIdMap.get(objectSpecId);
        if (oidLookedUpByName != null) {
            if (oidLookedUpByName.equals(oid)) {
                throw new IsisException("Already another service registered as name: " + objectSpecId + " (existing Oid: " + oidLookedUpByName + ", " + "intended: " + oid + ")");
            }
        } else {
            serviceOidByIdMap.put(objectSpecId, oid);
        }
    }

    // TODO: this is where the clever logic needs to go to determine how to save
    // into our custom Map.
    // also think we shouldn't surface the entire Map, just the API we require
    // (keySet, values etc).
    public ObjectStoreInstances instancesFor(final ObjectSpecId specId) {
        ObjectStoreInstances ins = instancesBySpecMap.get(specId);
        if (ins == null) {
            ins = new ObjectStoreInstances(specId);
            instancesBySpecMap.put(specId, ins);
        }
        return ins;
    }

    public Iterable<ObjectSpecId> specifications() {
        return instancesBySpecMap.keySet();
    }

    public void clear() {
        instancesBySpecMap.clear();
    }

    public Iterable<ObjectStoreInstances> instances() {
        return instancesBySpecMap.values();
    }

}
