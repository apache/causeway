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
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.adapter.concurrency.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.oid.Oid.State;
import org.apache.isis.core.metamodel.adapter.oid.ParentedCollectionOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.TransactionalResource;
import org.apache.isis.core.runtime.runner.opts.OptionHandlerFixtureAbstract;
import org.apache.isis.core.runtime.system.persistence.adaptermanager.ObjectAdapterContext.MementoRecreateObjectSupport;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;

public interface PersistenceSession extends ObjectAdapterProvider.Delegating, TransactionalResource, SessionScopedComponent {

    // -- CONSTANTS

    public static final String SERVICE_IDENTIFIER = "1";

    /**
     * @see #isFixturesInstalled()
     */
    public static final String INSTALL_FIXTURES_KEY = OptionHandlerFixtureAbstract.DATANUCLEUS_INSTALL_FIXTURES_KEY;
    public static final boolean INSTALL_FIXTURES_DEFAULT = false;

    // [ahuber] could as well be 'protected', not referenced from other then implementing classes
    public static final String ROOT_KEY = OptionHandlerFixtureAbstract.DATANUCLEUS_ROOT_KEY;

    /**
     * Append regular <a href="http://www.datanucleus.org/products/accessplatform/persistence_properties.html">datanucleus properties</a> to this key
     */
    public static final String DATANUCLEUS_PROPERTIES_ROOT = ROOT_KEY + "impl.";

    //---

    MementoRecreateObjectSupport mementoSupport();

    ObjectAdapter adapterFor(RootOid rootOid, ConcurrencyChecking concurrencyChecking);
    Map<RootOid, ObjectAdapter> adaptersFor(List<RootOid> rootOids, ConcurrencyChecking concurrencyChecking);
    ObjectAdapter adapterForAny(RootOid rootOid);
    <T> List<ObjectAdapter> allMatchingQuery(final Query<T> query);
    
    /**
     * As per {@link #adapterFor(RootOid, ConcurrencyChecking)}, with
     * {@link ConcurrencyChecking#NO_CHECK no checking}.
     *
     * <p>
     * This method  will <i>always</i> return an object, possibly indicating it is persistent; so make sure that you
     * know that the oid does indeed represent an object you know exists.
     * </p>
     */
    default ObjectAdapter adapterFor(final RootOid rootOid) {
        return adapterFor(rootOid, ConcurrencyChecking.NO_CHECK);
    }
    
    default Map<RootOid, ObjectAdapter> adaptersFor(List<RootOid> rootOids) {
        return adaptersFor(rootOids, ConcurrencyChecking.NO_CHECK);
    }

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
     * @param pojo
     * @param type
     * @return String representing an object's id.
     * @since 2.0.0-M2
     */
    String identifierFor(Object pojo, State type);

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

    boolean isTransient(Object pojo);
    boolean isRepresentingPersistent(Object pojo);
    boolean isDestroyed(Object pojo);




}
