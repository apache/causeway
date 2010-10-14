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


package org.apache.isis.extensions.hibernate.objectstore;

import org.apache.log4j.Logger;
import org.apache.isis.metamodel.config.IsisConfiguration;
import org.apache.isis.extensions.hibernate.objectstore.persistence.algorithm.SimplePersistAlgorithm;
import org.apache.isis.extensions.hibernate.objectstore.persistence.algorithm.TwoPassPersistAlgorithm;
import org.apache.isis.extensions.hibernate.objectstore.persistence.oidgenerator.HibernateOidGenerator;
import org.apache.isis.runtime.persistence.PersistenceSession;
import org.apache.isis.runtime.persistence.PersistenceSessionFactory;
import org.apache.isis.runtime.persistence.PersistenceSessionLogger;
import org.apache.isis.runtime.persistence.adapterfactory.AdapterFactory;
import org.apache.isis.runtime.persistence.adaptermanager.AdapterManager;
import org.apache.isis.runtime.persistence.objectstore.ObjectStore;
import org.apache.isis.runtime.persistence.objectstore.ObjectStorePersistenceMechanismInstallerAbstract;
import org.apache.isis.runtime.persistence.objectstore.algorithm.PersistAlgorithm;
import org.apache.isis.runtime.persistence.oidgenerator.OidGenerator;
import org.apache.isis.runtime.system.DeploymentType;


/**
 * Installs the Hibernate object store.
 */
public class HibernatePersistenceMechanismInstaller extends ObjectStorePersistenceMechanismInstallerAbstract {
    
	private static final Logger LOG = Logger.getLogger(HibernatePersistenceMechanismInstaller.class);

	public HibernatePersistenceMechanismInstaller() {
		super("hibernate");
	}
    

    //////////////////////////////////////////////////
    // createPersistenceSessionFactory
    //////////////////////////////////////////////////

    public PersistenceSessionFactory createPersistenceSessionFactory(final DeploymentType deploymentType) {
        return new HibernatePersistenceSessionFactory(deploymentType, this);
    }

    @Override
    public PersistenceSession createPersistenceSession(PersistenceSessionFactory persistenceSessionFactory) {
    	LOG.info("installing " + this.getClass().getName());
    	
    	return new PersistenceSessionLogger(super.createPersistenceSession(persistenceSessionFactory));
    }
    

    //////////////////////////////////////////////////
    // createObjectStore
    //////////////////////////////////////////////////

    /**
     * Creates one of four variants of {@link ObjectStore}, dependent on {@link IsisConfiguration}:
     * <ul>
     * <li>not remapping &amp; is save immediate : {@link HibernateObjectStoreImmediate} </li>
     * <li>not remapping &amp; not save immediate: {@link HibernateObjectStore} </li>
     * <li>is remapping &amp;  is  save immediate: {@link HibernateObjectStoreRemapping}( {@link HibernateObjectStoreImmediate} ) </li>
     * <li>is remapping &amp;  not save immediate: {@link HibernateObjectStoreRemapping}( {@link HibernateObjectStore} ) </li>
     * </ul> 
     * 
     */
    @Override
    protected ObjectStore createObjectStore(IsisConfiguration configuration, AdapterFactory objectFactory, AdapterManager adapterManager) {
        
        ObjectStore objectStore = 
            isSaveImmediate(configuration) ? 
                    new HibernateObjectStoreImmediate() : 
                    new HibernateObjectStore();
        
        return isRemapping(configuration) ? 
                    new HibernateObjectStoreRemapping(objectStore) : 
                    objectStore;
    }

    private boolean isSaveImmediate(final IsisConfiguration configuration) {
        return configuration.getBoolean(HibernateConstants.SAVE_IMMEDIATE_KEY, true);
    }

    private boolean isRemapping(final IsisConfiguration configuration) {
        return configuration.getBoolean(HibernateConstants.REMAPPING_KEY, false);
    }



    //////////////////////////////////////////////////
    // OidGenerator
    //////////////////////////////////////////////////

    @Override
    protected OidGenerator createOidGenerator(IsisConfiguration configuration) {
        return new HibernateOidGenerator();
    }

    //////////////////////////////////////////////////
    // createPersistAlgorithm
    //////////////////////////////////////////////////
    
    @Override
    protected PersistAlgorithm createPersistAlgorithm(final IsisConfiguration configuration) {
        final String algorithm = getPersistAlgorithm(configuration);
        if ("simple".equals(algorithm)) {
            return new SimplePersistAlgorithm();
        } else {
            return new TwoPassPersistAlgorithm();
        }
    }

    private String getPersistAlgorithm(final IsisConfiguration configuration) {
        return configuration.getString(HibernateConstants.PERSIST_ALGORITHM_KEY);
    }



}
