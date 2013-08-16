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

import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.objectstore.nosql.NoSqlCommandContext;
import org.apache.isis.objectstore.nosql.NoSqlStoreException;
import org.apache.isis.objectstore.nosql.db.NoSqlDataDatabase;
import org.apache.isis.objectstore.nosql.db.StateReader;
import org.apache.isis.objectstore.nosql.db.StateWriter;
import org.apache.isis.objectstore.nosql.keys.KeyCreatorDefault;

public class MongoDb implements NoSqlDataDatabase {

	private static final String SERIALNUMBERS_COLLECTION_NAME = "serialnumbers";

	private static final Logger LOG = LoggerFactory.getLogger(MongoDb.class);
	
	private static final int DEFAULT_PORT = 27017;

    private final String host;
    private final int port;
    private final String dbName;
    private final KeyCreatorDefault keyCreator;
    
	private Mongo mongo;
	private DB db;

    public MongoDb(final String host, final int port, final String name, final KeyCreatorDefault keyCreator) {
        this.host = host;
        this.port = port == 0 ? DEFAULT_PORT : port;
        this.dbName = name;
        this.keyCreator = keyCreator;
    }

    public KeyCreatorDefault getKeyCreator() {
        return keyCreator;
    }

    @Override
    public void open() {
        try {
            if (mongo == null) {
                mongo = new Mongo(host, port);
                db = mongo.getDB(dbName);
                db.setWriteConcern(com.mongodb.WriteConcern.SAFE);
                LOG.info("opened database (" + dbName + "): " + mongo);
            } else {
                LOG.info(" using opened database " + db);
            }
        } catch (final UnknownHostException e) {
            throw new NoSqlStoreException(e);
        } catch (final MongoException e) {
            throw new NoSqlStoreException(e);
        }
    }

    @Override
    public void close() {
    }

    public NoSqlCommandContext createTransactionContext() {
        return null;
    }

    //////////////////////////////////////////////////
    // contains data
    //////////////////////////////////////////////////

    @Override
    public boolean containsData() {
        return db.getCollectionNames().size() > 0;
    }

    
    //////////////////////////////////////////////////
    // serial numbers
    //////////////////////////////////////////////////
    
    @Override
    public long nextSerialNumberBatch(final ObjectSpecId name, final int batchSize) {
        long next = readSerialNumber();
        writeSerialNumber(next + batchSize);
        return next + 1;
    }

    private void writeSerialNumber(final long serialNumber) {
        final DBCollection system = db.getCollection(SERIALNUMBERS_COLLECTION_NAME);
        DBObject object = system.findOne();
        if (object == null) {
            object = new BasicDBObject();
        }
        object.put("next-id", Long.toString(serialNumber));
        system.save(object);
        LOG.info("serial number written: " + serialNumber);
    }

    private long readSerialNumber() {
        final DBCollection system = db.getCollection(SERIALNUMBERS_COLLECTION_NAME);
        final DBObject data = system.findOne();
        if (data == null) {
            return 0;
        } else {
            final String number = (String) data.get("next-id");
            LOG.info("serial number read: " + number);
            return Long.valueOf(number);
        }
    }

    //////////////////////////////////////////////////
    // hasInstances, instancesOf
    //////////////////////////////////////////////////

    @Override
    public boolean hasInstances(final ObjectSpecId objectSpecId) {
        final DBCollection instances = db.getCollection(objectSpecId.asString());
        return instances.getCount() > 0;
    }

    @Override
    public Iterator<StateReader> instancesOf(final ObjectSpecId objectSpecId) {
        final DBCollection instances = db.getCollection(objectSpecId.asString());
        final DBCursor cursor = instances.find();
        LOG.info("searching for instances of: " + objectSpecId);
        return new Iterator<StateReader>() {
            @Override
            public boolean hasNext() {
                return cursor.hasNext();
            }

            @Override
            public StateReader next() {
                return new MongoStateReader(cursor.next());
            }

            @Override
            public void remove() {
                throw new NoSqlStoreException("Can't remove elements");
            }

        };
    }
    
    @Override
    public Iterator<StateReader> instancesOf(ObjectSpecId objectSpecId, ObjectAdapter pattern) {
        final DBCollection instances = db.getCollection(objectSpecId.asString());

        // REVIEW check the right types are used in matches 
        final BasicDBObject query = new BasicDBObject();
        for ( ObjectAssociation association  : pattern.getSpecification().getAssociations(Contributed.EXCLUDED)) {
            ObjectAdapter field = association.get(pattern);
            if (!association.isEmpty(pattern)) {
                if (field.isValue()) {
                    query.put(association.getIdentifier().getMemberName(), field.titleString());
                } else if (association.isOneToOneAssociation()) {
                    query.put(association.getIdentifier().getMemberName(), field.getOid());
                }
            }
        }
        final DBCursor cursor = instances.find(query);
        LOG.info("searching for instances of: " + objectSpecId);
        return new Iterator<StateReader>() {
            @Override
            public boolean hasNext() {
                return cursor.hasNext();
            }

            @Override
            public StateReader next() {
                return new MongoStateReader(cursor.next());
            }

            @Override
            public void remove() {
                throw new NoSqlStoreException("Can't remove elements");
            }

        };
    }

    @Override
    public StateReader getInstance(final String key, final ObjectSpecId objectSpecId) {
        return new MongoStateReader(db, objectSpecId, key);
    }

    //////////////////////////////////////////////////
    // write, delete
    //////////////////////////////////////////////////

    public StateWriter createStateWriter(final ObjectSpecId objectSpecId) {
        return new MongoStateWriter(db, objectSpecId);
    }


    @Override
    public void write(final List<PersistenceCommand> commands) {
        final NoSqlCommandContext context = new MongoClientCommandContext(db);
        for (final PersistenceCommand command : commands) {
            command.execute(context);
        }
    }


    //////////////////////////////////////////////////
    // services
    //////////////////////////////////////////////////

    @Override
    public void addService(final ObjectSpecId objectSpecId, final String key) {
        final DBCollection services = db.getCollection("services");
        services.insert(new BasicDBObject().append("name", objectSpecId.asString()).append("key", key));
        LOG.info("service added " + objectSpecId + ":" + key);
    }

    @Override
    public String getService(final ObjectSpecId objectSpecId) {
        final DBCollection services = db.getCollection("services");
        final DBObject object = services.findOne(new BasicDBObject().append("name", objectSpecId.asString()));
        if (object == null) {
            return null;
        } else {
            final String id = (String) object.get("key");
            LOG.info("service found " + objectSpecId + ":" + id);
            return id;
        }
    }
}
