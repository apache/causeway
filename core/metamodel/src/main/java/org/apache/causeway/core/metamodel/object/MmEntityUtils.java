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
package org.apache.causeway.core.metamodel.object;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.services.repository.EntityState;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.config.beans.PersistenceStack;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.causeway.core.metamodel.facets.properties.property.entitychangepublishing.EntityPropertyChangePublishingPolicyFacet;
import org.apache.causeway.core.metamodel.services.objectlifecycle.PropertyChangeRecordId;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class MmEntityUtils {

    @NonNull
    public Optional<PersistenceStack> getPersistenceStandard(final @Nullable ManagedObject adapter) {
        if(adapter==null) {
            return Optional.empty();
        }
        var spec = adapter.getSpecification();
        if(spec==null || !spec.isEntity()) {
            return Optional.empty();
        }

        return spec.entityFacet()
                .map(EntityFacet::getPersistenceStack);
    }

    @NonNull
    public EntityState getEntityState(final @Nullable ManagedObject adapter) {
        return adapter!=null
             ? adapter.getEntityState()
             : EntityState.NOT_PERSISTABLE;
    }

    public void persistInCurrentTransaction(final ManagedObject managedObject) {
        requiresEntity(managedObject);
        var spec = managedObject.getSpecification();
        var entityFacet = spec.entityFacetElseFail();
        entityFacet.persist(managedObject.getPojo());
    }

    public void deleteInCurrentTransaction(final ManagedObject managedObject) {
        requiresEntity(managedObject);
        var spec = managedObject.getSpecification();
        var entityFacet = spec.entityFacetElseFail();
        entityFacet.delete(managedObject.getPojo());
    }

    public void requiresEntity(final ManagedObject managedObject) {
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(managedObject)) {
            throw _Exceptions.illegalArgument("requires an entity object but got null, unspecified or empty");
        }
        var spec = managedObject.getSpecification();
        if(!spec.isEntity()) {
            throw _Exceptions.illegalArgument("not an entity type %s (sort=%s)",
                    spec.getCorrespondingClass(),
                    spec.getBeanSort());
        }
    }

    /**
     * Handles transient entities that have no OID yet, but get one once the current transaction flushes.
     * As a side-effect transitions a transient entity to a bookmarked one. For bookmarked entities,
     * or any non-entity types acts as a no-op.
     */
    public void ifHasNoOidThenFlush(final @Nullable ManagedObject entity) {
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(entity)
                || !entity.getSpecialization().isEntity()
                || entity.isBookmarkMemoized()) {
            return;
        }
        if(!getEntityState(entity).hasOid()) {
            entity.getTransactionService().flushTransaction();
            // force reassessment: as a side-effect transitions the transient entity to a bookmarked one
            entity.getEntityState();
        }
    }

    /**
     * Side-effect free check for whether given entity is attached.
     */
    public boolean isAttachedEntity(final @Nullable ManagedObject entity) {
        return entity!=null
                ? entity.getSpecialization().isEntity()
                    && entity.isBookmarkMemoized()
                    && entity.getEntityState().isAttached()
                : false;
    }

    /**
     * @param managedObject
     * @return managedObject
     * @throws AssertionError if managedObject is a detached entity
     */
    @NonNull
    public ManagedObject requiresAttached(final @NonNull ManagedObject managedObject) {
        if(managedObject instanceof PackedManagedObject) {
            ((PackedManagedObject)managedObject).unpack().forEach(MmEntityUtils::requiresAttached);
            return managedObject;
        }
        var entityState = MmEntityUtils.getEntityState(managedObject);
        if(entityState.isPersistable()) {
            // ensure we have an attached entity
            _Assert.assertEquals(
                    EntityState.ATTACHED,
                    entityState,
                    ()-> String.format("entity %s is required to be attached (not detached)",
                            managedObject.getSpecification().getLogicalTypeName()));
        }
        return managedObject;
    }

    public void requiresWhenFirstIsBookmarkableSecondIsAlso(
            final ManagedObject first,
            final ManagedObject second) {

        if(!ManagedObjects.isIdentifiable(first) || !ManagedObjects.isSpecified(second)) {
            return;
        }
        var secondSpec = second.getSpecification();
        if(secondSpec.isParented() || !secondSpec.isEntity()) {
            return;
        }

        if(!MmEntityUtils.getEntityState(second).hasOid()) {
            throw _Exceptions.illegalArgument(
                    "can't set a reference to a transient object [%s] from a persistent one [%s]",
                    second,
                    first.getTitle());
        }
    }

    // -- PROPERTY CHANGE PUBLISHING

    public Stream<OneToOneAssociation> streamPropertiesEnabledForChangePublishing(final @NonNull ManagedObject entity) {
        return entity.getSpecification().streamProperties(MixedIn.EXCLUDED)
            .filter(property->!EntityPropertyChangePublishingPolicyFacet.isExcludedFromPublishing(property));
    }

    public Stream<PropertyChangeRecordId> streamPropertyChangeRecordIdsForChangePublishing(final @NonNull ManagedObject entity) {
        return streamPropertiesEnabledForChangePublishing(entity)
                .map(property->PropertyChangeRecordId.of(entity, property));
    }

    public Optional<OneToOneAssociation> lookupPropertyEnabledForChangePublishing(
            final @NonNull ManagedObject entity, final String propertyName) {
        return entity
                .getSpecification()
                .getProperty(propertyName, MixedIn.EXCLUDED)
                .filter(property -> !EntityPropertyChangePublishingPolicyFacet.isExcludedFromPublishing(property));
    }

    public Optional<PropertyChangeRecordId> lookupPropertyChangeRecordIdForChangePublishing(
            final @NonNull ManagedObject entity, final String propertyName) {
        return lookupPropertyEnabledForChangePublishing(entity, propertyName)
                .map(property->PropertyChangeRecordId.of(entity, property));
    }

}
