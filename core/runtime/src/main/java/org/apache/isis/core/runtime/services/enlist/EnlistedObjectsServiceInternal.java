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
package org.apache.isis.core.runtime.services.enlist;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.RequestScoped;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.PublishedObject;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction;

@DomainService(nature = NatureOfService.DOMAIN)
@RequestScoped
public class EnlistedObjectsServiceInternal {

    // used for auditing
    private final Map<AdapterAndProperty, PreAndPostValues> changedObjectProperties = Maps.newLinkedHashMap();

    // used for publishing
    private final Map<ObjectAdapter,PublishedObject.ChangeKind> changeKindByEnlistedAdapter = Maps.newLinkedHashMap();

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
    @Programmatic
    public void enlistCreated(final ObjectAdapter adapter) {

        enlistForPublishing(adapter, PublishedObject.ChangeKind.CREATE);

        for (ObjectAssociation property : adapter.getSpecification().getAssociations(Contributed.EXCLUDED, ObjectAssociation.Filters.PROPERTIES)) {
            final AdapterAndProperty aap = AdapterAndProperty.of(adapter, property);
            if(property.isNotPersisted()) {
                continue;
            }
            if(changedObjectProperties.containsKey(aap)) {
                // already enlisted, so ignore
                return;
            }
            PreAndPostValues papv = PreAndPostValues.pre(IsisTransaction.Placeholder.NEW);
            changedObjectProperties.put(aap, papv);
        }
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
    @Programmatic
    public void enlistUpdating(final ObjectAdapter adapter) {

        enlistForPublishing(adapter, PublishedObject.ChangeKind.UPDATE);

        for (ObjectAssociation property : adapter.getSpecification().getAssociations(Contributed.EXCLUDED, ObjectAssociation.Filters.PROPERTIES)) {
            final AdapterAndProperty aap = AdapterAndProperty.of(adapter, property);
            if(property.isNotPersisted()) {
                continue;
            }
            if(changedObjectProperties.containsKey(aap)) {
                // already enlisted, so ignore
                return;
            }
            PreAndPostValues papv = PreAndPostValues.pre(aap.getPropertyValue());
            changedObjectProperties.put(aap, papv);
        }
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
    @Programmatic
    public void enlistDeleting(final ObjectAdapter adapter) {

        final boolean enlisted = enlistForPublishing(adapter, PublishedObject.ChangeKind.DELETE);
        if(!enlisted) {
            return;
        }

        for (ObjectAssociation property : adapter.getSpecification().getAssociations(Contributed.EXCLUDED, ObjectAssociation.Filters.PROPERTIES)) {
            final AdapterAndProperty aap = AdapterAndProperty.of(adapter, property);
            if(property.isNotPersisted()) {
                continue;
            }
            if(changedObjectProperties.containsKey(aap)) {
                // already enlisted, so ignore
                return;
            }
            PreAndPostValues papv = PreAndPostValues.pre(aap.getPropertyValue());
            changedObjectProperties.put(aap, papv);
        }
    }


    /**
     *
     * @param adapter
     * @param current
     * @return <code>true</code> if successfully enlisted, <code>false</code> if was already enlisted
     */
    private boolean enlistForPublishing(final ObjectAdapter adapter, final PublishedObject.ChangeKind current) {
        final PublishedObject.ChangeKind previous = changeKindByEnlistedAdapter.get(adapter);
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


    @Programmatic
    public boolean hasChangedAdapters() {
        final Set<Map.Entry<AdapterAndProperty, PreAndPostValues>> changedObjectProperties = getChangedObjectProperties();

        final Set<ObjectAdapter> changedAdapters = Sets.newHashSet(
                Iterables.filter(
                        Iterables.transform(
                                changedObjectProperties,
                                AdapterAndProperty.Functions.GET_ADAPTER),
                        Predicates.not(IS_COMMAND)));
        return !changedAdapters.isEmpty();
    }

    private static final Predicate<ObjectAdapter> IS_COMMAND = new Predicate<ObjectAdapter>() {
        @Override
        public boolean apply(ObjectAdapter input) {
            return Command.class.isAssignableFrom(input.getSpecification().getCorrespondingClass());
        }
    };

    @Programmatic
    public Set<Map.Entry<AdapterAndProperty, PreAndPostValues>> getChangedObjectProperties() {
        final Map<AdapterAndProperty, PreAndPostValues> processedObjectProperties =  getAdapterAndPropertyPreAndPostValuesMap();

        return Collections.unmodifiableSet(
                Sets.filter(processedObjectProperties.entrySet(), PreAndPostValues.Predicates.CHANGED));
    }

    private Map<AdapterAndProperty, PreAndPostValues> getAdapterAndPropertyPreAndPostValuesMap() {
        final Map<AdapterAndProperty, PreAndPostValues> processedObjectProperties = Maps.newLinkedHashMap();

        while(!changedObjectProperties.isEmpty()) {

            final Set<AdapterAndProperty> keys = Sets.newLinkedHashSet(changedObjectProperties.keySet());
            for (final AdapterAndProperty aap : keys) {

                final PreAndPostValues papv = changedObjectProperties.remove(aap);

                final ObjectAdapter adapter = aap.getAdapter();
                if(adapter.isDestroyed()) {
                    // don't touch the object!!!
                    // JDO, for example, will complain otherwise...
                    papv.setPost(IsisTransaction.Placeholder.DELETED);
                } else {
                    papv.setPost(aap.getPropertyValue());
                }

                // if we encounter the same objectProperty again, this will simply overwrite it
                processedObjectProperties.put(aap, papv);
            }
        }
        return processedObjectProperties;
    }

    @Programmatic
    public void clearChangedObjectProperties() {
        changedObjectProperties.clear();
    }


    @Programmatic
    public Map<ObjectAdapter, PublishedObject.ChangeKind> getChangeKindByEnlistedAdapter() {
        return changeKindByEnlistedAdapter;
    }


    static String asString(Object object) {
        return object != null? object.toString(): null;
    }



}
