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

package org.apache.isis.objectstore.nosql.db.mongo;

import java.util.List;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.objectstore.nosql.db.StateWriter;

public class MongoStateWriter implements StateWriter {
    
    private static final Logger LOG = LoggerFactory.getLogger(MongoStateWriter.class);
    private final DB db;
    private final BasicDBObject dbObject;
    private DBCollection instances;

    public MongoStateWriter(final DB db, final ObjectSpecId objectSpecId) {
        this(db);
        instances = db.getCollection(objectSpecId.asString());
    }

    private MongoStateWriter(final DB db) {
        this.db = db;
        dbObject = new BasicDBObject();
    }

    public void flush() {
        instances.save(dbObject);
        if(LOG.isDebugEnabled()) {
            LOG.debug("saved " + dbObject);
        }
    }

    @Override
    public void writeOid(final TypedOid typedOid) {
        writeField(PropertyNames.OID, typedOid.enString(getOidMarshaller()));
        if(typedOid instanceof RootOid) {
            RootOid rootOid = (RootOid) typedOid;
            writeField(PropertyNames.MONGO_INTERNAL_ID, rootOid.getIdentifier());
        }
    }

    @Override
    public void writeField(final String id, final String data) {
        dbObject.put(id, data);
    }

    @Override
    public void writeField(final String id, final long l) {
        dbObject.put(id, Long.toString(l));
    }

    @Override
    public void writeEncryptionType(final String type) {
        writeField(PropertyNames.ENCRYPT, type);
    }

    @Override
    public void writeVersion(final String currentVersion, final String newVersion) {
         writeField(PropertyNames.VERSION, newVersion);
    }

    @Override
    public void writeTime(final String time) {
        writeField(PropertyNames.TIME, time);
    }

    @Override
    public void writeUser(final String user) {
        writeField(PropertyNames.USER, user);
    }

    @Override
    public StateWriter addAggregate(final String id) {
        final MongoStateWriter stateWriter = new MongoStateWriter(db);
        dbObject.put(id, stateWriter.dbObject);
        return stateWriter;
    }

    @Override
    public StateWriter createElementWriter() {
        return new MongoStateWriter(db);
    }

    @Override
    public void writeCollection(final String id, final List<StateWriter> elements) {
        final List<BasicDBObject> collection = Lists.newArrayList();
        for (final StateWriter writer : elements) {
            collection.add(((MongoStateWriter) writer).dbObject);
        }
        dbObject.put(id, collection);
    }
    
    // ///////////////////////////////////////////////////////////////////
    // dependencies
    // ///////////////////////////////////////////////////////////////////

    protected OidMarshaller getOidMarshaller() {
		return IsisContext.getOidMarshaller();
	}

}
