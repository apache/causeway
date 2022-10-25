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
package org.apache.causeway.core.transaction.changetracking;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.services.objectlifecycle.PropertyChangeRecord;

/**
 * Responsible for collecting the details of all changes to domain objects
 * within a transaction.
 *
 * @since 1.x but renamed/refactored for v2 {@index}
 */
public interface EntityChangeTracker extends DisposableBean {

    /**
     * Provided primarily for testing, but also used in cases where an attempt is made to resolve a bean but
     * there is no active interaction.
     */
    EntityChangeTracker NOOP = new EntityChangeTracker() {
        @Override public void destroy() throws Exception {}
        @Override public void enlistCreated(final ManagedObject entity) {}
        @Override public void enlistUpdating(final ManagedObject entity, final Can<PropertyChangeRecord> propertyChangeRecords) {}
        @Override public void enlistDeleting(final ManagedObject entity) {}
        @Override public void incrementLoaded(final ManagedObject entity) {}
    };

    /**
     * Publishing support: for object stores to enlist an object that has just been created,
     * capturing a dummy value <tt>'[NEW]'</tt> for the pre-modification value.
     *
     * <p>
     * The post-modification values are captured when the transaction commits.
     * </p>
     */
    void enlistCreated(ManagedObject entity);

    /**
     * Publishing support: for object stores to enlist an object that is about to be updated,
     * capturing the pre-modification values of the properties of the {@link ManagedObject}.
     *
     * <p>
     * The post-modification values are captured when the transaction commits.
     *
     * <p>
     * Overload as an optimization for ORMs (specifically, JPA) where already have access to the changed records by
     * accessing the ORM-specific data structures (<code>EntityManager</code>'s unit-of-work).
     *
     * </p>
     *
     * @param entity
     * @param propertyChangeRecords - optional parameter (as a performance optimization) to provide the pre-computed {@link PropertyChangeRecord}s from the ORM.  JPA does this, JDO does not.
     */
    void enlistUpdating(ManagedObject entity, @Nullable Can<PropertyChangeRecord> propertyChangeRecords);


    /**
     * Publishing support: for object stores to enlist an object that is about to be deleted,
     * capturing the pre-deletion value of the properties of the {@link ManagedObject}.
     *
     * <p>
     * The post-modification values are captured  when the transaction commits.  In the case of deleted objects, a
     * dummy value <tt>'[DELETED]'</tt> is used as the post-modification value.
     * </p>
     */
    void enlistDeleting(ManagedObject entity) ;

    /**
     * Not strictly part of the concern of entity tracking, but allows the default implementation to also implement
     * the {@link org.apache.causeway.applib.services.metrics.MetricsService}.
     */
    void incrementLoaded(ManagedObject entity);




}

