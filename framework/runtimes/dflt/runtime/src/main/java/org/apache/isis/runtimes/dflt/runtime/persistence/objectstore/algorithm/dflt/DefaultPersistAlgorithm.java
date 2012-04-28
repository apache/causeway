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

package org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.algorithm.dflt;

import java.util.List;

import org.apache.log4j.Logger;

import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacetUtils;
import org.apache.isis.core.metamodel.facets.object.callbacks.CallbackUtils;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistingCallbackFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.algorithm.PersistAlgorithmAbstract;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.algorithm.ToPersistObjectSet;
import org.apache.isis.runtimes.dflt.runtime.transaction.ObjectPersistenceException;

public class DefaultPersistAlgorithm extends PersistAlgorithmAbstract {
    private static final Logger LOG = Logger.getLogger(DefaultPersistAlgorithm.class);

    @Override
    public String name() {
        return "Simple Bottom Up Persistence Walker";
    }

    @Override
    public void makePersistent(final ObjectAdapter adapter, final ToPersistObjectSet toPersistObjectSet) {
        if (adapter.getSpecification().isParentedOrFreeCollection()) {
            if(LOG.isDebugEnabled()) {
                LOG.debug("persist " + adapter);
            }
            if (adapter.getResolveState() == ResolveState.GHOST) {
                adapter.changeState(ResolveState.RESOLVING);
                adapter.changeState(ResolveState.RESOLVED);
            } else if (adapter.getResolveState() == ResolveState.TRANSIENT) {
                adapter.changeState(ResolveState.RESOLVED);
            }
            final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(adapter);
            for (final ObjectAdapter element : facet.iterable(adapter)) {
                persist(element, toPersistObjectSet);
            }
        } else {
            assertObjectNotPersistentAndPersistable(adapter);
            persist(adapter, toPersistObjectSet);
        }
    }

    protected void persist(final ObjectAdapter object, final ToPersistObjectSet toPersistObjectSet) {
        if (alreadyPersistedOrNotPersistableOrServiceOrStandalone(object)) {
            return;
        }

        final List<ObjectAssociation> fields = object.getSpecification().getAssociations();
        if (!object.getSpecification().isEncodeable() && fields.size() > 0) {
            if(LOG.isDebugEnabled()) {
                LOG.debug("make persistent " + object);
            }
            CallbackUtils.callCallback(object, PersistingCallbackFacet.class);
            toPersistObjectSet.remapAsPersistent(object);
            object.changeState(ResolveState.SERIALIZING_RESOLVED);

            for (int i = 0; i < fields.size(); i++) {
                final ObjectAssociation field = fields.get(i);
                if (field.isNotPersisted()) {
                    continue;
                }
                if (field.isOneToManyAssociation()) {
                    final ObjectAdapter collection = field.get(object);
                    if (collection == null) {
                        throw new ObjectPersistenceException("Collection " + field.getName() + " does not exist in " + object.getSpecification().getFullIdentifier());
                    }
                    makePersistent(collection, toPersistObjectSet);
                } else {
                    final ObjectAdapter fieldValue = field.get(object);
                    if (fieldValue == null) {
                        continue;
                    }
                    persist(fieldValue, toPersistObjectSet);
                }
            }
            toPersistObjectSet.addPersistedObject(object);
            CallbackUtils.callCallback(object, PersistedCallbackFacet.class);
            object.changeState(ResolveState.SERIALIZING_RESOLVED.getEndState());
        }

    }

    @Override
    public String toString() {
        final ToString toString = new ToString(this);
        return toString.toString();
    }

}
