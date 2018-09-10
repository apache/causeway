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
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService.FieldResetPolicy;
import org.apache.isis.core.commons.components.SessionScopedComponent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterByIdProvider;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.adapter.oid.ParentedCollectionOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
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

    // -- CONSTANTS

    public static final String SERVICE_IDENTIFIER = "1";

    /**
     * @see #isFixturesInstalled()
     */
    public static final String INSTALL_FIXTURES_KEY = OptionHandlerFixtureAbstract.DATANUCLEUS_INSTALL_FIXTURES_KEY;
    public static final boolean INSTALL_FIXTURES_DEFAULT = false;

    //---

    MementoRecreateObjectSupport mementoSupport();

    ObjectAdapter adapterForAny(RootOid rootOid);
    <T> List<ObjectAdapter> allMatchingQuery(final Query<T> query);

    // --

    void close();

    ObjectAdapter createTransientInstance(ObjectSpecification spec);

    ObjectAdapter createViewModelInstance(ObjectSpecification spec, String memento);

    void destroyObjectInTransaction(ObjectAdapter adapter);

    void execute(List<PersistenceCommand> persistenceCommandList);
    <T> ObjectAdapter firstMatchingQuery(final Query<T> query);

    boolean flush();

    ObjectAdapter getAggregateRoot(ParentedCollectionOid collectionOid);

    IsisConfiguration getConfiguration();

    PersistenceManager getPersistenceManager();
    
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
    Map<RootOid, Object> fetchPersistentPojos(List<RootOid> rootOids);

    /**
     * Convenient equivalent to {@code getPersistenceManager()}.
     * @return
     */
    default PersistenceManager pm() {
        return getPersistenceManager();
    }

    ServicesInjector getServicesInjector();

    IsisTransactionManager getTransactionManager();

    Object instantiateAndInjectServices(ObjectSpecification spec);

    boolean isFixturesInstalled();

    Object lookup(Bookmark bookmark, FieldResetPolicy fieldResetPolicy);

    void makePersistentInTransaction(ObjectAdapter adapter);

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

    void open();

    void refreshRoot(ObjectAdapter adapter);

    void resolve(Object parent);

    

}
