/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.runtime.system.persistence;

import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.core.commons.collections.Bin;
import org.apache.isis.core.commons.components.SessionScopedComponent;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterByIdProvider;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ManagedObjectState;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.memento.Data;
import org.apache.isis.core.runtime.persistence.FixturesInstalledState;
import org.apache.isis.core.runtime.persistence.FixturesInstalledStateHolder;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.TransactionalResource;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManagerJdoInternal;

public interface PersistenceSession 
extends 
    ObjectAdapterProvider.Delegating,
    ObjectAdapterByIdProvider.Delegating,
    TransactionalResource, 
    SessionScopedComponent {

    // -------------------------------------------------------------------------------------------------
    // -- STABLE API (DRAFT)
    // -------------------------------------------------------------------------------------------------
    
    IsisConfiguration getConfiguration();
    IsisTransactionManagerJdoInternal getTransactionManager();
    ServiceInjector getServiceInjector();
    
    void open();
    void close();
    
    default boolean flush() {
        return getTransactionManager().flushTransaction();
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
        getTransactionManager().executeWithinTransaction(()->refreshRoot(domainObject));
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
    ManagedObjectState stateOf(Object pojo);
    
    /** whether pojo is recognized by the persistence layer, that is, it has an ObjectId
     * @since 2.0*/
    boolean isRecognized(Object pojo);
    
    /**@since 2.0*/
    Object fetchPersistentPojo(RootOid rootOid);

    /**@since 2.0*/
    default Object fetchPersistentPojoInTransaction(final RootOid oid) {
        return getTransactionManager().executeWithinTransaction(()->fetchPersistentPojo(oid));
    }

    /**@since 2.0*/
    Map<RootOid, Object> fetchPersistentPojos(List<RootOid> rootOids);




    // -------------------------------------------------------------------------------------------------
    // -- JDO SPECIFIC
    // -------------------------------------------------------------------------------------------------
    
    PersistenceManager getPersistenceManager();
    /**
     * Convenient equivalent to {@code getPersistenceManager()}.
     * @return
     */
    default PersistenceManager pm() {
        return getPersistenceManager();
    }
    
    /**
     * Not type safe. For type-safe queries use <br/><br/> {@code pm().newNamedQuery(cls, queryName)}
     * @param cls
     * @param queryName
     * @return
     */
    default <T> javax.jdo.Query newJdoNamedQuery(Class<T> cls, String queryName){
        return pm().newNamedQuery(cls, queryName);
    }

    /**
     * Not type safe. For type-safe queries use <br/><br/> {@code pm().newQuery(cls, queryName)}
     * @param cls
     * @return
     */
    default <T> javax.jdo.Query newJdoQuery(Class<T> cls){
        return pm().newQuery(cls);
    }

    /**
     * Not type safe. For type-safe queries use <br/><br/> {@code pm().newQuery(cls, filter)}
     * @param cls
     * @param filter
     * @return
     */
    default <T> javax.jdo.Query newJdoQuery(Class<T> cls, String filter){
        return pm().newQuery(cls, filter);
    }
    
    // -------------------------------------------------------------------------------------------------
    // -- API NOT STABLE YET - SUBJECT TO REFACTORING
    // -------------------------------------------------------------------------------------------------

    // -- FIXTURE SUPPORT

    /**
     * @see #isFixturesInstalled()
     */
    static final String INSTALL_FIXTURES_KEY = "isis.persistor.datanucleus.install-fixtures";
    static final boolean INSTALL_FIXTURES_DEFAULT = false;
    
    /**
     * Determine if the object store has been initialized with its set of start
     * up objects.
     *
     * <p>
     * This method is called only once after the init has been called. If this flag
     * returns <code>not_Installed</code> the framework will run the fixtures to
     * initialise the persistor.
     *
     * <p>
     * Returns whether fixtures are installed.
     * <p>
     * This caching is important because if we've determined, for a given run,
     * that fixtures are not installed, then we don't want to change our mind by
     * asking the object store again in another session.
     *
     * @see FixturesInstalledStateHolder
     */
    FixturesInstalledState getFixturesInstalledState();
    
    // -- MEMENTO SUPPORT
    
    ObjectAdapter adapterOfMemento(ObjectSpecification spec, Oid oid, Data data);
    
    // -- TODO remove ObjectAdapter references from API
    
    ObjectAdapter adapterFor(RootOid rootOid);
    
    <T> List<ObjectAdapter> allMatchingQuery(final Query<T> query);
    <T> ObjectAdapter firstMatchingQuery(final Query<T> query);
    
    void destroyObjectInTransaction(ObjectAdapter adapter);
    void makePersistentInTransaction(ObjectAdapter adapter);
    
    // -- OTHERS
    
    void execute(List<PersistenceCommand> persistenceCommandList);
    
    long getLifecycleStartedAtSystemNanos();
    
    // -- LOOKUP
    
    static <T extends PersistenceSession> Bin<T> current(Class<T> requiredType) {
        return _Context.threadLocalSelect(PersistenceSession.class, requiredType);
    }

}
