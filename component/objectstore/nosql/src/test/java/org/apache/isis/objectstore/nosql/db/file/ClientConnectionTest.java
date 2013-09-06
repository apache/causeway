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
package org.apache.isis.objectstore.nosql.db.file;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.core.commons.lang.InputStreamExtensions;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.runtime.persistence.ObjectNotFoundException;

public class ClientConnectionTest {

    private InputStream input;
    private ByteArrayOutputStream output;
    private ClientConnection connection;

    @Before
    public void setup() throws Exception {
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);

        input = InputStreamExtensions.asUtf8ByteStream("org.domain.Class false true 1025\n{data...}\n\n102334");
        output = new ByteArrayOutputStream();
        connection = new ClientConnection(input, output);
    }

    @Test
    public void testRequest() throws Exception {
        connection.request('D', "xxx yyy");
        connection.close();
        assertEquals("Dxxx yyy\n", output.toString());
    }

    @Test
    public void testRequestData() throws Exception {
        connection.requestData("{data...}");
        connection.close();
        // assertEquals("{data...}\n\n5de98274", output.toString());
    }

    @Test
    public void testResponseHeaders() throws Exception {
        connection.getReponseHeader();
        assertEquals("org.domain.Class", connection.getResponse());
        assertEquals(false, connection.getResponseAsBoolean());
        assertEquals(true, connection.getResponseAsBoolean());
        assertEquals(1025L, connection.getResponseAsLong());
    }

    @Test
    public void tooManyResponseHeadersExpected() throws Exception {
        connection.getReponseHeader();
        connection.getResponse();
        connection.getResponse();
        connection.getResponse();
        connection.getResponse();
        try {
            connection.getResponse();
            fail();
        } catch (final RemotingException e) {
            assertThat(e.getMessage(), containsString("are only 4"));
        }
    }

    @Test
    public void testResponseData() throws Exception {
        connection.getReponseHeader();
        final String data = connection.getResponseData();
        assertEquals("{data...}\n", data);
    }

    @Test
    public void validateResponseOk() throws Exception {
        input = InputStreamExtensions.asUtf8ByteStream("ok xx xx\n{data...}");
        connection = new ClientConnection(input, output);
        connection.validateRequest();
    }

    @Test(expected = RemotingException.class)
    public void validateResponseError() throws Exception {
        input = InputStreamExtensions.asUtf8ByteStream("error message about it\n");
        connection = new ClientConnection(input, output);
        connection.validateRequest();
    }

    @Test(expected = ObjectNotFoundException.class)
    public void validateObjectNotFound() throws Exception {
        input = InputStreamExtensions.asUtf8ByteStream("not-found message about it\n");
        connection = new ClientConnection(input, output);
        connection.validateRequest();
    }

    @Test(expected = ConcurrencyException.class)
    public void validateConcurrencyException() throws Exception {
        input = InputStreamExtensions.asUtf8ByteStream("concurrency message about it\n");
        connection = new ClientConnection(input, output);
        connection.validateRequest();
    }

}
