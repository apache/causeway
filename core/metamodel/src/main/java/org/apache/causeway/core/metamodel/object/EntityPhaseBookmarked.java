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
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.exceptions.unrecoverable.ObjectNotFoundException;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.repository.EntityState;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.debug._Debug;
import org.apache.causeway.commons.internal.debug._XrayEvent;
import org.apache.causeway.commons.internal.debug.xray.XrayUi;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.ref.TransientObjectRef;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject.Specialization;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.extern.log4j.Log4j2;

/**
 * (package private) specialization corresponding to a attached {@link Specialization#ENTITY}
 * @see ManagedObject.Specialization#ENTITY
 */
@Log4j2
record EntityPhaseBookmarked(
    @NonNull ObjectSpecification objSpec,
    @NonNull TransientObjectRef<Object> pojoRef,
    @NonNull Bookmark bookmark)
implements EntityPhase, _Refetchable {

    EntityPhaseBookmarked(
            final ObjectSpecification objSpec,
            final Object pojo,
            final @NonNull Optional<Bookmark> bookmarkIfKnown) {
        this(objSpec, new TransientObjectRef<>(pojo), bookmarkIfKnown.orElse(null));
    }

    EntityPhaseBookmarked(
        final ObjectSpecification objSpec,
        final TransientObjectRef<Object> pojoRef,
        @Nullable final Bookmark bookmark) {
        _Assert.assertTrue(objSpec.isEntity());
        _Compliance.assertCompliance(objSpec, specialization(), pojoRef.getObject());
        this.objSpec = objSpec;
        this.pojoRef = pojoRef;
        //sanity check bookmark
        this.bookmark = Optional.ofNullable(bookmark)
                .map(entityFacet()::validateBookmark)
                .orElseGet(this::createBookmark);
    }

    @Override
    public Object peekAtPojo() {
        return pojoRef.getObject();
    }

    @Override
    public Object getPojo() {

        // refetch only if required ...

        var entityState = getEntityState();
        if(!entityState.isPersistable()) {
            throw _Exceptions.illegalState("not persistable %s", objSpec());
        }
        if(entityState.isAttached()) return pojoRef.getObject(); // is attached

        //[CAUSEWAY-3265] this getPojo() call might originate from a CausewayEntityListener.onPostLoad event,
        // in which case potentially runs into a nested loop, which we detect and throw
        return pojoRef.update(old->{
            var refetchedPojo = refetchPojo(entityState);
            return _Compliance.assertCompliance(objSpec(), specialization(), refetchedPojo);
        });
    }

    @Override
    public @NonNull EntityState getEntityState() {
        return entityFacet().getEntityState(peekAtPojo());
    }

    // -- HELPER

    private Specialization specialization() {
        return ManagedObject.Specialization.ENTITY;
    }

    private Object refetchPojo(final EntityState entityState) {

        var pojo = pojoRef.getObject();
        var entityFacet = entityFacet();

        // triggers live-cycle events
        var refetchedIfSuccess = entityFacet.fetchByBookmark(bookmark);

        if(refetchedIfSuccess.isEmpty()) {
            // if cannot refetch from this special JPA state, try again later
            if(entityState.isDetached()) return pojo;

            // eg. throws on deleted entity
            throw new ObjectNotFoundException("" + bookmark);
        }

        var refetchedPojo = refetchedIfSuccess.get();

        if(!entityFacet.getEntityState(refetchedPojo).hasOid()) {
            throw new ObjectNotFoundException("" + bookmark);
        }

        _XrayEvent.event("Entity %s refetched from persistence.", objSpec());

        return refetchedPojo;
    }

    private EntityFacet entityFacet() {
        return objSpec().entityFacetElseFail();
    }

    @SuppressWarnings("deprecation")
    private Bookmark createBookmark() {
        var entityFacet = entityFacet();

        // fail early when detached entities are detected
        // should have been re-fetched at start of this request-cycle
        if(!getEntityState().hasOid()) {

            _Debug.onCondition(XrayUi.isXrayEnabled(), ()->{
                _Debug.log("detached entity detected %s", peekAtPojo());
            });

            var msg = String.format(
                    "The persistence layer does not recognize given object %s, "
                    + "meaning the object has no identifier that associates it with the persistence layer. "
                    + "(most likely, because the object is detached, eg. was not persisted after being new-ed up)",
                    objSpec());

            // in case of the exception getting swallowed, also write a log
            log.error(msg);

            throw _Exceptions.illegalArgument(msg);
        }

        return entityFacet.bookmarkForElseFail(peekAtPojo());
    }

}
