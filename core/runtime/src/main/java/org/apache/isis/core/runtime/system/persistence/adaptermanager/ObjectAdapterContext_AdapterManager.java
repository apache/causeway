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

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.ParentedCollectionOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;

/**
 *  
 * @since 2.0.0-M2
 */
class ObjectAdapterContext_AdapterManager {
    
    private static final Logger LOG = LoggerFactory.getLogger(ObjectAdapterContext_AdapterManager.class);
    private final ObjectAdapterContext objectAdapterContext;
    private final PersistenceSession persistenceSession;
    private final ServicesInjector servicesInjector; 
    
    ObjectAdapterContext_AdapterManager(ObjectAdapterContext objectAdapterContext,
            PersistenceSession persistenceSession) {
        this.objectAdapterContext = objectAdapterContext;
        this.persistenceSession = persistenceSession;
        this.servicesInjector = persistenceSession.getServicesInjector();
    }
    
    /**
     * Either returns an existing {@link ObjectAdapter adapter} (as per
     * {@link #lookupAdapterFor(Object)} or {@link #lookupAdapterFor(Oid)}), otherwise
     * re-creates an adapter with the specified (persistent) {@link Oid}.
     *
     * <p>
     * Typically called when the {@link Oid} is already known, that is, when
     * resolving an already-persisted object. Is also available for
     * <tt>Memento</tt> support however, so {@link Oid} could also represent a
     * {@link Oid#isTransient() transient} object.
     *
     * @param oid
     * @param recreatedPojo - already known to the object store impl, or a service
     */
    //@Override
    public ObjectAdapter addRecreatedPojoToCache(Oid oid, Object recreatedPojo) {
        // attempt to locate adapter for the pojo
        // REVIEW: this check is possibly redundant because the pojo will most likely
        // have just been instantiated, so won't yet be in any maps
        final ObjectAdapter adapterLookedUpByPojo = lookupAdapterFor(recreatedPojo);
        if (adapterLookedUpByPojo != null) {
            return adapterLookedUpByPojo;
        }

        // attempt to locate adapter for the Oid
        final ObjectAdapter adapterLookedUpByOid = lookupAdapterFor(oid);
        if (adapterLookedUpByOid != null) {
            return adapterLookedUpByOid;
        }

        final ObjectAdapter createdAdapter = createRootOrAggregatedAdapter(oid, recreatedPojo);
        return mapAndInjectServices(createdAdapter);
    }

    /**
     * Removes the specified object from both the identity-adapter map, and the
     * pojo-adapter map.
     *
     * <p>
     * This indicates that the object is no longer in use, and therefore that no
     * objects exists within the system.
     *
     * <p>
     * If an {@link ObjectAdapter adapter} is removed while its pojo still is
     * referenced then a subsequent interaction of that pojo will create a
     * different {@link ObjectAdapter adapter}.
     *
     * <p>
     * TODO: should do a cascade remove of any aggregated objects.
     */
    //@Override
    void removeAdapterFromCache(final ObjectAdapter adapter) {
        objectAdapterContext.ensureMapsConsistent(adapter);
        objectAdapterContext.removeAdapter(adapter);
    }

    /**
     * Gets the {@link ObjectAdapter adapter} for the specified domain object if
     * it exists in the identity map.
     *
     * <p>
     * Provided by the <tt>AdapterManager</tt> when used by framework.
     *
     * @param pojo
     *            - must not be <tt>null</tt>
     * @return adapter, or <tt>null</tt> if doesn't exist.
     * @deprecated don't expose caching
     */
    //@Override
    ObjectAdapter lookupAdapterFor(final Object pojo) {
        Objects.requireNonNull(pojo);

        return objectAdapterContext.lookupAdapterByPojo(pojo);  
    }

    /**
     * Gets the {@link ObjectAdapter adapter} for the {@link Oid} if it exists
     * in the identity map.
     *
     * @param oid
     *            - must not be <tt>null</tt>
     * @return adapter, or <tt>null</tt> if doesn't exist.
     * @deprecated don't expose caching
     */
    //@Override
    ObjectAdapter lookupAdapterFor(final Oid oid) {
        Objects.requireNonNull(oid);
        objectAdapterContext.ensureMapsConsistent(oid);

        return objectAdapterContext.lookupAdapterById(oid);
    }
 
    ObjectAdapter mapAndInjectServices(final ObjectAdapter adapter) {
        // since the whole point of this method is to map an adapter that's just been created.
        // so we *don't* call ensureMapsConsistent(adapter);

        Assert.assertNotNull(adapter);
        final Object pojo = adapter.getObject();
        Assert.assertFalse("POJO Map already contains object", pojo, objectAdapterContext.containsAdapterForPojo(pojo));

        if (LOG.isDebugEnabled()) {
            // don't interact with the underlying object because may be a ghost
            // and would trigger a resolve
            // don't call toString() on adapter because calls hashCode on
            // underlying object, may also trigger a resolve.
            LOG.debug("adding identity for adapter with oid={}", adapter.getOid());
        }

        // value adapters are not mapped (but all others - root and aggregated adapters - are)
        if (adapter.isValue()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("not mapping value adapter");
            }
            servicesInjector.injectServicesInto(pojo);
            return adapter;
        }

        objectAdapterContext.addAdapterHonoringSpecImmutability(pojo, adapter);

        // must inject after mapping, otherwise infinite loop
        servicesInjector.injectServicesInto(pojo);

        return adapter;
    }
    
    ObjectAdapter createRootOrAggregatedAdapter(final Oid oid, final Object pojo) {
        final ObjectAdapter createdAdapter;
        if(oid instanceof RootOid) {
            final RootOid rootOid = (RootOid) oid;
            createdAdapter = objectAdapterContext.getFactories().createRootAdapter(pojo, rootOid);
        } else /*if (oid instanceof CollectionOid)*/ {
            final ParentedCollectionOid collectionOid = (ParentedCollectionOid) oid;
            createdAdapter = objectAdapterContext.getFactories().createCollectionAdapter(pojo, collectionOid);
        }
        return createdAdapter;
    }
    
}