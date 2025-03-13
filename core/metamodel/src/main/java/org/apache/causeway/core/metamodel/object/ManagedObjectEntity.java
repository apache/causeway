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

import java.util.Objects;
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
    @NonNull TransientObjectRef<EntityPhase> phaseRef)
implements ManagedObject {

    enum PhaseState {
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
        this(transientPhase.objSpec(), new TransientObjectRef<>(transientPhase));
    }

    ManagedObjectEntity(
            final @NonNull EntityPhaseBookmarked bookmarkedPhase) {
        this(bookmarkedPhase.objSpec(), new TransientObjectRef<>(bookmarkedPhase));
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
        return phaseState().isBookmarked();
    }

    @Override
    public @NonNull EntityState getEntityState() {
        if(phaseState().isRemoved()) return EntityState.REMOVED; // don't reassess

        var entityState = phase().reassessEntityState();
        var newPhaseState = PhaseState.valueOf(entityState);

        if(!phaseState().equals(newPhaseState)) {
            log.debug("about to transition to {} phase given {}", newPhaseState.name(), entityState);
            reassessPhase(entityState, peekAtPojo());
            switch (newPhaseState) {
                case BOOKMARKED, REMOVED -> _Assert.assertEquals(newPhaseState, phaseState(), ()->"transition failed");
                case TRANSIENT -> {
                    //TODO fails when was requested to transition to TRANSIENT but turned out to be actually REMOVED
                    //_Assert.assertEquals(newPhaseState, phaseState(), ()->"transition failed");
                }
            }
        }
        return entityState;
    }

    @Override @SneakyThrows
    public Object getPojo() {
        if(phaseState().isRemoved()) return null; // don't reassess

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
        return obj instanceof ManagedObjectEntity other
            ? Objects.equals(this.objSpec().logicalTypeName(), other.objSpec().logicalTypeName())
                && Objects.equals(this.phaseState(), other.phaseState())
                && Objects.equals(this.peekAtPojo(), other.peekAtPojo())
            : false;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(objSpec().logicalTypeName(), phaseState());
    }

    @Override
    public final String toString() {
        return "ManagedObjectEntity[logicalTypeName=%s,state=%s%s]"
            .formatted(
                objSpec().logicalTypeName(),
                phaseState().name(),
                getBookmark()
                    .map(Bookmark::identifier)
                    .map(",id=%s"::formatted)
                    .orElse(""));
    }

    // -- HELPER

    private EntityPhase phase() {
        return phaseRef.getObject();
    }

    private PhaseState phaseState() {
        return phase().phaseState();
    }

    private void triggerReassessment() {
        if(phaseState().isTransient()) {
            getEntityState(); // has desired side-effects
        }
    }

    private synchronized void reassessPhase(final EntityState entityState, final Object pojo) {
        if(phaseState().isTransient()
                && entityState.hasOid()) {
            makeBookmarked(pojo);
            return;
        }
        /* if the current EntityState is REMOVED, we handle phase transition
         * - from BOOKMARKED
         * - as well as from TRANSIENT
         * to REMOVED */
        if((phaseState().isBookmarked()
                || phaseState().isTransient())
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