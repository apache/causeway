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

import org.apache.log4j.Logger;

import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterFactory;
import org.apache.isis.runtimes.dflt.runtime.installerregistry.installerapi.ObjectStorePersistenceMechanismInstallerAbstract;
import org.apache.isis.runtimes.dflt.runtime.persistence.PersistenceSessionFactoryDelegating;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.ObjectStore;
import org.apache.isis.runtimes.dflt.runtime.system.DeploymentType;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManager;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.OidGenerator;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSessionFactory;

public abstract class NoSqlPersistorMechanismInstaller extends ObjectStorePersistenceMechanismInstallerAbstract {

    private static final Logger LOG = Logger.getLogger(NoSqlPersistorMechanismInstaller.class);

    private static final String NAKEDOBJECTS_ENCRYPTION_CLASSES = ConfigurationConstants.ROOT + "nosql.encryption";

    private NoSqlObjectStore objectStore;

    public NoSqlPersistorMechanismInstaller(final String name) {
        super(name);
    }

    @Override
    protected ObjectStore createObjectStore(final IsisConfiguration configuration, final ObjectAdapterFactory objectFactory, final AdapterManager adapterManager) {
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

            final Map<String, DataEncryption> availableDataEncryption = new HashMap<String, DataEncryption>();
            try {
                final String[] encryptionClasses = getConfiguration().getList(NAKEDOBJECTS_ENCRYPTION_CLASSES);
                DataEncryption writeWithEncryption = null;
                boolean encryptionSpecified = false;
                for (final String fullyQualifiedClass : encryptionClasses) {
                    LOG.info("  adding encryption " + fullyQualifiedClass);
                    final DataEncryption encryption = (DataEncryption) InstanceUtil.createInstance(fullyQualifiedClass);
                    encryption.init(configuration);
                    availableDataEncryption.put(encryption.getType(), encryption);
                    if (!encryptionSpecified) {
                        writeWithEncryption = encryption;
                    }
                    encryptionSpecified = true;
                }
                if (!encryptionSpecified) {
                    LOG.warn("No encryption specified");
                    final DataEncryption encryption = new NoEncryption();
                    availableDataEncryption.put(encryption.getType(), encryption);
                    writeWithEncryption = encryption;
                }
                objectStore = new NoSqlObjectStore(db, oidGenerator, keyCreator, versionCreator, writeWithEncryption, availableDataEncryption);
            } catch (final IllegalArgumentException e) {
                throw new IsisException(e);
            } catch (final SecurityException e) {
                throw new IsisException(e);
            }
        }
        return objectStore;
    }

    protected NoSqlOidGenerator createOidGenerator(final NoSqlDataDatabase db) {
        return new NoSqlOidGenerator(db);
    }

    protected abstract NoSqlDataDatabase createNoSqlDatabase(IsisConfiguration configuration);

    protected NoSqlKeyCreator createKeyCreator() {
        return new NoSqlKeyCreator();
    }

    private VersionCreator createVersionCreator() {
        return new SerialNumberVersionCreator();
    }
}
