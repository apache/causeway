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

package org.apache.isis.runtimes.dflt.objectstores.xml.internal.version;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jmock.Expectations;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.commons.encoding.DataInputExtended;
import org.apache.isis.core.commons.encoding.DataOutputExtended;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2.Mode;

public class FileVersionTest {
    
    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_ONLY);
    
    final DataInputExtended extended = context.mock(DataInputExtended.class);;

//    protected TestProxySystem system;
//
//    private TestProxyConfiguration mockConfiguration;
//    private TestProxyReflector mockReflector;
//    private AuthenticationSession mockAuthSession;
//    private TestProxyPersistenceSessionFactory mockPersistenceSessionFactory;
//    private TestProxyPersistenceSession mockPersistenceSession;
//    private TestUserProfileStore mockUserProfileStore;

//    @Before
//    public void setUpSystem() throws Exception {
//
//        Logger.getRootLogger().setLevel(Level.OFF);
//        system = new TestProxySystem();
//        
//        mockConfiguration = new TestProxyConfiguration();
//        mockReflector = new TestProxyReflector();
//        mockAuthSession = new TestProxySession();
//        mockPersistenceSessionFactory = new TestProxyPersistenceSessionFactory();
//        mockPersistenceSession = new TestProxyPersistenceSession(mockPersistenceSessionFactory);
//        mockPersistenceSessionFactory.setPersistenceSessionToCreate(mockPersistenceSession);
//        mockUserProfileStore = new TestUserProfileStore();
//        
//        system.openSession(mockConfiguration, mockReflector, mockAuthSession, null, null, null, mockUserProfileStore, null, mockPersistenceSessionFactory, null);
//    }

    @Test
    public void FileVersion_instantiateUsingDataInputExtended() throws Exception {
        context.checking(new Expectations() {
            {
                ignoring(extended);
            }
        });
        new FileVersion(extended);
    }

    @Test
    public void testFileVersionEncoding() throws Exception {
        final DataOutputExtended outputExtended = context.mock(DataOutputExtended.class);
        context.checking(new Expectations() {
            {
                ignoring(extended);
                ignoring(outputExtended);
            }
        });
        final FileVersion fileVersion = new FileVersion(extended);
        fileVersion.encode(outputExtended);
    }

    @Test
    public void testFileVersionDifferenceAndSequenceAndEquals() throws Exception {
        final Version version = context.mock(Version.class);
        context.checking(new Expectations() {
            {
                ignoring(extended);
                one(version);

            }
        });
        final FileVersion testVersion = new FileVersion(extended);
        assertFalse(testVersion.different(version));
        assertTrue(testVersion.sequence().length() > 0);
        assertTrue(testVersion.getSequence() == 0);
        assertTrue(testVersion.toString().length() > 0);
        assertTrue(testVersion.equals(testVersion));
        assertFalse(testVersion.equals(version));
    }

}
