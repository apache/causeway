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

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.spec.ManagedObject;

/**
 * Responsible for collecting then immediately publishing changes to domain objects
 * within a transaction, that is, notify publishing subscribers and call the various
 * persistence call-back facets.
 *
 * @since 2.0 {index}
 * @apiNote Introduced for JPA (EclipseLink implementation). More lightweight than
 * {@link EntityChangeTracker}
 */
public interface PropertyChangePublisher {

    /**
     * TODO update ... An override of {@link EntityChangeTracker#enlistUpdating(ManagedObject)}
     * where the caller already knows the old value (and so the
     * {@link EntityChangeTracker} does not need to capture the old value itself.
     *
     * @param entity - being enlisted
     * @param preValue - the pre-value (could be null)
     */
    void onPreUpdate(ManagedObject entity, Can<PropertyChangeRecord> changeRecords);

    void onPrePersist(ManagedObject entity);

    void onPreRemove(ManagedObject entity);

    void onPostPersist(ManagedObject entity);

    void onPostUpdate(ManagedObject entity);

    void onPostRemove(ManagedObject entity);

}
