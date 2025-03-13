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
record ManagedObjectEntity(
    @NonNull ObjectSpecification objSpec,
    /**
     * One of {ManagedObjectEntityTransient, ManagedObjectEntityBookmarked, ManagedObjectEntityRemoved}.
     * <p>
     * May dynamically mutate from 'left' to 'right' based on pojo's persistent state.
     * However, the pojo reference must be kept identical, unless the entity becomes 'removed',
     * in which case the pojo reference is invalidated and should no longer be accessible to callers.
     */
    @NonNull TransientObjectRef<EntityPhase> phaseRef,
    @NonNull TransientObjectRef<PhaseState> morphStateRef)
implements ManagedObject {

    private enum PhaseState {
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
        static PhaseState valueOf(final EntityState entityState) {
            return entityState.isRemoved()
                    ? REMOVED
                    : entityState.isAttached()
                        ? BOOKMARKED
                        : TRANSIENT;
        }
    }

    ManagedObjectEntity(
            final @NonNull EntityPhaseTransient transientPhase) {
        this(transientPhase.objSpec(), new TransientObjectRef<>(transientPhase), new TransientObjectRef<>(PhaseState.TRANSIENT));
    }

    ManagedObjectEntity(
            final @NonNull EntityPhaseBookmarked bookmarkedPhase) {
        this(bookmarkedPhase.objSpec(), new TransientObjectRef<>(bookmarkedPhase), new TransientObjectRef<>(PhaseState.BOOKMARKED));
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
        return (phase() instanceof EntityPhaseBookmarked bookmarked)
                ? Optional.of(bookmarked.bookmark())
                : Optional.empty();
    }

    @Override
    public boolean isBookmarkMemoized() {
        return isBookmarkedPhase();
    }

    @Override
    public @NonNull EntityState getEntityState() {
        var entityState = phase().getEntityState();
        var newMorphState = PhaseState.valueOf(entityState);

        if(morphStateRef.getObject()!=newMorphState) {
            log.debug("about to transition to {} phase given {}", newMorphState.name(), entityState);
            reassessPhase(entityState, peekAtPojo());
            if(newMorphState.isBookmarked()) {
                _Assert.assertTrue(isBookmarkedPhase(), ()->"successful transition");
            } else if(newMorphState.isRemoved()) {
                _Assert.assertTrue(isRemovedPhase(), ()->"successful transition");
            }
            morphStateRef.update(__->newMorphState);
        }
        return entityState;
    }

    @Override @SneakyThrows
    public Object getPojo() {
        if(isRemovedPhase()) return null; // don't reassess

        // handle the 'deleted' / 'not found' case gracefully ...
        try {
            var pojo = phase().getPojo();
            triggerReassessment();
            //if(pojo==null) makeRemoved(); seems reasonable, not tested yet
            return pojo;
        } catch (ObjectNotFoundException e) {
            // if object not found, transition to 'removed' state
            makeRemoved();
            return null;
        }
    }

    public Object peekAtPojo() {
        return phase().peekAtPojo();
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

    private EntityPhase phase() {
        return phaseRef.getObject();
    }

    private void triggerReassessment() {
        if(morphStateRef.getObject().isTransient()) {
            getEntityState(); // has side-effects
        }
    }

    private boolean isBookmarkedPhase() {
        return phase() instanceof EntityPhaseBookmarked;
    }

    private boolean isTransientPhase() {
        return phase() instanceof EntityPhaseTransient;
    }

    private boolean isRemovedPhase() {
        return phase() instanceof EntityPhaseRemoved;
    }

    private synchronized void reassessPhase(final EntityState entityState, final Object pojo) {
        if(isTransientPhase()
                && entityState.hasOid()) {
            makeBookmarked(pojo);
            return;
        }
        /* if the current EntityState is REMOVED, we handle phase transition
         * - from BOOKMARKED
         * - as well as from TRANSIENT
         * to REMOVED */
        if((isBookmarkedPhase()
                || isTransientPhase())
                && entityState.isTransientOrRemoved()) {
            makeRemoved();
            return;
        }
    }

    // transition to 'attached' state
    private void makeBookmarked(final Object pojo) {
        var attached = new EntityPhaseBookmarked(objSpec(), pojo);
        phaseRef.update(__->attached);
    }

    // transition to 'removed' state
    private void makeRemoved() {
        var removed = new EntityPhaseRemoved();
        phaseRef.update(__->removed);
    }

}