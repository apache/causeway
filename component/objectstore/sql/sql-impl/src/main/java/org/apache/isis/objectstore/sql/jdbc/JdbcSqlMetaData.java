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

package org.apache.isis.objectstore.sql.jdbc;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.apache.isis.objectstore.sql.SqlMetaData;

public class JdbcSqlMetaData implements SqlMetaData {

    private final boolean hasQuoteString;
    private final String quoteString;
    private final String keywords;
    private final String timeDateFunctions;
    private final boolean storesLowerCaseIdentifiers;
    private final boolean storesMixedCaseIdentifiers;
    private final boolean storesUpperCaseIdentifiers;

    public JdbcSqlMetaData(final DatabaseMetaData metaData) throws SQLException {
        keywords = metaData.getSQLKeywords();
        timeDateFunctions = metaData.getTimeDateFunctions();
        quoteString = metaData.getIdentifierQuoteString();
        hasQuoteString = (quoteString != " ");
        storesLowerCaseIdentifiers = metaData.storesLowerCaseIdentifiers();
        storesMixedCaseIdentifiers = metaData.storesMixedCaseIdentifiers();
        storesUpperCaseIdentifiers = metaData.storesUpperCaseIdentifiers();

    }

    @Override
    public String getKeywords() {
        return keywords;
    }

    @Override
    public String getTimeDateFunctions() {
        return timeDateFunctions;
    }

    @Override
    public String getQuoteString() {
        return quoteString;
    }

    @Override
    public boolean isStoresLowerCaseIdentifiers() {
        return storesLowerCaseIdentifiers;
    }

    @Override
    public boolean isStoresMixedCaseIdentifiers() {
        return storesMixedCaseIdentifiers;
    }

    @Override
    public boolean isStoresUpperCaseIdentifiers() {
        return storesUpperCaseIdentifiers;
    }

    @Override
    public String quoteIdentifier(final String identifier) {
        if (hasQuoteString) {
            return quoteString + identifier + quoteString;
        } else {
            return identifier;
        }
    }
}
