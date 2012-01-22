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

package org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;

public class AggregateAdapters implements Iterable<ObjectAdapter> {

    private final ObjectAdapter rootAdapter;
    private final Map<OneToManyAssociation, ObjectAdapter> collectionAdapters = new LinkedHashMap<OneToManyAssociation, ObjectAdapter>();

    public AggregateAdapters(final ObjectAdapter rootAdapter) {
        Assert.assertNotNull(rootAdapter);
        this.rootAdapter = rootAdapter;
    }

    public ObjectAdapter getRootAdapter() {
        return rootAdapter;
    }

    public void addCollectionAdapter(final OneToManyAssociation otma, final ObjectAdapter collectionAdapter) {
        Assert.assertNotNull(otma);
        Assert.assertNotNull(collectionAdapter);
        collectionAdapters.put(otma, collectionAdapter);
    }

    public Map<OneToManyAssociation, ObjectAdapter> getCollectionAdapters() {
        return Collections.unmodifiableMap(collectionAdapters);
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

    public Set<OneToManyAssociation> getCollections() {
        return getCollectionAdapters().keySet();
    }

    public ObjectAdapter getCollectionAdapter(final OneToManyAssociation otma) {
        return collectionAdapters.get(otma);
    }
}