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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.runtime.persistence.ObjectPersistenceException;
import org.apache.isis.core.runtime.system.persistence.IdentifierGenerator;

public class SqlIdentifierGenerator implements IdentifierGenerator {
    
    private final DatabaseConnectorPool connectionPool;
    private final IdNumbers ids = new IdNumbers();

    //////////////////////////////////////////////////////////////////
    // constructor
    //////////////////////////////////////////////////////////////////

    public SqlIdentifierGenerator(final DatabaseConnectorPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    ///////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////
    
    @Override
    public String createAggregateLocalId(ObjectSpecId objectSpecId, final Object pojo, final ObjectAdapter parentAdapter) {
        throw new SqlObjectStoreException("Aggregated objects are not supported in this store");
    }

    @Override
    public String createTransientIdentifierFor(ObjectSpecId objectSpecId, final Object pojo) {
        return ""+ids.nextTransientId();
    }

    @Override
    public String createPersistentIdentifierFor(ObjectSpecId objectSpecId, Object pojo, RootOid transientRootOid) {
        Assert.assertNotNull("No connection set up", connectionPool);
        return "" + (int) ids.nextPersistentId(connectionPool);
    }

    
    ///////////////////////////////////////////////////////
    // Debug
    ///////////////////////////////////////////////////////

    @Override
    public void debugData(final DebugBuilder debug) {
        debug.appendln(this.toString());
        debug.indent();
        ids.debugData(debug);
        debug.unindent();
    }

    @Override
    public String debugTitle() {
        return "Sql Identifier Generator";
    }
}



class IdNumbers {

    private static final Logger LOG = LoggerFactory.getLogger(IdNumbers.class);

    private static final String NUMBER_COLUMN = "number";
    private static final String TABLE_NAME = "isis_admin_serial_id";
    private static int BATCH_SIZE = 50;
    private long transientNumber = -9999999;
    private long lastId = 0;
    private long newBatchAt = 0;

    public synchronized long nextTransientId() {
        return transientNumber++;
    }

    public synchronized long nextPersistentId(final DatabaseConnectorPool connectionPool) {
        if (lastId > newBatchAt) {
            throw new SqlObjectStoreException("ID exception, last id (" + lastId + ") past new batch boundary (" + newBatchAt + ")");
        }
        if (lastId == newBatchAt) {
            prepareNewBatch(connectionPool);
        }
        lastId++;
        return lastId;
    }

    private void prepareNewBatch(final DatabaseConnectorPool connectionPool) {
        final DatabaseConnector db = connectionPool.acquire();
        try {
            final String tableName = Sql.tableIdentifier(TABLE_NAME);
            final String numberColumn = Sql.identifier(NUMBER_COLUMN);
            if (!db.hasTable(tableName)) {
                lastId = 1;
                newBatchAt = BATCH_SIZE;
                db.update("create table " + tableName + " (" + numberColumn + " INTEGER)");
                db.update("insert into " + tableName + " values (" + newBatchAt + ")");
                LOG.debug("Initial ID batch created, from " + lastId + " to " + newBatchAt);
            } else {
                if (db.update("update " + tableName + " set " + numberColumn + " = " + numberColumn + " + " + BATCH_SIZE) != 1) {
                    throw new SqlObjectStoreException("failed to update serial id table; no rows updated");
                }
                final Results rs = db.select("select " + numberColumn + " from " + tableName);
                rs.next();
                newBatchAt = rs.getLong(NUMBER_COLUMN); // TODO here
                lastId = newBatchAt - BATCH_SIZE;
                LOG.debug("New ID batch created, from " + lastId + " to " + newBatchAt);
                rs.close();
            }
        } catch (final ObjectPersistenceException e) {
            throw e;
        } finally {
            connectionPool.release(db);
        }
    }

    public void debugData(final DebugBuilder debug) {
        debug.appendln("id", lastId);
        debug.appendln("transient id", transientNumber);
    }

}

