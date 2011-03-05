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


package org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.internal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.map.IdentityMap;
import org.apache.log4j.Logger;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;


/**
 * TODO: an alternative might be to use {@link IdentityMap}.
 */
public class PojoAdapterHashMap implements PojoAdapterMap {

    private static class IdentityHashKey {
        private Object pojo;

        public IdentityHashKey(Object pojo) {
            this.pojo = pojo;
        }

        public int hashCode() {
            return System.identityHashCode(pojo);
        }

        public boolean equals(Object obj) {
            return obj == this || (obj instanceof IdentityHashKey && ((IdentityHashKey) obj).pojo == pojo);
        }
    }

    private static final Logger LOG = Logger.getLogger(PojoAdapterHashMap.class);
    public static final int DEFAULT_POJO_ADAPTER_MAP_SIZE = 10;

    protected final Map<Object, ObjectAdapter> adapterByPojoMap;

    // ///////////////////////////////////////////////////////////////////////////
    // Constructors, finalize
    // ///////////////////////////////////////////////////////////////////////////

    public PojoAdapterHashMap() {
        this(DEFAULT_POJO_ADAPTER_MAP_SIZE);
    }

    public PojoAdapterHashMap(final int capacity) {
        adapterByPojoMap = new HashMap<Object, ObjectAdapter>(capacity);
        //adapterByPojoMap = new IdentityMap(capacity);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        LOG.info("finalizing hash of pojos");
    }

    // ///////////////////////////////////////////////////////////////////////////
    // open, close
    // ///////////////////////////////////////////////////////////////////////////

    public void open() {
    // nothing to do
    }

    public void close() {
        LOG.debug("close");
        adapterByPojoMap.clear();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // reset
    // ///////////////////////////////////////////////////////////////////////////

    public void reset() {
        LOG.debug("reset");
        for (Iterator<Map.Entry<Object, ObjectAdapter>> iterator = adapterByPojoMap.entrySet().iterator(); iterator.hasNext();) {
        	Map.Entry<Object, ObjectAdapter> entry = iterator.next();
        	ObjectAdapter adapter = entry.getValue();
			if (!adapter.getSpecification().isService()) {
        		iterator.remove();
        	}
		}
    }

    // ///////////////////////////////////////////////////////////////////////////
    // add, remove
    // ///////////////////////////////////////////////////////////////////////////

    public void add(final Object pojo, final ObjectAdapter adapter) {
        adapterByPojoMap.put(key(pojo), adapter);
        LOG.debug("add adapter: #" + Long.toHexString(pojo.hashCode()) +  " -> #" + Long.toHexString(adapter.hashCode()));
        // log at end so that if toString needs adapters they're in maps.
        if (adapter.getResolveState().isResolved()) {
        	if (LOG.isDebugEnabled()) {
        		LOG.debug("add " + new ToString(pojo) + " as " + adapter);
        	}
        }
    }

    public void remove(final ObjectAdapter object) {
        LOG.debug("remove adapater: " + object);
        adapterByPojoMap.remove(key(object.getObject()));
    }

    // ///////////////////////////////////////////////////////////////////////////
    // contains
    // ///////////////////////////////////////////////////////////////////////////

    public boolean containsPojo(final Object pojo) {
        return adapterByPojoMap.containsKey(key(pojo));
    }

    // ///////////////////////////////////////////////////////////////////////////
    // get
    // ///////////////////////////////////////////////////////////////////////////

    public ObjectAdapter getAdapter(final Object pojo) {
        return adapterByPojoMap.get(key(pojo));
    }

    // ///////////////////////////////////////////////////////////////////////////
    // elements
    // ///////////////////////////////////////////////////////////////////////////

    public Iterator<ObjectAdapter> iterator() {
        return adapterByPojoMap.values().iterator();
    }

    private Object key(Object pojo) {
        return new IdentityHashKey(pojo);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Debugging
    // ///////////////////////////////////////////////////////////////////////////

    public void debugData(final DebugString debug) {
        int count = 0;
        for (final Object pojo : adapterByPojoMap.keySet()) {
            final ObjectAdapter object = adapterByPojoMap.get(pojo);
            debug.append(count++ + 1, 5);
            debug.append(" '");
            debug.append(pojo.toString(), 50);
            debug.append("'    ");
            debug.appendln(object.toString());
        }
    }

    public String debugTitle() {
        return "POJO Adapter Hashtable";
    }
}
