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
package org.apache.isis.objectstore.sql;

import java.util.Properties;

import org.apache.isis.objectstore.sql.common.SqlIntegrationTestData;

public class MySqlIntegrationTest extends SqlIntegrationTestData {

    /**/
    @Override
    public Properties getProperties() {
        Properties properties = super.getProperties();
        if (properties == null) {
            // Only used if *sql.properties is not found
            properties = new Properties();
            properties.put(SqlObjectStore.BASE_NAME + ".jdbc.driver", "com.mysql.jdbc.Driver");
            // properties.put(SqlObjectStore.BASE_NAME + ".jdbc.connection",
            // "jdbc:mysql://abacus/noftest&useTimezone=true&serverTimezone=GMT");
            properties.put(SqlObjectStore.BASE_NAME + ".jdbc.connection", "jdbc:mysql://abacus/noftest&useLegacyDatetimeCode=false");
            properties.put(SqlObjectStore.BASE_NAME + ".jdbc.user", "nof");
            properties.put(SqlObjectStore.BASE_NAME + ".jdbc.jdbc.password", "");
        }
        return properties;
    }

    /**/

    @Override
    public String getPropertiesFilename() {
        return "mysql.properties";
    }
    
}
