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
package org.apache.isis.jdo.objectadapter;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ObjectAdapterByIdProvider;
import org.apache.isis.metamodel.adapter.oid.ObjectNotFoundException;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.adapter.oid.PojoRecreationException;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.metamodel.adapter.version.Version;
import org.apache.isis.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.runtime.system.context.session.RuntimeContext;
import org.apache.isis.runtime.system.persistence.PersistenceSession;
import org.apache.isis.security.authentication.AuthenticationSession;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * package private mixin for ObjectAdapterContext
 * <p>
 * Responsibility: implements ObjectAdapterByIdProvider 
 * </p> 
 * @since 2.0
 */
@Log4j2
class ObjectAdapterContext_ObjectAdapterByIdProvider implements ObjectAdapterByIdProvider {

    private final ObjectAdapterContext objectAdapterContext;
    private final PersistenceSession persistenceSession;
    private final SpecificationLoader specificationLoader;
    private final AuthenticationSession authenticationSession;

    ObjectAdapterContext_ObjectAdapterByIdProvider(
            ObjectAdapterContext objectAdapterContext,
            PersistenceSession persistenceSession, 
            RuntimeContext runtimeContext) {

        this.objectAdapterContext = objectAdapterContext;
        this.persistenceSession = persistenceSession;
        this.specificationLoader = runtimeContext.getSpecificationLoader();
        this.authenticationSession = runtimeContext.getAuthenticationSession();

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
     * The pojo itself is recreated by delegating to a FIXME:AdapterManager
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
     * @throws {@link org.apache.isis.metamodel.adapter.oid.ObjectNotFoundException} if the object does not exist.
     */
    @Override
    public ObjectAdapter adapterFor(final RootOid rootOid) {

        /* FIXME[ISIS-1976] SPI for adapterFor(RootOid)
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

        //FIXME[ISIS-1976] remove guard
        val spec = specificationLoader.loadSpecification(rootOid.getObjectSpecId());
        if(spec.isManagedBean()) {
            throw _Exceptions.unexpectedCodeReach();
        }

        final ObjectAdapter adapter;
        {
            // else recreate
            try {
                final Object pojo;
                if(rootOid.isTransient() || rootOid.isViewModel()) {
                    pojo = recreatePojoTransientOrViewModel(rootOid);
                } else {
                    pojo = persistenceSession.fetchPersistentPojo(rootOid);
                }
                adapter = objectAdapterContext.recreatePojo(rootOid, pojo);
            } catch(ObjectNotFoundException ex) {
                throw ex; // just rethrow
            } catch(RuntimeException ex) {
                throw new PojoRecreationException(rootOid, ex);
            }
        }

        // sync versions of original, with concurrency checking if required
        syncVersion(adapter, rootOid);

        return adapter;

    }

    @Override
    public Map<RootOid,ObjectAdapter> adaptersFor(final Stream<RootOid> rootOids) {

        final Map<RootOid, ObjectAdapter> adapterByOid = _Maps.newLinkedHashMap();

        List<RootOid> notYetLoadedOids = _Lists.newArrayList();

        rootOids.forEach(rootOid->{
            // attempt to locate adapter for the Oid
            ObjectAdapter adapter = null;
            // handle view models or transient
            if (rootOid.isTransient() || rootOid.isViewModel()) {
                final Object pojo = recreatePojoTransientOrViewModel(rootOid);
                adapter = objectAdapterContext.recreatePojo(rootOid, pojo);
                syncVersion(adapter, rootOid);
            }
            if (adapter != null) {
                adapterByOid.put(rootOid, adapter);
            } else {
                // persistent oid, to load in bulk
                notYetLoadedOids.add(rootOid);
            }
        });

        // recreate, in bulk, all those not yet loaded
        final Map<RootOid, Object> pojoByOid = persistenceSession.fetchPersistentPojos(notYetLoadedOids);
        for (Map.Entry<RootOid, Object> entry : pojoByOid.entrySet()) {
            final RootOid rootOid = entry.getKey();
            final Object pojo = entry.getValue();
            if(pojo != null) {
                ObjectAdapter adapter;
                try {
                    adapter = objectAdapterContext.recreatePojo(rootOid, pojo);
                    adapterByOid.put(rootOid, adapter);
                } catch(ObjectNotFoundException ex) {
                    throw ex; // just rethrow
                } catch(RuntimeException ex) {
                    throw new PojoRecreationException(rootOid, ex);
                }
                syncVersion(adapter, rootOid);
            } else {
                // null indicates it couldn't be loaded
                // do nothing here...
            }
        }

        return adapterByOid;
    }

    // -- HELPER

    private Object recreatePojoTransientOrViewModel(final RootOid rootOid) {
        final ObjectSpecification spec =
                specificationLoader.lookupBySpecIdElseLoad(rootOid.getObjectSpecId());
        final Object pojo;
        if(rootOid.isViewModel()) {
            final String memento = rootOid.getIdentifier();
            pojo = recreateViewModel(spec, memento);
        } else {
            pojo = objectAdapterContext.instantiateAndInjectServices(spec);
        }

        _Assert.assertFalse("Pojo most likely should not be an Oid", (pojo instanceof Oid));

        return pojo;
    }

    private Object recreateViewModel(final ObjectSpecification spec, final String memento) {
        final ViewModelFacet facet = spec.getFacet(ViewModelFacet.class);
        if(facet == null) {
            throw new IllegalArgumentException("spec does not have ViewModelFacet; spec is " + spec.getFullIdentifier());
        }

        final Object viewModelPojo;
        if(facet.getRecreationMechanism().isInitializes()) {
            viewModelPojo = objectAdapterContext.instantiateAndInjectServices(spec);
            facet.initialize(viewModelPojo, memento);
        } else {
            viewModelPojo = facet.instantiate(spec.getCorrespondingClass(), memento);
        }
        return viewModelPojo;
    }

    @Deprecated // no-op
    private void syncVersion(final ObjectAdapter adapter, final RootOid rootOid) {
        // sync versions of original, with concurrency checking if required
        Oid adapterOid = adapter.getOid();
        if(adapterOid instanceof RootOid) {
            final RootOid recreatedOid = (RootOid) adapterOid;
            final RootOid originalOid = rootOid;

            try {
                //                if(concurrencyChecking.isChecking()) {
                //
                //                    // check for exception, but don't throw if suppressed through thread-local
                //                    final Version otherVersion = originalOid.getVersion();
                //                    final Version thisVersion = recreatedOid.getVersion();
                //                    if( thisVersion != null &&
                //                            otherVersion != null &&
                //                            thisVersion.different(otherVersion)) {
                //
                //                        if(concurrencyCheckingGloballyEnabled && ConcurrencyChecking.isCurrentlyEnabled()) {
                //                            log.info("concurrency conflict detected on {} ({})", recreatedOid, otherVersion);
                //                            final String currentUser = authenticationSession.getUserName();
                //                            throw new ConcurrencyException(currentUser, recreatedOid, thisVersion, otherVersion);
                //                        } else {
                //                            log.info("concurrency conflict detected but suppressed, on {} ({})", recreatedOid, otherVersion);
                //                        }
                //                    }
                //                }
            } finally {
                final Version originalVersion = originalOid.getVersion();
                final Version recreatedVersion = recreatedOid.getVersion();
                if(recreatedVersion != null && (
                        originalVersion == null ||
                        recreatedVersion.different(originalVersion))
                        ) {
                    if(log.isDebugEnabled()) {
                        log.debug("updating version in oid, on {} ({}) to ({})", originalOid, originalVersion, recreatedVersion);
                    }
                    originalOid.setVersion(recreatedVersion);
                }
            }
        }
    }


}