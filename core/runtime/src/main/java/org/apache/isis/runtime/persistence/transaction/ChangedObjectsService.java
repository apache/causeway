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
package org.apache.isis.runtime.persistence.transaction;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.annotation.PublishingChangeKind;
import org.apache.isis.applib.services.HasUniqueId;
import org.apache.isis.applib.services.WithTransactionScope;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.Contributed;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Named("isisRuntime.ChangedObjectsService")
@Order(OrderPrecedence.HIGH)
@Primary
@Qualifier("Default")
@RequestScoped
@Log4j2
public class ChangedObjectsService implements WithTransactionScope {

    /**
     * Used for auditing: this contains the pre- values of every property of every object enlisted.
     *
     * <p>
     *     When {@link #getChangedObjectProperties()} is called, then this is cleared out and {@link #changedObjectProperties} is non-null, containing
     *     the actual differences.
     * </p>
     */
    private final Map<AdapterAndProperty, PreAndPostValues> enlistedObjectProperties = _Maps.newLinkedHashMap();

    /**
     * Used for auditing; contains the pre- and post- values of every property of every object that actually changed.
     *
     * <p>
     *  Will be null until {@link #getChangedObjectProperties()} is called, thereafter contains the actual changes.
     * </p>
     */
    private Set<Map.Entry<AdapterAndProperty, PreAndPostValues>> changedObjectProperties;


    // used for publishing
    private final Map<ObjectAdapter,PublishingChangeKind> changeKindByEnlistedAdapter = _Maps.newLinkedHashMap();

    public boolean isEnlisted(ObjectAdapter adapter) {
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
    public void enlistCreated(final ObjectAdapter adapter) {

        if(shouldIgnore(adapter)) {
            return;
        }

        enlistForPublishing(adapter, PublishingChangeKind.CREATE);

        final Stream<ObjectAssociation> properties = adapter.getSpecification()
                .streamAssociations(Contributed.EXCLUDED)
                .filter(ObjectAssociation.Predicates.PROPERTIES);

        enlist(adapter, properties, aap->PreAndPostValues.pre(IsisTransactionPlaceholder.NEW));
    }


    /**
     * Auditing and publishing support: for object stores to enlist an object that is about to be updated,
     * capturing the pre-modification values of the properties of the {@link ObjectAdapter}.
     *
     * <p>
     * The post-modification values are captured when the transaction commits.
     *
     * <p>
     * Supported by the JDO object store; check documentation for support in other objectstores.
     */
    public void enlistUpdating(final ObjectAdapter adapter) {

        if(shouldIgnore(adapter)) {
            return;
        }

        enlistForPublishing(adapter, PublishingChangeKind.UPDATE);

        final Stream<ObjectAssociation> properties = adapter.getSpecification()
                .streamAssociations(Contributed.EXCLUDED)
                .filter(ObjectAssociation.Predicates.PROPERTIES);

        enlist(adapter, properties, aap->PreAndPostValues.pre(aap.getPropertyValue()));
    }

    /**
     * Auditing and publishing support: for object stores to enlist an object that is about to be deleted,
     * capturing the pre-deletion value of the properties of the {@link ObjectAdapter}.
     *
     * <p>
     * The post-modification values are captured  when the transaction commits.  In the case of deleted objects, a
     * dummy value <tt>'[DELETED]'</tt> is used as the post-modification value.
     *
     * <p>
     * Supported by the JDO object store; check documentation for support in other objectstores.
     */
    public void enlistDeleting(final ObjectAdapter adapter) {

        if(shouldIgnore(adapter)) {
            return;
        }

        final boolean enlisted = enlistForPublishing(adapter, PublishingChangeKind.DELETE);
        if(!enlisted) {
            return;
        }

        final Stream<ObjectAssociation> properties = adapter.getSpecification()
                .streamAssociations(Contributed.EXCLUDED)
                .filter(ObjectAssociation.Predicates.PROPERTIES);

        enlist(adapter, properties, aap->PreAndPostValues.pre(aap.getPropertyValue()));

    }


    /**
     * @return <code>true</code> if successfully enlisted, <code>false</code> if was already enlisted
     */
    private boolean enlistForPublishing(final ObjectAdapter adapter, final PublishingChangeKind current) {
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

    /**
     * Intended to be called at the end of the transaction.  Use {@link #resetForNextTransaction()} once fully read.
     */
    public Set<Map.Entry<AdapterAndProperty, PreAndPostValues>> getChangedObjectProperties() {
        return changedObjectProperties != null
                ? changedObjectProperties
                        : (changedObjectProperties = capturePostValuesAndDrain(enlistedObjectProperties));
    }

    private Set<Map.Entry<AdapterAndProperty, PreAndPostValues>> capturePostValuesAndDrain(final Map<AdapterAndProperty, PreAndPostValues> changedObjectProperties) {

        final Map<AdapterAndProperty, PreAndPostValues> processedObjectProperties = _Maps.newLinkedHashMap();

        while(!changedObjectProperties.isEmpty()) {

            final Set<AdapterAndProperty> keys = _Sets.newLinkedHashSet(changedObjectProperties.keySet());
            for (final AdapterAndProperty aap : keys) {

                val preAndPostValues = changedObjectProperties.remove(aap);

                val adapter = aap.getAdapter();
                if(ManagedObject._isDestroyed(adapter)) {
                    // don't touch the object!!!
                    // JDO, for example, will complain otherwise...
                    preAndPostValues.setPost(IsisTransactionPlaceholder.DELETED);
                } else {
                    preAndPostValues.setPost(aap.getPropertyValue());
                }

                // if we encounter the same objectProperty again, this will simply overwrite it
                processedObjectProperties.put(aap, preAndPostValues);
            }
        }

        return Collections.unmodifiableSet(
                processedObjectProperties.entrySet().stream()
                .filter(PreAndPostValues.Predicates.SHOULD_AUDIT)
                .collect(Collectors.toSet())    );

    }

    protected boolean shouldIgnore(final ObjectAdapter adapter) {
        final ObjectSpecification adapterSpec = adapter.getSpecification();
        final Class<?> adapterClass = adapterSpec.getCorrespondingClass();
        return HasUniqueId.class.isAssignableFrom(adapterClass);
    }


    public Map<ObjectAdapter, PublishingChangeKind> getChangeKindByEnlistedAdapter() {
        return changeKindByEnlistedAdapter;
    }

    public int numberObjectsDirtied() {
        return changeKindByEnlistedAdapter.size();
    }

    public int numberObjectPropertiesModified() {
        if(changedObjectProperties == null) {
            // normally done during auditing, but in case none of the objects in this xactn are audited...
            getChangedObjectProperties();
        }
        return changedObjectProperties.size();
    }

    /**
     * Intended to be called at the end of a transaction.  (This service really ought to be considered
     * a transaction-scoped service; since that isn't yet supported by the framework, we have to manually reset).
     */
    @Override
    public void resetForNextTransaction() {
        enlistedObjectProperties.clear();
        changedObjectProperties = null;
    }


    static String asString(Object object) {
        return object != null? object.toString(): null;
    }

    private void enlist(
            final ObjectAdapter adapter, 
            final Stream<ObjectAssociation> properties, 
            final Function<AdapterAndProperty, PreAndPostValues> pre) {
        properties
        .filter(property->!property.isNotPersisted())
        .map(property->AdapterAndProperty.of(adapter, property))
        .filter(aap->!enlistedObjectProperties.containsKey(aap)) // already enlisted, so ignore
        .forEach(aap->{
            enlistedObjectProperties.put(aap, pre.apply(aap));
        });
    }


}
