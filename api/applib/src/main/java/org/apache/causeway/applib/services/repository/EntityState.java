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

/**
 * Enumerates the state of an entity.
 *
 * @apiNote use the provided predicates rather then directly referencing the enum names
 *
 * @since 2.0 {@index}
 */
public enum EntityState {

    /**
     * Object with this state is not an entity (for example it might be a view
     * model, value type or a domain service).
     */
    NOT_PERSISTABLE,
    /**
     * Object with this state is an entity that is attached to a persistence
     * session, in other words changes to the entity will be flushed back to
     * the database.
     */
    PERSISTABLE_ATTACHED,
    /**
     * Object with this state is an entity but that is detached from a
     * persistence session, in other words changes to the entity will <i>not</i>
     * be flushed back to the database.
     */
    PERSISTABLE_DETACHED,
    /**
     * Object with this state is an entity that has been removed from the
     * database. Objects in this state may no longer be interacted with.
     */
    PERSISTABLE_REMOVED,
    /**
     * Is attached, has no OID yet. On pre-store.
     */
    PERSISTABLE_ATTACHED_NO_OID,
    /**
     * JPA specific. Is detached, but has an OID.
     */
    PERSISTABLE_DETACHED_WITH_OID,

    ;

    /**
     * Object is an entity so is <i>potentially</i> persistable ot the database.
     */
    public boolean isPersistable() { return this != NOT_PERSISTABLE; }
    /**
     * Object with this state is an entity that is attached to a persistence
     * session, in other words changes to the entity will be flushed back to
     * the database.
     */
    public boolean isAttached() { return this == PERSISTABLE_ATTACHED; }
    /**
     * Object with this state is an entity but that is detached from a
     * persistence session, in other words changes to the entity will <i>not</i>
     * be flushed back to the database.
     */
    public boolean isDetached() { return this == PERSISTABLE_DETACHED; }
    /**
     * Object with this state is an entity that has been removed from the
     * database.  Objects in this state may no longer be interacted with.
     * <p>
     * Only supported by JDO. Will always return false with JPA.
     */
    public boolean isRemoved() { return this == PERSISTABLE_REMOVED; }

    // -- SPECIAL STATES

    /**
     * @apiNote Is attached, has no OID yet. (On pre-store.)
     */
    public boolean isAttachedNoOid() {
        return this == PERSISTABLE_ATTACHED_NO_OID;
    }

    public boolean isDetachedCannotReattach() {
        return (isDetached()
                || isRemoved())
                && !isSpecicalJpaDetachedWithOid();
    }

    /**
     * @apiNote 'removed' is only supported by JDO.
     * @deprecated not supported by JPA
     */
    @Deprecated
    public boolean isAttachedOrRemoved() {
        return isAttached()
                || isRemoved();
    }

    // -- JPA SPECIFIC STATES

    /**
     * @apiNote JPA specific. Is detached, but has an OID.
     */
    public boolean isSpecicalJpaDetachedWithOid() {
        return this == PERSISTABLE_DETACHED_WITH_OID;
    }

    // -- BOOKMARKABLE

    public boolean hasOid() {
        return isAttached()
                || isSpecicalJpaDetachedWithOid();
    }

}
