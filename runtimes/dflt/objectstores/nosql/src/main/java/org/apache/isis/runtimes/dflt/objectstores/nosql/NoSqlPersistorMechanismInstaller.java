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

package org.apache.isis.runtimes.dflt.objectstores.nosql;

import java.util.HashMap;
import java.util.Map;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterFactory;
import org.apache.isis.runtimes.dflt.runtime.installerregistry.installerapi.ObjectStorePersistenceMechanismInstallerAbstract;
import org.apache.isis.runtimes.dflt.runtime.persistence.PersistenceSessionFactoryDelegating;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.ObjectStore;
import org.apache.isis.runtimes.dflt.runtime.system.DeploymentType;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManager;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.OidGenerator;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSessionFactory;

public abstract class NoSqlPersistorMechanismInstaller extends ObjectStorePersistenceMechanismInstallerAbstract {

    private NoSqlObjectStore objectStore;

    public NoSqlPersistorMechanismInstaller(final String name) {
        super(name);
    }

    @Override
    protected ObjectStore createObjectStore(final IsisConfiguration configuration,
        final ObjectAdapterFactory objectFactory, final AdapterManager adapterManager) {
        return getObjectStore(configuration);
    }

    @Override
    protected OidGenerator createOidGenerator(final IsisConfiguration configuration) {
        return getObjectStore(configuration).getOidGenerator();
    }

    @Override
    public PersistenceSessionFactory createPersistenceSessionFactory(final DeploymentType deploymentType) {
        return new PersistenceSessionFactoryDelegating(deploymentType, this) {
        };
    }

    private NoSqlObjectStore getObjectStore(final IsisConfiguration configuration) {
        if (objectStore == null) {
            final KeyCreator keyCreator = createKeyCreator();
            final VersionCreator versionCreator = createVersionCreator();
            final NoSqlDataDatabase db = createNoSqlDatabase(configuration);
            final NoSqlOidGenerator oidGenerator = createOidGenerator(db);
            
            DataEncrypter writingDataEncrypter = new Rot13Encryption();
            Map<String, DataEncrypter> availableDataEncrypters = new HashMap<String, DataEncrypter>();
            availableDataEncrypters.put(writingDataEncrypter.getType(), writingDataEncrypter);
            DataEncrypter passThoughEncrypter = new NoEncryption();
            availableDataEncrypters.put(passThoughEncrypter.getType(), passThoughEncrypter);
            
            objectStore = new NoSqlObjectStore(db, oidGenerator, keyCreator, versionCreator, writingDataEncrypter, availableDataEncrypters);
        }
        return objectStore;
    }

    protected NoSqlOidGenerator createOidGenerator(final NoSqlDataDatabase db) {
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
