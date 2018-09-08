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
package org.apache.isis.core.runtime.system.persistence;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.concurrency.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.runtime.persistence.ObjectNotFoundException;
import org.apache.isis.core.runtime.persistence.PojoRecreationException;
import org.apache.isis.core.runtime.system.persistence.adaptermanager.ObjectAdapterContext;

class PersistenceSession5_Decouple  {

    private static final Logger LOG = LoggerFactory.getLogger(PersistenceSession5_Decouple.class);
    private final PersistenceSession5 holder;
    private final ObjectAdapterContext objectAdapterContext;
    private final AuthenticationSession authenticationSession;
    private final boolean concurrencyCheckingGloballyEnabled;

    protected PersistenceSession5_Decouple(PersistenceSession5 holder, ObjectAdapterContext objectAdapterContext) {
        this.holder = holder;
        this.objectAdapterContext = objectAdapterContext;
        this.authenticationSession = holder.getAuthenticationSession();
        this.concurrencyCheckingGloballyEnabled = !ConcurrencyChecking.isGloballyDisabled(holder.getConfiguration());
    }

    /**
     * Either returns an existing {@link ObjectAdapter adapter} (as per
     * {@link #lookupAdapterFor(Oid)}), otherwise re-creates an adapter with the
     * specified (persistent) {@link Oid}.
     *
     * <p>
     * Typically called when the {@link Oid} is already known, that is, when
     * resolving an already-persisted object. Is also available for
     * <tt>Memento</tt> support however, so {@link Oid} could also represent a
     * {@link Oid#isTransient() transient} object.
     *
     * <p>
     * The pojo itself is recreated by delegating to a {@link AdapterManager}.
     *
     * <p>
     * The {@link ConcurrencyChecking} parameter determines whether concurrency checking is performed.
     * If it is requested, then a check is made to ensure that the {@link Oid#getVersion() version}
     * of the {@link RootOid oid} of the recreated adapter is the same as that of the provided {@link RootOid oid}.
     * If the version differs, then a {@link ConcurrencyException} is thrown.
     *
     * <p>
     * ALSO, even if a {@link ConcurrencyException}, then the provided {@link RootOid oid}'s {@link Version version}
     * will be {@link RootOid#setVersion(Version) set} to the current
     * value.  This allows the client to retry if they wish.
     *
     * @throws {@link org.apache.isis.core.runtime.persistence.ObjectNotFoundException} if the object does not exist.
     */
    public ObjectAdapter adapterFor(
            final RootOid rootOid,
            final ConcurrencyChecking concurrencyChecking) {
        
        /* FIXME[ISIS-1976] guard against service lookup
         * https://github.com/apache/isis/pull/121#discussion_r215889748
         * 
         * Eventually I'm hoping that this code will simplify and then become pluggable.
         * Assuming that we stop supporting transient pojos, instead this code could
         * iterate over a set of "PersistenceProviders", following the chain of
         * responsibility pattern, where the first PersistenceProvider that recognises
         * the format of the rootOid then takes responsibility for retrieving the
         * corresponding pojo from its persistence store.
         * 
         * In the case of a PersistenceProvider for DN, that means a query to the DB. In
         * the case of a PersistenceProvider for view models, it means unmarshalling the
         * state from the oid into the pojo. (fyi, there's also the optional SPI
         * service, UrlEncodingService or something like, whereby the root oid is a key
         * into some other datastore. So really my "PersistenceProvider" is a
         * generalization of that concept).
         */

        final ObjectAdapter serviceAdapter = objectAdapterContext.lookupServiceAdapterFor(rootOid);
        if (serviceAdapter != null) {
            _Exceptions.unexpectedCodeReach();
            return serviceAdapter;
        }
        
        // attempt to locate adapter for the Oid
        ObjectAdapter adapter = objectAdapterContext.lookupAdapterFor(rootOid);
        if (adapter == null) {
            // else recreate
            try {
                final Object pojo;
                if(rootOid.isTransient() || rootOid.isViewModel()) {
                    pojo = holder.recreatePojoTransientOrViewModel(rootOid);
                } else {
                    pojo = holder.loadPersistentPojo(rootOid);
                }
                adapter = objectAdapterContext.addRecreatedPojoToCache(rootOid, pojo);
            } catch(ObjectNotFoundException ex) {
                throw ex; // just rethrow
            } catch(RuntimeException ex) {
                
                //FIXME[ISIS-1976] remove
                System.err.println("------------------------------------------");
                System.err.println("rootOid: "+rootOid.enString());
                
                ex.printStackTrace();
                System.err.println("------------------------------------------");
                //--

                
                throw new PojoRecreationException(rootOid, ex);
            }
        }

        // sync versions of original, with concurrency checking if required
        syncVersion(concurrencyChecking, adapter, rootOid);

        return adapter;

    }
    
