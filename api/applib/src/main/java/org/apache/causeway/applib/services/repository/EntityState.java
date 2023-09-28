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
     * Is attached, has no OID yet. On pre-store.
     */
    ATTACHED_NO_OID(false),
    /**
     * Is detached, hence (per definition) has an OID.
     * <p>
     * Supported by both JDO and JPA. However, historically never used by the framework for JDO.
     */
    DETACHED(true),
    /**
     * <h1>JDO specific</h1>
     * Object with this state is an entity that no longer attached to a
     * persistence session and cannot be re-attached.
     * In other words: changes to the entity will <i>not</i>
     * be tracked nor flushed back to the database.
     * <p>
     * JDO distinguishes between DETACHED and HOLLOW,
     * by virtue of {@code javax.jdo.option.detachAllOnCommit=false}.
     * <p>
     * (Unfortunately, we have not found a way to recover _OIDs_ from _hollow_ entities, as used for serialization post commit.
     * We have instead implemented a workaround using the <code>DnStateManagerForHollow</code> class).
     *
     * @see "https://www.datanucleus.org/products/accessplatform_6_0/jdo/persistence.html#lifecycle"
     */
    HOLLOW(true),
    /**
     * Object with this state is an entity that is transient
     * or has been removed from the database.
     * Objects in this state may no longer be interacted with.
     */
    TRANSIENT_OR_REMOVED(false),
    /**
     * <h1>JDO specific</h1>
     * Not supported by JPA. (Cannot distinguish between TRANSIENT and REMOVED.)
     */
    REMOVED(false)
    ;

    // -- PREDICATES

    @Getter @Accessors(fluent=true) private final boolean hasOid;

    /**
     * Object is an entity, hence is persistable to the database.
     */
    public boolean isPersistable() { return this != NOT_PERSISTABLE; }
    /** @see #ATTACHED */
    public boolean isAttached() { return this == ATTACHED; }
    /** @see #ATTACHED_NO_OID */
    public boolean isAttachedNoOid() { return this == ATTACHED_NO_OID; }
    /** @see #DETACHED */
    public boolean isDetached() { return this == DETACHED; }
    /** @see #HOLLOW */
    public boolean isHollow() { return this == HOLLOW; }
    /** @see #TRANSIENT_OR_REMOVED
     *  @see #REMOVED */
    public boolean isTransientOrRemoved() { return this == TRANSIENT_OR_REMOVED
            || this == REMOVED; }
    /** @see #REMOVED */
    public boolean isRemoved() { return this == REMOVED; }

    // -- SPECIAL STATES

    public boolean isAttachedOrRemoved() {
        return isAttached()
                || isTransientOrRemoved();
    }

}
