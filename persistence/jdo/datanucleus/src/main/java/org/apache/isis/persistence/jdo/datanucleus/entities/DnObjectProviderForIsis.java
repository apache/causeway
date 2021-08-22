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
package org.apache.isis.persistence.jdo.datanucleus.entities;

import java.util.Optional;

import javax.jdo.PersistenceManager;

import org.datanucleus.ExecutionContext;
import org.datanucleus.cache.CachedPC;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.state.ReferentialStateManagerImpl;
import org.datanucleus.store.FieldValues;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.persistence.jdo.spring.integration.TransactionAwarePersistenceManagerFactoryProxy;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Handles injection into JDO entities.
 * <p>
 * Installed via config property "datanucleus.objectProvider.className".
 */
@Log4j2
public class DnObjectProviderForIsis
extends ReferentialStateManagerImpl {

    private ServiceInjector serviceInjector;

    public DnObjectProviderForIsis(final ExecutionContext ec, final AbstractClassMetaData cmd) {
        super(ec, cmd);
        this.serviceInjector = extractServiceInjectorFrom(ec).orElse(null);
    }

    @Override
    public void initialiseForHollow(final Object id, final FieldValues fv, final Class pcClass) {
        super.initialiseForHollow(id, fv, pcClass);
        injectServices(myPC);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void initialiseForHollowAppId(final FieldValues fv, final Class pcClass) {
        super.initialiseForHollowAppId(fv, pcClass);
        injectServices(myPC);
    }

    @Override
    public void initialiseForHollowPreConstructed(final Object id, final Persistable pc) {
        super.initialiseForHollowPreConstructed(id, pc);
        injectServices(myPC);
    }

    @Override
    public void initialiseForPersistentClean(final Object id, final Persistable pc) {
        super.initialiseForPersistentClean(id, pc);
        injectServices(myPC);
    }

    @Override
    public void initialiseForEmbedded(final Persistable pc, final boolean copyPc) {
        super.initialiseForEmbedded(pc, copyPc);
        injectServices(myPC);
    }

    @Override
    public void initialiseForPersistentNew(final Persistable pc, final FieldValues preInsertChanges) {
        super.initialiseForPersistentNew(pc, preInsertChanges);
        injectServices(myPC);
    }

    @Override
    public void initialiseForTransactionalTransient(final Persistable pc) {
        super.initialiseForTransactionalTransient(pc);
        injectServices(myPC);
    }

    @Override
    public void initialiseForDetached(final Persistable pc, final Object id, final Object version) {
        super.initialiseForDetached(pc, id, version);
        injectServices(myPC);
    }

    @Override
    public void initialiseForPNewToBeDeleted(final Persistable pc) {
        super.initialiseForPNewToBeDeleted(pc);
        injectServices(myPC);
    }

    @Override
    public void initialiseForCachedPC(final CachedPC<Persistable> cachedPC, final Object id) {
        super.initialiseForCachedPC(cachedPC, id);
        injectServices(myPC);
    }

    // -- HELPER

    private Optional<ServiceInjector> extractServiceInjectorFrom(final ExecutionContext ec) {

        val pm = ec.getOwner();
        if(! (pm instanceof PersistenceManager)) {
            log.error("could not extract the current PersistenceManager from given ExecutionContext");
            return Optional.empty();
        }

        val mmcKey = TransactionAwarePersistenceManagerFactoryProxy.MMC_USER_OBJECT_KEY;
        val mmc = ((PersistenceManager)pm)
                .getUserObject(mmcKey);
        if(! (mmc instanceof MetaModelContext)) {
            log.error("MetaModelContext, stored as key/value pair with key '" + mmcKey +
                    "', was not found amoung current PersistenceManager's user objects");
            return Optional.empty();
        }

        val serviceInjector = ((MetaModelContext)mmc).getServiceInjector();
        if(serviceInjector == null) {
            log.error("could not find a usable ServiceInjector with given MetaModelContext");
            return Optional.empty();
        }

        return Optional.of(serviceInjector);
    }

    private void injectServices(final Persistable entity) {
        if(entity==null) {
            return;
        }
        if(serviceInjector!=null) {
            serviceInjector.injectServicesInto(entity);
        } else {
            log.warn("cannot inject services into entity of type {}, "
                    + "as there is no ServiceInjector available",
                    entity.getClass());
        }
    }

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

