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


package org.apache.isis.alternatives.objectstore.xml.internal.data;

import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.isis.alternatives.objectstore.xml.internal.clock.DefaultClock;
import org.apache.isis.alternatives.objectstore.xml.internal.data.ObjectData;
import org.apache.isis.alternatives.objectstore.xml.internal.version.FileVersion;
import org.apache.isis.core.metamodel.testspec.TestProxySpecification;
import org.apache.isis.core.runtime.persistence.oidgenerator.simple.SerialOid;


public class ObjectDataTest extends TestCase {

    public void testValueField() {
        FileVersion.setClock(new DefaultClock());

        final TestProxySpecification type = new TestProxySpecification("test");
        final ObjectData objectData = new ObjectData(type, SerialOid.createPersistent(13), new FileVersion(""));

        assertEquals(null, objectData.get("name"));
        objectData.set("name", "value");
        assertEquals("value", objectData.get("name"));

        final Iterator<String> e = objectData.fields().iterator();
        e.next();
        assertFalse(e.hasNext());

    }

}
