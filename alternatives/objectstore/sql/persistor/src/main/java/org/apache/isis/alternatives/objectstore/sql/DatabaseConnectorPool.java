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

import java.util.Vector;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.log4j.Logger;


public class DatabaseConnectorPool {
    private static final Logger LOG = Logger.getLogger(DatabaseConnectorPool.class);
    private static final int AVERAGE_POOL_SIZE = 5;

    private final DatabaseConnectorFactory factory;
    private final Vector<DatabaseConnector> connectorPool;

    public DatabaseConnectorPool(final DatabaseConnectorFactory factory) {
        this(factory, AVERAGE_POOL_SIZE);
    }

    public DatabaseConnectorPool(final DatabaseConnectorFactory factory, final int size) {
        this.factory = factory;
        connectorPool = new Vector<DatabaseConnector>();
        for (int i = 0; i < size; i++) {
            newConnector();
        }
        LOG.info("Created an intial pool of " + size + " database connections");
        
        DatabaseConnector connection = acquire();
        Sql.setMetaData(connection.getMetaData());
        release(connection);
    }

    private DatabaseConnector newConnector() {
        DatabaseConnector connector = factory.createConnector();
        connector.setConnectionPool(this);
        connectorPool.addElement(connector);
        return connector;
    }

    public DatabaseConnector acquire() {
        DatabaseConnector connector = findFreeConnector();
        if (connector == null) {
            connector = newConnector();
            connector.setUsed(true);
            LOG.info("Added an additional database connection; now contains " + connectorPool.size() + " connections");
        }
        LOG.debug("acquired connection " + connector);
        return connector;
    }

    private DatabaseConnector findFreeConnector() {
        for (int i = 0, no = connectorPool.size(); i < no; i++) {
            DatabaseConnector connector = (DatabaseConnector) connectorPool.elementAt(i);
            if (!connector.isUsed()) {
                connector.setUsed(true);
                return connector;
            }
        }
        return null;
    }

    public void release(final DatabaseConnector connector) {
        connector.setUsed(false);
        LOG.debug("released connection " + connector);
    }

    public void shutdown() {
        for (int i = 0, no = connectorPool.size(); i < no; i++) {
            DatabaseConnector connector = (DatabaseConnector) connectorPool.elementAt(i);
            try {
                connector.close();
            } catch (SqlObjectStoreException e) {
                LOG.error("Failed to release connectuion", e);
            }
        }
        connectorPool.removeAllElements();
    }

    public void debug(DebugString debug) {
        DatabaseConnector connection = acquire();
        connection.debug(debug);
        release(connection);
        
    }

    public SqlMetaData getMetaData() {
        return null;
    }
}
