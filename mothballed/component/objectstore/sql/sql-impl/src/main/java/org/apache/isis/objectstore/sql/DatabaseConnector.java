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

import org.apache.isis.core.commons.debug.DebugBuilder;

public interface DatabaseConnector {
    /*
     * @deprecated Results callStoredProcedure(String name, Parameter[]
     * parameters);
     */
    void close();

    int count(String sql);

    void delete(String sql);

    // MultipleResults executeStoredProcedure(String name, Parameter[]
    // parameters);

    boolean hasTable(String tableName);

    boolean hasColumn(String tableName, String columnName);

    void insert(String sql);

    void insert(String sql, Object oid);

    Results select(String sql);

    /**
     * Updates the database using the specified sql statement, and returns the
     * number of rows affected.
     */
    int update(String sql);

    void setUsed(boolean isUsed);

    boolean isUsed();

    void commit();

    void rollback();

    void setConnectionPool(DatabaseConnectorPool pool);

    DatabaseConnectorPool getConnectionPool();

    void begin();

    void debug(DebugBuilder debug);

    SqlMetaData getMetaData();

    // Full PreparedStatement support
    public String addToQueryValues(int i);

    public String addToQueryValues(String s);

    public String addToQueryValues(Object object);

}
