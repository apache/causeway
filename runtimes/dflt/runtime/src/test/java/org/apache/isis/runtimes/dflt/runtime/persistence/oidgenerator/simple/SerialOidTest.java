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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SerialOidTest {

    @Test
    public void testEquals() {
        final SerialOid oid1 = SerialOid.createTransient(123);
        final SerialOid oid2 = SerialOid.createTransient(123);
        final SerialOid oid3 = SerialOid.createTransient(321);
        final SerialOid oid4 = SerialOid.createPersistent(321);
        final SerialOid oid5 = SerialOid.createPersistent(456);
        final SerialOid oid6 = SerialOid.createPersistent(456);

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

    public void testHashCode() {
        final SerialOid oid1 = SerialOid.createTransient(123);
        final SerialOid oid2 = SerialOid.createTransient(123);
        final SerialOid oid3 = SerialOid.createTransient(321);
        final SerialOid oid4 = SerialOid.createPersistent(321);
        final SerialOid oid5 = SerialOid.createPersistent(456);
        final SerialOid oid6 = SerialOid.createPersistent(456);

        assertEquals(oid1.hashCode(), oid2.hashCode());
        assertFalse(oid1.hashCode() == oid3.hashCode());

        assertEquals(oid5.hashCode(), oid6.hashCode());
        assertFalse(oid4.hashCode() == oid5.hashCode());

        assertFalse(oid3.hashCode() == oid4.hashCode());
    }

    @Test
    public void testCopy() {
        final SerialOid oid1 = SerialOid.createTransient(123);
        final SerialOid oid3 = SerialOid.createTransient(321);

        assertFalse(oid1.hashCode() == oid3.hashCode());

        oid1.copyFrom(oid3);

        assertEquals(oid1.hashCode(), oid3.hashCode());
    }

    @Test
    public void testStringAsHex() {
        assertEquals("TOID#7B", SerialOid.createTransient(123).toString());
        assertEquals("OID#80", SerialOid.createPersistent(128).toString());
    }

    @Test
    public void testMakePersistent() {
        final SerialOid oid1 = SerialOid.createTransient(123);

        assertNull(oid1.getPrevious());

        oid1.setId(567);
        oid1.makePersistent();

        assertEquals(567, oid1.getSerialNo());
        assertEquals(false, oid1.isTransient());
        assertEquals(oid1.getPrevious(), SerialOid.createTransient(123));
    }

    @Test
    public void testGetPrevious() {
        final SerialOid oid1 = SerialOid.createTransient(123);

        assertNull(oid1.getPrevious());

        final SerialOid oidCopy = SerialOid.createTransient(0);
        oidCopy.copyFrom(oid1);

        oid1.setId(567);
        oid1.makePersistent();

        assertThat(oid1.getPrevious().hashCode(), is(equalTo(oidCopy.hashCode())));
        assertThat(oid1.getPrevious().equals(oidCopy), is(true));
    }

}
