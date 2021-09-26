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
package org.apache.isis.core.metamodel.interactions.managed;

import java.util.Optional;
import java.util.stream.Stream;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.collections.CollectionFacet;
import org.apache.isis.core.metamodel.interactions.managed.nonscalar.DataTableModel;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public final class ManagedCollection extends ManagedMember {

    // -- FACTORIES

    public static final ManagedCollection of(
            final @NonNull ManagedObject owner,
            final @NonNull OneToManyAssociation collection,
            final @NonNull Where where) {
        return new ManagedCollection(owner, collection, where);
    }

    public static final Optional<ManagedCollection> lookupCollection(
            final @NonNull ManagedObject owner,
            final @NonNull String memberId,
            final @NonNull Where where) {

        return ManagedMember.<OneToManyAssociation>lookup(owner, MemberType.COLLECTION, memberId)
        .map(objectAction -> of(owner, objectAction, where));
    }

    // -- IMPLEMENTATION

    @Getter private final OneToManyAssociation collection;

    private ManagedCollection(
            final @NonNull ManagedObject owner,
            final @NonNull OneToManyAssociation collection,
            final @NonNull Where where) {

        super(owner, where);
        this.collection = collection;
    }

    @Override
    public OneToManyAssociation getMetaModel() {
        return getCollection();
    }

    @Override
    public MemberType getMemberType() {
        return MemberType.COLLECTION;
    }

    public ObjectSpecification getElementSpecification() {
        return getCollection().getElementType();
    }

    public ManagedObject getCollectionValue() {
        return Optional.ofNullable(getCollection().get(getOwner(), InteractionInitiatedBy.USER))
                .orElse(ManagedObject.empty(getElementSpecification()));
    }

    // -- INTERACTION

    /**
     * If visibility is vetoed, returns an empty Stream.
     * @param interactionInitiatedBy
     * @return Stream of this collection's element values as to be used by the UI for representation
     */
    public Stream<ManagedObject> streamElements(final InteractionInitiatedBy interactionInitiatedBy) {
        val valueAdapter = getCollection().get(getOwner(), interactionInitiatedBy);
        return CollectionFacet.streamAdapters(valueAdapter);
    }

    /**
     * If visibility is vetoed, returns an empty Stream.
     * @return Stream of this collection's element values as to be used by the UI for representation
     */
    public Stream<ManagedObject> streamElements() {
        return streamElements(InteractionInitiatedBy.USER);
    }

    public DataTableModel createDataTableModel() {
        return new DataTableModel(this);
    }

}
