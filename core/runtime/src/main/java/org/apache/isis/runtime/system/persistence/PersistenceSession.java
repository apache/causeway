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
package org.apache.isis.runtime.system.persistence;

import java.util.List;
import java.util.Map;

import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.components.SessionScopedComponent;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.metamodel.spec.EntityState;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtime.memento.Data;
import org.apache.isis.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.runtime.persistence.objectstore.transaction.TransactionalResource;

public interface PersistenceSession 
extends 
ObjectAdapterProvider,
TransactionalResource, 
SessionScopedComponent {

    // -------------------------------------------------------------------------------------------------
    // -- STABLE API (DRAFT)
    // -------------------------------------------------------------------------------------------------

    ServiceInjector getServiceInjector();
    TransactionService getTransactionService();

    void open();
    void close();

    default void flush() {
        getTransactionService().flushTransaction();
    }

    /**
     * Forces a reload (refresh in JDO terminology) of the domain object
     */
    void refreshRoot(Object domainObject);


    /**
     * Re-initializes the fields of an object. If the object is unresolved then
     * the object's missing data should be retrieved from the persistence
     * mechanism and be used to set up the value objects and associations.
     * @since 2.0
     */
    default void refreshRootInTransaction(final Object domainObject) {
        getTransactionService().executeWithinTransaction(()->refreshRoot(domainObject));
    }

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

    /**@since 2.0*/
    Object fetchPersistentPojo(RootOid rootOid);

    /**@since 2.0*/
    default Object fetchPersistentPojoInTransaction(final RootOid oid) {
        return getTransactionService().executeWithinTransaction(()->fetchPersistentPojo(oid));
    }

    /**@since 2.0*/
    Map<RootOid, Object> fetchPersistentPojos(List<RootOid> rootOids);

    // -------------------------------------------------------------------------------------------------
    // -- API NOT STABLE YET - SUBJECT TO REFACTORING
    // -------------------------------------------------------------------------------------------------
    
    // -- TODO remove ObjectAdapter references from API
    
    ObjectAdapter adapterOfMemento(ObjectSpecification spec, Oid oid, Data data);

    <T> List<ObjectAdapter> allMatchingQuery(final Query<T> query);
    <T> ObjectAdapter firstMatchingQuery(final Query<T> query);

    void destroyObjectInTransaction(ObjectAdapter adapter);
    void makePersistentInTransaction(ObjectAdapter adapter);

    // -- OTHERS

    void execute(List<PersistenceCommand> persistenceCommandList);

    long getLifecycleStartedAtSystemNanos();

    // -- LOOKUP

    static <T extends PersistenceSession> Can<T> current(Class<T> requiredType) {
        return _Context.threadLocalSelect(PersistenceSession.class, requiredType);
    }

}
