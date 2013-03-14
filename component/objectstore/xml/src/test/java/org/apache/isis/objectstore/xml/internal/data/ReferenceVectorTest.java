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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.core.metamodel.adapter.oid.RootOidDefault;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;

public class ReferenceVectorTest {
    
    private final String objectType = "FOO";
    
    private ListOfRootOid listOfRootOid;
    private RootOidDefault oid;

    @Before
    public void setUp() throws Exception {
        oid = RootOidDefault.createTransient(ObjectSpecId.of(objectType), ""+1);
        listOfRootOid = new ListOfRootOid();
    }

    @Test
    public void validatesSerialOidIsStoredInElements() throws Exception {
        listOfRootOid.add(oid);
        assertTrue(listOfRootOid.elementAt(0).equals(oid));
    }

    @Test
    public void validatesSerialOidIsRemovedInElements() throws Exception {
        listOfRootOid.add(oid);
        listOfRootOid.remove(oid);
        assertTrue(listOfRootOid.size() == 0);
    }

    @Test
    public void validatesReferenceVectorIsEqual() throws Exception {
        assertTrue(listOfRootOid.equals(listOfRootOid));
        assertTrue(listOfRootOid.equals(new ListOfRootOid()));
        assertFalse(listOfRootOid.equals(new Object()));
    }

    @Test
    public void validateReferenceVectorHashCode() throws Exception {
        assertTrue(listOfRootOid.hashCode() == 630);
    }

    @Test
    public void validateReferenceToString() throws Exception {
        assertTrue(listOfRootOid.toString() != null);
    }

}
