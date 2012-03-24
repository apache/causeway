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

package org.apache.isis.runtimes.dflt.objectstores.sql;

import org.apache.log4j.Logger;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLookup;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.OidGeneratorAbstract;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.serial.RootOidDefault;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.transaction.ObjectPersistenceException;

public class SqlOidGenerator extends OidGeneratorAbstract {
    
    private final DatabaseConnectorPool connectionPool;
    private final IdNumbers ids = new IdNumbers();

    public SqlOidGenerator(final DatabaseConnectorPool connectionPool) {
        super(RootOidDefault.class);
        this.connectionPool = connectionPool;
    }

    public String name() {
        return "Sql Oids";
    }

    @Override
    public RootOid createTransientOid(final Object object) {
        final Class<? extends Object> cls = object.getClass();
        final ObjectSpecification objectSpec = getSpecificationLookup().loadSpecification(cls);
        return RootOidDefault.createTransient(objectSpec.getObjectType(), ""+ids.nextTransientId());
    }

    @Override
    public String createAggregateLocalId(final Object pojo) {
        throw new SqlObjectStoreException("Aggregated objects are not supported in this store");
    }

    @Override
    public RootOid asPersistent(final RootOid rootOid) {
        Assert.assertNotNull("No connection set up", connectionPool);
        
        final int next = (int) ids.nextPersistentId(connectionPool);
        
        return rootOid.asPersistent("" + next);
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
        return "SQL OID Generator";
    }
    
    ///////////////////////////////////////////////////////
    // Dependencies
    ///////////////////////////////////////////////////////
    
    protected SpecificationLookup getSpecificationLookup() {
        return IsisContext.getSpecificationLoader();
    }
}

class IdNumbers {

    private static final Logger LOG = Logger.getLogger(IdNumbers.class);

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

