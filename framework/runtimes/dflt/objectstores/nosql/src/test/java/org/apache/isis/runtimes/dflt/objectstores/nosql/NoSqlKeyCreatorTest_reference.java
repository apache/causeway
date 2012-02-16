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

package org.apache.isis.runtimes.dflt.objectstores.nosql;

import static org.junit.Assert.assertEquals;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2.Mode;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.simple.SerialOid;

public class NoSqlKeyCreatorTest_reference {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_ONLY);


    @Mock
    private ObjectSpecification specification;
    @Mock
    private ObjectAdapter adapter;
    
    private final String className = "com.foo.bar.SomeClass";
    private final SerialOid serialOid = SerialOid.createPersistent(123);
    private final NoSqlOid noSqlOid = new NoSqlOid(className, serialOid);
    
    private NoSqlKeyCreator noSqlKeyCreator;

    @Before
    public void setup() {
        noSqlKeyCreator = new NoSqlKeyCreator();
        
        context.checking(new Expectations() {
            {
                allowing(adapter).getSpecification();
                will(returnValue(specification));

                allowing(adapter).getOid();
                will(returnValue(noSqlOid));

                allowing(specification).getFullIdentifier();
                will(returnValue(className));
            }
        });
    }

    @Test
    public void reference() throws Exception {
        final String expectedReference = className + "@" + Long.toString(serialOid.getSerialNo(), 16);
        assertEquals(expectedReference, noSqlKeyCreator.reference(adapter));
    }
}
