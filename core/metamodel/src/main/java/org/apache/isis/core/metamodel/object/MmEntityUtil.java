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
package org.apache.isis.core.metamodel.object;

import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.services.repository.EntityState;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.config.beans.PersistenceStack;
import org.apache.isis.core.metamodel.facets.object.entity.EntityFacet;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class MmEntityUtil {

    @NonNull
    public static Optional<PersistenceStack> getPersistenceStandard(final @Nullable ManagedObject adapter) {
        if(adapter==null) {
            return Optional.empty();
        }
        val spec = adapter.getSpecification();
        if(spec==null || !spec.isEntity()) {
            return Optional.empty();
        }

        return spec.entityFacet()
                .map(EntityFacet::getPersistenceStack);
    }

    @NonNull
    public static EntityState getEntityState(final @Nullable ManagedObject adapter) {
        return adapter!=null
             ? adapter.getEntityState()
             : EntityState.NOT_PERSISTABLE;
    }

    public static void persistInCurrentTransaction(final ManagedObject managedObject) {
        requiresEntity(managedObject);
        val spec = managedObject.getSpecification();
        val entityFacet = spec.entityFacetElseFail();
        entityFacet.persist(managedObject.getPojo());
    }

    public static void destroyInCurrentTransaction(final ManagedObject managedObject) {
        requiresEntity(managedObject);
        val spec = managedObject.getSpecification();
        val entityFacet = spec.entityFacetElseFail();
        entityFacet.delete(managedObject.getPojo());
    }

    public static void requiresEntity(final ManagedObject managedObject) {
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(managedObject)) {
            throw _Exceptions.illegalArgument("requires an entity object but got null, unspecified or empty");
        }
        val spec = managedObject.getSpecification();
        if(!spec.isEntity()) {
            throw _Exceptions.illegalArgument("not an entity type %s (sort=%s)",
                    spec.getCorrespondingClass(),
                    spec.getBeanSort());
        }
    }

    /**
     * @param managedObject
     * @return managedObject
     * @throws AssertionError if managedObject is a detached entity
     */
    @NonNull
    public static ManagedObject requiresAttached(final @NonNull ManagedObject managedObject) {
        if(managedObject instanceof PackedManagedObject) {
            ((PackedManagedObject)managedObject).unpack().forEach(MmEntityUtil::requiresAttached);
            return managedObject;
        }
        val entityState = MmEntityUtil.getEntityState(managedObject);
        if(entityState.isPersistable()) {
            // ensure we have an attached entity
            _Assert.assertEquals(
                    EntityState.PERSISTABLE_ATTACHED,
                    entityState,
                    ()-> String.format("entity %s is required to be attached (not detached)",
                            managedObject.getSpecification().getLogicalTypeName()));
        }
        return managedObject;
    }

    public static void requiresWhenFirstIsBookmarkableSecondIsAlso(
            final ManagedObject first,
            final ManagedObject second) {

        if(!ManagedObjects.isIdentifiable(first) || !ManagedObjects.isSpecified(second)) {
            return;
        }
        val secondSpec = second.getSpecification();
        if(secondSpec.isParented() || !secondSpec.isEntity()) {
            return;
        }

        if(!MmEntityUtil.hasOid(second)) {
            throw _Exceptions.illegalArgument(
                    "can't set a reference to a transient object [%s] from a persistent one [%s]",
                    second,
                    first.getTitle());
        }
    }

    // -- SHORTCUTS

    public static boolean hasOid(final @Nullable ManagedObject adapter) {
        return MmEntityUtil.getEntityState(adapter).hasOid();
    }

    public static boolean isDetachedCannotReattach(final @Nullable ManagedObject adapter) {
        return MmEntityUtil.getEntityState(adapter).isDetachedCannotReattach();
    }

    /** TODO very strange logic */
    public static boolean isDeleted(final @Nullable ManagedObject entity) {
        val state = MmEntityUtil.getEntityState(entity);
        return state.isDetached()
                || state.isRemoved()
                || state.isSpecicalJpaDetachedWithOid();
    }

}