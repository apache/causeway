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

import java.util.Map;
import java.util.stream.Stream;

import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.ParentedCollectionOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;

/**
 * A root {@link ObjectAdapter adapter} along with aggregated {@link ObjectAdapter adapters}
 * for any of its {@link OneToManyAssociation collection}s that are currently present in
 * the {@link ObjectAdapterContext}s map.
 *
 * <p>
 * Used for &quot;impact analysis&quot; when persisting transient root objects; all aggregated adapters
 * must also be persisted.
 */
final class RootAndCollectionAdapters {
    
    private final ObjectAdapterContext context;
    private final ObjectAdapter parentAdapter;
    private final RootOid rootAdapterOid;

    private final Map<OneToManyAssociation, ObjectAdapter> collectionAdapters = _Maps.newLinkedHashMap();

    public RootAndCollectionAdapters(
            final ObjectAdapter parentAdapter,
            final ObjectAdapterContext context) {
        Assert.assertNotNull(parentAdapter);
        this.rootAdapterOid = (RootOid) parentAdapter.getOid();
        this.parentAdapter = parentAdapter;
        this.context = context;
        addCollectionAdapters();
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
    public Stream<ObjectAdapter> stream() {
        return collectionAdapters.values().stream();
    }

    /**
     * Which collections are present?
     * @return
     */
    public Stream<OneToManyAssociation> streamCollections() {
        return collectionAdapters.keySet().stream();
    }

    /**
     * Corresponding adapter for each collection (values).
     *
     * @see #streamCollections()
     */
    public ObjectAdapter getCollectionAdapter(final OneToManyAssociation otma) {
        return collectionAdapters.get(otma);
    }


    ////////////////////////////////////////////////////////////////////////
    // Helpers
    ////////////////////////////////////////////////////////////////////////

    private void addCollectionAdapters() {
        for (final OneToManyAssociation otma : parentAdapter.getSpecification().getCollections(Contributed.EXCLUDED)) {
            final ParentedCollectionOid collectionOid = new ParentedCollectionOid((RootOid) rootAdapterOid, otma);
            final ObjectAdapter collectionAdapter = null;//FIXME context.lookupParentedCollectionAdapter(collectionOid);
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


}