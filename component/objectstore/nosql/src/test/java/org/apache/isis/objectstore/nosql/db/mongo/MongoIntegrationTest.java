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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeThat;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.integtestsupport.IsisSystemWithFixtures;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.objectstore.nosql.keys.KeyCreatorDefault;

public class MongoIntegrationTest {
    
    @Rule
    public IsisSystemWithFixtures iswf = IsisSystemWithFixtures.builder().with(new MongoPersistorMechanismInstaller()).build();
    
    private MongoDb db;
    private DB testDb;

    private ObjectAdapter adapter1;

    private ObjectSpecification specification;

    @Before
    public void setupMongo() throws Exception {

        try {
            final Mongo m = new Mongo();
            m.dropDatabase("testdb");
            testDb = m.getDB("testdb");
        } catch (final Exception e) {
            assumeThat(true, is(false)); // ignore if no MongoDB instance to
                                         // connect to
            return;
        }

        db = new MongoDb("localhost", 0, "testdb", new KeyCreatorDefault());
        db.open();

        adapter1 = iswf.adapterFor(iswf.fixtures.smpl1);
        specification = adapter1.getSpecification();
    }

    @Test
    public void newDatabaseContainsNothing() throws Exception {
        assertFalse(db.containsData());
    }

    @Test
    public void serialNumberSaved() throws Exception {
        assertEquals(1, db.nextSerialNumberBatch(ObjectSpecId.of("oid"), 10));
        assertEquals(11, db.nextSerialNumberBatch(ObjectSpecId.of("oid"), 10));
    }

    @Test
    public void hasInstances() throws Exception {
        assertFalse(db.hasInstances(specification.getSpecId()));
        db.close();

        final DBCollection instances = testDb.getCollection(specification.getSpecId().asString());
        instances.insert(new BasicDBObject().append("test", "test"));

        db.open();
        assertTrue(db.hasInstances(specification.getSpecId()));
        assertFalse(db.hasInstances(ObjectSpecId.of("org.xxx.unknown")));
    }

    @Test
    public void serviceIds() throws Exception {
        final ObjectSpecId osi = ObjectSpecId.of("one");
        db.addService(osi, "123");
        assertEquals("123", db.getService(osi));
    }

    @Test
    public void unknownServiceIds() throws Exception {
        assertNull(db.getService(ObjectSpecId.of("two")));
    }

}