    protected Map<RootOid,ObjectAdapter> adaptersFor(
            final List<RootOid> rootOids,
            final ConcurrencyChecking concurrencyChecking) {

        final Map<RootOid, ObjectAdapter> adapterByOid = _Maps.newLinkedHashMap();

        List<RootOid> notYetLoadedOids = _Lists.newArrayList();
        for (RootOid rootOid : rootOids) {
            // attempt to locate adapter for the Oid
            ObjectAdapter adapter = objectAdapterContext.lookupAdapterFor(rootOid);
            // handle view models or transient
            if (adapter == null) {
                if (rootOid.isTransient() || rootOid.isViewModel()) {
                    final Object pojo = holder.recreatePojoTransientOrViewModel(rootOid);
                    adapter = objectAdapterContext.addRecreatedPojoToCache(rootOid, pojo);
                    syncVersion(concurrencyChecking, adapter, rootOid);
                }
            }
            if (adapter != null) {
                adapterByOid.put(rootOid, adapter);
            } else {
                // persistent oid, to load in bulk
                notYetLoadedOids.add(rootOid);
            }
        }

        // recreate, in bulk, all those not yet loaded
        final Map<RootOid, Object> pojoByOid = holder.loadPersistentPojos(notYetLoadedOids);
        for (Map.Entry<RootOid, Object> entry : pojoByOid.entrySet()) {
            final RootOid rootOid = entry.getKey();
            final Object pojo = entry.getValue();
            if(pojo != null) {
                ObjectAdapter adapter;
                try {
                    adapter = objectAdapterContext.addRecreatedPojoToCache(rootOid, pojo);
                    adapterByOid.put(rootOid, adapter);
                } catch(ObjectNotFoundException ex) {
                    throw ex; // just rethrow
                } catch(RuntimeException ex) {
                    throw new PojoRecreationException(rootOid, ex);
                }
                syncVersion(concurrencyChecking, adapter, rootOid);
            } else {
                // null indicates it couldn't be loaded
                // do nothing here...
            }
        }

        return adapterByOid;
    }

    private void syncVersion(
            final ConcurrencyChecking concurrencyChecking,
            final ObjectAdapter adapter, final RootOid rootOid) {
        // sync versions of original, with concurrency checking if required
        Oid adapterOid = adapter.getOid();
        if(adapterOid instanceof RootOid) {
            final RootOid recreatedOid = (RootOid) adapterOid;
            final RootOid originalOid = rootOid;

            try {
                if(concurrencyChecking.isChecking()) {

                    // check for exception, but don't throw if suppressed through thread-local
                    final Version otherVersion = originalOid.getVersion();
                    final Version thisVersion = recreatedOid.getVersion();
                    if( thisVersion != null &&
                            otherVersion != null &&
                            thisVersion.different(otherVersion)) {

                        if(concurrencyCheckingGloballyEnabled && ConcurrencyChecking.isCurrentlyEnabled()) {
                            LOG.info("concurrency conflict detected on {} ({})", recreatedOid, otherVersion);
                            final String currentUser = authenticationSession.getUserName();
                            throw new ConcurrencyException(currentUser, recreatedOid, thisVersion, otherVersion);
                        } else {
                            LOG.info("concurrency conflict detected but suppressed, on {} ({})", recreatedOid, otherVersion);
                        }
                    }
                }
            } finally {
                final Version originalVersion = originalOid.getVersion();
                final Version recreatedVersion = recreatedOid.getVersion();
                if(recreatedVersion != null && (
                        originalVersion == null ||
                        recreatedVersion.different(originalVersion))
                        ) {
                    if(LOG.isDebugEnabled()) {
                        LOG.debug("updating version in oid, on {} ({}) to ({})", originalOid, originalVersion, recreatedVersion);
                    }
                    originalOid.setVersion(recreatedVersion);
                }
            }
        }
    }




}



