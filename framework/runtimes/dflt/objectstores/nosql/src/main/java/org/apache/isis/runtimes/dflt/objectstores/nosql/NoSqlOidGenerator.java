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

import org.apache.log4j.Logger;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.runtimes.dflt.objectstores.nosql.db.NoSqlDataDatabase;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.OidGeneratorAbstract;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.serial.RootOidDefault;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;

public class NoSqlOidGenerator extends OidGeneratorAbstract {
    
    private static final Logger LOG = Logger.getLogger(NoSqlOidGenerator.class);
    private static int INITIAL_TRANSIENT_ID = -9999999;
    private static int DEFAULT_BATCH_SIZE = 50;

    private static class IdNumbers {
        
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
                nextSubId = connectionPool.nextSerialNumberBatch("_sub-id", batchSize);
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
        super(RootOidDefault.class);
        this.database = database;
        ids = new IdNumbers(initialTransientId, batchSize);
    }

    public String name() {
        return "NoSql Oids";
    }

    @Override
    public RootOid createTransientOid(final Object object) {
        final ObjectSpecification objectSpec = getSpecificationLoader().loadSpecification(object.getClass());
        final String objectType = objectSpec.getObjectType();
        return RootOidDefault.createTransient(objectType, "" + (ids.nextTransientId()));
    }

    @Override
    public String createAggregateLocalId(final Object pojo) {
        Assert.assertNotNull("No connection set up", database);
        return Long.toHexString(ids.nextSubId(database));
    }

    @Override
    public RootOid asPersistent(final RootOid rootOid) {
        Assert.assertNotNull("No connection set up", database);
        final long persistentId = ids.nextPersistentId(database);
        return rootOid.asPersistent("" + persistentId);
    }

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
    
    protected SpecificationLoader getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }


}
