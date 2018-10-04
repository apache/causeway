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

package org.apache.isis.core.metamodel.facets.collections.collection.notpersisted;

import java.util.List;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.MementoSerialization;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.propcoll.notpersisted.NotPersistedFacet;
import org.apache.isis.core.metamodel.facets.propcoll.notpersisted.NotPersistedFacetAbstract;

public class NotPersistedFacetForCollectionAnnotation extends NotPersistedFacetAbstract {

    public NotPersistedFacetForCollectionAnnotation(final FacetHolder holder) {
        super(holder);
    }

    public static NotPersistedFacet create(
            final List<Collection> collections,
            final FacetHolder holder) {

        return collections.stream()
                .map(Collection::mementoSerialization)
                .filter(mementoSerialization -> mementoSerialization != MementoSerialization.NOT_SPECIFIED)
                .findFirst()
                .map(mementoSerialization -> {
                    switch (mementoSerialization) {
                    case INCLUDED:
                        return null;
                    case EXCLUDED:
                        return new NotPersistedFacetForCollectionAnnotation(holder);
                    default:
                    }
                    throw new IllegalStateException("mementoSerialization '" + mementoSerialization + "' not recognized");
                })
                .orElse(null);
    }
}
