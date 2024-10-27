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
package org.apache.causeway.persistence.jdo.datanucleus.entities;

import java.util.Objects;
import java.util.Optional;

import org.datanucleus.ExecutionContext;
import org.datanucleus.api.jdo.DataNucleusHelperJDO;
import org.datanucleus.cache.CachedPC;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.identity.SingleFieldId;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.state.ReferentialStateManagerImpl;
import org.datanucleus.store.FieldValues;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._Refs;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.persistence.jdo.datanucleus.metamodel.facets.entity.JdoEntityFacet;

import lombok.extern.log4j.Log4j2;

/**
 * Handles injection into JDO entities.
 * <p>
 * Installed via config property "datanucleus.objectProvider.className".
 */
@Log4j2
public class DnStateManagerForCauseway
extends ReferentialStateManagerImpl {

    public DnStateManagerForCauseway(final ExecutionContext ec, final AbstractClassMetaData cmd) {
        super(ec, cmd);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void initialiseForHollow(final Object id, final FieldValues fv, final Class pcClass) {
        super.initialiseForHollow(id, fv, pcClass);
        injectServicesIfNotAlready();
    }

    @SuppressWarnings({ "deprecation", "rawtypes" })
    @Override
    public void initialiseForHollowAppId(final FieldValues fv, final Class pcClass) {
        super.initialiseForHollowAppId(fv, pcClass);
        injectServicesIfNotAlready();
    }

    @Override
    public void initialiseForHollowPreConstructed(final Object id, final Persistable pc) {
        super.initialiseForHollowPreConstructed(id, pc);
        injectServicesIfNotAlready();
    }

    @Override
    public void initialiseForPersistentClean(final Object id, final Persistable pc) {
        super.initialiseForPersistentClean(id, pc);
        injectServicesIfNotAlready();
    }

    @Override
    public void initialiseForEmbedded(final Persistable pc, final boolean copyPc) {
        super.initialiseForEmbedded(pc, copyPc);
        injectServicesIfNotAlready();
    }

    @Override
    public void initialiseForPersistentNew(final Persistable pc, final FieldValues preInsertChanges) {
        super.initialiseForPersistentNew(pc, preInsertChanges);
        injectServicesIfNotAlready();
    }

    @Override
    public void initialiseForTransactionalTransient(final Persistable pc) {
        super.initialiseForTransactionalTransient(pc);
        injectServicesIfNotAlready();
    }

    @Override
    public void initialiseForDetached(final Persistable pc, final Object id, final Object version) {
        super.initialiseForDetached(pc, id, version);
        injectServicesIfNotAlready();
    }

    @Override
    public void initialiseForPNewToBeDeleted(final Persistable pc) {
        super.initialiseForPNewToBeDeleted(pc);
        injectServicesIfNotAlready();
    }

    @Override
    public void initialiseForCachedPC(final CachedPC cachedPC, final Object id) {
        super.initialiseForCachedPC(cachedPC, id);
        injectServicesIfNotAlready();
    }

    @Override
    public void disconnect() {
        /* If a previously attached entity becomes hollow (or detached),
         * we memoize the OID in the Persistable.dnStateManager field.
         * Using a pseudo StateManager, that only acts as a holder of an OID. */
        var entityPojo = myPC; // keep local reference
        var snapshotOid = snapshotOid();

        super.disconnect();

        snapshotOid
            .ifPresent(oid->DnOidStoreAndRecoverHelper.forEntity(entityPojo).storeOid(oid));
    }

    // -- PREDICATES

    public boolean isInsertingOrInsertingCallbacks() {
        return super.isInserting()
                || (flags & FLAG_INSERTING_CALLBACKS) != 0;
    }

    // -- HELPER

    private boolean injectionPointsResolved = false;

    /**
     * Returns whether injection points are resolved for myPC.
     */
    public boolean injectServicesIfNotAlready() {
        if(myPC==null) {
            this.injectionPointsResolved = false; // reset
            return true;
        }
        if(injectionPointsResolved) {
            return true;
        }

        MetaModelContext.instance()
        .ifPresentOrElse(
                mmc->{
                    mmc.getServiceInjector().injectServicesInto(myPC);
                    this.injectionPointsResolved = true;
                },
                ()->{
                    log.warn("cannot inject services into entity of type {}, "
                            + "as there is no ServiceInjector available",
                            myPC.getClass());
                });

        return injectionPointsResolved;
    }

    // -- SNAPSHOT OID

    private Optional<String> snapshotOid() {
        if(myID==null) {
            return Optional.empty();
        }
        try {
            Object id = myPC.dnGetObjectId();
            if(id==null) {
                return Optional.empty();
            }
            var entityFacet = lookupEntityFacet().orElse(null);
            if(entityFacet==null) {
                return Optional.empty();
            }
            if (id instanceof SingleFieldId) {
                id = DataNucleusHelperJDO
                        .getSingleFieldIdentityForDataNucleusIdentity((SingleFieldId<?, ?>)id, myPC.getClass());
            }
            var oidIfAny = entityFacet.identifierForDnPrimaryKey(id);
            return oidIfAny;
        } catch (Exception e) {
            log.error("exception while trying to extract entity's current primary key", e);
            return Optional.empty();
        }
    }

    private Optional<JdoEntityFacet> lookupEntityFacet() {
        if(myPC==null) {
            return Optional.empty();
        }
        var entityFacet = MetaModelContext.instance()
            .map(MetaModelContext::getSpecificationLoader)
            .flatMap(specLoader->specLoader.specForType(myPC.getClass()))
            .flatMap(ObjectSpecification::entityFacet)
            .flatMap(facet->_Casts.castTo(JdoEntityFacet.class, facet));
        return entityFacet;
    }

    // -- [CAUSEWAY-3126] PRE-DIRTY NESTED LOOP PREVENTION

    @FunctionalInterface
    public static interface PreDirtyPropagationLock {
        void release();
        default void releaseAfter(final Runnable runnable) {
            try {
                runnable.run();
            } finally {
                release();
            }
        }
    }

    // assuming we don't need thread-safety here,
    // as each thread presumably has its own DN execution context
    private final _Refs.ObjectReference<PreDirtyPropagationLock> preDirtyPropagationLockRef =
            _Refs.objectRef(null);

    private final PreDirtyPropagationLock createPreDirtyPropagationLock() {
        return ()->preDirtyPropagationLockRef.set(null);
    }

    /**
     * Optionally provides a {@link PreDirtyPropagationLock} for pre-dirty event propagation,
     * based on whether there is NOT already a pre-dirty event in progress for the same OID.
     */
    public Optional<PreDirtyPropagationLock> acquirePreDirtyPropagationLock(final Object id) {

        // this algorithm is not thread-safe
        // assuming we don't need thread-safety here,
        // as each thread presumably has its own DN execution context

        var lockIfGranted = !Objects.equals(myID, id)
                || preDirtyPropagationLockRef.isNotNull()
                ? Optional.<PreDirtyPropagationLock>empty()
                : Optional.of(preDirtyPropagationLockRef.set(createPreDirtyPropagationLock()));

        if(log.isDebugEnabled()) {
            log.debug("acquirePreDirtyPropagationLock({}) -> {}",
                    id, lockIfGranted.map(lock->"GRANTED").orElse("DENIED"));
        }

        return lockIfGranted;
    }

    // -- UTILITY

    public static Optional<DnStateManagerForCauseway> extractFrom(final @Nullable Persistable pojo) {
        return pojo!=null
                ? _Casts.castTo(DnStateManagerForCauseway.class, pojo.dnGetStateManager())
                : Optional.empty();
    }

    // --

    /*

    could be used as hooks to detect field changes ...

    @Override
    public void replaceField(final Persistable pc, final int fieldNumber, final Object value) {
        super.replaceField(pc, fieldNumber, value);
    }

    @Override
    public void replaceFieldValue(final int fieldNumber, final Object value) {
        super.replaceField(fieldNumber, value);
    }

    @Override
    public void replaceField(final int fieldNumber, final Object value) {
        super.replaceField(fieldNumber, value);
    }

    @Override
    public void replaceFieldMakeDirty(final int fieldNumber, final Object value) {
        super.replaceField(fieldNumber, value);
    }

    @Override
    public void replaceField(final Persistable pc, final int fieldNumber, final Object value, final boolean makeDirty) {
        super.replaceField(pc, fieldNumber, value, makeDirty);
    }

    @Override
    public void replaceFields(final int[] fieldNumbers, final FieldManager fm) {
        super.replaceFields(fieldNumbers, fm);
    }

    @Override
    public void replaceFields(final int[] fieldNumbers, final FieldManager fm, final boolean replaceWhenDirty) {
        super.replaceFields(fieldNumbers, fm, replaceWhenDirty);
    }

    @Override
    public void replaceNonLoadedFields(final int fieldNumbers[], final FieldManager fm) {
        super.replaceNonLoadedFields(fieldNumbers, fm);
    }

    @Override
    public void postCommit(final org.datanucleus.Transaction tx) {
        super.postCommit(tx);
    } */

}
