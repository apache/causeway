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


package org.apache.isis.nof.reflect.remote.spec;

import junit.framework.TestSuite;

import org.apache.isis.noa.adapter.ObjectAdapter;
import org.apache.isis.nof.reflect.peer.MemberIdentifierImpl;
import org.apache.isis.nof.testsystem.ProxyTestCase;
import org.apache.isis.nof.testsystem.TestProxySpecification;


public class JavaFieldTest extends ProxyTestCase {
    private static final String MEMBERS_FIELD_NAME = "members";

    public static void main(final String[] args) {
        junit.textui.TestRunner.run(new TestSuite(JavaFieldTest.class));
    }

    private JavaField field;

    protected void setUp() throws Exception  {
        super.setUp();
        
        MemberIdentifierImpl memberIdentifierImpl = new MemberIdentifierImpl("cls", MEMBERS_FIELD_NAME);

        field = new JavaField(memberIdentifierImpl, String.class){
            public boolean isEmpty(ObjectAdapter inObject) {
                return true;
            }
        };

    }

    protected void tearDown() throws Exception {
        system.shutdown();
    }

    public void testType() {
        TestProxySpecification spec = system.getSpecification(String.class);
        assertEquals(spec, field.getSpecification());
    }

    public void testIsEmpty() throws Exception {
        ObjectAdapter inObject = system.createPersistentTestObject();
        assertTrue(field.isEmpty(inObject));
    }

}
