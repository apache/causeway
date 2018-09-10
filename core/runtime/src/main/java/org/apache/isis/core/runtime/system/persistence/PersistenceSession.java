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
import org.apache.isis.core.commons.components.SessionScopedComponent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterByIdProvider;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.adapter.oid.ParentedCollectionOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.TransactionalResource;
import org.apache.isis.core.runtime.runner.opts.OptionHandlerFixtureAbstract;
import org.apache.isis.core.runtime.system.persistence.adaptermanager.ObjectAdapterContext.MementoRecreateObjectSupport;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;

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
    IsisTransactionManager getTransactionManager();
    ServicesInjector getServicesInjector();
    
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
     * @since 2.0.0-M2
     */
    void refreshRootInTransaction(Object domainObject);
    
    
    /**
     * @param pojo a persistable object
     * @return String representing an object's id.
     * @since 2.0.0-M2
     */
    String identifierFor(Object pojo);
    
    /**@since 2.0.0-M2*/
    boolean isTransient(Object pojo);
    /**@since 2.0.0-M2*/
    boolean isRepresentingPersistent(Object pojo);
    /**@since 2.0.0-M2*/
    boolean isDestroyed(Object pojo);
    /**@since 2.0.0-M2*/
    Object fetchPersistentPojo(RootOid rootOid);
    /**@since 2.0.0-M2*/
    Object fetchPersistentPojoInTransaction(final RootOid oid);
    /**@since 2.0.0-M2*/
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
    
    // -- SERVICE SUPPORT

    static final String SERVICE_IDENTIFIER = "1";

    // -- FIXTURE SUPPORT
    
    /**
     * @see #isFixturesInstalled()
     */
    static final String INSTALL_FIXTURES_KEY = OptionHandlerFixtureAbstract.DATANUCLEUS_INSTALL_FIXTURES_KEY;
    static final boolean INSTALL_FIXTURES_DEFAULT = false;
    
    boolean isFixturesInstalled();
    
    // -- MEMENTO SUPPORT
    
    MementoRecreateObjectSupport mementoSupport();
    
    // -- TODO remove ObjectAdapter references from API
    
    <T> List<ObjectAdapter> allMatchingQuery(final Query<T> query);

    void destroyObjectInTransaction(ObjectAdapter adapter);

    <T> ObjectAdapter firstMatchingQuery(final Query<T> query);
    
    ObjectAdapter getAggregateRoot(ParentedCollectionOid collectionOid);
    
    void makePersistentInTransaction(ObjectAdapter adapter);
    
    // -- OTHERS
    
    void execute(List<PersistenceCommand> persistenceCommandList);

}
