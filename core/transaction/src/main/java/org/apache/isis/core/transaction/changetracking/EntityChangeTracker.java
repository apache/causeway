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
package org.apache.isis.core.transaction.changetracking;

import org.apache.isis.core.metamodel.spec.ManagedObject;

/**
 * Responsible for collecting the details of all changes to domain objects
 * within a transaction.
 *
 * @since 1.x but renamed/refactored for v2 {@index}
 */
public interface EntityChangeTracker {

    /**
     * Auditing and publishing support: for object stores to enlist an object that has just been created,
     * capturing a dummy value <tt>'[NEW]'</tt> for the pre-modification value.
     * <p>
     * Fires the appropriate event and lifecycle callback: {@literal PERSISTED}
     * <p>
     * The post-modification values are captured when the transaction commits.
     */
    void enlistCreated(ManagedObject entity);

    /**
     * Auditing and publishing support: for object stores to enlist an object that is about to be deleted,
     * capturing the pre-deletion value of the properties of the {@link ManagedObject}.
     * <p>
     * Fires the appropriate event and lifecycle callback: {@literal REMOVING}
     * <p>
     * The post-modification values are captured  when the transaction commits.  In the case of deleted objects, a
     * dummy value <tt>'[DELETED]'</tt> is used as the post-modification value.
     */
    void enlistDeleting(ManagedObject entity);

    /**
     * Auditing and publishing support: for object stores to enlist an object that is about to be updated,
     * capturing the pre-modification values of the properties of the {@link ManagedObject}.
     * <p>
     * Fires the appropriate event and lifecycle callback: {@literal UPDATING}
     * <p>
     * The post-modification values are captured when the transaction commits.
     */
    void enlistUpdating(ManagedObject entity);

    /**
     * Fires the appropriate event and lifecycle callback: {@literal LOADED}
     */
    void recognizeLoaded(ManagedObject entity);

    /**
     * Fires the appropriate event and lifecycle callback: {@literal PERSISTING}
     */
    void recognizePersisting(ManagedObject entity);

    /**
     * Fires the appropriate event and lifecycle callback: {@literal UPDATING}
     */
    void recognizeUpdating(ManagedObject entity);

}

