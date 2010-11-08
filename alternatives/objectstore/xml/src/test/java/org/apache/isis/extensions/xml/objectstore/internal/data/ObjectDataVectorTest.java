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


package org.apache.isis.extensions.xml.objectstore.internal.data;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.testspec.TestProxySpecification;
import org.apache.isis.extensions.xml.objectstore.internal.version.FileVersion;
import org.apache.isis.runtime.persistence.oidgenerator.simple.SerialOid;

public class ObjectDataVectorTest {
	private ObjectDataVector objectDataVector;
	private ObjectData objectData;
    private TestProxySpecification spec;
	private SerialOid oid;
	private FileVersion version;

	@Before
	public void setUp() throws Exception {
		boolean isTransient = true;
		long serialNum = Long.parseLong("1", 16);
		oid = isTransient ? SerialOid.createTransient(serialNum) : SerialOid.createPersistent(serialNum);

		spec = new TestProxySpecification(this.getClass());
        spec.fields = new ObjectAssociation[0];

        version = new FileVersion("", System.currentTimeMillis());
        objectData = new ObjectData(spec, oid, version);
		objectDataVector = new ObjectDataVector();
	}
	
	@Test 
	public void validatesObjectDataIsStored()throws Exception {
		objectDataVector.addElement(objectData);
		assertTrue(objectDataVector.contains(objectData));
		assertTrue(objectDataVector.element(0).equals(objectData));
	}
	
	@Test 
	public void validatesObjectDataVectorSize()throws Exception {
		objectDataVector.addElement(objectData);
		assertTrue(objectDataVector.size() == 1);
	}
	
}


