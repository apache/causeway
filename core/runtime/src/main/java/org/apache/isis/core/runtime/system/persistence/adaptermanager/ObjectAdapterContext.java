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

import org.apache.isis.core.commons.ensure.IsisAssertException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.ParentedCollectionOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;

public class ObjectAdapterContext {
    private final PojoAdapterHashMap pojoAdapterMap = new PojoAdapterHashMap();
    private final OidAdapterHashMap oidAdapterMap = new OidAdapterHashMap();
    private final ServicesInjector servicesInjector;
    
    ObjectAdapterContext(ServicesInjector servicesInjector) {
        this.servicesInjector = servicesInjector;
    }

    // -- LIFE-CYCLING
    
    public void open() {
        oidAdapterMap.open();
        pojoAdapterMap.open();
    }
    
    public void close() {
        
        try {
            oidAdapterMap.close();
        } catch(final Throwable ex) {
            // ignore
            ObjectAdapterLegacy.LOG.error("close: oidAdapterMap#close() failed; continuing to avoid memory leakage");
        }

        try {
            pojoAdapterMap.close();
        } catch(final Throwable ex) {
            // ignore
            ObjectAdapterLegacy.LOG.error("close: pojoAdapterMap#close() failed; continuing to avoid memory leakage");
        }
    }

    public boolean containsAdapterForPojo(Object pojo) {
        return pojoAdapterMap.containsPojo(pojo);
    }
    
    public ObjectAdapter lookupAdapterByPojo(Object pojo) {
        return pojoAdapterMap.getAdapter(pojo);
    }

    public ObjectAdapter lookupAdapterById(Oid oid) {
        return oidAdapterMap.getAdapter(oid);
    }
    
    public void removeAdapter(ObjectAdapter adapter) {
        if(adapter==null) {
            return; // nothing to do
        }
        final Oid oid = adapter.getOid();
        if (oid != null) { // eg. value objects don't have an Oid
            oidAdapterMap.remove(oid);
        }
        pojoAdapterMap.remove(adapter);
    }
    
    // ------------------------------------------------------------------------------------------------
    
    public void addAdapterHonoringSpecImmutability(Object pojo, ObjectAdapter adapter) {
        // add all aggregated collections
        final ObjectSpecification objSpec = adapter.getSpecification();
        if (!adapter.isParentedCollection() || adapter.isParentedCollection() && !objSpec.isImmutable()) {
            pojoAdapterMap.add(pojo, adapter);
        }

        // order is important - add to pojo map first, then identity map
        oidAdapterMap.add(adapter.getOid(), adapter);
    }
    
    /**
     * was in PersisenceSessionX, temporarily moved here to successfully compile
     *
     * <p>
     * Note that there is no management of {@link Version}s here. That is
     * because the {@link PersistenceSession} is expected to manage this.
     *
     * @param hintRootOid - allow a different persistent root oid to be provided.
     * @param session 
     */
    public void remapAsPersistent(final ObjectAdapter adapter, RootOid hintRootOid, PersistenceSession session) {

        final ObjectAdapter rootAdapter = adapter.getAggregateRoot();  // TODO: REVIEW: think this is redundant; would seem this method is only ever called for roots anyway.
        final RootOid transientRootOid = (RootOid) rootAdapter.getOid();

        final RootAndCollectionAdapters rootAndCollectionAdapters = new RootAndCollectionAdapters(adapter, session);

        removeFromCache(rootAndCollectionAdapters, transientRootOid);
        
        // intended for testing (bit nasty)
        final RootOid persistedRootOid;
        if(hintRootOid != null) {
            if(hintRootOid.isTransient()) {
                throw new IsisAssertException("hintRootOid must be persistent");
            }
            final ObjectSpecId hintRootOidObjectSpecId = hintRootOid.getObjectSpecId();
            final ObjectSpecId adapterObjectSpecId = adapter.getSpecification().getSpecId();
            if(!hintRootOidObjectSpecId.equals(adapterObjectSpecId)) {
                throw new IsisAssertException("hintRootOid's objectType must be same as that of adapter " +
                        "(was: '" + hintRootOidObjectSpecId + "'; adapter's is " + adapterObjectSpecId + "'");
            }
            // ok
            persistedRootOid = hintRootOid;
        } else {
            // normal flow - delegate to OidGenerator to obtain a persistent root oid
            persistedRootOid = session.createPersistentOrViewModelOid(adapter.getObject());
        }

        // associate root adapter with the new Oid, and remap
        if (ObjectAdapterLegacy.LOG.isDebugEnabled()) {
            ObjectAdapterLegacy.LOG.debug("replacing root adapter and re-adding into maps; oid is now: {} (was: {})", persistedRootOid.enString(), transientRootOid.enString());
        }
        
        final ObjectAdapter adapterReplacement = adapter.withOid(persistedRootOid); 
        
        replaceRootAdapter(persistedRootOid, adapterReplacement, rootAndCollectionAdapters);
        
        if (ObjectAdapterLegacy.LOG.isDebugEnabled()) {
            ObjectAdapterLegacy.LOG.debug("made persistent {}; was {}", adapterReplacement, transientRootOid);
        }
    }

