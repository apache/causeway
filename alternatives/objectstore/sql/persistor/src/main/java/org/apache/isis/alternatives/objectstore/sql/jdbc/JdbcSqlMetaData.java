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


package org.apache.isis.alternatives.objectstore.sql.jdbc;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.apache.isis.alternatives.objectstore.sql.SqlMetaData;


public class JdbcSqlMetaData implements SqlMetaData {

    private String keywords;
    private String timeDateFunctions;
    private boolean storesLowerCaseIdentifiers;
    private boolean storesMixedCaseIdentifiers;
    private boolean storesUpperCaseIdentifiers;

    public JdbcSqlMetaData(DatabaseMetaData metaData) throws SQLException {
        keywords = metaData.getSQLKeywords();
        timeDateFunctions = metaData.getTimeDateFunctions();
        storesLowerCaseIdentifiers = metaData.storesLowerCaseIdentifiers();
        storesMixedCaseIdentifiers = metaData.storesMixedCaseIdentifiers();
        storesUpperCaseIdentifiers = metaData.storesUpperCaseIdentifiers();

    }

    public String getKeywords() {
        return keywords;
    }

    public String getTimeDateFunctions() {
        return timeDateFunctions;
    }

    public boolean isStoresLowerCaseIdentifiers() {
        return storesLowerCaseIdentifiers;
    }

    public boolean isStoresMixedCaseIdentifiers() {
        return storesMixedCaseIdentifiers;
    }

    public boolean isStoresUpperCaseIdentifiers() {
        return storesUpperCaseIdentifiers;
    }

}

