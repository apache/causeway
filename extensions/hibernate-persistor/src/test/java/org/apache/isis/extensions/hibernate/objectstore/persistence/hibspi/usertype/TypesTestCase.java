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


package org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.usertype;

import junit.framework.TestCase;

import org.hibernate.usertype.UserType;


public abstract class TypesTestCase extends TestCase {
    public static String[] names = { "name1" };

    public void basicTest(final UserType type) {
        final String test = "a";

        assertTrue("deep copy ==", test == type.deepCopy(test));
        assertTrue("assemble", test == type.assemble(test, null));
        assertTrue("disassemble", test == type.disassemble(test));
        assertTrue("replace", test == type.replace(test, null, null));
        assertTrue("!isMutable", !type.isMutable());
        assertTrue("equals", type.equals(test, test));
        assertTrue("!equals", !type.equals(test, "b"));
        assertEquals("hash", test.hashCode(), type.hashCode(test));
    }
}
