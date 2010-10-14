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


package org.apache.isis.extensions.xml.objectstore;

import org.apache.log4j.Logger;
import org.apache.isis.metamodel.config.IsisConfiguration;
import org.apache.isis.extensions.xml.objectstore.internal.adapter.XmlAdapterManager;
import org.apache.isis.extensions.xml.objectstore.internal.clock.DefaultClock;
import org.apache.isis.runtime.persistence.PersistenceSessionFactory;
import org.apache.isis.runtime.persistence.adapterfactory.AdapterFactory;
import org.apache.isis.runtime.persistence.adaptermanager.AdapterManager;
import org.apache.isis.runtime.persistence.adaptermanager.AdapterManagerExtended;
import org.apache.isis.runtime.persistence.objectstore.ObjectStore;
import org.apache.isis.runtime.persistence.objectstore.ObjectStorePersistenceMechanismInstallerAbstract;
import org.apache.isis.runtime.persistence.oidgenerator.OidGenerator;
import org.apache.isis.runtime.persistence.oidgenerator.timebased.TimeBasedOidGenerator;
import org.apache.isis.runtime.system.DeploymentType;


public class XmlPersistenceMechanismInstaller extends ObjectStorePersistenceMechanismInstallerAbstract {
    
	@SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(XmlPersistenceMechanismInstaller.class);
    private XmlObjectStore objectStore;

    public XmlPersistenceMechanismInstaller() {
    	super("xml");
    }
    

    @Override
    protected ObjectStore createObjectStore(IsisConfiguration configuration, AdapterFactory objectFactory, AdapterManager adapterManager) {
        if (objectStore == null) {
            objectStore = new XmlObjectStore(configuration);
            objectStore.setClock(new DefaultClock());
        }
        return objectStore;
    }

    
    @Override
    protected AdapterManagerExtended createAdapterManager(final IsisConfiguration configuration) {
        return new XmlAdapterManager();
    }

    @Override
    protected OidGenerator createOidGenerator(IsisConfiguration configuration) {
        return new TimeBasedOidGenerator();
    }

    public PersistenceSessionFactory createPersistenceSessionFactory(final DeploymentType deploymentType) {
        return new XmlPersistenceSessionFactory(deploymentType, this);
    }
}
