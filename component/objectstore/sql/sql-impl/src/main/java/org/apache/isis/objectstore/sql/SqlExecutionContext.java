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

import org.apache.isis.core.runtime.persistence.objectstore.transaction.PersistenceCommandContext;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.runtime.system.transaction.MessageBroker;
import org.apache.isis.core.runtime.system.transaction.UpdateNotifier;

public class SqlExecutionContext implements PersistenceCommandContext {
    private final DatabaseConnector connection;

    public SqlExecutionContext(final DatabaseConnector connection, final IsisTransactionManager transactionManager, final MessageBroker messageBroker, final UpdateNotifier updateNotifier) {
        this.connection = connection;
    }

    public DatabaseConnector getConnection() {
        return connection;
    }

    @Override
    public void start() {
    }

    @Override
    public void end() {
    }

}
