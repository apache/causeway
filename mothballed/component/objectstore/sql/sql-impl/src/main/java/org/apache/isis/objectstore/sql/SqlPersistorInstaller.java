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

package org.apache.isis.objectstore.sql;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterFactory;
import org.apache.isis.core.runtime.installerregistry.installerapi.PersistenceMechanismInstallerAbstract;
import org.apache.isis.core.runtime.persistence.objectstore.ObjectStoreSpi;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.AdapterManagerSpi;
import org.apache.isis.core.runtime.system.persistence.IdentifierGenerator;
import org.apache.isis.objectstore.sql.auto.AutoMapperFactory;
import org.apache.isis.objectstore.sql.jdbc.JdbcConnectorFactory;
import org.apache.isis.objectstore.sql.jdbc.installer.JdbcFieldMappingFactoryInstaller;

public class SqlPersistorInstaller extends PersistenceMechanismInstallerAbstract {

    public static final String NAME = "sql";
    
    private SqlObjectStore objectStore;
    private DatabaseConnectorPool connectionPool;

    public SqlPersistorInstaller() {
        super(NAME);
    }

    @Override
    protected ObjectStoreSpi createObjectStore(final IsisConfiguration configuration, final ObjectAdapterFactory objectFactory, final AdapterManagerSpi adapterManager) {

        if (objectStore == null) {
            final FieldMappingLookup fieldMappingLookup = new FieldMappingLookup();
            final JdbcFieldMappingFactoryInstaller installer = new JdbcFieldMappingFactoryInstaller();

            Defaults.initialise(SqlObjectStore.BASE_NAME, IsisContext.getConfiguration());

            installer.load(fieldMappingLookup);
            // fieldMappingLookup.setValueMappingFactory(new
            // JdbcFieldMappingFactoryInstaller());

            final ObjectMappingLookup objectMappingLookup = new ObjectMappingLookup();
            objectMappingLookup.setValueMappingLookup(fieldMappingLookup);
            objectMappingLookup.setObjectMappingFactory(new AutoMapperFactory());
            objectMappingLookup.setConnectionPool(connectionPool);

            final SqlObjectStore objectStore = new SqlObjectStore();
            objectStore.setMapperLookup(objectMappingLookup);
            objectStore.setConnectionPool(connectionPool);
            this.objectStore = objectStore;
        }
        return objectStore;
    }

    public SqlObjectStore getObjectStore() {
        return objectStore;
    }

    @Override
    public IdentifierGenerator createIdentifierGenerator(final IsisConfiguration configuration) {
        final DatabaseConnectorFactory connectorFactory = new JdbcConnectorFactory();
        connectionPool = new DatabaseConnectorPool(connectorFactory, 1);

        return new SqlIdentifierGenerator(connectionPool);
    }

    /*
     * 
     * 
     * @Override protected AdapterManagerExtended createAdapterManager(final
     * IsisConfiguration configuration) { return new XmlAdapterManager(); }
     */
}
