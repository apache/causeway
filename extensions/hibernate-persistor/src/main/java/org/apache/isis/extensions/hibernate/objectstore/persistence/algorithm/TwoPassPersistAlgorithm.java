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


package org.apache.isis.extensions.hibernate.objectstore.persistence.algorithm;

import org.apache.log4j.Logger;
import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.commons.lang.ToString;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ResolveState;
import org.apache.isis.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.metamodel.facets.object.callbacks.PersistedCallbackFacet;
import org.apache.isis.metamodel.facets.object.callbacks.PersistingCallbackFacet;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.metamodel.util.CallbackUtils;
import org.apache.isis.metamodel.util.CollectionFacetUtils;
import org.apache.isis.runtime.persistence.PersistenceSession;
import org.apache.isis.runtime.persistence.objectstore.algorithm.PersistAlgorithmAbstract;
import org.apache.isis.runtime.persistence.objectstore.algorithm.ToPersistObjectSet;


/**
 * Implements persistence-by-reachability, explicitly walks the graph and
 * persisting first 1:1 associations and then 1:m associations.
 * 
 * <p>
 * This is an alternative to the {@link SimplePersistAlgorithm} that simply relies
 * on Hibernate to do its thang using its <tt>cascade</tt> setting.
 */
public class TwoPassPersistAlgorithm extends PersistAlgorithmAbstract {
    
    private static final Logger LOG = Logger.getLogger(TwoPassPersistAlgorithm.class);



    //////////////////////////////////////////////////////////////////
    // name
    //////////////////////////////////////////////////////////////////

    public String name() {
        return "Two pass,  bottom up persistence walker";
    }


    //////////////////////////////////////////////////////////////////
    // makePersistent
    //////////////////////////////////////////////////////////////////

    /**
     * @param persistedObjectAdder - will actually be implemented by {@link PersistenceSession}
     */
    public void makePersistent(final ObjectAdapter object, final ToPersistObjectSet toPersistObjectSet) {
        if (object.getSpecification().isCollection()) {
            makeCollectionPersistent(object, toPersistObjectSet);
        } else {
            makeObjectPersistent(object, toPersistObjectSet);
        }
    }

    private void makeObjectPersistent(final ObjectAdapter adapter, final ToPersistObjectSet toPersistObjectSet) {
        if (alreadyPersistedOrNotPersistableOrServiceOrStandalone(adapter)) {
            return;
        }

        if (adapter.isPersistent()) {
            return;
        }

        LOG.info("persist " + adapter);

        // this done elsewhere, I think... 
        // (... see similar commenting out in SimpleStrategy)
        // Isis.getObjectLoader().madePersistent(object);
        
        CallbackUtils.callCallback(adapter, PersistingCallbackFacet.class);

        // set state here so we don't loop round again to save the same object
        adapter.changeState(ResolveState.RESOLVED);
        
        // TODO resolved state needs looking at
        adapter.changeState(ResolveState.UPDATING);
        toPersistObjectSet.addPersistedObject(adapter);

        final ObjectAssociation[] fields = adapter.getSpecification().getAssociations();
        for (int i = 0; i < fields.length; i++) {
            final ObjectAssociation association = fields[i];
            if (association.isNotPersisted()) {
                continue;
            }
            
            if (association.isOneToManyAssociation()) {
                // skip in first pass
            } else {
                processOneToOneAssociation(adapter, toPersistObjectSet, association);
            }
        }

        for (int i = 0; i < fields.length; i++) {
            final ObjectAssociation field = fields[i];
            if (field.isNotPersisted()) {
                continue;
            }
            
            if (field.isOneToManyAssociation()) {
                processOneToManyAssociation(adapter, toPersistObjectSet, field);
            } else {
                // 1:1 association, skip in second pass
            }
        }

        CallbackUtils.callCallback(adapter, PersistedCallbackFacet.class);
    }

    private void processOneToOneAssociation(
            final ObjectAdapter object,
            final ToPersistObjectSet toPersistObjectSet,
            final ObjectAssociation association) {
        final ObjectAdapter fieldValue = association.get(object);
        if (fieldValue != null) {
            if (!(fieldValue instanceof ObjectAdapter)) {
                throw new IsisException();
            }
            makePersistent(fieldValue, toPersistObjectSet);
        }
    }

    /**
     * Walks the graph.
     */
    private void processOneToManyAssociation(
            final ObjectAdapter object,
            final ToPersistObjectSet toPersistObjectSet,
            final ObjectAssociation field) {
        final ObjectAdapter collection = field.get(object);
        makePersistent(collection, toPersistObjectSet);
        final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(collection);
        for(ObjectAdapter adapter: facet.iterable(collection)) {
            makePersistent(adapter, toPersistObjectSet);
        }
    }


    private void makeCollectionPersistent(final ObjectAdapter collection, final ToPersistObjectSet toPersistObjectSet) {
        if (alreadyPersistedOrNotPersistable(collection)) {
            return;
        }
        LOG.info("persist " + collection);
        if (collection.getResolveState() == ResolveState.TRANSIENT) {
            collection.changeState(ResolveState.RESOLVED);
        }
        toPersistObjectSet.remapAsPersistent(collection);
        final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(collection);
        for(ObjectAdapter adapter: facet.iterable(collection)) {
            makePersistent(adapter, toPersistObjectSet);
        }
    }

    //////////////////////////////////////////////////////////////////
    // toString
    //////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        final ToString toString = new ToString(this);
        return toString.toString();
    }

}