    private void replaceRootAdapter(
            final RootOid persistedRootOid, 
            final ObjectAdapter adapterReplacement, 
            final RootAndCollectionAdapters rootAndCollectionAdapters) {
        
        oidAdapterMap.add(persistedRootOid, adapterReplacement);
        pojoAdapterMap.add(adapterReplacement.getObject(), adapterReplacement);

        // associate the collection adapters with new Oids, and re-map
        ObjectAdapterLegacy.LOG.debug("replacing Oids for collection adapter(s) and re-adding into maps");
        
        for (final ObjectAdapter collectionAdapter : rootAndCollectionAdapters) {
            final ParentedCollectionOid previousCollectionOid = (ParentedCollectionOid) collectionAdapter.getOid();
            final ParentedCollectionOid persistedCollectionOid = previousCollectionOid.asPersistent(persistedRootOid);
            oidAdapterMap.add(persistedCollectionOid, collectionAdapter);
        }

        // some object store implementations may replace collection instances (eg ORM may replace with a cglib-enhanced
        // proxy equivalent.  So, ensure that the collection adapters still wrap the correct pojos.
        if (ObjectAdapterLegacy.LOG.isDebugEnabled()) {
            ObjectAdapterLegacy.LOG.debug("synchronizing collection pojos, remapping in pojo map if required");
        }
        for (final OneToManyAssociation otma : rootAndCollectionAdapters.getCollections()) {
            final ObjectAdapter collectionAdapter = rootAndCollectionAdapters.getCollectionAdapter(otma);

            final Object collectionPojoWrappedByAdapter = collectionAdapter.getObject();
            final Object collectionPojoActuallyOnPojo = getCollectionPojo(otma, adapterReplacement);

            if (collectionPojoActuallyOnPojo != collectionPojoWrappedByAdapter) {
                pojoAdapterMap.remove(collectionAdapter);
                collectionAdapter.replacePojo(collectionPojoActuallyOnPojo);
                pojoAdapterMap.add(collectionPojoActuallyOnPojo, collectionAdapter);
            }
        }
        
    }

    private void removeFromCache(
            final RootAndCollectionAdapters rootAndCollectionAdapters, 
            final RootOid transientRootOid) {
        ObjectAdapterLegacy.LOG.debug("remapAsPersistent: {}", transientRootOid);
        ObjectAdapterLegacy.LOG.debug("removing root adapter from oid map");
    
        boolean removed = oidAdapterMap.remove(transientRootOid);
        if (!removed) {
            ObjectAdapterLegacy.LOG.warn("could not remove oid: {}", transientRootOid);
            // should we fail here with a more serious error?
        }
    
        if (ObjectAdapterLegacy.LOG.isDebugEnabled()) {
            ObjectAdapterLegacy.LOG.debug("removing collection adapter(s) from oid map");
        }
        for (final ObjectAdapter collectionAdapter : rootAndCollectionAdapters) {
            final Oid collectionOid = collectionAdapter.getOid();
            removed = oidAdapterMap.remove(collectionOid);
            if (!removed) {
                ObjectAdapterLegacy.LOG.warn("could not remove collectionOid: {}", collectionOid);
                // should we fail here with a more serious error?
            }
        }
    }

    private static Object getCollectionPojo(final OneToManyAssociation association, final ObjectAdapter ownerAdapter) {
        final PropertyOrCollectionAccessorFacet accessor = association.getFacet(PropertyOrCollectionAccessorFacet.class);
        return accessor.getProperty(ownerAdapter, InteractionInitiatedBy.FRAMEWORK);
    }





}