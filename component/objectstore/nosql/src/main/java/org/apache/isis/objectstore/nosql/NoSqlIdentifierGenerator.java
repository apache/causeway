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
package org.apache.isis.objectstore.nosql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.runtime.system.persistence.IdentifierGenerator;
import org.apache.isis.objectstore.nosql.db.NoSqlDataDatabase;

public class NoSqlIdentifierGenerator implements IdentifierGenerator {
    
    private static int INITIAL_TRANSIENT_ID = -9999999;
    private static int DEFAULT_BATCH_SIZE = 50;

    private final NoSqlDataDatabase database;
    private final IdNumbers ids;

    
    //////////////////////////////////////////////////////////////////
    // constructor
    //////////////////////////////////////////////////////////////////

    public NoSqlIdentifierGenerator(final NoSqlDataDatabase database) {
        this(database, INITIAL_TRANSIENT_ID, DEFAULT_BATCH_SIZE);
    }

    public NoSqlIdentifierGenerator(final NoSqlDataDatabase database, final int initialTransientId, final int batchSize) {
        this.database = database;
        ids = new IdNumbers(initialTransientId, batchSize);
    }

    
    //////////////////////////////////////////////////////////////////
    // API
    //////////////////////////////////////////////////////////////////

    @Override
    public String createTransientIdentifierFor(ObjectSpecId objectSpecId, Object pojo) {
        final String identifier = "" + ids.nextTransientId();
        return identifier;
    }

    @Override
    public String createAggregateLocalId(ObjectSpecId objectSpecId, final Object pojo, final ObjectAdapter parentAdapter) {
        Assert.assertNotNull("No connection set up", database);
        return Long.toHexString(ids.nextSubId(database));
    }

    @Override
    public String createPersistentIdentifierFor(ObjectSpecId objectSpecId, Object pojo, RootOid transientRootOid) {
        return "" + ids.nextPersistentId(database);
    }

    
    //////////////////////////////////////////////////////////////////
    // debug
    //////////////////////////////////////////////////////////////////
    
    @Override
    public void debugData(final DebugBuilder debug) {
        debug.appendln(this.toString());
        debug.indent();
        ids.debugData(debug);
        debug.unindent();
    }

    @Override
    public String debugTitle() {
        return "NoSql OID Generator";
    }
}

class IdNumbers {

    private static final Logger LOG = LoggerFactory.getLogger(IdNumbers.class);

    private final int batchSize;
    
    private long transientNumber;
    private long nextId = 0;
    private long newIdBatchAt = 0;
    private long nextSubId = 0;
    private long newSubIdBatchAt = 0;
    
    public IdNumbers(final int initialTransientId, final int batchSize) {
        transientNumber = initialTransientId;
        this.batchSize = batchSize;
    }

    public synchronized long nextTransientId() {
        return transientNumber++;
    }

    public synchronized long nextSubId(final NoSqlDataDatabase connectionPool) {
        if (nextSubId > newSubIdBatchAt) {
            final String message = "ID exception, last id (" + nextSubId + ") past new batch boundary (" + newSubIdBatchAt + ")";
            throw new NoSqlStoreException(message);
        }
        if (nextSubId == newSubIdBatchAt) {
            nextSubId = connectionPool.nextSerialNumberBatch(ObjectSpecId.of("_sub-id"), batchSize);
            newSubIdBatchAt = nextSubId + batchSize;
            LOG.debug("New Sub-ID batch created, from " + nextSubId + " to " + newSubIdBatchAt);
        }
        return nextSubId++;
    }

    public synchronized long nextPersistentId(final NoSqlDataDatabase connectionPool) {
        if (nextId > newIdBatchAt) {
            final String message = "ID exception, last id (" + nextId + ") past new batch boundary (" + newIdBatchAt + ")";
            throw new NoSqlStoreException(message);
        }
        if (nextId == newIdBatchAt) {
            nextId = connectionPool.nextSerialNumberBatch(ObjectSpecId.of("_id"), batchSize);
            newIdBatchAt = nextId + batchSize;
            LOG.debug("New ID batch created, from " + nextId + " to " + newIdBatchAt);
        }
        return nextId++;
    }

    public void debugData(final DebugBuilder debug) {
        debug.appendln("id", nextId);
        debug.appendln("sub-id", nextSubId);
        debug.appendln("transient id", transientNumber);
    }
}

