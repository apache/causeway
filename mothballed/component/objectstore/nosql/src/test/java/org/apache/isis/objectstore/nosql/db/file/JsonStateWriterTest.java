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
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.integtestsupport.IsisSystemWithFixtures;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOidDefault;
import org.apache.isis.objectstore.nosql.db.StateWriter;

public class JsonStateWriterTest {

    @Rule
    public IsisSystemWithFixtures iswf = IsisSystemWithFixtures.builder().build();
    
    private JsonStateWriter writer;

    @Before
    public void setup() {
        writer = new JsonStateWriter();
    }

    @Test
    public void noData() throws Exception {
        assertEquals("{}", writer.getData());
    }

    @Test
    public void basicData() throws Exception {
//        writer.writeObjectType("com.planchase.ClassName");
//        writer.writeId("#1");
        writer.writeOid(RootOidDefault.deString("com.planchase.ClassName:1", new OidMarshaller()));
        writer.writeTime("ddmmyy");
        writer.writeVersion("1", "2");
        writer.writeUser("fred");
        assertEquals("{\n" +
        		"    \"_oid\": \"com.planchase.ClassName:1\",\n" +
        		"    \"_time\": \"ddmmyy\",\n" +
        		"    \"_user\": \"fred\",\n" +
        		"    \"_version\": \"2\"\n" +
        		"}", 
        		writer.getData());
    }

    @Test
    public void encrytionVersion() throws Exception {
        writer.writeEncryptionType("etc1");
        assertEquals("{\"_encrypt\": \"etc1\"}", writer.getData());
    }

    @Test
    public void numberData() throws Exception {
        writer.writeField("number", 1239912);
        assertEquals("{\"number\": \"1239912\"}", writer.getData());
    }

    @Test
    public void stringData() throws Exception {
        writer.writeField("number", "string-data");
        assertEquals("{\"number\": \"string-data\"}", writer.getData());
    }

    @Test
    public void nullData() throws Exception {
        writer.writeField("number", null);
        assertEquals("{\"number\": null}", writer.getData());
    }

    @Test
    public void addAggregate() throws Exception {
        final StateWriter aggregate = writer.addAggregate("#4");
        aggregate.writeField("number", "string-data");
        assertEquals("{\"#4\": {\"number\": \"string-data\"}}", writer.getData());
    }

    @Test
    public void elementData() throws Exception {
        final List<StateWriter> elements = new ArrayList<StateWriter>();
        final StateWriter elementWriter1 = writer.createElementWriter();
        elementWriter1.writeField("number", "1");
        elements.add(elementWriter1);
        final StateWriter elementWriter2 = writer.createElementWriter();
        elementWriter2.writeField("number", "4");
        elements.add(elementWriter2);

        writer.writeCollection("coll", elements);

        assertEquals("{\"coll\": [\n    {\"number\": \"1\"},\n    {\"number\": \"4\"}\n]}", writer.getData());
    }

    @Test
    public void requestData() throws Exception {
//        writer.writeObjectType("com.planchase.ClassName");
//        writer.writeId("#8");
        writer.writeOid(RootOidDefault.deString("com.planchase.ClassName:8", new OidMarshaller()));
        writer.writeVersion("1", "2");
        assertEquals("com.planchase.ClassName:8 1 2", writer.getRequest());
    }

}
