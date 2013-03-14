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
package org.apache.isis.objectstore.nosql.db.file;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.objectstore.nosql.db.StateReader;

public class JsonStateReaderTest {

    private JsonStateReader reader;

    @Before
    public void setup() {
        reader = new JsonStateReader(
                "{" + 
                    "\"_encrypt\": \"etc1\"," + 
                    "\"_oid\": \"com.package.ClassName:#2\"," + 
                    "\"_time\": \"ddmmyy\"," + 
                    "\"_user\": \"fred\"," + 
                    "\"_version\": \"2\"," + 
                    "\"field-1\": \"1234\"," + 
                    "\"field-2\": \"data\"," + 
                    "\"field-3\": null,"
                    + "\"list\": [{}, {}]," + 
                    "\"aggregate\": {" +
                        "\"_oid\": \"com.package.ClassName:#2~com.package.AggregatedClassName:#3\""  +
                        "}," + 
                    "}");
    }

    @Test
    public void readEncryptionType() throws Exception {
        assertEquals("etc1", reader.readEncrytionType());
    }

//    @Test
//    public void readId() throws Exception {
//        assertEquals("#2", reader.readId());
//    }
//
//    @Test
//    public void readObjectType() throws Exception {
//        assertEquals("com.package.ClassName", reader.readObjectType());
//    }

    @Test
    public void readOid() throws Exception {
        assertEquals("com.package.ClassName:#2", reader.readOid());
    }

    @Test
    public void readTime() throws Exception {
        assertEquals("ddmmyy", reader.readTime());
    }

    @Test
    public void readUser() throws Exception {
        assertEquals("fred", reader.readUser());
    }

    @Test
    public void readVersion() throws Exception {
        assertEquals("2", reader.readVersion());
    }

    @Test
    public void readNumberField() throws Exception {
        assertEquals(1234L, reader.readLongField("field-1"));
    }

    @Test
    public void readNumberFieldAsNull() throws Exception {
        assertEquals(0L, reader.readLongField("field-4"));
    }

    @Test
    public void readStringField() throws Exception {
        assertEquals("data", reader.readField("field-2"));
    }

    @Test
    public void readStringFieldAsNull() throws Exception {
        assertEquals(null, reader.readField("field-4"));
    }

    @Test
    public void readUnsavedCollection() throws Exception {
        assertEquals(new ArrayList<StateReader>(), reader.readCollection("unknown-list"));
    }

    @Test
    public void readList() throws Exception {
        final List<StateReader> collection = reader.readCollection("list");
        assertEquals(2, collection.size());
        // assertEquals(null, reader.readField("field-4"));
    }

    @Test
    public void readAggregate() throws Exception {
        final StateReader aggregate = reader.readAggregate("aggregate");
        assertEquals("com.package.ClassName:#2~com.package.AggregatedClassName:#3", aggregate.readOid());
    }
}
