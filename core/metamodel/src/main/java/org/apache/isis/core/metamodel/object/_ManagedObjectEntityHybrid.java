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
import java.util.concurrent.atomic.AtomicReference;

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
     * dynamically mutates from one to the other based on pojos persistent state;
     * however the pojo reference must be kept identical
     */
    private @NonNull Either<_ManagedObjectEntityDetached, _ManagedObjectEntityAttached>
        eitherDetachedOrAttached;

    private EntityState entityState;

    _ManagedObjectEntityHybrid(
            final @NonNull _ManagedObjectEntityDetached detached) {
        super(ManagedObject.Specialization.ENTITY, detached.getSpecification());
        this.eitherDetachedOrAttached = Either.left(detached);
        this.entityState = EntityState.PERSISTABLE_DETACHED;
    }

    _ManagedObjectEntityHybrid(
            final @NonNull _ManagedObjectEntityAttached attached) {
        super(ManagedObject.Specialization.ENTITY, attached.getSpecification());
        this.eitherDetachedOrAttached = Either.right(attached);
        this.entityState = EntityState.PERSISTABLE_ATTACHED;
        this.bookmarkRef.set(attached.getBookmark().orElseThrow());
    }

    @Override
    public Optional<Bookmark> getBookmark() {
        if(!isBookmarkMemoized()) {
            triggerReassessment();
        }
        return Optional.ofNullable(bookmarkRef.get());
    }

    @Override
    public Optional<Bookmark> getBookmarkRefreshed() {
        return getBookmark(); // identity op
    }

    @Override
    public boolean isBookmarkMemoized() {
        return bookmarkRef.get()!=null;
    }

    @Override
    public @NonNull EntityState getEntityState() {

        val entityState = eitherDetachedOrAttached
                .fold(ManagedObject::getEntityState, ManagedObject::getEntityState);

        if(this.entityState!=entityState) {
            log.debug("about to morph {} -> {}", this.entityState, entityState);
            this.entityState = entityState;
            reassessVariant(entityState, peekAtPojo());
            if(entityState.hasOid()) {
                _Assert.assertTrue(isVariantAttached());
            } else {
                _Assert.assertTrue(isVariantDetached());
            }
        }

        return entityState;
    }

    @Override
    public Object getPojo() {
        val pojo = eitherDetachedOrAttached
                .fold(ManagedObject::getPojo, ManagedObject::getPojo);

        triggerReassessment();

        return pojo;
    }

    @Override
    public Object peekAtPojo() {
        return eitherDetachedOrAttached
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
        _Blackhole.consume(getEntityState());
    }

    private final AtomicReference<Bookmark> bookmarkRef = new AtomicReference<Bookmark>();

    private boolean isVariantAttached() {
        return eitherDetachedOrAttached.isRight();
    }

    private boolean isVariantDetached() {
        return eitherDetachedOrAttached.isLeft();
    }

    @Synchronized
    private void reassessVariant(final EntityState entityState, final Object pojo) {
        if(isVariantDetached()
                && entityState.hasOid()) {
            attach(pojo);
        }
        // only run when the above has not run!
        else if(isVariantAttached()
                && !entityState.hasOid()) {
            detach(pojo);
        }
    }

    // morph into attached
    private void attach(final Object pojo) {
        val bookmark = isBookmarkMemoized()
                ? Optional.ofNullable(bookmarkRef.get())
                : getSpecification().entityFacetElseFail().bookmarkFor(pojo);
        val attached = new _ManagedObjectEntityAttached(getSpecification(), pojo, bookmark);
        eitherDetachedOrAttached = Either.right(attached);
        // set in any case
        bookmarkRef.set(attached.getBookmark().orElseThrow());
    }

    // morph into detached
    private void detach(final Object pojo) {
        eitherDetachedOrAttached = Either.left(
                new _ManagedObjectEntityDetached(getSpecification(), pojo));
    }


}