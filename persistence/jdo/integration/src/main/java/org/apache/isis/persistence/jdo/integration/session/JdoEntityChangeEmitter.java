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
package org.apache.isis.persistence.jdo.integration.session;

import javax.jdo.PersistenceManager;

import org.datanucleus.enhancement.Persistable;

import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.transaction.changetracking.EntityChangeTracker;
import org.apache.isis.persistence.jdo.datanucleus.entities.DnEntityStateProvider;
import org.apache.isis.persistence.jdo.integration.lifecycles.JdoLifecycleListener;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class JdoEntityChangeEmitter implements JdoLifecycleListener.EntityChangeEmitter {

    private final MetaModelContext metaModelContext;
    private final PersistenceManager persistenceManager;
    private final EntityChangeTracker entityChangeTracker;
    
    @Override
    public ManagedObject adaptEntityAndInjectServices(final @NonNull Persistable pojo) {
        return _Utils.adaptEntityAndInjectServices(metaModelContext, pojo);
    }

    @Override
    public void enlistDeletingAndInvokeIsisRemovingCallbackFacet(final Persistable pojo) {
        val entity = adaptEntityAndInjectServices(pojo);
        entityChangeTracker.enlistDeleting(entity);
    }

    /**
     * Called either when an entity is initially persisted, or when an entity is updated; fires the appropriate
     * lifecycle callback.
     *
     * <p>
     * The implementation therefore uses Isis' {@link Oid#isTransient() oid}
     * to determine which callback to fire.
     */
    @Override
    public void invokeIsisPersistingCallback(final Persistable pojo) {
        if (DnEntityStateProvider.entityState(pojo).isDetached()) {
            val entity = ManagedObject.of(
                    metaModelContext.getSpecificationLoader()::loadSpecification, 
                    pojo);

            entityChangeTracker.recognizePersisting(entity);

        } else {
            // updating

            // don't call here, already called in preDirty.

            // CallbackFacet.Util.callCallback(adapter, UpdatingCallbackFacet.class);
        }
    }

    /**
     * Called either when an entity is initially persisted, or when an entity is updated;
     * fires the appropriate lifecycle callback
     *
     * <p>
     * The implementation therefore uses Isis' {@link Oid#isTransient() oid}
     * to determine which callback to fire.
     */
    @Override
    public void enlistCreatedAndInvokeIsisPersistedCallback(final Persistable pojo) {
        val entity = adaptEntityAndInjectServices(pojo);
        entityChangeTracker.enlistCreated(entity);
    }

    @Override
    public void enlistUpdatingAndInvokeIsisUpdatingCallback(final Persistable pojo) {
        val entity = _Utils.fetchEntityElseFail(metaModelContext, persistenceManager, pojo);
        entityChangeTracker.enlistUpdating(entity);
    }

    @Override
    public void invokeIsisUpdatedCallback(Persistable pojo) {
        val entity = _Utils.fetchEntityElseFail(metaModelContext, persistenceManager, pojo);
        // the callback and transaction.enlist are done in the preStore callback
        // (can't be done here, as the enlist requires to capture the 'before' values)
        entityChangeTracker.recognizeUpdating(entity);
    }

}
