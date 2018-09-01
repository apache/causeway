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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class ObjectAdapterLegacy {
    
    private static final Logger LOG = LoggerFactory.getLogger(ObjectAdapterLegacy.class);
    
    public static ObjectAdapterContext openContext(ServicesInjector servicesInjector) {
        final ObjectAdapterContext objectAdapterContext = new ObjectAdapterContext(servicesInjector);
        objectAdapterContext.open();
        return objectAdapterContext;
    }
    
    public static class ObjectAdapterContext /*implements AdapterManager*/ {
        private final PojoAdapterHashMap pojoAdapterMap = new PojoAdapterHashMap();
        private final OidAdapterHashMap oidAdapterMap = new OidAdapterHashMap();
        private final ServicesInjector servicesInjector;
        
        private ObjectAdapterContext(ServicesInjector servicesInjector) {
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
                LOG.error("close: oidAdapterMap#close() failed; continuing to avoid memory leakage");
            }

            try {
                pojoAdapterMap.close();
            } catch(final Throwable ex) {
                // ignore
                LOG.error("close: pojoAdapterMap#close() failed; continuing to avoid memory leakage");
            }
        }

        public boolean containsPojoAdapter(Object pojo) {
            return pojoAdapterMap.containsPojo(pojo);
        }
        
        public ObjectAdapter lookupPojoAdapter(Object pojo) {
            return pojoAdapterMap.getAdapter(pojo);
        }

        public ObjectAdapter lookupOidAdapter(Oid oid) {
            return oidAdapterMap.getAdapter(oid);
        }
        
        public void removeAdapter(ObjectAdapter adapter) {
            final Oid oid = adapter.getOid();
            if (oid != null) {
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
         * because the {@link PersistenceSession5} is expected to manage this.
         *
         * @param hintRootOid - allow a different persistent root oid to be provided.
         * @param session 
         */
        public void remapAsPersistent(final ObjectAdapter adapter, RootOid hintRootOid, PersistenceSession session) {

            final ObjectAdapter rootAdapter = adapter.getAggregateRoot();  // TODO: REVIEW: think this is redundant; would seem this method is only ever called for roots anyway.
            final RootOid transientRootOid = (RootOid) rootAdapter.getOid();

            // no longer true, because isTransient now looks directly at the underlying pojo's state (for entities)
            // and doesn't apply for services.
            //        Ensure.ensureThatArg(rootAdapter.isTransient(), is(true), "root adapter should be transient; oid:" + transientRootOid);
            //        Ensure.ensureThatArg(transientRootOid.isTransient(), is(true), "root adapter's OID should be transient; oid:" + transientRootOid);

            final RootAndCollectionAdapters rootAndCollectionAdapters = new RootAndCollectionAdapters(adapter, session);

            LOG.debug("remapAsPersistent: {}", transientRootOid);
            LOG.debug("removing root adapter from oid map");

            boolean removed = oidAdapterMap.remove(transientRootOid);
            if (!removed) {
                LOG.warn("could not remove oid: {}", transientRootOid);
                // should we fail here with a more serious error?
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("removing collection adapter(s) from oid map");
            }
            for (final ObjectAdapter collectionAdapter : rootAndCollectionAdapters) {
                final Oid collectionOid = collectionAdapter.getOid();
                removed = oidAdapterMap.remove(collectionOid);
                if (!removed) {
                    LOG.warn("could not remove collectionOid: {}", collectionOid);
                    // should we fail here with a more serious error?
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("updating the Oid");
            }

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
            if (LOG.isDebugEnabled()) {
                LOG.debug("replacing Oid for root adapter and re-adding into maps; oid is now: {} (was: {})", persistedRootOid.enString(), transientRootOid.enString());
            }
            adapter.replaceOid(persistedRootOid);
            oidAdapterMap.add(persistedRootOid, adapter);

            // associate the collection adapters with new Oids, and re-map
            if (LOG.isDebugEnabled()) {
                LOG.debug("replacing Oids for collection adapter(s) and re-adding into maps");
            }
            for (final ObjectAdapter collectionAdapter : rootAndCollectionAdapters) {
                final ParentedCollectionOid previousCollectionOid = (ParentedCollectionOid) collectionAdapter.getOid();
                final ParentedCollectionOid persistedCollectionOid = previousCollectionOid.asPersistent(persistedRootOid);
                oidAdapterMap.add(persistedCollectionOid, collectionAdapter);
            }


            // some object store implementations may replace collection instances (eg ORM may replace with a cglib-enhanced
            // proxy equivalent.  So, ensure that the collection adapters still wrap the correct pojos.
            if (LOG.isDebugEnabled()) {
                LOG.debug("synchronizing collection pojos, remapping in pojo map if required");
            }
            for (final OneToManyAssociation otma : rootAndCollectionAdapters.getCollections()) {
                final ObjectAdapter collectionAdapter = rootAndCollectionAdapters.getCollectionAdapter(otma);

                final Object collectionPojoWrappedByAdapter = collectionAdapter.getObject();
                final Object collectionPojoActuallyOnPojo = getCollectionPojo(otma, adapter);

                if (collectionPojoActuallyOnPojo != collectionPojoWrappedByAdapter) {
                    pojoAdapterMap.remove(collectionAdapter);
                    collectionAdapter.replacePojo(collectionPojoActuallyOnPojo);
                    pojoAdapterMap.add(collectionPojoActuallyOnPojo, collectionAdapter);
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("made persistent {}; was {}", adapter, transientRootOid);
            }
        }
        
        private static Object getCollectionPojo(final OneToManyAssociation association, final ObjectAdapter ownerAdapter) {
            final PropertyOrCollectionAccessorFacet accessor = association.getFacet(PropertyOrCollectionAccessorFacet.class);
            return accessor.getProperty(ownerAdapter, InteractionInitiatedBy.FRAMEWORK);
        }




       
    }
    


}
