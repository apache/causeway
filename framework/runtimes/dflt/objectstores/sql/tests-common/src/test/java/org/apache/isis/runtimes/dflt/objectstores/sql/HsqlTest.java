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

/**
 * 
 */
package org.apache.isis.runtimes.dflt.objectstores.sql;

import java.util.Properties;

import org.apache.isis.runtimes.dflt.objectstores.sql.common.SqlIntegrationTestCommon;

public class HsqlTest extends SqlIntegrationTestCommon {

    @Override
    public Properties getProperties() {
        Properties properties = super.getProperties();
        if (properties == null) { // Only used if properties file does not exist.
            properties = new Properties();
            properties.put(SqlObjectStore.BASE_NAME + ".jdbc.driver", "org.hsqldb.jdbcDriver");
            properties.put(SqlObjectStore.BASE_NAME + ".jdbc.connection", "jdbc:hsqldb:file:hsql-db/tests");
            properties.put(SqlObjectStore.BASE_NAME + ".jdbc.user", "sa");
            properties.put(SqlObjectStore.BASE_NAME + ".jdbc.password", "");
            properties.put("isis.logging.objectstore", "on");
        }

        return properties;
    }

    @Override
    public String getPropertiesFilename() {
        return "hsql.properties";
    }

    @Override
    public String getSqlTeardownString() {
        return "SHUTDOWN;";
    }

    @Override
    public String getPersonTableName() {
        return "ISIS_SQLDATACLASS";
    }

    @Override
    public String getSimpleClassTableName() {
        return "ISIS_SIMPLECLASS";
    }

    @Override
    public String getSimpleClassTwoTableName() {
        return "ISIS_SIMPLECLASSTWO";
    }

}
