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


package org.apache.isis.runtimes.dflt.objecstores.xml.internal.version;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.isis.core.commons.encoding.DataInputExtended;
import org.apache.isis.core.commons.encoding.DataOutputExtended;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.runtimes.dflt.runtime.testsystem.ProxyJunit4TestCase;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;

public class FileVersionTest extends ProxyJunit4TestCase {
    Mockery context = new JUnit4Mockery();
    final DataInputExtended extended = context.mock(DataInputExtended.class);; 

    @Before
	public void setUp() throws Exception {
		
	}
	@Test
	public void testFileVersionDataInputExtended() throws Exception { 
        context.checking(new Expectations() {{
            ignoring (extended);
        }});		
		FileVersion fileVersion = new FileVersion(extended);
	}

	@Test
	public void testFileVersionEncoding() throws Exception { 
		final DataOutputExtended outputExtended = context.mock(DataOutputExtended.class);
        context.checking(new Expectations() {{
            ignoring (extended);
            ignoring (outputExtended);
        }});		
		FileVersion fileVersion = new FileVersion(extended);
        fileVersion.encode(outputExtended);
	}

	@Test
	public void testFileVersionDifferenceAndSequenceAndEquals() throws Exception { 
		final Version version = context.mock(Version.class);
        context.checking(new Expectations() {{
        	ignoring (extended);
            one (version);
            	
        }});		
		FileVersion testVersion = new FileVersion(extended);
		assertFalse(testVersion.different(version));
		assertTrue(testVersion.sequence().length() > 0);
		assertTrue(testVersion.getSequence() == 0);
		assertTrue(testVersion.toString().length() > 0);
		assertTrue(testVersion.equals(testVersion));
		assertFalse(testVersion.equals(version));
	}

}
