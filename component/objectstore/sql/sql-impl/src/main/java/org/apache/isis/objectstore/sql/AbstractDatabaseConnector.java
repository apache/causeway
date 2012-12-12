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

public abstract class AbstractDatabaseConnector implements DatabaseConnector {
    private boolean isUsed;

    @Override
    public final void setUsed(final boolean isUsed) {
        this.isUsed = isUsed;
    }

    @Override
    public final boolean isUsed() {
        return isUsed;
    }

    private DatabaseConnectorPool pool;

    @Override
    public final void setConnectionPool(final DatabaseConnectorPool pool) {
        this.pool = pool;
    }

    @Override
    public final DatabaseConnectorPool getConnectionPool() {
        return pool;
    }
}
