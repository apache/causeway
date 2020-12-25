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
package org.apache.isis.persistence.jdo.integration.persistence;

import java.rmi.NoSuchObjectException;

import org.apache.isis.core.metamodel.context.HasMetaModelContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.persistence.jdo.integration.transaction.TransactionalProcessor;
import org.apache.isis.persistence.jdo.provider.persistence.HasPersistenceManager;

public interface JdoPersistenceSession 
extends 
    HasMetaModelContext,
    HasPersistenceManager {

    // -------------------------------------------------------------------------------------------------
    // -- STABLE API (DRAFT)
    // -------------------------------------------------------------------------------------------------

    void open();
    void close();
    
    TransactionalProcessor getTransactionalProcessor();
    
    /**
     * Forces a reload (refresh in JDO terminology) of the domain object
     */
    void refreshEntity(Object pojo);

    /**
     * @since 2.0
     * @throws NoSuchObjectException if not found
     */
    ManagedObject fetchByIdentifier(ObjectSpecification spec, String identifier);

    /**
     * Removes the specified object from the system. The specified object's data
     * should be removed from the persistence mechanism.
     */
    void destroyObjectInTransaction(ManagedObject adapter);
    
    /**
     * Makes an {@link ManagedObject} persistent. The specified object should be
     * stored away via this object store's persistence mechanism, and have a
     * new and unique OID assigned to it. The object, should also be added to
     * the {@link JdoPersistenceSession} as the object is implicitly 'in use'.
     *
     * <p>
     * If the object has any associations then each of these, where they aren't
     * already persistent, should also be made persistent by recursively calling
     * this method.
     *
     * <p>
     * If the object to be persisted is a collection, then each element of that
     * collection, that is not already persistent, should be made persistent by
     * recursively calling this method.
     */
    void makePersistentInTransaction(ManagedObject adapter);


}
