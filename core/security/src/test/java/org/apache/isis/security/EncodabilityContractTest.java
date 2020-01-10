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

package org.apache.isis.security;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.apache.isis.core.commons.internal.encoding.DataInputExtended;
import org.apache.isis.core.commons.internal.encoding.DataInputStreamExtended;
import org.apache.isis.core.commons.internal.encoding.DataOutputStreamExtended;
import org.apache.isis.core.commons.internal.encoding.Encodable;
import org.apache.isis.core.security.authentication.AuthenticationSession;

public abstract class EncodabilityContractTest {

    protected final Mockery context = new JUnit4Mockery();
    protected AuthenticationSession mockAuthSession;

    protected Encodable encodable;

    public EncodabilityContractTest() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        encodable = createEncodable();
        mockAuthSession = context.mock(AuthenticationSession.class);
    }

    /**
     * Hook for subclasses to provide object to be tested.
     */
    protected abstract Encodable createEncodable();

    @Test
    public void shouldImplementEncodeable() throws Exception {
        assertThat(encodable, is(instanceOf(Encodable.class)));
    }

    @Test
    public void shouldHaveOneArgConstructorThatAcceptsInput() {
        final Object o = encodable;
        try {
            o.getClass().getConstructor(DataInputExtended.class);
        } catch (final Exception e) {
            fail("could not locate 1-arg constructor accepting a DataInputExtended instance");
        }
    }

    @Test
    public void shouldRoundTrip() throws IOException {
        final PipedInputStream pipedInputStream = new PipedInputStream();
        final PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);
        final DataOutputStreamExtended outputImpl = new DataOutputStreamExtended(pipedOutputStream);
        final DataInputStreamExtended inputImpl = new DataInputStreamExtended(pipedInputStream);

        outputImpl.writeEncodable(encodable);
        final Object decodedEncodable = inputImpl.readEncodable(Object.class);

        assertRoundtripped(decodedEncodable, encodable);
    }

    protected abstract void assertRoundtripped(Object decodedEncodable, Object originalEncodable);

}