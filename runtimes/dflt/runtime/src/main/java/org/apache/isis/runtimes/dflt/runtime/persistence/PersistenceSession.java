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


package org.apache.isis.runtimes.dflt.runtime.persistence;

import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.components.Injectable;
import org.apache.isis.core.commons.components.SessionScopedComponent;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderAware;
import org.apache.isis.runtimes.dflt.runtime.persistence.adapterfactory.AdapterFactory;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.AdapterManager;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectfactory.ObjectFactory;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.OidGenerator;
import org.apache.isis.runtimes.dflt.runtime.transaction.IsisTransactionManager;
import org.apache.isis.runtimes.dflt.runtime.transaction.IsisTransactionManagerAware;


public interface PersistenceSession extends
        PersistenceSessionContainer,
        PersistenceSessionForceReloader, 
        PersistenceSessionAdaptedServiceManager, 
        PersistenceSessionTransactionManagement, 
        PersistenceSessionHydrator, 
        PersistenceSessionTestSupport, 
        SpecificationLoaderAware,
        IsisTransactionManagerAware,
        SessionScopedComponent,
        Injectable,
        DebuggableWithTitle {

    


	/**
     * The {@link PersistenceSessionFactory} that created this {@link PersistenceSession}.
     */
    public PersistenceSessionFactory getPersistenceSessionFactory();
    
    // ///////////////////////////////////////////////////////////////////////////
    // open, close
    // ///////////////////////////////////////////////////////////////////////////

    public void open();
    
    public void close();


    /**
     * Determine if the object store has been initialized with its set of start up objects.
     * 
     * <p>
     * This method is called only once after the {@link ApplicationScopedComponent#init()} has been called. If this flag
     * returns <code>false</code> the framework will run the fixtures to initialise the persistor.
     */
    boolean isFixturesInstalled();

    


    
    
    // ///////////////////////////////////////////////////////////////////////////
    // Dependencies
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * The configured {@link OidGenerator}.
     */
    OidGenerator getOidGenerator();

    /**
     * The configured {@link AdapterFactory}.
     * 
     * @return
     */
    AdapterFactory getAdapterFactory();
    
    /**
     * The configured {@link ObjectFactory}.
     */
	public ObjectFactory getObjectFactory();

    /**
     * The configured {@link ServicesInjector}.
     */
    ServicesInjector getServicesInjector();


    /**
     * The configured {@link AdapterManager}.
     */
    AdapterManager getAdapterManager();
    


    /**
     * Inject the {@link IsisTransactionManager}.
     * 
     * <p>
     * This must be injected using setter-based injection rather than through the constructor
     * because there is a bidirectional relationship between the {@link PersistenceSessionHydrator}
     * and the {@link IsisTransactionManager}.
     * 
     * @see #getTransactionManager()
     */
    void setTransactionManager(final IsisTransactionManager transactionManager);

    /**
     * The configured {@link IsisTransactionManager}.
     * 
     * @see #setTransactionManager(IsisTransactionManager)
     */
    IsisTransactionManager getTransactionManager();

    
    /**
     * Inject the {@link SpecificationLoader}.
     * 
     * <p>
     * The need to inject the reflector was introduced to support the HibernateObjectStore, which installs
     * its own <tt>HibernateClassStrategy</tt> to cope with the proxy classes that Hibernate wraps around
     * lists, sets and maps.
     */
    void setSpecificationLoader(SpecificationLoader specificationLoader);





}
