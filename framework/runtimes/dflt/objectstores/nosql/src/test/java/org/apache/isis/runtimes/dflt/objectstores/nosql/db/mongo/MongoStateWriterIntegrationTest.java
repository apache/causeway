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

package org.apache.isis.runtimes.dflt.objectstores.nosql.db.mongo;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeThat;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.core.metamodel.adapter.oid.RootOidDefault;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;

public class MongoStateWriterIntegrationTest {

    private static final String SPEC_NAME = "org.test.Object";
    private DB testDb;
    private MongoStateWriter writer;

    @Before
    public void setup() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);

        try {

            final Mongo m = new Mongo();
            m.dropDatabase("mydb");
            testDb = m.getDB("mydb");
        } catch (final Exception e) {
            assumeThat(true, is(false)); // ie no exceptions
            return;
        }

        writer = new MongoStateWriter(testDb, ObjectSpecId.of(SPEC_NAME));
    }

    @Test
    public void flushSavesObject() throws Exception {
        writer.flush();

        final DBCollection instances = testDb.getCollection(SPEC_NAME);
        assertEquals(1, instances.getCount());
    }

    @Test
    public void objectNotSavedUntilFlush() throws Exception {
        writer.writeField("number", 1023);
        writer.writeField("string", "testing");

        final DBCollection instances = testDb.getCollection(SPEC_NAME);
        assertEquals(0, instances.getCount());
    }

    @Test
    public void serialNumberNotStored() throws Exception {
        //writer.writeId("D01");
        writer.writeOid(RootOidDefault.deString(SPEC_NAME+":"+"D01"));
        writer.flush();

        final DBCollection instances = testDb.getCollection(SPEC_NAME);
        assertEquals(1, instances.getCount());
        final DBObject object = instances.findOne();
        
        assertEquals(SPEC_NAME+":"+"D01", object.get("_oid"));
        assertEquals("D01", object.get("_id"));
        
        assertEquals(2, object.keySet().size());
    }

    @Test
    public void writeFields() throws Exception {
        //writer.writeObjectType(SPEC_NAME);
        writer.writeOid(RootOidDefault.deString(SPEC_NAME+":"+"D01"));
        writer.writeField("number", 1023);
        writer.writeField("string", "testing");
        writer.flush();

        final DBCollection instances = testDb.getCollection(SPEC_NAME);
        assertEquals(1, instances.getCount());
        final DBObject object = instances.findOne();
        assertEquals(SPEC_NAME+":"+"D01", object.get("_oid"));
        assertEquals("1023", object.get("number"));
        assertEquals("testing", object.get("string"));
    }

    @Test
    public void writeFields2() throws Exception {
//        writer.writeId("3");
//        writer.writeObjectType(SPEC_NAME);
        writer.writeOid(RootOidDefault.deString(SPEC_NAME + ":" + "3"));
        writer.flush();

        writer.writeField("number", 1023);
        writer.writeField("string", "testing");
        writer.flush();

        final DBCollection instances = testDb.getCollection(SPEC_NAME);
        assertEquals(1, instances.getCount());
        final DBObject object = instances.findOne();
        assertEquals(SPEC_NAME + ":" + "3", object.get("_oid"));
        assertEquals("1023", object.get("number"));
        assertEquals("testing", object.get("string"));
    }

}
