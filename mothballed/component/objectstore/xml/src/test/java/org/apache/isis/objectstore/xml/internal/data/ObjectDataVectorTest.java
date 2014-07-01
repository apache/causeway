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

package org.apache.isis.objectstore.xml.internal.data;

import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.core.metamodel.adapter.oid.Oid.State;
import org.apache.isis.core.metamodel.adapter.oid.RootOidDefault;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.testspec.ObjectSpecificationStub;
import org.apache.isis.objectstore.xml.internal.version.FileVersion;

public class ObjectDataVectorTest {
    
    private final String objectType = "ODV";
    
    private ObjectDataVector objectDataVector;
    private ObjectData objectData;
    private ObjectSpecificationStub spec;
    private RootOidDefault oid;
    private Version version;

    @Before
    public void setUp() throws Exception {
        final ObjectSpecId objectSpecId = ObjectSpecId.of(objectType);
        oid = new RootOidDefault(objectSpecId, ""+1, State.TRANSIENT);

        spec = new ObjectSpecificationStub(this.getClass());
        spec.fields = Collections.emptyList();

        version = FileVersion.create("", System.currentTimeMillis());
        objectData = new ObjectData(oid, version);
        objectDataVector = new ObjectDataVector();
    }

    @Test
    public void validatesObjectDataIsStored() throws Exception {
        objectDataVector.addElement(objectData);
        assertTrue(objectDataVector.contains(objectData));
        assertTrue(objectDataVector.element(0).equals(objectData));
    }

    @Test
    public void validatesObjectDataVectorSize() throws Exception {
        objectDataVector.addElement(objectData);
        assertTrue(objectDataVector.size() == 1);
    }

}
