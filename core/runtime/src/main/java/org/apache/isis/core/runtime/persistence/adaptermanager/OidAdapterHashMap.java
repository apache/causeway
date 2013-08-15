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
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

/**
 * A map of the objects' identities and the adapters' of the objects.
 */
public class OidAdapterHashMap implements DebuggableWithTitle, Iterable<Oid>, SessionScopedComponent, Resettable {

    private static final Logger LOG = LoggerFactory.getLogger(OidAdapterHashMap.class);
    public static final int DEFAULT_OID_ADAPTER_MAP_SIZE = 100;

    private final Map<Oid, ObjectAdapter> adapterByOidMap = Maps.newHashMapWithExpectedSize(DEFAULT_OID_ADAPTER_MAP_SIZE);


    // ///////////////////////////////////////////////////////
    // open, close
    // ///////////////////////////////////////////////////////

    @Override
    public void open() {
        // nothing to do
    }

    @Override
    public void close() {
        LOG.debug("close");
        adapterByOidMap.clear();
    }

    // ///////////////////////////////////////////////////////
    // reset
    // ///////////////////////////////////////////////////////

    /**
     * Removes all {@link ObjectSpecification#isService() non-service} adapters.
     */
    @Override
    public void reset() {
        LOG.debug("reset");
        for (final Iterator<Map.Entry<Oid, ObjectAdapter>> iterator = adapterByOidMap.entrySet().iterator(); iterator.hasNext();) {
            final Map.Entry<Oid, ObjectAdapter> entry = iterator.next();
            final ObjectAdapter adapter = entry.getValue();
            if (!adapter.getSpecification().isService()) {
                iterator.remove();
            }
        }
    }

    // ///////////////////////////////////////////////////////
    // add, remove
    // ///////////////////////////////////////////////////////

    /**
     * Add an adapter for a given oid
     */
    public void add(final Oid oid, final ObjectAdapter adapter) {

        adapterByOidMap.put(oid, adapter);
        // log at end so that if toString needs adapters they're in maps.
        if (LOG.isDebugEnabled()) {
            // do not call toString() on adapter because would call hashCode on
            // the pojo,
            // which for Hibernate PersistentCollections would trigger a
            // resolve.
            LOG.debug("add oid: " + oid + " ; oid.hashCode: + #" + Long.toHexString(oid.hashCode()) + " ; adapter.hashCode(): #" + Long.toHexString(adapter.hashCode()));
        }
    }

    /**
     * Remove the adapter for the given oid
     * 
     * @return <tt>true</tt> if an adapter was removed.
     */
    public boolean remove(final Oid oid) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("remove oid: " + oid);
        }
        return adapterByOidMap.remove(oid) != null;
    }


    /**
     * Get the adapter identified by the specified OID.
     */
    public ObjectAdapter getAdapter(final Oid oid) {
        return adapterByOidMap.get(oid);
    }

    // ///////////////////////////////////////////////////////
    // iterator
    // ///////////////////////////////////////////////////////

    @Override
    public Iterator<Oid> iterator() {
        return adapterByOidMap.keySet().iterator();
    }

    // ///////////////////////////////////////////////////////
    // debugging
    // ///////////////////////////////////////////////////////

    @Override
    public String debugTitle() {
        return "Identity adapter map";
    }

    @Override
    public void debugData(final DebugBuilder debug) {
        int count = 1;
        for (final Oid oid : this) {
            final ObjectAdapter adapter = getAdapter(oid);
            debug.append(count++, 5);
            debug.append(" '");
            debug.append(oid.toString(), 15);
            debug.append("'    ");
            debug.appendln(adapter != null ? adapter.toString() : "(MISSING OBJECT ?!)");
        }
    }


}
