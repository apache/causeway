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

package org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.algorithm.twopass;

import java.util.Enumeration;
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
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.algorithm.PersistAlgorithmAbstract;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.algorithm.ToPersistObjectSet;

public class TwoPassPersistAlgorithm extends PersistAlgorithmAbstract {
    private static final Logger LOG = Logger.getLogger(TwoPassPersistAlgorithm.class);

    @Override
    public String name() {
        return "Two pass,  bottom up persistence walker";
    }

    @Override
    public void makePersistent(final ObjectAdapter object, final ToPersistObjectSet toPersistObjectSet) {
        if (object.getSpecification().isParentedOrFreeCollection()) {
            makeCollectionPersistent(object, toPersistObjectSet);
        } else {
            makeObjectPersistent(object, toPersistObjectSet);
        }
    }

    private void makeObjectPersistent(final ObjectAdapter object, final ToPersistObjectSet toPersistObjectSet) {
        if (alreadyPersistedOrNotPersistableOrServiceOrStandalone(object)) {
            return;
        }
        final List<ObjectAssociation> fields = object.getSpecification().getAssociations();
        if (!object.getSpecification().isEncodeable() && fields.size() > 0) {
            LOG.info("persist " + object);
            CallbackUtils.callCallback(object, PersistingCallbackFacet.class);
            toPersistObjectSet.remapAsPersistent(object);

            for (int i = 0; i < fields.size(); i++) {
                final ObjectAssociation field = fields.get(i);
                if (field.isNotPersisted()) {
                    continue;
                } else if (field.isOneToManyAssociation()) {
                    // skip in first pass
                    continue;
                } else {
                    final ObjectAdapter fieldValue = field.get(object);
                    if (fieldValue == null) {
                        continue;
                    }
                    makePersistent(fieldValue, toPersistObjectSet);
                }
            }

            for (int i = 0; i < fields.size(); i++) {
                final ObjectAssociation field = fields.get(i);
                if (field.isNotPersisted()) {
                    continue;
                } else if (field instanceof OneToManyAssociation) {
                    final ObjectAdapter collection = field.get(object);
                    makeCollectionPersistent(collection, toPersistObjectSet);
                    /**
                     * if (collection == null) { throw new
                     * ObjectPersistenceException("Collection " +
                     * field.getName() + " does not exist in " +
                     * object.getSpecification().getFullName()); }
                     * makePersistent(collection, toPersistObjectSet); final
                     * CollectionFacet facet =
                     * CollectionFacetUtils.getCollectionFacetFromSpec
                     * (collection); final Enumeration elements =
                     * facet.elements(collection); while
                     * (elements.hasMoreElements()) {
                     * makePersistent((ObjectAdapter) elements.nextElement(),
                     * toPersistObjectSet); }
                     */
                } else {
                    // skip in second pass
                    continue;
                }
            }

            toPersistObjectSet.addCreateObjectCommand(object);
            CallbackUtils.callCallback(object, PersistedCallbackFacet.class);
        }
    }

    private void makeCollectionPersistent(final ObjectAdapter collection, final ToPersistObjectSet toPersistObjectSet) {
        LOG.info("persist " + collection);
        if (collection.representsTransient()) {
            collection.changeState(ResolveState.RESOLVED);
        }
        final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(collection);
        final Enumeration elements = facet.elements(collection);
        while (elements.hasMoreElements()) {
            makePersistent((ObjectAdapter) elements.nextElement(), toPersistObjectSet);
        }
    }

    @Override
    public String toString() {
        final ToString toString = new ToString(this);
        return toString.toString();
    }

}
