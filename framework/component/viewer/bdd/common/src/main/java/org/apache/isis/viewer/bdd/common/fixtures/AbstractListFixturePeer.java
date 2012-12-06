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
package org.apache.isis.viewer.bdd.common.fixtures;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Iterables;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioValueException;

public class AbstractListFixturePeer extends AbstractFixturePeer {

    private final String listAlias;

    /**
     * @see #collectionAdapters()
     */
    private List<ObjectAdapter> objects;

    public AbstractListFixturePeer(final AliasRegistry aliasesRegistry, final String listAlias, final CellBinding... cellBindings) {
        super(aliasesRegistry, cellBindings);

        this.listAlias = listAlias;
    }

    protected boolean isValidListAlias() {
        return getListAdapter() != null && isList();
    }

    protected ObjectAdapter getListAdapter() {
        return getAliasRegistry().getAliased(listAlias);
    }

    public void assertIsList() throws ScenarioValueException {
        if (!(getListAdapter() != null)) {
            throw new ScenarioValueException("no such alias");
        }
        if (!isList()) {
            throw new ScenarioValueException("not a list");
        }
    }

    public boolean isList() {
        return getCollectionFacet() != null;
    }

    /**
     * Lazily populated, and populated only once.
     */
    protected List<ObjectAdapter> collectionAdapters() {
        if (objects == null) {
            objects = new ArrayList<ObjectAdapter>();
            Iterables.addAll(objects, collectionContents());
        }
        return objects;
    }

    private Iterable<ObjectAdapter> collectionContents() {
        return getCollectionFacet().iterable(getListAdapter());
    }

    private CollectionFacet getCollectionFacet() {
        return getListAdapter() != null ? getListAdapter().getSpecification().getFacet(CollectionFacet.class) : null;
    }

}
