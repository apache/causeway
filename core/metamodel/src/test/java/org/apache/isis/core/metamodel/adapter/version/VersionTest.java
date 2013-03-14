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

package org.apache.isis.core.metamodel.adapter.version;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.commons.encoding.DataInputExtended;
import org.apache.isis.core.commons.encoding.DataOutputExtended;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

public class VersionTest {
    
    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);
    
    @Mock
    private DataInputExtended extended;

    @Mock
    private DataOutputExtended outputExtended;

    @Mock
    private Version version;;

    @Before
    public void setUp() throws Exception {
        context.ignoring(extended);
    }
    
    @Test
    public void instantiate_usingDataInputExtended() throws Exception {
        new Version(extended);
    }

    @Test
    public void encode() throws Exception {
        context.ignoring(outputExtended);
        
        final Version oidVersion = new Version(extended);
        oidVersion.encode(outputExtended);
    }

    @Test
    public void sequence_and_toString() throws Exception {
        final Version testVersion = new Version(extended);
        
        assertTrue(testVersion.sequence().length() > 0);
        assertTrue(testVersion.getSequence() == 0);
        assertTrue(testVersion.toString().length() > 0);
        assertTrue(testVersion.equals(testVersion));
        assertFalse(testVersion.equals(version));
    }

}
