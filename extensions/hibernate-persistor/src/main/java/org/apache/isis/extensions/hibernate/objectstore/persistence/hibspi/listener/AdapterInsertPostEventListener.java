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


package org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.listener;

import org.apache.log4j.Logger;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.tuple.StandardProperty;
import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.extensions.hibernate.objectstore.persistence.oidgenerator.HibernateOid;
import org.apache.isis.runtime.persistence.objectstore.algorithm.ToPersistObjectSet;


public class AdapterInsertPostEventListener extends AdapterEventListenerAbstract
        implements PostInsertEventListener {

    private static final long serialVersionUID = 1L;
    private final static Logger LOG = Logger.getLogger(AdapterInsertPostEventListener.class);

    

    public void onPostInsert(final PostInsertEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("PostInsertEvent " + logString(event));
        }
        
        Object entity = event.getEntity();
        final ObjectAdapter adapter = getAdapterFor(entity);
        final HibernateOid oid = (HibernateOid) adapter.getOid();
        
        // make sure the oid is loaded with the hibernate id
        // make sure the adapter is set to resolved
        if (!oid.isTransient()) {
            throw new IsisException(
                    "Not transient: oid=" + oid + "," +
                    " resolve state=" + adapter.getResolveState() + " for " + entity);
        }

        // remap our adapters for each of the collection objects
        // that hibernate injects.
        replaceCollections(adapter, event);

        // in case id is a property of the object, and hence not set using OidAccessor
        oid.setHibernateId(event.getId());
        oid.makePersistent();

        // REVIEW: is this the place to make the object persistent?
        // is it not already done in the PersistAlgorithm?
        if (getPersistenceSession() instanceof ToPersistObjectSet) {
            // should be true (it is the ProxyPersistor that doesn't implement this...)
            ToPersistObjectSet persistedObjectAdder = (ToPersistObjectSet) getPersistenceSession();
            persistedObjectAdder.remapAsPersistent(adapter);
        }
        
        clearDirtyFor(adapter);
    }

    
    
    /////////////////////////////////////////////////////////
    // Helpers
    /////////////////////////////////////////////////////////

    /**
     * Updates the {@link ObjectAdapter adapter} that wraps all collections with the
     * collection possibly injected by Hibernate.
     */
    private void replaceCollections(final ObjectAdapter parent, final PostInsertEvent event) {
        final ObjectAssociation[] nofAssociations = parent.getSpecification().getAssociations();
        final StandardProperty[] hibProperties = event.getPersister().getEntityMetamodel().getProperties();
        Object[] hibCollections = event.getState();

        for (int i = 0; i < nofAssociations.length; i++) {
            if (!nofAssociations[i].isOneToManyAssociation()) {
                continue;
            }
            final String nofCollectionId = nofAssociations[i].getId();
            replaceCollection(parent, hibProperties, hibCollections, nofCollectionId);
        }
    }

    /**
     * Updates the {@link ObjectAdapter adapter} for the specified collection with the
     * collection possibly injected by Hibernate.
     */
    private void replaceCollection(
            final ObjectAdapter parent,
            final StandardProperty[] hibProperties,
            final Object[] hibPropertyObjects,
            final String nofCollectionId) {
        
        Oid parentOid = parent.getOid();
        
        for (int j = 0; j < hibProperties.length; j++) {
            String hibPropertyName = hibProperties[j].getName();
            if (!hibPropertyName.equals(nofCollectionId)) {
                continue;
            }
            
            Object hibCollectionObject = hibPropertyObjects[j];
            final Oid collectionOid = new AggregatedOid(parentOid, nofCollectionId);
            final ObjectAdapter collectionAdapter = getAdapterFor(collectionOid);
            collectionAdapter.replacePojo(hibCollectionObject);
            break;
        }
    }
    



    /////////////////////////////////////////////////////////
    // Helpers (for logging)
    /////////////////////////////////////////////////////////

    private String logString(final PostInsertEvent event) {
        return event.getEntity().getClass() + " " + event.getId();
    }

}
