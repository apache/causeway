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
     * Is attached, has no OID yet. On pre-store.
     */
    PERSISTABLE_ATTACHED_NO_OID,
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
    PERSISTABLE_HOLLOW,
    /**
     * Is detached (has an OID). Supports re-attachment.
     * <p>
     * That is,
     * when after a commit the entity had become DETACHED, it can be used elsewhere in the application.
     * You then attach any changes back to persistence and it becomes ATTACHED again.
     * <p>
     * Supported by JDO and JPA. However, at the time of writing, we don't make use of this technology,
     * but instead re-fetch entities based on their OID.
     */
    PERSISTABLE_DETACHED,

    PERSISTABLE_DETACHED_NO_OID,
    /**
     * Object with this state is an entity that has been removed from the
     * database. Objects in this state may no longer be interacted with.
     */
    PERSISTABLE_REMOVED,
    ;

    /**
     * Object is an entity so is <i>potentially</i> persistable to the database.
     */
    public boolean isPersistable() { return this != NOT_PERSISTABLE; }
    /**
     * Object with this state is an entity that is attached to a persistence
     * session, in other words changes to the entity will be flushed back to
     * the database.
     * @see #PERSISTABLE_ATTACHED
     */
    public boolean isAttached() { return this == PERSISTABLE_ATTACHED; }
    /**
     * Object with this state is an entity but that is detached from a
     * persistence session and cannot be re-attached,
     * in other words changes to the entity will <i>not</i>
     * be flushed back to the database.
     * @see #PERSISTABLE_HOLLOW
     */
    //TODO[CAUSEWAY-3500] perhaps reflect the fact, that this is JDO only with a better name
    public boolean isHollow() { return this == PERSISTABLE_HOLLOW; }

    /**
     * Is detached and has an OID.
     */
    public boolean isDetachedWithOid() { return this == PERSISTABLE_DETACHED; }

    /**
     * Is detached but has <b>no</b> OID.
     */
    //TODO[CAUSEWAY-3500] potential misnomer: detached per def. has an OID
    public boolean isDetachedNoOid() { return this == PERSISTABLE_DETACHED_NO_OID; }

    /**
     * Object with this state is an entity that has been removed from the
     * database.  Objects in this state may no longer be interacted with.
     * <p>
     * Only supported by JDO. Will always return false with JPA.
     * @see #PERSISTABLE_REMOVED
     */
    //TODO[CAUSEWAY-3500] perhaps reflect the fact, that this is JDO only with a better name
    public boolean isRemoved() { return this == PERSISTABLE_REMOVED; }

    // -- SPECIAL STATES

    /**
     * Is attached, has no OID yet. (On pre-store.)
     */
    public boolean isAttachedNoOid() {
        return this == PERSISTABLE_ATTACHED_NO_OID;
    }

    //TODO[CAUSEWAY-3500] potential misnomer: if detached per def has an OID
    //FIXME[CAUSEWAY-3500] in fact hollow can now be re-attached
    public boolean isDetachedCannotReattach() {
        return (isHollow()
                || isDetachedNoOid()
                || isRemoved())
                && !isDetachedWithOid();
    }

    //TODO[CAUSEWAY-3500] either remove or un-deprecate
    /**
     * @apiNote 'removed' is only supported by JDO.
     * @deprecated not supported by JPA
     */
    @Deprecated
    public boolean isAttachedOrRemoved() {
        return isAttached()
                || isRemoved();
    }

    // -- BOOKMARKABLE

    //TODO[CAUSEWAY-3500] in fact hollow potentially also has an OID now
    public boolean hasOid() {
        return isAttached() || isDetachedWithOid();
    }

}
