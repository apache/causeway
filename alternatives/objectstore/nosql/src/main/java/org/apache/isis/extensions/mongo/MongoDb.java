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


package org.apache.isis.extensions.mongo;

import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.core.runtime.persistence.oidgenerator.simple.SerialOid;
import org.apache.isis.extensions.nosql.KeyCreator;
import org.apache.isis.extensions.nosql.NoSqlCommandContext;
import org.apache.isis.extensions.nosql.NoSqlDataDatabase;
import org.apache.isis.extensions.nosql.NoSqlStoreException;
import org.apache.isis.extensions.nosql.StateReader;
import org.apache.isis.extensions.nosql.StateWriter;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.ObjectId;

public class MongoDb implements NoSqlDataDatabase {
    private static final Logger LOG = Logger.getLogger(MongoDb.class);

    private final String host;
    private final int port;
    private final String dbName;
    private final KeyCreator keyCreator;
    private DB db;



    public MongoDb(String host, int port, String name, KeyCreator keyCreator) {
        this.host = host;
        this.port = port == 0 ? 27017 : port;
        this.dbName = name;
        this.keyCreator = keyCreator;
    }

    public KeyCreator getKeyCreator() {
        return keyCreator;
    }
    
    public NoSqlCommandContext createTransactionContext() {
        return null;
    }
    
    public void open() {
        Mongo m;
        try {
            m = new Mongo(host, port);
            db = m.getDB(dbName);
            LOG.info("opened database (" + dbName + "): " + db);
        } catch (UnknownHostException e) {
            throw new NoSqlStoreException(e);
        } catch (MongoException e) {
            throw new NoSqlStoreException(e);
        }
    }
   
    public void close() {
        // TODO is there a close mechanism?
    }
    
    public boolean containsData() {
        return db.getCollectionNames().size() > 0;
    }
    
    public long nextSerialNumberBatch(int batchSize) {
        throw new NotYetImplementedException();
    }
    
    public void writeSerialNumber(long serialNumber) {
        DBCollection system = db.getCollection("serialnumbers");
        DBObject object = system.findOne();
        if (object == null) {
            object = new BasicDBObject();
        }
        object.put("next-id", Long.toString(serialNumber));
        system.save(object);
        LOG.info("serial number written: " + serialNumber);
    }
    
    public long readSerialNumber() {
        DBCollection system = db.getCollection("serialnumbers");
        DBObject data = system.findOne();
        if (data == null) {
            return 0;
        } else {
            String number = (String) data.get("next-id");
            LOG.info("serial number read: " + number);
            return Long.valueOf(number);
        }
    }
    
    public boolean hasInstances(String specificationName) {
        DBCollection instances = db.getCollection(specificationName);
        return instances.getCount() > 0;
    }
    
    public Iterator<StateReader> instancesOf(String specificationName) {
        DBCollection instances = db.getCollection(specificationName);
        final DBCursor cursor = instances.find();
        LOG.info("searching for instances of: " + specificationName);
        return new Iterator<StateReader>() {
            public boolean hasNext() {
                return cursor.hasNext();
            }

            public StateReader next() {
                return new MongoStateReader(cursor.next());
            }

            public void remove() {
                throw new NoSqlStoreException("Can't remove elements");
            }
            
        };
    }
    
    public StateWriter createStateWriter(String specName) {
        return new MongoStateWriter(db, specName);
    }

    public StateReader getInstance(String key, String specName) {
        return new MongoStateReader(db, specName, key);
    }
    
    public void delete(String specificationName, String key) {
        DBCollection instances = db.getCollection(specificationName);
        ObjectId id = new ObjectId(key);
        DBObject object = instances.findOne(id);
        instances.remove(object);
        LOG.info("removed " + key);
    }
    
    
    public void write(List<PersistenceCommand> commands) {}
    
    public void addService(String name, String key) {
        DBCollection services = db.getCollection("services");
        services.insert(new BasicDBObject().append("name", name).append("key", key));
        LOG.info("service added " + name + ":" + key);
    }

    public String getService(String name) {
        DBCollection services = db.getCollection("services");
        DBObject object = services.findOne(new BasicDBObject().append("name", name));
        if (object == null) {
            return null;
        } else {
            String id = (String) object.get("key");
            LOG.info("service found " + name + ":" + id);
            return id;
        }
    }
}


