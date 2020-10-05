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
package org.apache.isis.core.runtime.persistence.changetracking;

import org.apache.isis.core.metamodel.spec.ManagedObject;

public interface EntityChangeTracker {

    /**
     * @param adapter
     * @return whether given {@code adapter} has already been enlisted.
     */
    boolean isEnlisted(ManagedObject adapter);
    
    /**
     * Auditing and publishing support: for object stores to enlist an object that has just been created,
     * capturing a dummy value <tt>'[NEW]'</tt> for the pre-modification value.
     * <p>
     * fires the appropriate events and lifecycle callbacks TODO which ones
     * <p>
     * The post-modification values are captured when the transaction commits.
     */
    void enlistCreated(ManagedObject entity);
    
    /**
     * Auditing and publishing support: for object stores to enlist an object that is about to be deleted,
     * capturing the pre-deletion value of the properties of the {@link ManagedObject}.
     * <p>
     * fires the appropriate events and lifecycle callbacks TODO which ones
     * <p>
     * The post-modification values are captured  when the transaction commits.  In the case of deleted objects, a
     * dummy value <tt>'[DELETED]'</tt> is used as the post-modification value.
     */
    void enlistDeleting(ManagedObject entity);
    
    /**
     * Auditing and publishing support: for object stores to enlist an object that is about to be updated,
     * capturing the pre-modification values of the properties of the {@link ManagedObject}.
     * <p>
     * fires the appropriate events and lifecycle callbacks TODO which ones
     * <p>
     * The post-modification values are captured when the transaction commits.
     */
    void enlistUpdating(ManagedObject entity);

    void recognizeLoaded(ManagedObject entity);

    void recognizePersisting(ManagedObject entity);

    void recognizeUpdating(ManagedObject entity);

}
