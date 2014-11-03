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

package org.apache.isis.core.runtime.persistence.objectstore.algorithm;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacetUtils;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.persistence.ObjectPersistenceException;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;

class PersistAlgorithmDefault extends PersistAlgorithmAbstract {
    private static final Logger LOG = LoggerFactory.getLogger(PersistAlgorithmDefault.class);

    @Override
    public String name() {
        return "Simple Bottom Up Persistence Walker";
    }

    @Override
    public void makePersistent(final ObjectAdapter adapter, final PersistenceSession toPersistObjectSet) {
        if (adapter.getSpecification().isParentedOrFreeCollection()) {
            if(LOG.isDebugEnabled()) {
                LOG.debug("persist " + adapter);
            }
            if (adapter.isGhost()) {
                adapter.changeState(ResolveState.RESOLVING);
                adapter.changeState(ResolveState.RESOLVED);
            } else if (adapter.isTransient()) {
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

    protected void persist(final ObjectAdapter adapter, final PersistenceSession toPersistObjectSet) {
        if (alreadyPersistedOrNotPersistableOrServiceOrStandalone(adapter)) {
            return;
        }

        final List<ObjectAssociation> associations = adapter.getSpecification().getAssociations(Contributed.EXCLUDED);
        if (!adapter.getSpecification().isEncodeable() && associations.size() > 0) {
            if(LOG.isDebugEnabled()) {
                LOG.debug("make persistent " + adapter);
            }

            // this is now a responsibility of the objectstore
            // CallbackUtils.callCallback(adapter, PersistingCallbackFacet.class);

            toPersistObjectSet.remapAsPersistent(adapter);
            
            // was previously to SERIALIZING_RESOLVED, but 
            // after refactoring simplifications this is now equivalent to UPDATING
            final ResolveState stateWhilePersisting = ResolveState.UPDATING;
            
            adapter.changeState(stateWhilePersisting);  

            for (int i = 0; i < associations.size(); i++) {
                final ObjectAssociation objectAssoc = associations.get(i);
                if (objectAssoc.isNotPersisted()) {
                    continue;
                }
                if (objectAssoc.isOneToManyAssociation()) {
                    final ObjectAdapter collection = objectAssoc.get(adapter);
                    if (collection == null) {
                        throw new ObjectPersistenceException("Collection " + objectAssoc.getName() + " does not exist in " + adapter.getSpecification().getFullIdentifier());
                    }
                    makePersistent(collection, toPersistObjectSet);
                } else {
                    final ObjectAdapter fieldValue = objectAssoc.get(adapter);
                    if (fieldValue == null) {
                        continue;
                    }
                    persist(fieldValue, toPersistObjectSet);
                }
            }
            toPersistObjectSet.addCreateObjectCommand(adapter);

            // this is now a responsibility of the objectstore
            // CallbackFacet.Util.callCallback(adapter, PersistedCallbackFacet.class);
            
            adapter.changeState(stateWhilePersisting.getEndState());
        }

    }

    @Override
    public String toString() {
        final ToString toString = new ToString(this);
        return toString.toString();
    }

}
