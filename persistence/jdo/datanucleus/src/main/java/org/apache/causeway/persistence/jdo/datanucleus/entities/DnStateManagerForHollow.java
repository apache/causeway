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

import org.datanucleus.ExecutionContext;
import org.datanucleus.FetchPlan;
import org.datanucleus.FetchPlanForClass;
import org.datanucleus.FetchPlanState;
import org.datanucleus.cache.CachedPC;
import org.datanucleus.enhancement.Detachable;
import org.datanucleus.enhancement.ExecutionContextReference;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.enhancement.StateManager;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.state.DNStateManager;
import org.datanucleus.state.LifeCycleState;
import org.datanucleus.store.FieldValues;
import org.datanucleus.store.StoreManager;
import org.datanucleus.store.fieldmanager.FieldManager;
import org.datanucleus.transaction.Transaction;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.exceptions._Exceptions;

import lombok.NonNull;

/**
 * Used to memoize stringified OIDs of entities,
 * to be stored in the {@link Persistable}'s dnStateManager field.
 * <p>
 * Has no purpose otherwise.
 */
class DnStateManagerForHollow implements DNStateManager<Persistable> {

    // -- CONSTRUCTION

    public final @NonNull String oidStringified;

    public DnStateManagerForHollow(final @NonNull String oidStringified) {
        this.oidStringified = oidStringified;
    }

