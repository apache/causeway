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

package org.apache.isis.core.runtime.system.persistence.adaptermanager;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.ParentedCollectionOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;

/**
 * A root {@link ObjectAdapter adapter} along with aggregated {@link ObjectAdapter adapters}
 * for any of its {@link OneToManyAssociation collection}s that are currently present in
 * the {@link AdapterManager map}s.
 *
 * <p>
 * Used for &quot;impact analysis&quot; when persisting transient root objects; all aggregated adapters
 * must also be persisted.
 */
class RootAndCollectionAdapters implements Iterable<ObjectAdapter> {

    private final ObjectAdapter parentAdapter;
    private final RootOid rootAdapterOid;

    private final Map<OneToManyAssociation, ObjectAdapter> collectionAdapters = Maps.newLinkedHashMap();

    public RootAndCollectionAdapters(
            final ObjectAdapter parentAdapter,
            final AdapterManager adapterManager) {
        Assert.assertNotNull(parentAdapter);
        this.rootAdapterOid = (RootOid) parentAdapter.getOid();
        this.parentAdapter = parentAdapter;
        addCollectionAdapters(adapterManager);
    }

    public ObjectAdapter getRootAdapter() {
        return parentAdapter;
    }

    /**
     * Iterate over the
     * {@link #addCollectionAdapter(OneToManyAssociation, ObjectAdapter)
     * collection adapter}s (does not include the {@link #getRootAdapter() root
     * adapter}.
     */
    @Override
    public Iterator<ObjectAdapter> iterator() {
        return getCollectionAdapters().values().iterator();
    }

    /**
     * Which collections are present?
     * @return
     */
    public Set<OneToManyAssociation> getCollections() {
        return getCollectionAdapters().keySet();
    }

    /**
     * Corresponding adapter for each collection (values).
     *
     * @see #getCollections()
     */
    public ObjectAdapter getCollectionAdapter(final OneToManyAssociation otma) {
        return collectionAdapters.get(otma);
    }


    ////////////////////////////////////////////////////////////////////////
    // Helpers
    ////////////////////////////////////////////////////////////////////////

    private void addCollectionAdapters(AdapterManager objectAdapterLookup) {
        for (final OneToManyAssociation otma : parentAdapter.getSpecification().getCollections(Contributed.EXCLUDED)) {
            final ParentedCollectionOid collectionOid = new ParentedCollectionOid((RootOid) rootAdapterOid, otma);
            final ObjectAdapter collectionAdapter = objectAdapterLookup.getAdapterFor(collectionOid);
            if (collectionAdapter != null) {
                // collection adapters are lazily created and so there may not be one.
                addCollectionAdapter(otma, collectionAdapter);
            }
        }
    }

    private void addCollectionAdapter(final OneToManyAssociation otma, final ObjectAdapter collectionAdapter) {
        Assert.assertNotNull(otma);
        Assert.assertNotNull(collectionAdapter);
        collectionAdapters.put(otma, collectionAdapter);
    }

    private Map<OneToManyAssociation, ObjectAdapter> getCollectionAdapters() {
        return Collections.unmodifiableMap(collectionAdapters);
    }


}