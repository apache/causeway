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

package org.apache.isis.core.runtime.persistence.adaptermanager;

import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.components.Resettable;
import org.apache.isis.core.commons.components.SessionScopedComponent;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.runtime.services.RequestScopedService;

public class PojoAdapterHashMap implements DebuggableWithTitle, Iterable<ObjectAdapter>, SessionScopedComponent, Resettable {

    private static class IdentityHashKey {
        private final Object pojo;

        public IdentityHashKey(final Object pojo) {
            this.pojo = pojo;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(pojo);
        }

        @Override
        public boolean equals(final Object obj) {
            return obj == this || (obj instanceof IdentityHashKey && ((IdentityHashKey) obj).pojo == pojo);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(PojoAdapterHashMap.class);
    public static final int DEFAULT_POJO_ADAPTER_MAP_SIZE = OidAdapterHashMap.DEFAULT_OID_ADAPTER_MAP_SIZE;

    protected final Map<Object, ObjectAdapter> adapterByPojoMap;

    // ///////////////////////////////////////////////////////////////////////////
    // Constructors, finalize
    // ///////////////////////////////////////////////////////////////////////////

    public PojoAdapterHashMap() {
        this(DEFAULT_POJO_ADAPTER_MAP_SIZE);
    }

    public PojoAdapterHashMap(final int capacity) {
        adapterByPojoMap = Maps.newHashMapWithExpectedSize(capacity);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        LOG.debug("finalizing hash of pojos");
    }

    // ///////////////////////////////////////////////////////////////////////////
    // open, close
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public void open() {
        // nothing to do
    }

    @Override
    public void close() {
        LOG.debug("close");
        adapterByPojoMap.clear();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // reset
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public void reset() {
        LOG.debug("reset");
        for (final Iterator<Map.Entry<Object, ObjectAdapter>> iterator = adapterByPojoMap.entrySet().iterator(); iterator.hasNext();) {
            final Map.Entry<Object, ObjectAdapter> entry = iterator.next();
            final ObjectAdapter adapter = entry.getValue();
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

        if(LOG.isDebugEnabled()) {
            LOG.debug("add adapter: #" + key(pojo) + " -> #" + Long.toHexString(adapter.hashCode()));

            if (adapter.isResolved()) {
                if (pojo instanceof RequestScopedService) {
                    // avoid touching the service
                } else {
                    LOG.debug("add " + new ToString(pojo) + " as " + adapter);
                }
            }
        }

    }

    public void remove(final ObjectAdapter object) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("remove adapter: " + object);
        }
        adapterByPojoMap.remove(key(object.getObject()));
    }

    // ///////////////////////////////////////////////////////////////////////////
    // get, contains
    // ///////////////////////////////////////////////////////////////////////////

    public boolean containsPojo(final Object pojo) {
        return adapterByPojoMap.containsKey(key(pojo));
    }

    public ObjectAdapter getAdapter(final Object pojo) {
        return adapterByPojoMap.get(key(pojo));
    }

    // ///////////////////////////////////////////////////////////////////////////
    // elements
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public Iterator<ObjectAdapter> iterator() {
        return adapterByPojoMap.values().iterator();
    }

    private Object key(final Object pojo) {
        return new IdentityHashKey(pojo);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Debugging
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public void debugData(final DebugBuilder debug) {
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

    @Override
    public String debugTitle() {
        return "POJO Adapter Hashtable";
    }
}
