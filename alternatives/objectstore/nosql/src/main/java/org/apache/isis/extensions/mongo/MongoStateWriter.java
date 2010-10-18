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

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.isis.commons.exceptions.UnexpectedCallException;
import org.apache.isis.extensions.nosql.StateWriter;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

public class MongoStateWriter implements StateWriter {
    private static final Logger LOG = Logger.getLogger(MongoStateWriter.class);
    private BasicDBObject dbObject;
    private DBCollection instances;
    
    public MongoStateWriter(DB db, String specName) {
        dbObject = new BasicDBObject();
        instances = db.getCollection(specName);
    }
    
    public void flush() {
        instances.save(dbObject);
        LOG.debug("saved " + dbObject);
    }

    public void writeId(String oid) {
        writeField("_id", oid);
    }

    public void writeType(String type) {
        writeField("_type", type);
    }
    
    public void writeField(String id, String data) {
        dbObject.put(id, data);
    }

    public void writeField(String id, long l) {
        dbObject.put(id, Long.toString(l));
    }

    public void writeVersion(String currentVersion, String newVersion) {}

    public void writeTime(String time) {}

    public void writeUser(String user) {}

    public StateWriter addAggregate(String id) {
        throw new UnexpectedCallException();
    }

    public StateWriter createElementWriter() {
        return null;
    }
    
    public void writeCollection(String id, List<StateWriter> elements) {}
}


