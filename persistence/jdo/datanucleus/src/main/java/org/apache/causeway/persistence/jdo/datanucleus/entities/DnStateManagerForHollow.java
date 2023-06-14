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

import lombok.NonNull;

/**
 * Used to memoize stringified OIDs of entities,
 * to be stored in the {@link Persistable}'s dnStateManager field.
 * <p>
 * Has no purpose otherwise.
 */
public class DnStateManagerForHollow implements DNStateManager<Persistable> {

    public final @NonNull String oidStringified;

    public DnStateManagerForHollow(final AbstractClassMetaData cmd, final @NonNull String oidStringified) {
        this.oidStringified = oidStringified;
    }

    @Override
    public ExecutionContextReference getExecutionContextReference() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte replacingFlags(final Persistable pc) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public StateManager replacingStateManager(final Persistable pc, final StateManager sm) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isDirty(final Persistable pc) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isTransactional(final Persistable pc) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isPersistent(final Persistable pc) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isNew(final Persistable pc) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isDeleted(final Persistable pc) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void makeDirty(final Persistable pc, final String fieldName) {
        // TODO Auto-generated method stub

    }

    @Override
    public Object getObjectId(final Persistable pc) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getTransactionalObjectId(final Persistable pc) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getVersion(final Persistable pc) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isLoaded(final Persistable pc, final int field) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void preSerialize(final Persistable pc) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBooleanField(final Persistable pc, final int field, final boolean currentValue, final boolean newValue) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setCharField(final Persistable pc, final int field, final char currentValue, final char newValue) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setByteField(final Persistable pc, final int field, final byte currentValue, final byte newValue) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setShortField(final Persistable pc, final int field, final short currentValue, final short newValue) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setIntField(final Persistable pc, final int field, final int currentValue, final int newValue) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setLongField(final Persistable pc, final int field, final long currentValue, final long newValue) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setFloatField(final Persistable pc, final int field, final float currentValue, final float newValue) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setDoubleField(final Persistable pc, final int field, final double currentValue, final double newValue) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setStringField(final Persistable pc, final int field, final String currentValue, final String newValue) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setObjectField(final Persistable pc, final int field, final Object currentValue, final Object newValue) {
        // TODO Auto-generated method stub

    }

    @Override
    public void providedBooleanField(final Persistable pc, final int field, final boolean currentValue) {
        // TODO Auto-generated method stub

    }

    @Override
    public void providedCharField(final Persistable pc, final int field, final char currentValue) {
        // TODO Auto-generated method stub

    }

    @Override
    public void providedByteField(final Persistable pc, final int field, final byte currentValue) {
        // TODO Auto-generated method stub

    }

    @Override
    public void providedShortField(final Persistable pc, final int field, final short currentValue) {
        // TODO Auto-generated method stub

    }

    @Override
    public void providedIntField(final Persistable pc, final int field, final int currentValue) {
        // TODO Auto-generated method stub

    }

    @Override
    public void providedLongField(final Persistable pc, final int field, final long currentValue) {
        // TODO Auto-generated method stub

    }

    @Override
    public void providedFloatField(final Persistable pc, final int field, final float currentValue) {
        // TODO Auto-generated method stub

    }

    @Override
    public void providedDoubleField(final Persistable pc, final int field, final double currentValue) {
        // TODO Auto-generated method stub

    }

    @Override
    public void providedStringField(final Persistable pc, final int field, final String currentValue) {
        // TODO Auto-generated method stub

    }

    @Override
    public void providedObjectField(final Persistable pc, final int field, final Object currentValue) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean replacingBooleanField(final Persistable pc, final int field) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public char replacingCharField(final Persistable pc, final int field) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public byte replacingByteField(final Persistable pc, final int field) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public short replacingShortField(final Persistable pc, final int field) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int replacingIntField(final Persistable pc, final int field) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long replacingLongField(final Persistable pc, final int field) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float replacingFloatField(final Persistable pc, final int field) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double replacingDoubleField(final Persistable pc, final int field) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String replacingStringField(final Persistable pc, final int field) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object replacingObjectField(final Persistable pc, final int field) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object[] replacingDetachedState(final Detachable pc, final Object[] state) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void connect(final ExecutionContext ec, final AbstractClassMetaData cmd) {
        // TODO Auto-generated method stub

    }

    @Override
    public void disconnect() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isConnected() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void initialiseForHollow(final Object id, final FieldValues fv, final Class<Persistable> pcClass) {
        // TODO Auto-generated method stub

    }

    @Override
    public void initialiseForHollowAppId(final FieldValues fv, final Class<Persistable> pcClass) {
        // TODO Auto-generated method stub

    }

