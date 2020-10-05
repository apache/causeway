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
package org.apache.isis.core.runtime.persistence.transaction;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.IsisInteractionScope;
import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.annotation.PublishingChangeKind;
import org.apache.isis.applib.services.HasUniqueId;
import org.apache.isis.applib.services.TransactionScopeListener;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects.EntityUtil;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

// tag::refguide[]
@Service
@Named("isisRuntime.ChangedObjectsService")
@Order(OrderPrecedence.EARLY)
@Primary
@Qualifier("Default")
@IsisInteractionScope
//@Log4j2
public class ChangedObjectsService 
implements TransactionScopeListener, HasEnlistedForAuditing, HasEnlistedForPublishing, HasEnlistedForMetrics {

    // end::refguide[]
    /**
     * Used for auditing: this contains the pre- values of every property of every object enlisted.
     *
     * <p>
     *     When {@link #getChangedObjectProperties()} is called, then this is cleared out and {@link #changedObjectProperties} is non-null, containing
     *     the actual differences.
     * </p>
     */
    // tag::refguide[]
    private final Map<AdapterAndProperty, PreAndPostValues> enlistedObjectProperties = _Maps.newLinkedHashMap();

    /**
     * Used for auditing; contains the pre- and post- values of every property of every object that actually changed.
     *
     * <p>
     *  Will be null until {@link #getChangedObjectProperties()} is called, thereafter contains the actual changes.
     * </p>
     */
    private final _Lazy<Set<AuditEntry>> changedObjectPropertiesRef = _Lazy.threadSafe(this::capturePostValuesAndDrain);


    // used for publishing
    @Getter(onMethod_ = {@Override})
    private final Map<ManagedObject, PublishingChangeKind> changeKindByEnlistedAdapter = _Maps.newLinkedHashMap();
    

    public boolean isEnlisted(final @NonNull ManagedObject adapter) {
        return changeKindByEnlistedAdapter.containsKey(adapter);
    }


    /**
     * Auditing and publishing support: for object stores to enlist an object that has just been created,
     * capturing a dummy value <tt>'[NEW]'</tt> for the pre-modification value.
     *
     * <p>
     * The post-modification values are captured when the transaction commits.
     *
     * <p>
     * Supported by the JDO object store; check documentation for support in other objectstores.
     */
    public void enlistCreated(final @NonNull ManagedObject adapter) {
        if(shouldIgnore(adapter)) {
            return;
        }
        enlistForPublishing(adapter, PublishingChangeKind.CREATE);
        enlistForAuditing(adapter, aap->PreAndPostValues.pre(IsisTransactionPlaceholder.NEW));
    }


    /**
     * Auditing and publishing support: for object stores to enlist an object that is about to be updated,
     * capturing the pre-modification values of the properties of the {@link ManagedObject}.
     *
     * <p>
     * The post-modification values are captured when the transaction commits.
     *
     * <p>
     * Supported by the JDO object store; check documentation for support in other objectstores.
     */
    public void enlistUpdating(final @NonNull ManagedObject adapter) {
        if(shouldIgnore(adapter)) {
            return;
        }
        enlistForPublishing(adapter, PublishingChangeKind.UPDATE);
        enlistForAuditing(adapter, aap->PreAndPostValues.pre(aap.getPropertyValue()));
    }

    /**
     * Auditing and publishing support: for object stores to enlist an object that is about to be deleted,
     * capturing the pre-deletion value of the properties of the {@link ManagedObject}.
     *
     * <p>
     * The post-modification values are captured  when the transaction commits.  In the case of deleted objects, a
     * dummy value <tt>'[DELETED]'</tt> is used as the post-modification value.
     *
     * <p>
     * Supported by the JDO object store; check documentation for support in other objectstores.
     */
    public void enlistDeleting(final @NonNull ManagedObject adapter) {
        if(shouldIgnore(adapter)) {
            return;
        }
        final boolean enlisted = enlistForPublishing(adapter, PublishingChangeKind.DELETE);
        if(!enlisted) {
            return;
        }
        enlistForAuditing(adapter, aap->PreAndPostValues.pre(aap.getPropertyValue()));
    }


    @Override
    public Set<AuditEntry> getChangedObjectProperties() {
        return changedObjectPropertiesRef.get();
    }

    @Override
    public int numberObjectPropertiesModified() {
        return changedObjectPropertiesRef.get().size();
    }
    
    @Override
    public int numberObjectsDirtied() {
        return changeKindByEnlistedAdapter.size();
    }
    
    protected boolean shouldIgnore(final @NonNull ManagedObject adapter) {
        val spec = adapter.getSpecification();
        return HasUniqueId.class.isAssignableFrom(spec.getCorrespondingClass());
    }

    // end::refguide[]
    
    /**
     * @apiNote intended to be called at the end of a transaction by the framework internally
     */
    @Override
    public void onTransactionEnding() {
        System.err.println("PRE COMMIT");
        enlistedObjectProperties.clear();
        changeKindByEnlistedAdapter.clear();
        changedObjectPropertiesRef.clear();
    }

    // -- HELPER

    static String asString(Object object) {
        return object != null? object.toString(): null;
    }
    
    /**
     * @return <code>true</code> if successfully enlisted, <code>false</code> if was already enlisted
     */
    private boolean enlistForPublishing(
            final @NonNull ManagedObject adapter, 
            final @NonNull PublishingChangeKind current) {
        final PublishingChangeKind previous = changeKindByEnlistedAdapter.get(adapter);
        if(previous == null) {
            changeKindByEnlistedAdapter.put(adapter, current);
            return true;
        }
        switch (previous) {
        case CREATE:
            switch (current) {
            case DELETE:
                changeKindByEnlistedAdapter.remove(adapter);
            case CREATE:
            case UPDATE:
                return false;
            }
            break;
        case UPDATE:
            switch (current) {
            case DELETE:
                changeKindByEnlistedAdapter.put(adapter, current);
                return true;
            case CREATE:
            case UPDATE:
                return false;
            }
            break;
        case DELETE:
            return false;
        }
        return previous == null;
    }

    private void enlistForAuditing(
            final ManagedObject adapter, 
            final Function<AdapterAndProperty, PreAndPostValues> pre) {
        
        if(changedObjectPropertiesRef.isMemoized()) {
            throw _Exceptions.illegalState("Cannot enlist additional changes for auditing, "
                    + "since changedObjectPropertiesRef was already prepared (memoized) for auditing.");
        }
        
        System.err.println("ENLIST " + adapter);
        
        adapter.getSpecification()
        .streamAssociations(Contributed.EXCLUDED)
        .filter(ObjectAssociation.Predicates.PROPERTIES)
        .filter(property->!property.isNotPersisted())
        .map(property->AdapterAndProperty.of(adapter, property))
        .filter(aap->!enlistedObjectProperties.containsKey(aap)) // already enlisted, so ignore
        .forEach(aap->{
            enlistedObjectProperties.put(aap, pre.apply(aap));
        });
    }
    
    /** 
     * For any enlisted Object Properties collects those, that are meant for auditing. 
     * then clears enlisted objects.
     */
    private Set<AuditEntry> capturePostValuesAndDrain() {
        
        System.err.println("capturePostValuesAndDrain");
        
        val postValues = enlistedObjectProperties.entrySet().stream()
        .peek(this::updatePostOn)
        .filter(PreAndPostValues::shouldAudit)
        .map(entry->AuditEntry.of(entry.getKey(), entry.getValue()))
        .collect(_Sets.toUnmodifiable());
        
        enlistedObjectProperties.clear();
        
        return postValues;

    }

    private final void updatePostOn(Map.Entry<AdapterAndProperty, PreAndPostValues> enlistedEntry) {
        val adapterAndProperty = enlistedEntry.getKey();
        val preAndPostValues = enlistedEntry.getValue();
        val adapter = adapterAndProperty.getAdapter();
        if(EntityUtil.isDestroyed(adapter)) {
            // don't touch the object!!!
            // JDO, for example, will complain otherwise...
            preAndPostValues.setPost(IsisTransactionPlaceholder.DELETED);
        } else {
            preAndPostValues.setPost(adapterAndProperty.getPropertyValue());
        }
    }
    
}
