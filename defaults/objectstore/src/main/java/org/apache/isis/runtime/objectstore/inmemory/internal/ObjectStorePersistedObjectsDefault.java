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


package org.apache.isis.runtime.objectstore.inmemory.internal;

import java.util.HashMap;
import java.util.Map;

import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtime.persistence.oidgenerator.simple.SimpleOidGenerator.Memento;

/**
 * Represents the persisted objects.
 * 
 * Attached and detached to each session.
 */
public class ObjectStorePersistedObjectsDefault implements ObjectStorePersistedObjects {

    private final Map<ObjectSpecification, ObjectStoreInstances> instancesBySpecMap;
    private final Map<String, Oid> serviceOidByIdMap;

	private Memento oidGeneratorMemento;

    public ObjectStorePersistedObjectsDefault() {
        instancesBySpecMap = new HashMap<ObjectSpecification, ObjectStoreInstances>();
        serviceOidByIdMap = new HashMap<String, Oid>();
    }

    
    public Memento getOidGeneratorMemento() {
		return oidGeneratorMemento;
	}
	public void saveOidGeneratorMemento(Memento memento) {
		this.oidGeneratorMemento = memento;
	}

	public Oid getService(String name) {
		return serviceOidByIdMap.get(name);
	}
	public void registerService(String name, Oid oid) {
    	Oid oidLookedUpByName = serviceOidByIdMap.get(name);
    	if (oidLookedUpByName != null) {
    		if (oidLookedUpByName.equals(oid)) {
    			throw new IsisException(
    					"Already another service registered as name: " + name + 
    					" (existing Oid: " + oidLookedUpByName + ", " +
    					"intended: " + oid + ")");
    		}
    	} else {
    		serviceOidByIdMap.put(name, oid);
    	}
	}




	// TODO: this is where the clever logic needs to go to determine how to save into our custom Map.
	// also think we shouldn't surface the entire Map, just the API we require (keySet, values etc).
	public ObjectStoreInstances instancesFor(ObjectSpecification spec) {
        ObjectStoreInstances ins = instancesBySpecMap.get(spec);
        if (ins == null) {
            ins = new ObjectStoreInstances(spec);
            instancesBySpecMap.put(spec, ins);
        }
        return ins;
	}


	public Iterable<ObjectSpecification> specifications() {
		return instancesBySpecMap.keySet();
	}

	public void clear() {
		instancesBySpecMap.clear();		
	}

	public Iterable<ObjectStoreInstances> instances() {
		return instancesBySpecMap.values();
	}


}