    @Override
    public void initialiseForHollowPreConstructed(final Object id, final Persistable pc) {
        // TODO Auto-generated method stub

    }

    @Override
    public void initialiseForPersistentClean(final Object id, final Persistable pc) {
        // TODO Auto-generated method stub

    }

    @Override
    public void initialiseForEmbedded(final Persistable pc, final boolean copyPc) {
        // TODO Auto-generated method stub

    }

    @Override
    public void initialiseForEmbedded(final Class<Persistable> pcClass) {
        // TODO Auto-generated method stub

    }

    @Override
    public void initialiseForPersistentNew(final Persistable pc, final FieldValues preInsertChanges) {
        // TODO Auto-generated method stub

    }

    @Override
    public void initialiseForTransactionalTransient(final Persistable pc) {
        // TODO Auto-generated method stub

    }

    @Override
    public void initialiseForDetached(final Persistable pc, final Object id, final Object version) {
        // TODO Auto-generated method stub

    }

    @Override
    public void initialiseForPNewToBeDeleted(final Persistable pc) {
        // TODO Auto-generated method stub

    }

    @Override
    public void initialiseForCachedPC(final CachedPC cachedPC, final Object id) {
        // TODO Auto-generated method stub

    }

    @Override
    public AbstractClassMetaData getClassMetaData() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ExecutionContext getExecutionContext() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StoreManager getStoreManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FetchPlanForClass getFetchPlanForClass() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Persistable getObject() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getObjectAsPrintable() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getInternalObjectId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getExternalObjectId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LifeCycleState getLifecycleState() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void replaceField(final int fieldNumber, final Object value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void replaceFieldMakeDirty(final int fieldNumber, final Object value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void replaceFieldValue(final int fieldNumber, final Object newValue) {
        // TODO Auto-generated method stub

    }

    @Override
    public void replaceFields(final int[] fieldNumbers, final FieldManager fm) {
        // TODO Auto-generated method stub

    }

    @Override
    public void replaceFields(final int[] fieldNumbers, final FieldManager fm, final boolean replaceWhenDirty) {
        // TODO Auto-generated method stub

    }

    @Override
    public void replaceNonLoadedFields(final int[] fieldNumbers, final FieldManager fm) {
        // TODO Auto-generated method stub

    }

    @Override
    public void replaceAllLoadedSCOFieldsWithWrappers() {
        // TODO Auto-generated method stub

    }

    @Override
    public void replaceAllLoadedSCOFieldsWithValues() {
        // TODO Auto-generated method stub

    }

    @Override
    public void provideFields(final int[] fieldNumbers, final FieldManager fm) {
        // TODO Auto-generated method stub

    }

    @Override
    public Object provideField(final int fieldNumber) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setAssociatedValue(final Object key, final Object value) {
        // TODO Auto-generated method stub

    }

    @Override
    public Object getAssociatedValue(final Object key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean containsAssociatedValue(final Object key) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void removeAssociatedValue(final Object key) {
        // TODO Auto-generated method stub

    }

    @Override
    public int[] getDirtyFieldNumbers() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getDirtyFieldNames() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean[] getDirtyFields() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void makeDirty(final int field) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isEmbedded() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void updateOwnerFieldInEmbeddedField(final int fieldNumber, final Object value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setStoringPC() {
        // TODO Auto-generated method stub

    }

    @Override
    public void unsetStoringPC() {
        // TODO Auto-generated method stub

    }

    @Override
    public void setInserting() {
        // TODO Auto-generated method stub

    }

    @Override
    public void setInsertingCallbacks() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isFlushedToDatastore() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isFlushedNew() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setFlushedNew(final boolean flag) {
        // TODO Auto-generated method stub

    }

    @Override
    public void flush() {
        // TODO Auto-generated method stub

    }

    @Override
    public void setFlushing(final boolean flushing) {
        // TODO Auto-generated method stub

    }

    @Override
    public void markAsFlushed() {
        // TODO Auto-generated method stub

    }

    @Override
    public void locate() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isWaitingToBeFlushedToDatastore() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isInserting() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isDeleting() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean becomingDeleted() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isDeleted() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isDetaching() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void loadFieldValues(final FieldValues fv) {
        // TODO Auto-generated method stub

    }

    @Override
    public Persistable getReferencedPC() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void loadField(final int fieldNumber) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean loadStoredField(final int fieldNumber) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void storeFieldValue(final int fieldNumber, final Object value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void loadFieldsInFetchPlan(final FetchPlanState state) {
        // TODO Auto-generated method stub

    }

    @Override
    public void loadFieldFromDatastore(final int fieldNumber) {
        // TODO Auto-generated method stub

    }

    @Override
    public void loadUnloadedFieldsInFetchPlan() {
        // TODO Auto-generated method stub

    }

    @Override
    public void loadUnloadedFieldsOfClassInFetchPlan(final FetchPlan fetchPlan) {
        // TODO Auto-generated method stub

    }

    @Override
    public void loadUnloadedRelationFields() {
        // TODO Auto-generated method stub

    }

    @Override
    public void loadUnloadedFields() {
        // TODO Auto-generated method stub

    }

    @Override
    public void unloadField(final int fieldNumber) {
        // TODO Auto-generated method stub

    }

    @Override
    public void unloadNonFetchPlanFields() {
        // TODO Auto-generated method stub

    }

    @Override
    public void refreshLoadedFields() {
        // TODO Auto-generated method stub

    }

    @Override
    public void refreshFieldsInFetchPlan() {
        // TODO Auto-generated method stub

    }

    @Override
    public void clearNonPrimaryKeyFields() {
        // TODO Auto-generated method stub

    }

    @Override
    public void restoreFields() {
        // TODO Auto-generated method stub

    }

    @Override
    public void saveFields() {
        // TODO Auto-generated method stub

    }

    @Override
    public void clearSavedFields() {
        // TODO Auto-generated method stub

    }

    @Override
    public void clearFields() {
        // TODO Auto-generated method stub

    }

    @Override
    public void registerTransactional() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isRestoreValues() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void clearLoadedFlags() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean[] getLoadedFields() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int[] getLoadedFieldNumbers() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getLoadedFieldNames() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isLoaded(final int fieldNumber) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean getAllFieldsLoaded() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isFieldLoaded(final int fieldNumber) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void updateFieldAfterInsert(final Object pc, final int fieldNumber) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setPostStoreNewObjectId(final Object id) {
        // TODO Auto-generated method stub

    }

    @Override
    public void replaceManagedPC(final Persistable pc) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setTransactionalVersion(final Object nextVersion) {
        // TODO Auto-generated method stub

    }

    @Override
    public Object getTransactionalVersion() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setVersion(final Object version) {
        // TODO Auto-generated method stub

    }

    @Override
    public Object getVersion() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isVersionLoaded() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void evictFromTransaction() {
        // TODO Auto-generated method stub

    }

    @Override
    public void enlistInTransaction() {
        // TODO Auto-generated method stub

    }

    @Override
    public void makeTransactional() {
        // TODO Auto-generated method stub

    }

    @Override
    public void makeNontransactional() {
        // TODO Auto-generated method stub

    }

    @Override
    public void makeTransient(final FetchPlanState state) {
        // TODO Auto-generated method stub

    }

    @Override
    public void makeTransientForReachability() {
        // TODO Auto-generated method stub

    }

    @Override
    public void makePersistent() {
        // TODO Auto-generated method stub

    }

    @Override
    public void makePersistentTransactionalTransient() {
        // TODO Auto-generated method stub

    }

    @Override
    public void deletePersistent() {
        // TODO Auto-generated method stub

    }

    @Override
    public Persistable attachCopy(final Persistable detachedPC, final boolean embedded) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void attach(final boolean embedded) {
        // TODO Auto-generated method stub

    }

    @Override
    public void attach(final Persistable detachedPC) {
        // TODO Auto-generated method stub

    }

    @Override
    public Persistable detachCopy(final FetchPlanState state) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void detach(final FetchPlanState state) {
        // TODO Auto-generated method stub

    }

    @Override
    public void validate() {
        // TODO Auto-generated method stub

    }

    @Override
    public void markForInheritanceValidation() {
        // TODO Auto-generated method stub

    }

    @Override
    public void evict() {
        // TODO Auto-generated method stub

    }

    @Override
    public void refresh() {
        // TODO Auto-generated method stub

    }

    @Override
    public void retrieve(final boolean fgOnly) {
        // TODO Auto-generated method stub

    }

    @Override
    public void preBegin(final Transaction tx) {
        // TODO Auto-generated method stub

    }

    @Override
    public void postCommit(final Transaction tx) {
        // TODO Auto-generated method stub

    }

    @Override
    public void preRollback(final Transaction tx) {
        // TODO Auto-generated method stub

    }

    @Override
    public void resetDetachState() {
        // TODO Auto-generated method stub

    }

    @Override
    public void retrieveDetachState(final DNStateManager sm) {
        // TODO Auto-generated method stub

    }

    @Override
    public void checkInheritance(final FieldValues fv) {
        // TODO Auto-generated method stub

    }

    @Override
    public void markFieldsAsLoaded(final int[] fieldNumbers) {
        // TODO Auto-generated method stub

    }

}
