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
import java.util.function.BiConsumer;

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
        void reassessPhase(final EntityState newEntityState, final BiConsumer<EntityState, PhaseState> onNewPhaseRequired) {
            transitionAdvice(this, newEntityState)
                .ifPresent(newPhase->onNewPhaseRequired.accept(newEntityState, newPhase));
        }
        private static Optional<PhaseState> transitionAdvice(final PhaseState previous, final EntityState entityState) {
            return switch (previous) {
                case BOOKMARKED->entityState.isTransientOrRemoved()
                    ? Optional.of(PhaseState.REMOVED)
                    : Optional.empty();
                case TRANSIENT->{
                    var stayTransient = !entityState.isRemoved()
                        && !entityState.isAttached();
                    if(stayTransient) yield Optional.empty();
                    if(entityState.hasOid()) yield Optional.of(PhaseState.BOOKMARKED);
                    if(entityState.isTransientOrRemoved()) yield Optional.of(PhaseState.REMOVED);
                    yield Optional.empty();
                }
                case REMOVED->Optional.empty();
            };
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
        return phase().reassessEntityState((entityState, newPhaseState)->{
            log.debug("about to transition to {} phase given {}", newPhaseState.name(), entityState);
            switch (newPhaseState) {
                case BOOKMARKED -> makeBookmarked(peekAtPojo());
                case REMOVED -> makeRemoved();
                case TRANSIENT -> {
                    throw new UnsupportedOperationException("cannot transition to TRANSIENT (TRANSIENT is an initial state only)");
                }
            }
            _Assert.assertEquals(newPhaseState, phaseState(), ()->"transition failed");
        });
    }

    @Override @SneakyThrows
    public Object getPojo() {
        if(phaseState().isRemoved()) return null; // don't reassess

        // handle the 'deleted' / 'not found' case gracefully ...
        try {
            var pojo = phase().getPojo();

            // trigger reassessment when transient
            if(phaseState().isTransient()) getEntityState(); // has desired side-effects

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
        return Objects.hash(
            objSpec().logicalTypeName(),
            phaseState(),
            getBookmark().map(Bookmark::identifier).orElse(null));
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

     // transition to 'attached' state
    private void makeBookmarked(final Object pojo) {
        phaseRef.update(__->new EntityPhaseBookmarked(objSpec(), pojo));
    }

    // transition to 'removed' state
    private void makeRemoved() {
        phaseRef.update(__->new EntityPhaseRemoved());
    }

}