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

package org.apache.isis.objectstore.jdo.datanucleus;

import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.StateManager;

import org.datanucleus.ExecutionContext;
import org.datanucleus.cache.CachedPC;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.state.JDOStateManager;
import org.datanucleus.state.ObjectProvider;
import org.datanucleus.state.ReferentialJDOStateManager;
import org.datanucleus.store.FieldValues;
import org.datanucleus.store.fieldmanager.FieldManager;

import org.apache.isis.core.metamodel.services.ServicesInjectorSpi;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.objectstore.jdo.datanucleus.service.eventbus.EventBusServiceJdo;

public class JDOStateManagerForIsis extends ReferentialJDOStateManager implements StateManager, ObjectProvider {

    public JDOStateManagerForIsis(ExecutionContext ec, AbstractClassMetaData cmd) {
        super(ec, cmd);
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
        protected Hint initialValue() {
            return Hint.NONE;
        };
    };

    public void initialiseForHollow(Object id, FieldValues fv, Class pcClass) {
        super.initialiseForHollow(id, fv, pcClass);
        mapIntoIsis(myPC);
    }

    public void initialiseForHollowAppId(FieldValues fv, Class pcClass) {
        super.initialiseForHollowAppId(fv, pcClass);
        mapIntoIsis(myPC);
    }

    public void initialiseForHollowPreConstructed(Object id, Object pc) {
        super.initialiseForHollowPreConstructed(id, pc);
        mapIntoIsis(myPC);
    }

    public void initialiseForPersistentClean(Object id, Object pc) {
        super.initialiseForPersistentClean(id, pc);
        mapIntoIsis(myPC);
    }

    public void initialiseForEmbedded(Object pc, boolean copyPc) {
        super.initialiseForEmbedded(pc, copyPc);
        mapIntoIsis(myPC);
    }

    public void initialiseForPersistentNew(Object pc,
            FieldValues preInsertChanges) {
        super.initialiseForPersistentNew(pc, preInsertChanges);
        mapIntoIsis(myPC);
    }

    public void initialiseForTransactionalTransient(Object pc) {
        super.initialiseForTransactionalTransient(pc);
        mapIntoIsis(myPC);
    }

    public void initialiseForDetached(Object pc, Object id, Object version) {
        super.initialiseForDetached(pc, id, version);
        mapIntoIsis(myPC);
    }

    public void initialiseForPNewToBeDeleted(Object pc) {
        super.initialiseForPNewToBeDeleted(pc);
        mapIntoIsis(myPC);
    }

    public void initialiseForCachedPC(CachedPC cachedPC, Object id) {
        super.initialiseForCachedPC(cachedPC, id);
        mapIntoIsis(myPC);
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

    public void postCommit(org.datanucleus.Transaction tx) {
        final Hint previous = hint.get();
        try {
            hint.set(Hint.POST_COMMIT);
            super.postCommit(tx);
        } finally {
            hint.set(previous);
        }
    }

    protected void mapIntoIsis(PersistenceCapable pc) {
        getServicesInjector().injectServicesInto(pc);
    }

    protected ServicesInjectorSpi getServicesInjector() {
        return IsisContext.getPersistenceSession().getServicesInjector();
    }
}
