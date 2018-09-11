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

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.ParentedCollectionOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;

/**
 * package private mixin for ObjectAdapterContext
 * <p>
 * Responsibility: AdapterManager 'legacy' support
 * </p>
 *  
 * @since 2.0.0-M2
 */
class ObjectAdapterContext_AdapterManager {
    
    private final ObjectAdapterContext objectAdapterContext;
    @SuppressWarnings("unused")
    private final PersistenceSession persistenceSession;
    private final ServicesInjector servicesInjector; 
    
    ObjectAdapterContext_AdapterManager(ObjectAdapterContext objectAdapterContext,
            PersistenceSession persistenceSession) {
        this.objectAdapterContext = objectAdapterContext;
        this.persistenceSession = persistenceSession;
        this.servicesInjector = persistenceSession.getServicesInjector();
    }
    
    ObjectAdapter addRecreatedPojoToCache(Oid oid, Object recreatedPojo) {
        final ObjectAdapter createdAdapter = createRootOrAggregatedAdapter(oid, recreatedPojo);
        return injectServices(createdAdapter);
    }

    ObjectAdapter injectServices(final ObjectAdapter adapter) {
        Objects.requireNonNull(adapter);
        final Object pojo = adapter.getObject();
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