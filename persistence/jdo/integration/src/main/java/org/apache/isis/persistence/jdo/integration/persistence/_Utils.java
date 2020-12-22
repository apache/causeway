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
package org.apache.isis.persistence.jdo.integration.persistence;

import javax.annotation.Nullable;
import javax.jdo.listener.InstanceLifecycleEvent;

import org.datanucleus.enhancement.Persistable;

import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.NonNull;
import lombok.val;

final class _Utils {

    static Persistable persistableFor(InstanceLifecycleEvent event) {
        return (Persistable)event.getSource();
    }
    
    static boolean ensureRootObject(final Persistable pojo) {
        return pojo!=null; // why would a Persistable ever be something different?
    }
    
    static boolean isJUnitTest() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.getClassName().startsWith("org.junit.")) {
                return true;
            }           
        }
        return false;
    }

    @Nullable
    static ManagedObject adapterFor(
            final MetaModelContext mmc,
            final Object pojo) {
        
        if(pojo == null) {
            return null;
        }
        
        val objectManager = mmc.getObjectManager();
        val adapter = objectManager.adapt(pojo);
        return injectServices(mmc, adapter);
    }
    
    @Nullable
    static ManagedObject injectServices(
            final @NonNull MetaModelContext mmc,
            final @Nullable ManagedObject adapter) {
        
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(adapter)) {
            return adapter; 
        }
        
        val spec = adapter.getSpecification();
        if(spec==null 
                || spec.isValue()) {
            return adapter; // guard against value objects
        }
        mmc.getServiceInjector().injectServicesInto(adapter.getPojo());
        return adapter;
    }
    
    static RootOid createRootOid(
            final @NonNull MetaModelContext mmc,
            final @NonNull IsisPersistenceSessionJdo persistenceSession, // TODO don't depend on session
            final @NonNull Object pojo) {

        val spec = mmc.getSpecification(pojo.getClass());

        final String identifier = persistenceSession.identifierFor(pojo);

        final ObjectSpecId objectSpecId = spec.getSpecId();
        return Oid.Factory.root(objectSpecId, identifier);
    }

    static ManagedObject recreatePojo(
            final @NonNull MetaModelContext mmc,
            final @NonNull RootOid oid,
            final @NonNull Object recreatedPojo) {
        
        val spec = mmc.getSpecification(recreatedPojo.getClass());
        
        final ManagedObject createdAdapter = createRootOrAggregatedAdapter(spec, oid, recreatedPojo);
        return injectServices(mmc, createdAdapter);
    }

    static ManagedObject fetchPersistent(
            final @NonNull MetaModelContext mmc,
            final @NonNull IsisPersistenceSessionJdo persistenceSession, // TODO don't depend on session
            final Object pojo) {
        if (persistenceSession.getJdoPersistenceManager().getObjectId(pojo) == null) {
            return null;
        }
        final RootOid oid = createRootOid(mmc, persistenceSession, pojo);
        final ManagedObject adapter = recreatePojo(mmc, oid, pojo);
        return adapter;
    }
    
    private static ManagedObject createRootOrAggregatedAdapter(
            final @NonNull ObjectSpecification spec,
            final RootOid oid, 
            final Object pojo) {
        
        if(oid instanceof RootOid) {
            final RootOid rootOid = (RootOid) oid;
            return ManagedObject.identified(spec, pojo, rootOid);
        } 
        throw _Exceptions.illegalArgument("Parented Oids are no longer supported, or cannot use Value Oid.");
    }
    
//  @SuppressWarnings("unused")
//  private static Object jdoObjectIdFor(InstanceLifecycleEvent event) {
//      final Persistable persistable = persistableFor(event);
//      final Object jdoObjectId = persistable.dnGetObjectId();
//      return jdoObjectId;
//  }
    
}
