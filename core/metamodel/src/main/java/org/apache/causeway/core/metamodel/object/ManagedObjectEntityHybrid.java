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

import org.jspecify.annotations.NonNull;

import org.apache.causeway.applib.exceptions.unrecoverable.ObjectNotFoundException;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.repository.EntityState;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.ref.TransientObjectRef;
import org.apache.causeway.core.metamodel.facets.object.title.TitleRenderRequest;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

/**
 * (package private) specialization corresponding to {@link Specialization#ENTITY}
 * @see ManagedObject.Specialization#ENTITY
 */
@Log4j2
record ManagedObjectEntityHybrid(
    @NonNull ObjectSpecification objSpec,
    /**
     * One of {ManagedObjectEntityTransient, ManagedObjectEntityBookmarked, ManagedObjectEntityRemoved}.
     * <p>
     * May dynamically mutate from 'left' to 'right' based on pojo's persistent state.
     * However, the pojo reference must be kept identical, unless the entity becomes 'removed',
     * in which case the pojo reference is invalidated and should no longer be accessible to callers.
     */
    @NonNull TransientObjectRef<ManagedObject> variantRef,
    @NonNull TransientObjectRef<MorphState> morphStateRef)
implements ManagedObject, _Refetchable {

    private enum MorphState {
        /** Has no bookmark yet; can be transitioned to BOOKMARKED once
         *  for accompanied pojo, an OID becomes available. */
        TRANSIENT,
        /** We have an OID,
         * regardless of the accompanied pojo's persistent state (unless becomes removed). */
        BOOKMARKED,
        /** Final state, that can be entered once after we had an OID. */
        REMOVED;
        public boolean isTransient() { return this == TRANSIENT; }
        public boolean isBookmarked() { return this == BOOKMARKED; }
        public boolean isRemoved() { return this == REMOVED; }
        static MorphState valueOf(final EntityState entityState) {
            return entityState.isRemoved()
                    ? REMOVED
                    : entityState.isAttached()
                        ? BOOKMARKED
                        : TRANSIENT;
        }
    }

    ManagedObjectEntityHybrid(
            final @NonNull ManagedObjectEntityTransient transientEntity) {
        this(transientEntity.objSpec(), new TransientObjectRef<>(transientEntity), new TransientObjectRef<>(MorphState.TRANSIENT));
    }

    ManagedObjectEntityHybrid(
            final @NonNull ManagedObjectEntityBookmarked bookmarkedEntity) {
        this(bookmarkedEntity.objSpec(), new TransientObjectRef<>(bookmarkedEntity), new TransientObjectRef<>(MorphState.BOOKMARKED));
        _Assert.assertTrue(bookmarkedEntity.getBookmark().isPresent(),
                ()->"bookmarked entity must have bookmark");
    }

    @Override
    public Specialization specialization() {
        return ManagedObject.Specialization.ENTITY;
    }

    @Override
    public String getTitle() {
        return _InternalTitleUtil.titleString(
                TitleRenderRequest.forObject(this));
    }

    @Override
    public Optional<ObjectMemento> getMemento() {
        return Optional.ofNullable(ObjectMemento.singularOrEmpty(this));
    }

    @Override
    public Optional<Bookmark> getBookmark() {
        var variant = variant();
        return (variant instanceof Bookmarkable)
                ? variant.getBookmark()
                : Optional.empty();
    }

    @Override
    public boolean isBookmarkMemoized() {
        var variant = variant();
        return (variant instanceof Bookmarkable)
                ? variant.isBookmarkMemoized()
                : false;
    }

    @Override
    public @NonNull EntityState getEntityState() {
        var entityState = variant().getEntityState();
        var newMorphState = MorphState.valueOf(entityState);

        if(morphStateRef.getObject()!=newMorphState) {
            log.debug("about to transition to {} variant given {}", newMorphState.name(), entityState);
            reassessVariant(entityState, peekAtPojo());
            if(newMorphState.isBookmarked()) {
                _Assert.assertTrue(isVariantBookmarked(), ()->"successful transition");
            } else if(newMorphState.isRemoved()) {
                _Assert.assertTrue(isVariantRemoved(), ()->"successful transition");
            }
            morphStateRef.update(__->newMorphState);
        }
        return entityState;
    }

    @Override @SneakyThrows
    public Object getPojo() {
        if(isVariantRemoved()) return null; // don't reassess

        // handle the 'deleted' / 'not found' case gracefully ...
        try {
            var pojo = variant().getPojo();
            triggerReassessment();
            //if(pojo==null) makeRemoved(); seems reasonable, not tested yet
            return pojo;
        } catch (ObjectNotFoundException e) {
            // if object not found, transition to 'removed' state
            makeRemoved();
            return null;
        }
    }

    @Override
    public Object peekAtPojo() {
        return (variant() instanceof _Refetchable refetchable)
                ? refetchable.peekAtPojo()
                : null;
    }

    @Override
    public final boolean equals(final Object obj) {
        return _Compliance.equals(this, obj);
    }

    @Override
    public final int hashCode() {
        return _Compliance.hashCode(this);
    }

    @Override
    public final String toString() {
        return _Compliance.toString(this);
    }

    // -- HELPER

    private ManagedObject variant() {
        return variantRef.getObject();
    }

    private void triggerReassessment() {
        if(morphStateRef.getObject().isTransient()) {
            getEntityState(); // has side-effects
        }
    }

    private boolean isVariantBookmarked() {
        return variant() instanceof ManagedObjectEntityBookmarked;
    }

    private boolean isVariantTransient() {
        return variant() instanceof ManagedObjectEntityTransient;
    }

    private boolean isVariantRemoved() {
        return variant() instanceof ManagedObjectEntityRemoved;
    }

    private synchronized void reassessVariant(final EntityState entityState, final Object pojo) {
        if(isVariantTransient()
                && entityState.hasOid()) {
            makeBookmarked(pojo);
            return;
        }
        /* if the current EntityState is REMOVED, we handle variant transition
         * - from BOOKMARKED
         * - as well as from TRANSIENT
         * to REMOVED */
        if((isVariantBookmarked()
                || isVariantTransient())
                && entityState.isTransientOrRemoved()) {
            makeRemoved();
            return;
        }
    }

    // morph into attached
    private void makeBookmarked(final Object pojo) {
        var attached = new ManagedObjectEntityBookmarked(objSpec(), pojo, Optional.empty());
        variantRef.update(__->attached);
        _Assert.assertTrue(attached.getBookmark().isPresent(),
                ()->"bookmarked entity must have bookmark");
    }

    // morph into attached
    private void makeRemoved() {
        var removed = new ManagedObjectEntityRemoved(objSpec());
        variantRef.update(__->removed);
    }

}