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


package org.apache.isis.extensions.nosql;

import org.apache.log4j.Logger;
import org.apache.isis.commons.debug.DebugString;
import org.apache.isis.commons.ensure.Assert;
import org.apache.isis.commons.exceptions.NotYetImplementedException;
import org.apache.isis.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.runtime.persistence.oidgenerator.OidGeneratorAbstract;
import org.apache.isis.runtime.persistence.oidgenerator.simple.SerialOid;


public class NoSqlOidGenerator extends OidGeneratorAbstract {
    private static final Logger LOG = Logger.getLogger(NoSqlOidGenerator.class);
    private static int INITIAL_TRANSIENT_ID = -9999999;
    private static int DEFAULT_BATCH_SIZE = 50;

    private static class IdNumbers {
        private long transientNumber;
        private long nextId = 0;
        private long newBatchAt = 0;
        private final int batchSize;

        public IdNumbers(int initialTransientId, int batchSize) {
            transientNumber = initialTransientId;
            this.batchSize = batchSize;
        }

        public synchronized long nextTransientId() {
            return transientNumber++;
        }

        public synchronized long nextPersistentId(NoSqlDataDatabase connectionPool) {
            if (nextId > newBatchAt) {
                String message = "ID exception, last id (" + nextId + ") past new batch boundary (" + newBatchAt + ")";
                throw new NoSqlStoreException(message);
            }
            if (nextId == newBatchAt) {
                nextId = connectionPool.nextSerialNumberBatch(batchSize);
                newBatchAt = nextId + batchSize;
                LOG.debug("New ID batch created, from " + nextId + " to " + newBatchAt);
            }
            return nextId++;
        }
        
        public void debugData(DebugString debug) {
            debug.appendln("id", nextId);
            debug.appendln("transient id", transientNumber);
        }
    }

    private final NoSqlDataDatabase database;
    private final IdNumbers ids;

    public NoSqlOidGenerator(final NoSqlDataDatabase database) {
        this(database, INITIAL_TRANSIENT_ID, DEFAULT_BATCH_SIZE);
    }

    public NoSqlOidGenerator(final NoSqlDataDatabase database, int initialTransientId, int batchSize) {
        this.database = database;
        ids = new IdNumbers(initialTransientId, batchSize);
    }

    public SerialOid createTransientOid(final Object object) {
        return SerialOid.createTransient(ids.nextTransientId());
    }

    public void convertTransientToPersistentOid(final Oid oid) {
        Assert.assertNotNull("No connection set up", database);
        if (oid instanceof AggregatedOid) {
            return;
        }
        long persistentId = ids.nextPersistentId(database);
        ((SerialOid) oid).setId(persistentId);
        ((SerialOid) oid).makePersistent();
    }

    public void convertPersistentToTransientOid(final Oid oid) {
        throw new NotYetImplementedException();
    }

    public void debugData(final DebugString debug) {
        debug.appendln(this.toString());
        debug.indent();
        ids.debugData(debug);
        debug.unindent();
    }

    public String name() {
        return "NoSql Oids";
    }

    public String debugTitle() {
        return "NoSql OID Generator";
    }
}

