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

package org.apache.isis.persistence.jdo.datanucleus5.datanucleus;

import org.apache.isis.persistence.jdo.datanucleus5.datanucleus.service.eventbus.EventBusServiceJdo;
import org.datanucleus.ExecutionContext;
import org.datanucleus.cache.CachedPC;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.state.ReferentialStateManagerImpl;
import org.datanucleus.store.FieldValues;
import org.datanucleus.store.fieldmanager.FieldManager;

import org.apache.isis.applib.services.inject.ServiceInjector;

/**
 * @deprecated This is bad practice: The lifecycle of persistent entities is usually decoupled from the lifecycle of a managed bean
 */
@Deprecated
public class JDOStateManagerForIsis extends ReferentialStateManagerImpl {

    private final ServiceInjector serviceInjector = null;
    
    public JDOStateManagerForIsis(ExecutionContext ec, AbstractClassMetaData cmd) {
        super(ec, cmd);
//        this.serviceInjector = (ServiceInjector) ec.getProperty("serviceInjector");
//        requires(serviceInjector, "serviceInjector");
    }

    public enum Hint {
        NONE,
        REPLACE_FIELDS,
        POST_COMMIT
    }

    /**
     * Tunnel down the thread stack as a hint to the {@link EventBusServiceJdo}.
     */
    public final static ThreadLocal<Hint> hint = new ThreadLocal<JDOStateManagerForIsis.Hint>() {
        @Override
        protected Hint initialValue() {
            return Hint.NONE;
        };
    };

    @Override
    public void initialiseForHollow(Object id, FieldValues fv, Class pcClass) {
        super.initialiseForHollow(id, fv, pcClass);
        mapIntoIsis(myPC);
    }

    @Override
    public void initialiseForHollowAppId(FieldValues fv, Class pcClass) {
        super.initialiseForHollowAppId(fv, pcClass);
        mapIntoIsis(myPC);
    }

    @Override
    public void initialiseForHollowPreConstructed(Object id, Persistable pc) {
        super.initialiseForHollowPreConstructed(id, pc);
        mapIntoIsis(myPC);
    }

    @Override
    public void initialiseForPersistentClean(Object id, Persistable pc) {
        super.initialiseForPersistentClean(id, pc);
        mapIntoIsis(myPC);
    }

    @Override
    public void initialiseForEmbedded(Persistable pc, boolean copyPc) {
        super.initialiseForEmbedded(pc, copyPc);
        mapIntoIsis(myPC);
    }

    @Override
    public void initialiseForPersistentNew(Persistable pc,
            FieldValues preInsertChanges) {
        super.initialiseForPersistentNew(pc, preInsertChanges);
        mapIntoIsis(myPC);
    }

    @Override
    public void initialiseForTransactionalTransient(Persistable pc) {
        super.initialiseForTransactionalTransient(pc);
        mapIntoIsis(myPC);
    }

    @Override
    public void initialiseForDetached(Persistable pc, Object id, Object version) {
        super.initialiseForDetached(pc, id, version);
        mapIntoIsis(myPC);
    }

    @Override
    public void initialiseForPNewToBeDeleted(Persistable pc) {
        super.initialiseForPNewToBeDeleted(pc);
        mapIntoIsis(myPC);
    }

    @Override
    public void initialiseForCachedPC(CachedPC<Persistable> cachedPC, Object id) {
        super.initialiseForCachedPC(cachedPC, id);
        mapIntoIsis(myPC);
    }

    @Override
    public void replaceField(Persistable pc, int fieldNumber, Object value) {
        final Hint previous = hint.get();
        try {
            hint.set(Hint.REPLACE_FIELDS);
            super.replaceField(pc, fieldNumber, value);
        } finally {
            hint.set(previous);
        }
    }

    @Override
    public void replaceFieldValue(int fieldNumber, Object value) {
        final Hint previous = hint.get();
        try {
            hint.set(Hint.REPLACE_FIELDS);
            super.replaceField(fieldNumber, value);
        } finally {
            hint.set(previous);
        }
    }

    @Override
    public void replaceField(int fieldNumber, Object value) {
        final Hint previous = hint.get();
        try {
            hint.set(Hint.REPLACE_FIELDS);
            super.replaceField(fieldNumber, value);
        } finally {
            hint.set(previous);
        }
    }

    @Override
    public void replaceFieldMakeDirty(int fieldNumber, Object value) {
        final Hint previous = hint.get();
        try {
            hint.set(Hint.REPLACE_FIELDS);
            super.replaceField(fieldNumber, value);
        } finally {
            hint.set(previous);
        }
    }

    @Override
    public void replaceField(Persistable pc, int fieldNumber, Object value, boolean makeDirty) {
        final Hint previous = hint.get();
        try {
            hint.set(Hint.REPLACE_FIELDS);
            super.replaceField(pc, fieldNumber, value, makeDirty);
        } finally {
            hint.set(previous);
        }
    }

    @Override
    public void replaceFields(int[] fieldNumbers, FieldManager fm) {
        final Hint previous = hint.get();
        try {
            hint.set(Hint.REPLACE_FIELDS);
            super.replaceFields(fieldNumbers, fm);
        } finally {
            hint.set(previous);
        }
    }

    @Override
    public void replaceFields(int[] fieldNumbers, FieldManager fm, boolean replaceWhenDirty) {
        final Hint previous = hint.get();
        try {
            hint.set(Hint.REPLACE_FIELDS);
            super.replaceFields(fieldNumbers, fm, replaceWhenDirty);
        } finally {
            hint.set(previous);
        }
    }

    @Override
    public void replaceNonLoadedFields(int fieldNumbers[], FieldManager fm) {
        final Hint previous = hint.get();
        try {
            hint.set(Hint.REPLACE_FIELDS);
            super.replaceNonLoadedFields(fieldNumbers, fm);
        } finally {
            hint.set(previous);
        }
    }

    @Override
    public void postCommit(org.datanucleus.Transaction tx) {
        final Hint previous = hint.get();
        try {
            hint.set(Hint.POST_COMMIT);
            super.postCommit(tx);
        } finally {
            hint.set(previous);
        }
    }

    protected void mapIntoIsis(Persistable pc) {
        if(serviceInjector!=null) {
            serviceInjector.injectServicesInto(pc);
        }
    }
    
}
