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

public class PostgreSqlIntegrationTest extends SqlIntegrationTestData {

    @Override
    public Properties getProperties() {
        Properties properties = super.getProperties();
        if (properties == null) {
            properties = new Properties();
            // Only used if src/test/config/postgresql.properties does not
            // exist.
            properties.put(SqlObjectStore.BASE_NAME + ".jdbc.driver", "org.postgresql.Driver");
            properties.put(SqlObjectStore.BASE_NAME + ".jdbc.connection", "jdbc:postgresql://abacus/noftest");
            properties.put(SqlObjectStore.BASE_NAME + ".jdbc.user", "nof");
            properties.put(SqlObjectStore.BASE_NAME + ".jdbc.password", "");

            // properties.put(SqlObjectStore.BASE_NAME + ".datatypes.timestamp",
            // "TIMESTAMP");
            // properties.put(SqlObjectStore.BASE_NAME + ".datatypes.datetime",
            // "TIMESTAMP");
        }
        return properties;
    }

    @Override
    public String getPropertiesFilename() {
        return "postgresql.properties";
    }

}
