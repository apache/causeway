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

package org.apache.isis.runtimes.dflt.objectstores.nosql;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.core.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.OidGeneratorAbstract;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.simple.SerialOid;
import org.apache.log4j.Logger;

public class NoSqlOidGenerator extends OidGeneratorAbstract {
    private static final Logger LOG = Logger.getLogger(NoSqlOidGenerator.class);
    private static int INITIAL_TRANSIENT_ID = -9999999;
    private static int DEFAULT_BATCH_SIZE = 50;

    private static class IdNumbers {
        private long transientNumber;
        private long nextId = 0;
        private long newIdBatchAt = 0;
        private long nextSubId = 0;
        private long newSubIdBatchAt = 0;
        private final int batchSize;

        public IdNumbers(final int initialTransientId, final int batchSize) {
            transientNumber = initialTransientId;
            this.batchSize = batchSize;
        }

        public synchronized long nextTransientId() {
            return transientNumber++;
        }

        public synchronized long nextSubId(final NoSqlDataDatabase connectionPool) {
            if (nextSubId > newSubIdBatchAt) {
                final String message =
                    "ID exception, last id (" + nextSubId + ") past new batch boundary (" + newSubIdBatchAt + ")";
                throw new NoSqlStoreException(message);
            }
            if (nextSubId == newSubIdBatchAt) {
                nextSubId = connectionPool.nextSerialNumberBatch("_sub-id", batchSize);
                newSubIdBatchAt = nextSubId + batchSize;
                LOG.debug("New Sub-ID batch created, from " + nextSubId + " to " + newSubIdBatchAt);
            }
            return nextSubId++;
        }

        public synchronized long nextPersistentId(final NoSqlDataDatabase connectionPool) {
            if (nextId > newIdBatchAt) {
                final String message =
                    "ID exception, last id (" + nextId + ") past new batch boundary (" + newIdBatchAt + ")";
                throw new NoSqlStoreException(message);
            }
            if (nextId == newIdBatchAt) {
                nextId = connectionPool.nextSerialNumberBatch("_id", batchSize);
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

    private final NoSqlDataDatabase database;
    private final IdNumbers ids;

    public NoSqlOidGenerator(final NoSqlDataDatabase database) {
        this(database, INITIAL_TRANSIENT_ID, DEFAULT_BATCH_SIZE);
    }

    public NoSqlOidGenerator(final NoSqlDataDatabase database, final int initialTransientId, final int batchSize) {
        this.database = database;
        ids = new IdNumbers(initialTransientId, batchSize);
    }

    @Override
    public SerialOid createTransientOid(final Object object) {
        return SerialOid.createTransient(ids.nextTransientId());
    }

    @Override
    public String createAggregateId(final Object pojo) {
        Assert.assertNotNull("No connection set up", database);
        return Long.toHexString(ids.nextSubId(database));
    }

    @Override
    public void convertTransientToPersistentOid(final Oid oid) {
        Assert.assertNotNull("No connection set up", database);
        if (oid instanceof AggregatedOid) {
            return;
        }
        final long persistentId = ids.nextPersistentId(database);
        ((SerialOid) oid).setId(persistentId);
        ((SerialOid) oid).makePersistent();
    }

    public void convertPersistentToTransientOid(final Oid oid) {
        throw new NotYetImplementedException();
    }

    @Override
    public void debugData(final DebugBuilder debug) {
        debug.appendln(this.toString());
        debug.indent();
        ids.debugData(debug);
        debug.unindent();
    }

    public String name() {
        return "NoSql Oids";
    }

    @Override
    public String debugTitle() {
        return "NoSql OID Generator";
    }
}
