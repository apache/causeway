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
package org.apache.isis.objectstore.sql.common;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.objectstore.InMemoryPersistenceMechanismInstaller;
import org.apache.isis.core.runtime.installerregistry.installerapi.PersistenceMechanismInstallerAbstract;
import org.apache.isis.objectstore.sql.Sql;
import org.apache.isis.objectstore.sql.SqlObjectStore;
import org.apache.isis.objectstore.sql.SqlPersistorInstaller;
import org.apache.isis.objectstore.xml.XmlPersistenceMechanismInstaller;

public class Utils {

    static PersistenceMechanismInstallerAbstract createPersistorInstaller(final IsisConfiguration configuration) {
        
        final String jdbcDriver = configuration.getString(SqlObjectStore.BASE_NAME + ".jdbc.driver");
        if (jdbcDriver != null) {
            return new SqlPersistorInstaller();
        } 
        
        final String persistor = configuration.getString("isis.persistor");
        if (persistor.equals(InMemoryPersistenceMechanismInstaller.NAME)) {
            return new InMemoryPersistenceMechanismInstaller();
        }
        if (persistor.equals(XmlPersistenceMechanismInstaller.NAME)) {
            return new XmlPersistenceMechanismInstaller();
        }
        if (persistor.equals(SqlPersistorInstaller.NAME)) {
            return new SqlPersistorInstaller();
        }
        return new InMemoryPersistenceMechanismInstaller();
    }

    static String tableIdentifierFor(final String tableName) {
        if (tableName.substring(0, 4).toUpperCase().equals("ISIS")) {
            return Sql.tableIdentifier(tableName);
        } else {
            return Sql.tableIdentifier("isis_" + tableName);
        }
    }



}
