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
package org.apache.isis.persistence.jdo.integration.changetracking;

import javax.annotation.Nullable;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.InstanceLifecycleEvent;

import org.datanucleus.enhancement.Persistable;

import org.apache.isis.commons.functional.Result;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.persistence.jdo.datanucleus.oid.JdoObjectIdSerializer;

import lombok.NonNull;
import lombok.val;

final class _Utils {

    static Persistable persistableFor(InstanceLifecycleEvent event) {
        return (Persistable)event.getSource();
    }
    
    static ManagedObject adaptNullableAndInjectServices(
            final @NonNull MetaModelContext mmc,
            final @Nullable Object pojo) {
        
        if(pojo == null) {
            return ManagedObject.unspecified();
        }
        
        val objectManager = mmc.getObjectManager();
        val adapter = objectManager.adapt(pojo);
        return injectServices(mmc, adapter);
    }
    
    static ManagedObject adaptEntityAndInjectServices(
            final @NonNull MetaModelContext mmc,
            final @NonNull Object entityPojo) {
        
        val objectManager = mmc.getObjectManager();
        val entity = objectManager.adapt(entityPojo);
        _Assert.assertTrue(entity.getSpecification().isEntity());
        return injectServices(mmc, entity);
    }

    
    static ManagedObject injectServices(
            final @NonNull MetaModelContext mmc,
            final @NonNull ManagedObject adapter) {
        
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
    
    static ManagedObject fetchEntityElseFail(
            final @NonNull MetaModelContext mmc,
            final @NonNull PersistenceManager pm,
            final @Nullable Object pojo) {
    
        return Result.of(()->{
            
            if (pm.getObjectId(pojo) == null) {
                throw _Exceptions
                    .noSuchElement("DN could not find objectId for pojo (unexpected); pojo=[%s]", pojo);
            }
            final ManagedObject adapter = identify(mmc, pm, pojo);
            return adapter;
            
        })
        .orElseFail();
    }

    static ManagedObject identify(
            final @NonNull MetaModelContext mmc,
            final @NonNull PersistenceManager pm, 
            final @NonNull Object pojo) {
        
        val spec = mmc.getSpecification(pojo.getClass());

        final String identifier = JdoObjectIdSerializer.identifierForElseFail(pm, pojo);

        final ObjectSpecId objectSpecId = spec.getSpecId();
        final RootOid rootOid = Oid.Factory.root(objectSpecId, identifier);
        
        final ManagedObject createdAdapter = ManagedObject.identified(spec, pojo, rootOid);
        return injectServices(mmc, createdAdapter);
    }
    
}