    @Override public ExecutionContextReference getExecutionContextReference() { return null;}
    @Override public byte replacingFlags(final Persistable pc) { return 0;}
    @Override public StateManager replacingStateManager(final Persistable pc, final StateManager sm) { return null;}
    @Override public boolean isDirty(final Persistable pc) { return false;}
    @Override public boolean isTransactional(final Persistable pc) { return false;}
    @Override public boolean isPersistent(final Persistable pc) { return false;}
    @Override public boolean isNew(final Persistable pc) { return false;}
    @Override public boolean isDeleted(final Persistable pc) { return false;}
    @Override public void makeDirty(final Persistable pc, final String fieldName) { }
    @Override public Object getObjectId(final Persistable pc) { return null;}
    @Override public Object getTransactionalObjectId(final Persistable pc) { return null;}
    @Override public Object getVersion(final Persistable pc) { return null;}
    @Override public boolean isLoaded(final Persistable pc, final int field) { return false;}
    @Override public void preSerialize(final Persistable pc) { }
    @Override public void setBooleanField(final Persistable pc, final int field, final boolean currentValue, final boolean newValue) { }
    @Override public void setCharField(final Persistable pc, final int field, final char currentValue, final char newValue) { }
    @Override public void setByteField(final Persistable pc, final int field, final byte currentValue, final byte newValue) { }
    @Override public void setShortField(final Persistable pc, final int field, final short currentValue, final short newValue) { }
    @Override public void setIntField(final Persistable pc, final int field, final int currentValue, final int newValue) { }
    @Override public void setLongField(final Persistable pc, final int field, final long currentValue, final long newValue) { }
    @Override public void setFloatField(final Persistable pc, final int field, final float currentValue, final float newValue) { }
    @Override public void setDoubleField(final Persistable pc, final int field, final double currentValue, final double newValue) { }
    @Override public void setStringField(final Persistable pc, final int field, final String currentValue, final String newValue) { }
    @Override public void setObjectField(final Persistable pc, final int field, final Object currentValue, final Object newValue) { }
    @Override public void providedBooleanField(final Persistable pc, final int field, final boolean currentValue) { }
    @Override public void providedCharField(final Persistable pc, final int field, final char currentValue) { }
    @Override public void providedByteField(final Persistable pc, final int field, final byte currentValue) { }
    @Override public void providedShortField(final Persistable pc, final int field, final short currentValue) { }
    @Override public void providedIntField(final Persistable pc, final int field, final int currentValue) { }
    @Override public void providedLongField(final Persistable pc, final int field, final long currentValue) { }
    @Override public void providedFloatField(final Persistable pc, final int field, final float currentValue) { }
    @Override public void providedDoubleField(final Persistable pc, final int field, final double currentValue) { }
    @Override public void providedStringField(final Persistable pc, final int field, final String currentValue) { }
    @Override public void providedObjectField(final Persistable pc, final int field, final Object currentValue) { }
    @Override public boolean replacingBooleanField(final Persistable pc, final int field) { return false;}
    @Override public char replacingCharField(final Persistable pc, final int field) { return 0;}
    @Override public byte replacingByteField(final Persistable pc, final int field) { return 0;}
    @Override public short replacingShortField(final Persistable pc, final int field) { return 0;}
    @Override public int replacingIntField(final Persistable pc, final int field) { return 0;}
    @Override public long replacingLongField(final Persistable pc, final int field) { return 0;}
    @Override public float replacingFloatField(final Persistable pc, final int field) { return 0;}
    @Override public double replacingDoubleField(final Persistable pc, final int field) { return 0;}
    @Override public String replacingStringField(final Persistable pc, final int field) { return null;}
    @Override public Object replacingObjectField(final Persistable pc, final int field) { return null;}
    @Override public Object[] replacingDetachedState(final Detachable pc, final Object[] state) { return null;}
    @Override public void connect(final ExecutionContext ec, final AbstractClassMetaData cmd) { }
    @Override public void disconnect() { }
    @Override public boolean isConnected() { return false;}
    @Override public void initialiseForHollow(final Object id, final FieldValues fv, final Class<Persistable> pcClass) { }
    @Override public void initialiseForHollowAppId(final FieldValues fv, final Class<Persistable> pcClass) { }
    @Override public void initialiseForHollowPreConstructed(final Object id, final Persistable pc) { }
    @Override public void initialiseForPersistentClean(final Object id, final Persistable pc) { }
    @Override public void initialiseForEmbedded(final Persistable pc, final boolean copyPc) { }
    @Override public void initialiseForEmbedded(final Class<Persistable> pcClass) { }
    @Override public void initialiseForPersistentNew(final Persistable pc, final FieldValues preInsertChanges) { }
    @Override public void initialiseForTransactionalTransient(final Persistable pc) { }
    @Override public void initialiseForDetached(final Persistable pc, final Object id, final Object version) { }
    @Override public void initialiseForPNewToBeDeleted(final Persistable pc) { }
    @Override public void initialiseForCachedPC(final CachedPC cachedPC, final Object id) { }
    @Override public AbstractClassMetaData getClassMetaData() { return null;}
    @Override public ExecutionContext getExecutionContext() { return null;}
    @Override public StoreManager getStoreManager() { return null;}
    @Override public FetchPlanForClass getFetchPlanForClass() { return null;}
    @Override public Persistable getObject() { return null;}
    @Override public String getObjectAsPrintable() { return null;}
    @Override public Object getInternalObjectId() { return null;}
    @Override public Object getExternalObjectId() { return null;}
    @Override public LifeCycleState getLifecycleState() { return null;}
    @Override public void replaceField(final int fieldNumber, final Object value) { }
    @Override public void replaceFieldMakeDirty(final int fieldNumber, final Object value) { }
    @Override public void replaceFieldValue(final int fieldNumber, final Object newValue) { }
    @Override public void replaceFields(final int[] fieldNumbers, final FieldManager fm) { }
    @Override public void replaceFields(final int[] fieldNumbers, final FieldManager fm, final boolean replaceWhenDirty) { }
    @Override public void replaceNonLoadedFields(final int[] fieldNumbers, final FieldManager fm) { }
    @Override public void replaceAllLoadedSCOFieldsWithWrappers() { }
    @Override public void replaceAllLoadedSCOFieldsWithValues() { }
    @Override public void provideFields(final int[] fieldNumbers, final FieldManager fm) { }
    @Override public Object provideField(final int fieldNumber) { return null;}
    @Override public void setAssociatedValue(final Object key, final Object value) { }
    @Override public Object getAssociatedValue(final Object key) { return null;}
    @Override public boolean containsAssociatedValue(final Object key) { return false;}
    @Override public void removeAssociatedValue(final Object key) { }
    @Override public int[] getDirtyFieldNumbers() { return null;}
    @Override public String[] getDirtyFieldNames() { return null;}
    @Override public boolean[] getDirtyFields() { return null;}
    @Override public void makeDirty(final int field) { }
    @Override public boolean isEmbedded() { return false;}
    @Override public void updateOwnerFieldInEmbeddedField(final int fieldNumber, final Object value) { }
    @Override public void setStoringPC() { }
    @Override public void unsetStoringPC() { }
    @Override public void setInserting() { }
    @Override public void setInsertingCallbacks() { }
    @Override public boolean isFlushedToDatastore() { return false;}
    @Override public boolean isFlushedNew() { return false;}
    @Override public void setFlushedNew(final boolean flag) { }
    @Override public void flush() { }
    @Override public void setFlushing(final boolean flushing) { }
    @Override public void markAsFlushed() { }
    @Override public void locate() { }
    @Override public boolean isWaitingToBeFlushedToDatastore() { return false;}
    @Override public boolean isInserting() { return false;}
    @Override public boolean isDeleting() { return false;}
    @Override public boolean becomingDeleted() { return false;}
    @Override public boolean isDeleted() { return false;}
    @Override public boolean isDetaching() { return false;}
    @Override public void loadFieldValues(final FieldValues fv) { }
    @Override public Persistable getReferencedPC() { return null;}
    @Override public void loadField(final int fieldNumber) { }
    @Override public boolean loadStoredField(final int fieldNumber) { return false;}
    @Override public void storeFieldValue(final int fieldNumber, final Object value) { }
    @Override public void loadFieldsInFetchPlan(final FetchPlanState state) { }
    @Override public void loadFieldFromDatastore(final int fieldNumber) { }
    @Override public void loadUnloadedFieldsInFetchPlan() { }
    @Override public void loadUnloadedFieldsOfClassInFetchPlan(final FetchPlan fetchPlan) { }
    @Override public void loadUnloadedRelationFields() { }
    @Override public void loadUnloadedFields() { }
    @Override public void unloadField(final int fieldNumber) { }
    @Override public void unloadNonFetchPlanFields() { }
    @Override public void refreshLoadedFields() { }
    @Override public void refreshFieldsInFetchPlan() { }
    @Override public void clearNonPrimaryKeyFields() { }
    @Override public void restoreFields() { }
    @Override public void saveFields() { }
    @Override public void clearSavedFields() { }
    @Override public void clearFields() { }
    @Override public void registerTransactional() { }
    @Override public boolean isRestoreValues() { return false;}
    @Override public void clearLoadedFlags() { }
    @Override public boolean[] getLoadedFields() { return null;}
    @Override public int[] getLoadedFieldNumbers() { return null;}
    @Override public String[] getLoadedFieldNames() { return null;}
    @Override public boolean isLoaded(final int fieldNumber) { return false;}
    @Override public boolean getAllFieldsLoaded() { return false;}
    @Override public boolean isFieldLoaded(final int fieldNumber) { return false;}
    @Override public void updateFieldAfterInsert(final Object pc, final int fieldNumber) { }
    @Override public void setPostStoreNewObjectId(final Object id) { }
    @Override public void replaceManagedPC(final Persistable pc) { }
    @Override public void setTransactionalVersion(final Object nextVersion) { }
    @Override public Object getTransactionalVersion() { return null;}
    @Override public void setVersion(final Object version) { }
    @Override public Object getVersion() { return null;}
    @Override public boolean isVersionLoaded() { return false;}
    @Override public void evictFromTransaction() { }
    @Override public void enlistInTransaction() { }
    @Override public void makeTransactional() { }
    @Override public void makeNontransactional() { }
    @Override public void makeTransient(final FetchPlanState state) { }
    @Override public void makeTransientForReachability() { }
    @Override public void makePersistent() { }
    @Override public void makePersistentTransactionalTransient() { }
    @Override public void deletePersistent() { }
    @Override public Persistable attachCopy(final Persistable detachedPC, final boolean embedded) { return null;}
    @Override public void attach(final boolean embedded) { }
    @Override public void attach(final Persistable detachedPC) { }
    @Override public Persistable detachCopy(final FetchPlanState state) { return null;}
    @Override public void detach(final FetchPlanState state) { }
    @Override public void validate() { }
    @Override public void markForInheritanceValidation() { }
    @Override public void evict() { }
    @Override public void refresh() { }
    @Override public void retrieve(final boolean fgOnly) { }
    @Override public void preBegin(final Transaction tx) { }
    @Override public void postCommit(final Transaction tx) { }
    @Override public void preRollback(final Transaction tx) { }
    @Override public void resetDetachState() { }
    @Override public void retrieveDetachState(final DNStateManager sm) { }
    @Override public void checkInheritance(final FieldValues fv) { }
    @Override public void markFieldsAsLoaded(final int[] fieldNumbers) { }

