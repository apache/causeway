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


package org.apache.isis.alternatives.objectstore.nosql.mongo;

import org.apache.isis.alternatives.objectstore.nosql.mongo.MongoStateReader;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

import static org.junit.Assert.assertEquals;


public class MongoStateReaderTest {

    private static final String SPEC_NAME = "org.test.Object";
    private DB testDb;
    private MongoStateReader reader;

    @Before
    public void setup() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);

        Mongo m = new Mongo();
        m.dropDatabase("mydb");
        testDb = m.getDB("mydb");
        
        BasicDBObject object = new BasicDBObject();
        object.put("_id", "1023");
        object.put("_type", "org.xxx.Class");
        object.put("name", "Fred Smith");
        object.put("null name", "null");
        object.put("null name 2", null);
        object.put("number", "102");
        object.put("null number", "null");
        DBCollection instances = testDb.getCollection(SPEC_NAME);
        instances.insert(object);

        reader = new MongoStateReader(testDb, SPEC_NAME, "1023");
    }

    @Test
    public void readNonexistantFieldAsNull() throws Exception {
        assertEquals(null, reader.readField("unknown"));
    }
    
    @Test
    public void readStringField() throws Exception {
        assertEquals("Fred Smith", reader.readField("name"));
    }
    
    @Test
    public void readStringFieldAsNull() throws Exception {
        assertEquals(null, reader.readField("null name"));
    }
    
    @Test
    public void readNullFieldAsNull() throws Exception {
        assertEquals(null, reader.readField("null name 2"));
    }
    
    @Test
    public void readType() throws Exception {
        assertEquals("org.xxx.Class", reader.readObjectType());
    }
    
    @Test
    public void readNumberField() throws Exception {
        assertEquals(102L, reader.readLongField("number"));
    }
    
    @Test
    public void readNumberFieldAsNull() throws Exception {
        assertEquals(0L, reader.readLongField("null number"));
    }
    
    @Test
    public void readNonexistingNumberFieldAsZero() throws Exception {
        assertEquals(0L, reader.readLongField("unknown"));
    }
    
    @Test
    public void readId() throws Exception {
        assertEquals("1023", reader.readId());
    }
}


