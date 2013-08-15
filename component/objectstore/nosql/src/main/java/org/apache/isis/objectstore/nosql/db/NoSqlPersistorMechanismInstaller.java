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

package org.apache.isis.objectstore.nosql.db;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterFactory;
import org.apache.isis.core.runtime.installerregistry.installerapi.PersistenceMechanismInstallerAbstract;
import org.apache.isis.core.runtime.persistence.objectstore.ObjectStoreSpi;
import org.apache.isis.core.runtime.system.persistence.AdapterManagerSpi;
import org.apache.isis.core.runtime.system.persistence.IdentifierGenerator;
import org.apache.isis.core.runtime.system.persistence.OidGenerator;
import org.apache.isis.objectstore.nosql.NoSqlIdentifierGenerator;
import org.apache.isis.objectstore.nosql.NoSqlObjectStore;
import org.apache.isis.objectstore.nosql.encryption.DataEncryption;
import org.apache.isis.objectstore.nosql.encryption.none.DataEncryptionNone;
import org.apache.isis.objectstore.nosql.versions.VersionCreator;
import org.apache.isis.objectstore.nosql.versions.VersionCreatorDefault;

public abstract class NoSqlPersistorMechanismInstaller extends PersistenceMechanismInstallerAbstract {

    private static final Logger LOG = LoggerFactory.getLogger(NoSqlPersistorMechanismInstaller.class);

    private static final String NAKEDOBJECTS_ENCRYPTION_CLASSES = ConfigurationConstants.ROOT + "nosql.encryption";

    private NoSqlObjectStore objectStore;

    public NoSqlPersistorMechanismInstaller(final String name) {
        super(name);
    }

    @Override
    protected ObjectStoreSpi createObjectStore(final IsisConfiguration configuration, final ObjectAdapterFactory objectFactory, final AdapterManagerSpi adapterManager) {
        return getObjectStore(configuration);
    }

    @Override
    public IdentifierGenerator createIdentifierGenerator(final IsisConfiguration configuration) {
        return getObjectStore(configuration).getIdentifierGenerator();
    }

    private NoSqlObjectStore getObjectStore(final IsisConfiguration configuration) {
        if (objectStore == null) {
            //final KeyCreatorDefault keyCreator = createKeyCreator();
            final VersionCreator versionCreator = createVersionCreator();
            final NoSqlDataDatabase db = createNoSqlDatabase(configuration);
            final OidGenerator oidGenerator = new OidGenerator(createIdentifierGenerator(db));

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
                    final DataEncryption encryption = new DataEncryptionNone();
                    availableDataEncryption.put(encryption.getType(), encryption);
                    writeWithEncryption = encryption;
                }
                objectStore = new NoSqlObjectStore(db, oidGenerator, versionCreator, writeWithEncryption, availableDataEncryption);
            } catch (final IllegalArgumentException e) {
                throw new IsisException(e);
            } catch (final SecurityException e) {
                throw new IsisException(e);
            }
        }
        return objectStore;
    }

    protected NoSqlIdentifierGenerator createIdentifierGenerator(final NoSqlDataDatabase database) {
        return new NoSqlIdentifierGenerator(database);
    }

    protected abstract NoSqlDataDatabase createNoSqlDatabase(IsisConfiguration configuration);

    private VersionCreator createVersionCreator() {
        return new VersionCreatorDefault();
    }
}
