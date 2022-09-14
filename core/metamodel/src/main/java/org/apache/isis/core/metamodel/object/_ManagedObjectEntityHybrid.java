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

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.repository.EntityState;
import org.apache.isis.commons.functional.Either;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._Blackhole;

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
     * dynamically mutates from one to the other based on pojo's persistent state;
     * however, the pojo reference must be kept identical
     */
    private @NonNull Either<_ManagedObjectEntityTransient, _ManagedObjectEntityBookmarked>
        eitherDetachedOrBookmarked;

    private enum MorphState {
        /** Has no bookmark yet; can be transitioned to BOOKMARKED once
         *  for accompanied pojo, an OID becomes available. */
        TRANSIENT,
        /** Final state, once we have an OID,
         * regardless of the accompanied pojo's persistent state. */
        BOOKMARKED;
        public boolean isTransient() { return this == TRANSIENT; }
//        public boolean isBookmarked() { return this == BOOKMARKED; }
        static MorphState valueOf(final EntityState entityState) {
            return entityState.hasOid()
                    ? BOOKMARKED
                    : TRANSIENT;
        }
    }

    private MorphState morphState;

    _ManagedObjectEntityHybrid(
            final @NonNull _ManagedObjectEntityTransient _transient) {
        super(ManagedObject.Specialization.ENTITY, _transient.getSpecification());
        this.eitherDetachedOrBookmarked = Either.left(_transient);
        this.morphState = MorphState.TRANSIENT;
    }

    _ManagedObjectEntityHybrid(
            final @NonNull _ManagedObjectEntityBookmarked bookmarked) {
        super(ManagedObject.Specialization.ENTITY, bookmarked.getSpecification());
        this.eitherDetachedOrBookmarked = Either.right(bookmarked);
        this.morphState = MorphState.BOOKMARKED;
        _Assert.assertTrue(bookmarked.getBookmark().isPresent(),
                ()->"bookmarked entity must have bookmark");
    }

    @Override
    public Optional<Bookmark> getBookmark() {
        return eitherDetachedOrBookmarked
                .fold(Bookmarkable::getBookmark, Bookmarkable::getBookmark);
    }

    @Override
    public boolean isBookmarkMemoized() {
        return eitherDetachedOrBookmarked
                .fold(Bookmarkable::isBookmarkMemoized, Bookmarkable::isBookmarkMemoized);
    }

    @Override
    public @NonNull EntityState getEntityState() {

        val entityState = eitherDetachedOrBookmarked
                .fold(ManagedObject::getEntityState, ManagedObject::getEntityState);

        val newMorphState = MorphState.valueOf(entityState);

        if(this.morphState!=newMorphState) {
            log.debug("about to transition to bookmarked variant given {}", entityState);
            reassessVariant(entityState, peekAtPojo());
            _Assert.assertTrue(isVariantAttached(), ()->"successful transition");
            this.morphState = newMorphState;
        }
        return entityState;
    }

    @Override
    public Object getPojo() {
        val pojo = eitherDetachedOrBookmarked
                .fold(ManagedObject::getPojo, ManagedObject::getPojo);

        triggerReassessment();

        return pojo;
    }

    @Override
    public Object peekAtPojo() {
        return eitherDetachedOrBookmarked
            .fold(_Refetchable::peekAtPojo, _Refetchable::peekAtPojo);
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

    private boolean isVariantAttached() {
        return eitherDetachedOrBookmarked.isRight();
    }

    private boolean isVariantDetached() {
        return eitherDetachedOrBookmarked.isLeft();
    }

    @Synchronized
    private void reassessVariant(final EntityState entityState, final Object pojo) {
        if(isVariantDetached()
                && entityState.hasOid()) {
            attach(pojo);
        }
    }

    // morph into attached
    private void attach(final Object pojo) {
        val attached = new _ManagedObjectEntityBookmarked(getSpecification(), pojo, Optional.empty());
        eitherDetachedOrBookmarked = Either.right(attached);
        _Assert.assertTrue(attached.getBookmark().isPresent(),
                ()->"bookmarked entity must have bookmark");
    }

}