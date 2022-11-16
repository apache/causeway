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

import org.apache.causeway.applib.exceptions.unrecoverable.ObjectNotFoundException;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.repository.EntityState;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Blackhole;

import lombok.NonNull;
import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * (package private) specialization corresponding to {@link Specialization#ENTITY}
 * @see ManagedObject.Specialization#ENTITY
 */
@Log4j2
final class _ManagedObjectEntityHybrid
extends _ManagedObjectSpecified
implements _Refetchable {

    /**
     * One of {_ManagedObjectEntityTransient, _ManagedObjectEntityBookmarked, _ManagedObjectEntityRemoved}.
     * <p>
     * May dynamically mutate from 'left' to 'right' based on pojo's persistent state.
     * However, the pojo reference must be kept identical, unless the entity becomes 'removed',
     * in which case the pojo reference is invalidated and should no longer be accessible to callers.
     */
    private @NonNull ManagedObject variant;

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
                    : entityState.hasOid()
                        ? BOOKMARKED
                        : TRANSIENT;
        }
    }

    private MorphState morphState;

    _ManagedObjectEntityHybrid(
            final @NonNull _ManagedObjectEntityTransient _transient) {
        super(ManagedObject.Specialization.ENTITY, _transient.getSpecification());
        this.variant = _transient;
        this.morphState = MorphState.TRANSIENT;
    }

    _ManagedObjectEntityHybrid(
            final @NonNull _ManagedObjectEntityBookmarked bookmarked) {
        super(ManagedObject.Specialization.ENTITY, bookmarked.getSpecification());
        this.variant = bookmarked;
        this.morphState = MorphState.BOOKMARKED;
        _Assert.assertTrue(bookmarked.getBookmark().isPresent(),
                ()->"bookmarked entity must have bookmark");
    }

    @Override
    public Optional<Bookmark> getBookmark() {
        return (variant instanceof Bookmarkable)
                ? ((Bookmarkable)variant).getBookmark()
                : Optional.empty();
    }

    @Override
    public boolean isBookmarkMemoized() {
        return (variant instanceof Bookmarkable)
                ? ((Bookmarkable)variant).isBookmarkMemoized()
                : false;
    }

    @Override
    public @NonNull EntityState getEntityState() {

        val entityState = variant.getEntityState();
        val newMorphState = MorphState.valueOf(entityState);

        if(this.morphState!=newMorphState) {
            log.debug("about to transition to {} variant given {}", newMorphState.name(), entityState);
            reassessVariant(entityState, peekAtPojo());
            if(newMorphState.isBookmarked()) {
                _Assert.assertTrue(isVariantBookmarked(), ()->"successful transition");
            } else if(newMorphState.isRemoved()) {
                _Assert.assertTrue(isVariantRemoved(), ()->"successful transition");
            }
            this.morphState = newMorphState;
        }
        return entityState;
    }

    @Override
    public Object getPojo() {
        if(isVariantRemoved()) {
            return null; // don't reassess
        }

        // handle the 'deleted' / 'not found' case gracefully
        try {
            val pojo = variant.getPojo();
            triggerReassessment();
            return pojo;
        } catch (ObjectNotFoundException e) {
            // if object not found, transition to 'removed' state
            makeRemoved();
            return null;
        }
    }

    @Override
    public Object peekAtPojo() {
        return (variant instanceof _Refetchable)
                ? ((_Refetchable)variant).peekAtPojo()
                : null;
    }

    @Override
    protected boolean isInjectionPointsResolved() {
        // overriding the default for optimization, let the EntityFacet handle injection
        // as a side-effect potentially injects if required
        return getSpecification().entityFacetElseFail()
                .isInjectionPointsResolved(peekAtPojo());
    }

    // -- HELPER

    private void triggerReassessment() {
        if(morphState.isTransient()) {
            _Blackhole.consume(getEntityState());
        }
    }

    private boolean isVariantBookmarked() {
        return variant instanceof _ManagedObjectEntityBookmarked;
    }

    private boolean isVariantTransient() {
        return variant instanceof _ManagedObjectEntityTransient;
    }

    private boolean isVariantRemoved() {
        return variant instanceof _ManagedObjectEntityRemoved;
    }

    @Synchronized
    private void reassessVariant(final EntityState entityState, final Object pojo) {
        if(isVariantTransient()
                && entityState.hasOid()) {
            makeBookmarked(pojo);
            return;
        }
        if(isVariantBookmarked()
                && entityState.isRemoved()) {
            makeRemoved();
            return;
        }
    }

    // morph into attached
    private void makeBookmarked(final Object pojo) {
        val attached = new _ManagedObjectEntityBookmarked(getSpecification(), pojo, Optional.empty());
        this.variant = attached;
        _Assert.assertTrue(attached.getBookmark().isPresent(),
                ()->"bookmarked entity must have bookmark");
    }

    // morph into attached
    private void makeRemoved() {
        val removed = new _ManagedObjectEntityRemoved(getSpecification());
        this.variant = removed;
    }

}