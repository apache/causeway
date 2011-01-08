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


package org.apache.isis.alternatives.objectstore.sql;

import org.apache.isis.alternatives.objectstore.sql.auto.AutoMapperFactory;
import org.apache.isis.alternatives.objectstore.sql.jdbc.JdbcConnectorFactory;
import org.apache.isis.alternatives.objectstore.sql.jdbc.JdbcFieldMappingFactoryInstaller;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.runtime.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.persistence.adapterfactory.AdapterFactory;
import org.apache.isis.core.runtime.persistence.adaptermanager.AdapterManager;
import org.apache.isis.core.runtime.persistence.objectstore.ObjectStore;
import org.apache.isis.core.runtime.persistence.objectstore.ObjectStorePersistenceMechanismInstallerAbstract;
import org.apache.isis.core.runtime.persistence.oidgenerator.OidGenerator;
import org.apache.isis.core.runtime.system.DeploymentType;


public class SqlPersistorInstaller extends ObjectStorePersistenceMechanismInstallerAbstract {

    private SqlObjectStore objectStore;
    private DatabaseConnectorPool connectionPool;

    public SqlPersistorInstaller() {
        super("sql");
    }

    @Override
    protected ObjectStore createObjectStore(
            IsisConfiguration configuration,
            AdapterFactory objectFactory,
            AdapterManager adapterManager) {
        
        
        if (objectStore == null) {
            FieldMappingLookup fieldMappingLookup = new FieldMappingLookup();
            JdbcFieldMappingFactoryInstaller installer = new JdbcFieldMappingFactoryInstaller();
            installer.load(fieldMappingLookup);
            // fieldMappingLookup.setValueMappingFactory(new
            // JdbcFieldMappingFactoryInstaller());

            ObjectMappingLookup objectMappingLookup = new ObjectMappingLookup();
            objectMappingLookup.setValueMappingLookup(fieldMappingLookup);
            objectMappingLookup.setObjectMappingFactory(new AutoMapperFactory());
            objectMappingLookup.setConnectionPool(connectionPool);

            SqlObjectStore objectStore = new SqlObjectStore();
            objectStore.setMapperLookup(objectMappingLookup);
            objectStore.setConnectionPool(connectionPool);
            this.objectStore = objectStore;
        }
        return objectStore;
    }

    @Override
    protected OidGenerator createOidGenerator(IsisConfiguration configuration) {
        DatabaseConnectorFactory connectorFactory = new JdbcConnectorFactory();
        connectionPool = new DatabaseConnectorPool(connectorFactory, 1);

        return new SqlOidGenerator(connectionPool);
    }

    public PersistenceSessionFactory createPersistenceSessionFactory(final DeploymentType deploymentType) {
        return new SqlPersistenceSessionFactory(deploymentType, this);
    }
    
    
    /*
    

    
    @Override
    protected AdapterManagerExtended createAdapterManager(final IsisConfiguration configuration) {
        return new XmlAdapterManager();
    }
*/
}

