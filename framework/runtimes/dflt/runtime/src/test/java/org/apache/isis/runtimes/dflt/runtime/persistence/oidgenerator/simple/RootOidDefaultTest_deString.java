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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.serial.RootOidDefault;

public class RootOidDefaultTest_deString {

    private final String objectType = "CUS";

    @Test
    public void whenValidTransientWithPullsOutTransient() {
        final RootOidDefault oid = RootOidDefault.deString("TOID:CUS:1");
        assertThat(oid.isTransient(), is(true));
    }

    @Test
    public void whenValidThenPullsOutSerialNumber() {
        final RootOidDefault oid = RootOidDefault.deString("TOID:CUS:1");
        assertThat(oid.getIdentifier(), is("1"));
    }

    @Test
    public void whenValidPersistentThenPullsOutPersistent() {
        final RootOidDefault oid = RootOidDefault.deString("OID:CUS:1");
        assertThat(oid.isTransient(), is(false));
        assertThat(oid.getIdentifier(), is("1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenInvalidTransientModifierThrowsException() {
        RootOidDefault.deString("QOID:CUS:12ABF3");
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenInvalidOidLiteralModifierThrowsException() {
        RootOidDefault.deString("TOiD:CUS:12ABF3");
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenInvalidPreviousConcatenationCharacterThrowsException() {
        RootOidDefault.deString("OID:CUS:1F-OID#1A");
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenInvalidPreviousConcatenationCharacter2ThrowsException() {
        RootOidDefault.deString("OID:CUS:1F-OID@1A");
    }


    @Test
    public void shouldRoundtripOk() {
        final String identifier = Long.toString(Long.MIN_VALUE + 1, 16);
        final RootOidDefault oid = RootOidDefault.createTransient(objectType, identifier);
        final String enString = oid.enString();
        assertThat(enString, CoreMatchers.equalTo("TOID:CUS:" + identifier));

        final RootOidDefault oid2 = RootOidDefault.deString(enString);
        assertThat(oid2.getIdentifier(), is(oid.getIdentifier()));
        assertThat(oid2.isTransient(), is(oid.isTransient()));
    }
}
