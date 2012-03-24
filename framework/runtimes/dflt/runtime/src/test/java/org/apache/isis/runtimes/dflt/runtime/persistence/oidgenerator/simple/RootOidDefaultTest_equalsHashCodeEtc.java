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

package org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.simple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.serial.RootOidDefault;

public class RootOidDefaultTest_equalsHashCodeEtc {

    private final String objectType = "FOO";

    @Test
    public void testEquals() {
        final RootOidDefault oid1 = RootOidDefault.createTransient(objectType, ""+123);
        final RootOidDefault oid2 = RootOidDefault.createTransient(objectType, ""+123);
        final RootOidDefault oid3 = RootOidDefault.createTransient(objectType, ""+321);
        final RootOidDefault oid4 = RootOidDefault.create(objectType, ""+321);
        final RootOidDefault oid5 = RootOidDefault.create(objectType, ""+456);
        final RootOidDefault oid6 = RootOidDefault.create(objectType, ""+456);

        assertTrue(oid1.equals(oid2));
        assertTrue(oid2.equals(oid1));

        assertFalse(oid1.equals(oid3));
        assertFalse(oid3.equals(oid1));

        assertTrue(oid5.equals(oid6));
        assertTrue(oid6.equals(oid5));

        assertFalse(oid4.equals(oid5));
        assertFalse(oid5.equals(oid4));

        assertFalse(oid3.equals((Object) oid4));
        assertFalse(oid4.equals((Object) oid3));

        assertFalse(oid3.equals(oid4));
        assertFalse(oid4.equals(oid3));
    }

    @Test
    public void testHashCode() {
        final RootOidDefault oid1 = RootOidDefault.createTransient(objectType, ""+123);
        final RootOidDefault oid2 = RootOidDefault.createTransient(objectType, ""+123);
        final RootOidDefault oid3 = RootOidDefault.createTransient(objectType, ""+321);
        final RootOidDefault oid4 = RootOidDefault.create(objectType, ""+321);
        final RootOidDefault oid5 = RootOidDefault.create(objectType, ""+456);
        final RootOidDefault oid6 = RootOidDefault.create(objectType, ""+456);

        assertEquals(oid1.hashCode(), oid2.hashCode());
        assertFalse(oid1.hashCode() == oid3.hashCode());

        assertEquals(oid5.hashCode(), oid6.hashCode());
        assertFalse(oid4.hashCode() == oid5.hashCode());

        assertFalse(oid3.hashCode() == oid4.hashCode());
    }

    @Test
    public void test_toString() {
        assertEquals("TOID:" + objectType + "#123", RootOidDefault.createTransient(objectType, ""+123).toString());
        assertEquals("OID:" + objectType + "#128", RootOidDefault.create(objectType, ""+128).toString());
    }

    @Test
    public void asPersistent() {
        final RootOidDefault transientOid = RootOidDefault.createTransient(objectType, ""+123);

        final RootOidDefault persistentOid = transientOid.asPersistent(""+567);

        assertEquals(""+123, transientOid.getIdentifier());
        assertEquals(true, transientOid.isTransient());

        assertEquals(transientOid.getObjectType(), persistentOid.getObjectType());
        assertEquals(""+567, persistentOid.getIdentifier());
        assertEquals(false, persistentOid.isTransient());
    }
}
