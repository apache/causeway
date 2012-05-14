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

package org.apache.isis.runtimes.dflt.runtime.installerregistry.installerapi;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterFactory;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.runtime.logging.Logger;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.AdapterManagerExtended;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.IsisObjectStoreLogger;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.ObjectStore;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.ObjectStorePersistence;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.ObjectStoreTransactionManagement;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.algorithm.PersistAlgorithm;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.algorithm.dflt.DefaultPersistAlgorithm;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManager;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.IdentifierGenerator;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.ObjectFactory;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSessionTransactionManagement;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransactionManager;

public abstract class ObjectStorePersistenceMechanismInstallerAbstract extends PersistenceMechanismInstallerAbstract {

    private static final String LOGGING_PROPERTY = Logger.PROPERTY_ROOT + "objectstore";

    public ObjectStorePersistenceMechanismInstallerAbstract(final String name) {
        super(name);
    }

    /**
     * Will return a {@link PersistenceSessionObjectStore}; subclasses are free
     * to downcast if required.
     */
    @Override
    protected PersistenceSession createPersistenceSession(final PersistenceSessionFactory persistenceSessionFactory, final AdapterManagerExtended adapterManager, final ObjectAdapterFactory adapterFactory, final ObjectFactory objectFactory, final IdentifierGenerator identifierGenerator,
            final ServicesInjector servicesInjector) {

        final PersistAlgorithm persistAlgorithm = createPersistAlgorithm(getConfiguration());
        ObjectStore objectStore = createObjectStore(getConfiguration(), adapterFactory, adapterManager);

        ensureThatArg(persistAlgorithm, is(not(nullValue())));
        ensureThatArg(objectStore, is(not(nullValue())));

        if (getConfiguration().getBoolean(LOGGING_PROPERTY, false)) {
            final String level = getConfiguration().getString(LOGGING_PROPERTY + ".level", "debug");
            objectStore = new IsisObjectStoreLogger(objectStore, level);
        }

        final PersistenceSession persistenceSession = createObjectStorePersistor(persistenceSessionFactory, adapterFactory, objectFactory, servicesInjector, identifierGenerator, adapterManager, persistAlgorithm, objectStore);

        final IsisTransactionManager transactionManager = createTransactionManager(persistenceSession, objectStore);

        ensureThatArg(persistenceSession, is(not(nullValue())));
        ensureThatArg(transactionManager, is(not(nullValue())));

        persistenceSession.setDirtiableSupport(true);
        transactionManager.injectInto(persistenceSession);

        // ... and finally return
        return persistenceSession;
    }

    // ///////////////////////////////////////////
    // Optional hook methods
    // ///////////////////////////////////////////

    /**
     * Can optionally be overridden, but by default creates an
     * {@link PersistenceSessionObjectStore}.
     */
    protected PersistenceSession createObjectStorePersistor(final PersistenceSessionFactory persistenceSessionFactory, final ObjectAdapterFactory adapterFactory, final ObjectFactory objectFactory, final ServicesInjector servicesInjector, final IdentifierGenerator identifierGenerator,
            final AdapterManagerExtended adapterManager, final PersistAlgorithm persistAlgorithm, final ObjectStorePersistence objectStore) {
        return new PersistenceSession(persistenceSessionFactory, adapterFactory, objectFactory, servicesInjector, identifierGenerator, adapterManager, persistAlgorithm, objectStore);
    }

    /**
     * Hook method to create {@link PersistAlgorithm}.
     * 
     * <p>
     * By default returns a {@link DefaultPersistAlgorithm}.
     */
    protected PersistAlgorithm createPersistAlgorithm(final IsisConfiguration configuration) {
        return new DefaultPersistAlgorithm();
    }

    /**
     * Hook method to return an {@link IsisTransactionManager}.
     * 
     * <p>
     * By default returns a {@link ObjectStoreTransactionManager}.
     */
    protected IsisTransactionManager createTransactionManager(final PersistenceSessionTransactionManagement persistor, final ObjectStoreTransactionManagement objectStore) {
        return new IsisTransactionManager(persistor, objectStore);
    }

    // ///////////////////////////////////////////
    // Mandatory hook methods
    // ///////////////////////////////////////////

    /**
     * Hook method to return {@link ObjectStore}.
     */
    protected abstract ObjectStore createObjectStore(IsisConfiguration configuration, ObjectAdapterFactory adapterFactory, AdapterManager adapterManager);

}
