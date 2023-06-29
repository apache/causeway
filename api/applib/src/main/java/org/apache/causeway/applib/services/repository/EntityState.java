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
package org.apache.causeway.applib.services.repository;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Enumerates the state of an entity.
 *
 * @apiNote use the provided predicates rather then directly referencing the enum names
 *
 * @since 2.0 {@index}
 */
@RequiredArgsConstructor
public enum EntityState {

    /**
     * Object with this state is not an entity (for example it might be a view
     * model, value type or a domain service).
     */
    NOT_PERSISTABLE(false),
    /**
     * Object with this state is an entity that is attached to a persistence
     * session, in other words changes to the entity will be flushed back to
     * the database.
     */
    ATTACHED(true),
    /**
     * Is attached to a persistence session, but has no OID yet. (on pre-store)
     */
    ATTACHED_NO_OID(false),
    /**
     * <h1>JDO specific</h1>
     * Object with this state is an entity that is detached from a
     * persistence session and cannot be re-attached,
     * in other words changes to the entity will <i>not</i>
     * be tracked nor flushed back to the database.
     * <p>
     * JDO distinguishes between DETACHED and HOLLOW,
     * by virtue of {@code datanucleus.detachAllOnCommit=false}.
     * <p>
     * To recover <i>OIDs</i> from <i>hollow</i> entities, we introduced the DnStateManagerForHollow.
     *
     * @see "https://www.datanucleus.org/products/accessplatform_6_0/jdo/persistence.html#lifecycle"
     */
    JDO_HOLLOW(true),
    /**
     * <h1>JDO specific</h1>
     * Object with this state is an entity that has been deleted from the
     * database and may no longer be interacted with.
     * <p>
     * Closest match for JPA is {@link #TRANSIENT_OR_REMOVED}.
     */
    JDO_DELETED(false),
    /**
     * Entity is NOT attached to a persistence session. Has an OID, hence can be bookmarked and re-fetched.
     * <p>
     * That is, when after a commit the entity had become DETACHED,
     * it can be used elsewhere in the application.
     * You then attach any changes back to persistence and it becomes PERSISTENT again.
     * <p>
     * Supported by JDO and JPA. However, at the time of writing, we don't make use of this technology,
     * but instead re-fetch entities based on their OID,
     * discarding any changes that might have happened since the last commit.
     */
    DETACHED(true),
    /**
     * Entity is NOT attached to a persistence session. Has no OID.
     */
    TRANSIENT_OR_REMOVED(false),
    ;

    // -- PREDICATES

    @Getter @Accessors(fluent=true) private final boolean hasOid;

    /**
     * Object is an entity so is <i>potentially</i> persistable to the database.
     */
    public boolean isPersistable() { return this != NOT_PERSISTABLE; }

    /** @see #ATTACHED */
    public boolean isAttached() { return this == ATTACHED; }
    /** @see #ATTACHED_NO_OID */
    public boolean isAttachedNoOid() { return this == ATTACHED_NO_OID; }
    /** @see #JDO_HOLLOW */
    public boolean isJdoHollow() { return this == JDO_HOLLOW; }
    /** @see #DETACHED */
    public boolean isDetached() { return this == DETACHED; }
    /** @see #JDO_DELETED */
    public boolean isJdoDeleted() { return this == JDO_DELETED; }

    // -- ADVANCED PREDICATES

    /**
     * @see #TRANSIENT_OR_REMOVED
     * @see #JDO_DELETED
     */
    public boolean isTransientOrRemoved() {
        return this == TRANSIENT_OR_REMOVED
                || isJdoDeleted();
    }

    public boolean canDelete() {
        return isAttached()
                || isDetached();
    }

    /**
     * Used by entity change tracking.
     */
    public boolean canUseForPostValues() {
        return isPersistable()
                && !isJdoHollow()
                && !isTransientOrRemoved()
                && !isDetached();
    }

    // -- DEPRECATIONS

    @Deprecated
    public class Unsafe {

        //FIXME[CAUSEWAY-3500] 3 different types of flush checks, which is it though
        public boolean isFlushable() {
            return isDetached()
                    || (isTransientOrRemoved()
                            && !isJdoDeleted());
        }

        //FIXME[CAUSEWAY-3500] 3 different types of flush checks, which is it though
        public boolean canFlush() {
            return (!isAttached() && !isDetached());
        }

        //FIXME[CAUSEWAY-3500] 3 different types of flush checks, which is it though
        public boolean shouldFlush() {
            return isPersistable()
                    && !isAttached()
                    && !isJdoDeleted();
        }

    }

    public final Unsafe unsafe = new Unsafe();

}
