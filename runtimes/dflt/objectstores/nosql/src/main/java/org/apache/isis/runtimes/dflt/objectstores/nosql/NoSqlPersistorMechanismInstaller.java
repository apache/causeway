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


package org.apache.isis.runtimes.dflt.objecstores.nosql;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.runtimes.dflt.runtime.persistence.PersistenceSessionFactory;
import org.apache.isis.runtimes.dflt.runtime.persistence.PersistenceSessionFactoryDelegating;
import org.apache.isis.runtimes.dflt.runtime.persistence.adapterfactory.AdapterFactory;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.AdapterManager;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.ObjectStore;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.ObjectStorePersistenceMechanismInstallerAbstract;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.OidGenerator;
import org.apache.isis.runtimes.dflt.runtime.system.DeploymentType;


public abstract class NoSqlPersistorMechanismInstaller extends ObjectStorePersistenceMechanismInstallerAbstract {
	
	private NoSqlObjectStore objectStore;

	public NoSqlPersistorMechanismInstaller(String name) {
		super(name);
	}
	
    @Override
    protected ObjectStore createObjectStore(IsisConfiguration configuration, AdapterFactory objectFactory, AdapterManager adapterManager) {
        return getObjectStore(configuration);
    }

    @Override
    protected OidGenerator createOidGenerator(IsisConfiguration configuration) {
        return getObjectStore(configuration).getOidGenerator();
     }

    public PersistenceSessionFactory createPersistenceSessionFactory(final DeploymentType deploymentType) {
        return new PersistenceSessionFactoryDelegating(deploymentType, this) {};
    }

    private NoSqlObjectStore getObjectStore(IsisConfiguration configuration) {
        if (objectStore == null) {
            KeyCreator keyCreator = createKeyCreator();
            VersionCreator versionCreator = createVersionCreator();
            NoSqlDataDatabase db = createNoSqlDatabase(configuration);
            NoSqlOidGenerator oidGenerator = createOidGenerator(db);
            objectStore = new NoSqlObjectStore(db, oidGenerator, keyCreator, versionCreator);
        }
        return objectStore;
    }

    protected NoSqlOidGenerator createOidGenerator(NoSqlDataDatabase db) {
        return new NoSqlOidGenerator(db);
    }

    protected abstract NoSqlDataDatabase createNoSqlDatabase(IsisConfiguration configuration);

    protected SerialKeyCreator createKeyCreator() {
        return new SerialKeyCreator();
    }
    
    private VersionCreator createVersionCreator() {
        return new SerialNumberVersionCreator();
    }
}
