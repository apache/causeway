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
package org.apache.causeway.core.metamodel.services.objectlifecycle;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;

/**
 * Responsible for collecting and then passing along changes (to the EntityChangeTracker, in persistence commons) so
 * that they can be published; and is responsible for calling the various persistence call-back facets.
 *
 * @since 2.0 {index}
 */
public interface ObjectLifecyclePublisher {

    /**
     * Independent of the persistence stack, called when an object has been created in-memory, for example by
     * {@link FactoryService} and internal {@link ObjectManager}.
     *
     * <p>
     *     Default implementation fires off callback/lifecycle events.
     * </p>
     *
     * @param domainObject - an entity or view-model
     */
    void onPostCreate(ManagedObject domainObject);

    /**
     * Called by both JPA and JDO, just after an object is retrieved from the database.
     *
     * <p>
     *     Default implementation calls <code>EntityChangeTracker#recognizeLoaded(ManagedObject)</code> and
     *     fires off callback/lifecycle events.
     * </p>
     *
     * @param entity
     */
    void onPostLoad(ManagedObject entity);

    /**
     * Called by both JPA and JDO, just before an entity is inserted into the database.
     *
     * <p>
     *     Default implementation fires callbacks (including emitting the <code>PreStoreEvent</code>, eg as subscribed)
     *     by the <code>TimestampService</code>.
     * </p>
     *
     * @param eitherWithOrWithoutOid - either the adapted entity with OID <i>left</i>,
     *      otherwise adapted entity without OID <i>right</i>
     */
    void onPrePersist(Either<ManagedObject, ManagedObject> eitherWithOrWithoutOid);

    /**
     * Called by both JPA and JDO, just after an entity has been inserted into the database.
     *
     * <p>
     *     Default implementation fires callbacks and enlists the entity within <code>EntityChangeTracker</code>
     *     for create/persist.
     * </p>
     *
     * @param entity
     */
    void onPostPersist(ManagedObject entity);

    /**
     * Called by both JPA and JDO (though JDO does <i>not</i> provide any changeRecords).
     *
     * <p>
     *     Default implementation fires callbacks and enlists the entity within <code>EntityChangeTracker</code>
     *     for update.
     * </p>
     *
     * @param entity
     * @param changeRecords - optional parameter to provide the pre-computed {@link PropertyChangeRecord}s from the ORM.  JPA does this, JDO does not.
     */
    void onPreUpdate(ManagedObject entity, @Nullable Can<PropertyChangeRecord> changeRecords);

    /**
     * Called by both JPA and JDO, after an existing entity has been updated.
     *
     * <p>
     *     Default implementation fires callbacks.
     * </p>
     *
     * @param entity
     */
    void onPostUpdate(ManagedObject entity);

    /**
     * Called by both JPA and JDO, just beforean entity is deleted from the database.
     *
     * <p>
     *     Default implementation fires callbacks and enlists the entity within <code>EntityChangeTracker</code>
     *     for delete/remove.
     * </p>
     *
     * @param entity
     */
    void onPreRemove(ManagedObject entity);

    //void onPostRemove(ManagedObject entity);



}
