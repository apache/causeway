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
package org.apache.isis.core.runtime.persistence.changetracking;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.IsisInteractionScope;
import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.annotation.PublishingChangeKind;
import org.apache.isis.applib.events.lifecycle.AbstractLifecycleEvent;
import org.apache.isis.applib.services.HasUniqueId;
import org.apache.isis.applib.services.TransactionScopeListener;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.factory._InstanceUtil;
import org.apache.isis.core.metamodel.facets.object.callbacks.CallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.LifecycleEventFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.LoadedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.LoadedLifecycleEventFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistedLifecycleEventFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistingCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistingLifecycleEventFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.RemovingCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.RemovingLifecycleEventFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.UpdatedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.UpdatedLifecycleEventFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.UpdatingCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.UpdatingLifecycleEventFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects.EntityUtil;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.persistence.transaction.IsisTransactionPlaceholder;

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
implements TransactionScopeListener,
EntityChangeTracker,
HasEnlistedForAuditing, HasEnlistedForPublishing, HasEnlistedForMetrics {

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

    @Override
    public boolean isEnlisted(final @NonNull ManagedObject adapter) {
        return changeKindByEnlistedAdapter.containsKey(adapter);
    }

    private void enlistCreatedInternal(final @NonNull ManagedObject adapter) {
        if(shouldIgnore(adapter)) {
            return;
        }
        enlistForPublishing(adapter, PublishingChangeKind.CREATE);
        enlistForAuditing(adapter, aap->PreAndPostValues.pre(IsisTransactionPlaceholder.NEW));
    }

    private void enlistUpdatingInternal(final @NonNull ManagedObject adapter) {
        if(shouldIgnore(adapter)) {
            return;
        }
        enlistForPublishing(adapter, PublishingChangeKind.UPDATE);
        enlistForAuditing(adapter, aap->PreAndPostValues.pre(aap.getPropertyValue()));
    }

    private void enlistDeletingInternal(final @NonNull ManagedObject adapter) {
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

    // -- ENTITY CHANGE TRACKING

    @Override
    public void enlistCreated(ManagedObject entity) {
        val hasAlreadyBeenEnlisted = isEnlisted(entity);
        enlistCreatedInternal(entity);

        if(!hasAlreadyBeenEnlisted) {
            CallbackFacet.Util.callCallback(entity, PersistedCallbackFacet.class);
            postLifecycleEventIfRequired(entity, PersistedLifecycleEventFacet.class);
        }
    }

    @Override
    public void enlistDeleting(ManagedObject entity) {
        enlistDeletingInternal(entity);
        CallbackFacet.Util.callCallback(entity, RemovingCallbackFacet.class);
        postLifecycleEventIfRequired(entity, RemovingLifecycleEventFacet.class);
    }

    @Override
    public void enlistUpdating(ManagedObject entity) {
        val hasAlreadyBeenEnlisted = isEnlisted(entity);
        // we call this come what may;
        // additional properties may now have been changed, and the changeKind for publishing might also be modified
        enlistUpdatingInternal(entity);

        if(!hasAlreadyBeenEnlisted) {
            // prevent an infinite loop... don't call the 'updating()' callback on this object if we have already done so
            CallbackFacet.Util.callCallback(entity, UpdatingCallbackFacet.class);
            postLifecycleEventIfRequired(entity, UpdatingLifecycleEventFacet.class);
        }
    }

    @Override
    public void recognizeLoaded(ManagedObject entity) {
        CallbackFacet.Util.callCallback(entity, LoadedCallbackFacet.class);
        postLifecycleEventIfRequired(entity, LoadedLifecycleEventFacet.class);        
    }

    @Override
    public void recognizePersisting(ManagedObject entity) {
        CallbackFacet.Util.callCallback(entity, PersistingCallbackFacet.class);
        postLifecycleEventIfRequired(entity, PersistingLifecycleEventFacet.class);        
    }
    
    @Override
    public void recognizeUpdating(ManagedObject entity) {
        CallbackFacet.Util.callCallback(entity, UpdatedCallbackFacet.class);
        postLifecycleEventIfRequired(entity, UpdatedLifecycleEventFacet.class);
    }

    //  -- HELPER

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void postLifecycleEventIfRequired(
            ManagedObject adapter,
            Class<? extends LifecycleEventFacet> lifecycleEventFacetClass) {

        val lifecycleEventFacet = adapter.getSpecification().getFacet(lifecycleEventFacetClass);
        if(lifecycleEventFacet == null) {
            return;
        }
        val eventInstance = (AbstractLifecycleEvent) _InstanceUtil
                .createInstance(lifecycleEventFacet.getEventType());
        val pojo = adapter.getPojo();
        postEvent(eventInstance, pojo);

    }

    @Inject private EventBusService eventBusService;

    private void postEvent(final AbstractLifecycleEvent<Object> event, final Object pojo) {
        if(eventBusService!=null) {
            event.initSource(pojo);
            eventBusService.post(event);
        }
    }

}
