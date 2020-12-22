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

import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.commons.ToString;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.transaction.changetracking.EntityChangeTracker;
import org.apache.isis.persistence.jdo.applib.fixturestate.FixturesInstalledStateHolder;
import org.apache.isis.persistence.jdo.integration.persistence.command.PersistenceCommandQueue;
import org.apache.isis.persistence.jdo.integration.persistence.queries.PersistenceQueryProcessor;
import org.apache.isis.persistence.jdo.integration.persistence.query.PersistenceQueryFactory;
import org.apache.isis.persistence.jdo.integration.transaction.TxManFactory;

import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
abstract class IsisPersistenceSessionJdoBase 
implements IsisPersistenceSessionJdo {

    // -- FIELDS

    protected final FixturesInstalledStateHolder fixturesInstalledStateHolder;

    protected final PersistenceQueryFactory persistenceQueryFactory;
    protected final SpecificationLoader specificationLoader;

    @Getter protected final MetaModelContext metaModelContext;
    protected final ServiceInjector serviceInjector;
    protected final ServiceRegistry serviceRegistry;
    protected final IsisConfiguration configuration;

    protected final Supplier<EntityChangeTracker> entityChangeTrackerProvider;

    /**
     * Used to create the {@link #persistenceManager} when {@link #open()}ed.
     */
    protected final PersistenceManagerFactory jdoPersistenceManagerFactory;
    
    PersistenceCommandQueue commandQueue;

    /**
     * populated only when {@link #open()}ed.
     */
    protected PersistenceManager persistenceManager;

    /**
     * populated only when {@link #open()}ed.
     */
    protected final Map<Class<?>, PersistenceQueryProcessor<?>> persistenceQueryProcessorByClass = _Maps.newHashMap();

    // -- CONSTRUCTOR

    /**
     * Initialize the object store so that calls to this object store access
     * persisted objects and persist changes to the object that are saved.
     */
    protected IsisPersistenceSessionJdoBase(
            final MetaModelContext metaModelContext,
            final PersistenceManagerFactory jdoPersistenceManagerFactory,
            final FixturesInstalledStateHolder fixturesInstalledStateHolder) {

        if (log.isDebugEnabled()) {
            log.debug("creating {}", this);
        }

        this.metaModelContext = metaModelContext;
        this.serviceInjector = metaModelContext.getServiceInjector();
        this.serviceRegistry = metaModelContext.getServiceRegistry();
        this.jdoPersistenceManagerFactory = jdoPersistenceManagerFactory;
        this.fixturesInstalledStateHolder = fixturesInstalledStateHolder;

        // injected
        this.configuration = metaModelContext.getConfiguration();
        this.specificationLoader = metaModelContext.getSpecificationLoader();

        this.entityChangeTrackerProvider = ()->lookupService(EntityChangeTracker.class);

        // sub-components
        this.persistenceQueryFactory = PersistenceQueryFactory.of(
                obj->this.adapterFor(obj), 
                this.specificationLoader);
        this.commandQueue = TxManFactory.newCommandQueue(metaModelContext, this); 

        this.state = State.NOT_INITIALIZED;
    }

    // -- GETTERS

    protected SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }

    public EntityChangeTracker getEntityChangeTracker() {
        return entityChangeTrackerProvider.get();
    }
    
    /**
     * Only populated once {@link #open()}'d
     */
    @Override
    public PersistenceManager getPersistenceManager() {
        return persistenceManager;
    }

    // -- ENUMS

    protected enum Type {
        TRANSIENT,
        PERSISTENT
    }

    protected enum State {
        NOT_INITIALIZED, OPEN, CLOSED
    }

    // -- STATE

    protected State state;

    protected void ensureNotOpened() {
        if (state != State.NOT_INITIALIZED) {
            throw new IllegalStateException("Persistence session has already been initialized");
        }
    }

    protected void ensureOpened() {
        ensureStateIs(State.OPEN);
    }

    private void ensureStateIs(final State stateRequired) {
        if (state == stateRequired) {
            return;
        }
        throw new IllegalStateException("State is: " + state + "; should be: " + stateRequired);
    }

    
    // -- OID
    
    /**
     * @param pojo
     * @return oid for the given domain object 
     */
    protected @Nullable Oid oidFor(@Nullable Object pojo) {
        if(pojo==null) {
            return null;
        }
        val adapter = ManagedObject.of(getSpecificationLoader().loadSpecification(pojo.getClass()), pojo);
        return ManagedObjects.identify(adapter).orElse(null);
    }

    // -- HELPERS - SERVICE LOOKUP

    private <T> T lookupService(Class<T> serviceType) {
        T service = lookupServiceIfAny(serviceType);
        if(service == null) {
            throw new IllegalStateException("Could not locate service of type '" + serviceType + "'");
        }
        return service;
    }

    private <T> T lookupServiceIfAny(final Class<T> serviceType) {
        return serviceRegistry.lookupService(serviceType).orElse(null);
    }

    protected <T> Iterable<T> lookupServices(final Class<T> serviceClass) {
        return serviceRegistry.select(serviceClass);
    }

    @Override
    public String toString() {
        return new ToString(this).toString();
    }

}
