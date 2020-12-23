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
import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.repository.EntityState;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.context.HasMetaModelContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.persistence.jdo.provider.persistence.HasPersistenceManager;
import org.apache.isis.persistence.jdo.provider.persistence.JdoTypedQuerySupport;

public interface JdoPersistenceSession 
extends 
    HasMetaModelContext,
    HasPersistenceManager,
    JdoTypedQuerySupport {

    // -------------------------------------------------------------------------------------------------
    // -- STABLE API (DRAFT)
    // -------------------------------------------------------------------------------------------------

    void open();
    void close();

    default ManagedObject adapterFor(@Nullable Object pojo) {
        return _Utils.adapterFor(getMetaModelContext(), pojo);
    }
    
    /**
     * Forces a reload (refresh in JDO terminology) of the domain object
     */
    void refreshRoot(Object domainObject);


    /**
     * @param pojo a persistable object
     * @return String representing an object's id.
     * @since 2.0
     */
    String identifierFor(Object pojo);
    
    /**
     * @since 2.0
     */
    EntityState getEntityState(Object pojo);

    /** whether pojo is recognized by the persistence layer, that is, it has an ObjectId
     * @since 2.0*/
    boolean isRecognized(Object pojo);

    /**
     * @since 2.0
     * @throws NoSuchObjectException if not found
     */
    ManagedObject fetchByIdentifier(ObjectSpecification spec, String identifier);

    Can<ManagedObject> allMatchingQuery(final Query<?> query);
    Optional<ManagedObject> firstMatchingQuery(final Query<?> query);

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