    @Override public boolean getBooleanField(final Persistable pc, final int field, final boolean currentValue) {
        throw invalidFieldAccess(pc);
    }

    @Override public char getCharField(final Persistable pc, final int field, final char currentValue) {
        throw invalidFieldAccess(pc);
    }

    @Override public byte getByteField(final Persistable pc, final int field, final byte currentValue) {
        throw invalidFieldAccess(pc);
    }

    @Override public short getShortField(final Persistable pc, final int field, final short currentValue) {
        throw invalidFieldAccess(pc);
    }

    @Override public int getIntField(final Persistable pc, final int field, final int currentValue) {
        throw invalidFieldAccess(pc);
    }

    @Override public long getLongField(final Persistable pc, final int field, final long currentValue) {
        throw invalidFieldAccess(pc);
    }

    @Override public float getFloatField(final Persistable pc, final int field, final float currentValue) {
        throw invalidFieldAccess(pc);
    }

    @Override public double getDoubleField(final Persistable pc, final int field, final double currentValue) {
        throw invalidFieldAccess(pc);
    }

    @Override public String getStringField(final Persistable pc, final int field, final String currentValue) {
        throw invalidFieldAccess(pc);
        //return null; // mimics behavior as if there was no StateManager
    }

    @Override public Object getObjectField(final Persistable pc, final int field, final Object currentValue) {
        throw invalidFieldAccess(pc);
        //return null; // mimics behavior as if there was no StateManager
    }

    /**
     * There is no point in trying to generate a message from a hollow {@link Persistable}'s,
     * toString() method, as its fields are all set to null.
     * Instead we provide type and OID information.
     */
    private RuntimeException invalidFieldAccess(final @Nullable Persistable pc) {
        return  _Exceptions.unrecoverable
                ("JDO entity %s (oid=%s) is in HOLLOW state, its fields are no longer valid.",
                pc!=null
                    ? pc.getClass().getName()
                    : "Persistable",
                oidStringified);
    }

}
