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

import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.command.spi.CommandService;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.iactn.InteractionContext;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.metrics.MetricsService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.internal._Config;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.persistence.FixturesInstalledStateHolder;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.transaction.ChangedObjectsServiceInternal;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.queries.PersistenceQueryProcessor;

import lombok.Getter;

abstract class PersistenceSessionBase implements PersistenceSession {

    // -- CONSTANTS

    protected static final Logger LOG = LoggerFactory.getLogger(PersistenceSession.class);

    // -- FIELDS

    protected final FixturesInstalledStateHolder fixturesInstalledStateHolder;

    protected final PersistenceQueryFactory persistenceQueryFactory;
    protected final IsisConfiguration configuration;
    protected final SpecificationLoader specificationLoader;
    protected final AuthenticationSession authenticationSession;

    @Getter 
    protected final ServiceInjector serviceInjector;
    protected final ServiceRegistry serviceRegistry;

    protected final CommandContext commandContext;
    protected final CommandService commandService;

    protected final InteractionContext interactionContext;
    protected final ChangedObjectsServiceInternal changedObjectsServiceInternal;
    protected final FactoryService factoryService;
    protected final MetricsService metricsService;
    protected final ClockService clockService;
    protected final UserService userService;
    
    /**
     * Set to System.nanoTime() when session opens.
     */
    protected long openedAtSystemNanos = -1L;
    
    /**
     * Used to create the {@link #persistenceManager} when {@link #open()}ed.
     */
    protected final PersistenceManagerFactory jdoPersistenceManagerFactory;

    // not final only for testing purposes
    protected IsisTransactionManager transactionManager;


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
    protected PersistenceSessionBase(
            final AuthenticationSession authenticationSession,
            final PersistenceManagerFactory jdoPersistenceManagerFactory,
            final FixturesInstalledStateHolder fixturesInstalledStateHolder) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("creating {}", this);
        }

        this.serviceInjector = IsisContext.getServiceInjector();
        this.serviceRegistry = IsisContext.getServiceRegistry();
        this.jdoPersistenceManagerFactory = jdoPersistenceManagerFactory;
        this.fixturesInstalledStateHolder = fixturesInstalledStateHolder;

        // injected
        this.configuration = _Config.getConfiguration();
        this.specificationLoader = IsisContext.getSpecificationLoader();
        this.authenticationSession = authenticationSession;

        this.commandContext = lookupService(CommandContext.class);
        this.commandService = lookupService(CommandService.class);
        this.interactionContext = lookupService(InteractionContext.class);
        this.changedObjectsServiceInternal = lookupService(ChangedObjectsServiceInternal.class);
        this.metricsService = lookupService(MetricsService.class);
        this.factoryService = lookupService(FactoryService.class);
        this.clockService = lookupService(ClockService.class);
        this.userService = lookupService(UserService.class);

        // sub-components
        this.persistenceQueryFactory = new PersistenceQueryFactory(
                obj->this.getObjectAdapterProvider().adapterFor(obj), 
                this.specificationLoader);
        this.transactionManager = new IsisTransactionManager(this);

        this.state = State.NOT_INITIALIZED;
    }

    // -- GETTERS

    protected SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }
    protected AuthenticationSession getAuthenticationSession() {
        return authenticationSession;
    }

    /**
     * The configured {@link IsisTransactionManager}.
     */
    @Override
    public IsisTransactionManager getTransactionManager() {
        return transactionManager;
    }

    /**
     * Only populated once {@link #open()}'d
     */
    @Override
    public PersistenceManager getPersistenceManager() {
        return persistenceManager;
    }


    @Override
    public IsisConfiguration getConfiguration() {
        return configuration;
    }
    
    @Override
    public long getLifecycleStartedAtSystemNanos() {
        return openedAtSystemNanos;
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

    // -- TRANSACTIONS

    @Override
    public void startTransaction() {
        final javax.jdo.Transaction transaction = persistenceManager.currentTransaction();
        if (transaction.isActive()) {
            throw new IllegalStateException("Transaction already active");
        }
        transaction.begin();
    }

    @Override
    public void endTransaction() {
        final javax.jdo.Transaction transaction = persistenceManager.currentTransaction();
        if (transaction.isActive()) {
            transaction.commit();
        }
    }

    @Override
    public void abortTransaction() {
        final javax.jdo.Transaction transaction = persistenceManager.currentTransaction();
        if (transaction.isActive()) {
            transaction.rollback();
        }
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
