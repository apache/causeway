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
import java.util.function.UnaryOperator;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.services.repository.EntityState;
import org.apache.isis.commons.functional.Try;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.config.beans.PersistenceStack;
import org.apache.isis.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.isis.core.metamodel.objectmanager.load.ObjectLoader;

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

        val entityFacet = spec.getFacet(EntityFacet.class);
        if(entityFacet==null) {
            return Optional.empty();
        }

        return Optional.of(entityFacet.getPersistenceStack());
    }

    @NonNull
    public static EntityState getEntityState(final @Nullable ManagedObject adapter) {
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(adapter)) {
            return EntityState.NOT_PERSISTABLE;
        }
        val spec = adapter.getSpecification();
        val pojo = adapter.getPojo();

        if(!spec.isEntity()) {
            return EntityState.NOT_PERSISTABLE;
        }

        val entityFacet = spec.getFacet(EntityFacet.class);
        if(entityFacet==null) {
            throw _Exceptions.unrecoverable("Entity types must have an EntityFacet");
        }

        return entityFacet.getEntityState(pojo);
    }

    public static void persistInCurrentTransaction(final ManagedObject managedObject) {
        requiresEntity(managedObject);
        val spec = managedObject.getSpecification();
        val entityFacet = spec.getFacet(EntityFacet.class);
        entityFacet.persist(managedObject.getPojo());
    }

    public static void destroyInCurrentTransaction(final ManagedObject managedObject) {
        requiresEntity(managedObject);
        val spec = managedObject.getSpecification();
        val entityFacet = spec.getFacet(EntityFacet.class);
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

    public static ManagedObject refetch(final @Nullable ManagedObject managedObject) {
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(managedObject)) {
            return managedObject;
        }
        if(managedObject instanceof PackedManagedObject) {
            ((PackedManagedObject)managedObject).unpack().forEach(MmEntityUtil::refetch);
            return managedObject;
        }
        val entityState = MmEntityUtil.getEntityState(managedObject);
        if(!entityState.isPersistable()) {
            return managedObject;
        }
        if(!entityState.isDetached()) {
            return managedObject;
        }

        val spec = managedObject.getSpecification();
        val objectManager = managedObject.getObjectManager();

        val reattached = ManagedObjects.bookmark(managedObject)
        .map(bookmark->
                ObjectLoader.Request.of(
                                spec,
                                bookmark))
        .map(loadRequest->Try.call(
                ()->objectManager.loadObject(loadRequest)))
        .map(loadResult->
                // a valid scenario for entities: not found eg. after deletion,
                // which will fail the load request
                loadResult.isFailure()
                        ? ManagedObject.empty(managedObject.getSpecification())
                        : loadResult.getValue().get()
        )
        .orElse(managedObject);

        // handles deleted entities
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(reattached)) {
            // returns the 'emptied' ManagedObject from above
            return reattached;
        }

        val newState = MmEntityUtil.getEntityState(reattached);
        _Assert.assertTrue(newState.isAttached());

        _Casts.castTo(_ManagedObjectWithBookmark.class, managedObject)
        .ifPresent(obj->obj.replacePojo(old->reattached.getPojo()));

        return managedObject;
    }

    public static void requiresWhenFirstIsBookmarkableSecondIsAttached(
            final ManagedObject first,
            final ManagedObject second) {

        if(!ManagedObjects.isIdentifiable(first) || !ManagedObjects.isSpecified(second)) {
            return;
        }
        val secondSpec = second.getSpecification();
        if(secondSpec.isParented() || !secondSpec.isEntity()) {
            return;
        }

        if(!MmEntityUtil.isAttached(second)) {
            throw _Exceptions.illegalArgument(
                    "can't set a reference to a transient object [%s] from a persistent one [%s]",
                    second,
                    first.titleString());
        }
    }

    // -- SHORTCUTS

    public static boolean isAttached(final @Nullable ManagedObject adapter) {
        return MmEntityUtil.getEntityState(adapter).isAttached();
    }

    public static boolean isDetachedOrRemoved(final @Nullable ManagedObject adapter) {
        return MmEntityUtil.getEntityState(adapter).isDetachedOrRemoved();
    }

    /** only supported by JDO - always false with JPA */
    public static boolean isRemoved(final @Nullable ManagedObject adapter) {
        return MmEntityUtil.getEntityState(adapter).isRemoved();
    }

    public static ManagedObject assertAttachedWhenEntity(final @Nullable ManagedObject adapter) {
        if(adapter instanceof PackedManagedObject) {
            for(val element : ((PackedManagedObject)adapter).unpack()) {
                assertAttachedWhenEntity(element);
            }
        }
        val state = MmEntityUtil.getEntityState(adapter);
        if(state.isPersistable()) {
            _Assert.assertEquals(EntityState.PERSISTABLE_ATTACHED, state,
                    ()->String.format("detached entity %s", adapter));
        }
        return adapter;
    }

    public static ManagedObject computeIfDetached(
            final @Nullable ManagedObject adapter,
            final UnaryOperator<ManagedObject> onDetachedEntity) {
        val state = MmEntityUtil.getEntityState(adapter);
        if(state.isPersistable()
                &&!state.isAttached()) {
            return onDetachedEntity.apply(adapter);
        }
        return adapter;
    }

}