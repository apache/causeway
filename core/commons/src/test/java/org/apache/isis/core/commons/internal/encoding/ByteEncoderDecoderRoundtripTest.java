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

package org.apache.isis.core.commons.internal.encoding;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class ByteEncoderDecoderRoundtripTest {

    private DataOutputStreamExtended outputImpl;
    private DataInputStreamExtended inputImpl;

    @Before
    public void setUp() throws Exception {
        final PipedInputStream pipedInputStream = new PipedInputStream();
        final PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);
        outputImpl = new DataOutputStreamExtended(pipedOutputStream);
        inputImpl = new DataInputStreamExtended(pipedInputStream);
    }

    @After
    public void tearDown() throws Exception {
        // does nothing yet
    }

    @Test
    public void encodeAndDecodeBoolean() throws IOException {
        outputImpl.writeBoolean(true);
        outputImpl.writeBoolean(false);

        assertThat(inputImpl.readBoolean(), is(equalTo(true)));
        assertThat(inputImpl.readBoolean(), is(equalTo(false)));
    }

    @Test
    public void encodeAndDecodeByteArray() throws IOException {
        final byte[] bytes = { 3, 76, -1, 21 };
        outputImpl.writeBytes(bytes);
        final byte[] decodedBytes = inputImpl.readBytes();

        assertThatArraysEqual(bytes, decodedBytes);
    }

    @Test
    public void encodeAndDecodeInt() throws IOException {
        outputImpl.writeInt(245);
        outputImpl.writeInt(-456);

        assertThat(inputImpl.readInt(), is(equalTo(245)));
        assertThat(inputImpl.readInt(), is(equalTo(-456)));
    }

    @Test
    public void encodeAndDecodeLong() throws IOException {
        outputImpl.writeLong(245L);
        outputImpl.writeLong(-456L);

        assertThat(inputImpl.readLong(), is(equalTo(245L)));
        assertThat(inputImpl.readLong(), is(equalTo(-456L)));
    }

    @Test
    public void encodeAndDecodeObject() throws IOException {
        outputImpl.writeEncodable(new EncodableObject());

        final Object object = inputImpl.readEncodable(Object.class);
        assertThat(object, is(instanceOf(EncodableObject.class)));

        final EncodableObject encodeableObject = (EncodableObject) object;
        assertThat(encodeableObject.field, is(equalTo("test field")));
    }

    @Test
    public void encodeAndDecodeNullObject() throws IOException {
        outputImpl.writeEncodable(null);

        final Object object = inputImpl.readEncodable(Object.class);
        assertThat(object, is(nullValue()));
    }

    @Test
    public void encodeAndDecodeObjectArray() throws IOException {
        final EncodableObject[] array = new EncodableObject[] { new EncodableObject(), new EncodableObject(), new EncodableObject() };
        outputImpl.writeEncodables(array);

        final Encodable[] objects = inputImpl.readEncodables(Encodable.class);
        assertThat(objects.length, is(3));
        final EncodableObject encodeableObject = (EncodableObject) objects[2];
        assertThat(encodeableObject.field, is(equalTo("test field")));
    }

    @Test
    public void encodeAndDecodeNullObjectArray() throws IOException {
        outputImpl.writeEncodables(null);

        final Encodable[] objects = inputImpl.readEncodables(Encodable.class);
        assertThat(objects, is(nullValue()));
    }

    @Test
    public void encodeAndDecodeString() throws IOException {
        outputImpl.writeUTF("test");
        outputImpl.writeUTF("");
        outputImpl.writeUTF("second");

        assertThat(inputImpl.readUTF(), is(equalTo("test")));
        assertThat(inputImpl.readUTF(), is(equalTo("")));
        assertThat(inputImpl.readUTF(), is(equalTo("second")));
    }

    @Test
    public void encodeAndDecodeStringArray() throws IOException {
        final String[] list = { "one", "two", "three" };
        outputImpl.writeUTFs(list);

        final String[] returnedList = inputImpl.readUTFs();

        assertThat(returnedList.length, is(equalTo(list.length)));
        assertThat(returnedList[0], is(equalTo(list[0])));
        assertThat(returnedList[1], is(equalTo(list[1])));
        assertThat(returnedList[2], is(equalTo(list[2])));
    }

    private void assertThatArraysEqual(final byte[] bytes, final byte[] decodedBytes) {
        assertThat(decodedBytes.length, is(equalTo(bytes.length)));
        for (int i = 0; i < bytes.length; i++) {
            assertThat(decodedBytes[i], is(equalTo(bytes[i])));
        }
    }

}

class EncodableObject implements Encodable {
    String field;

    public EncodableObject() {
    }

    public EncodableObject(final DataInputExtended input) throws IOException {
        field = input.readUTF();
    }

    @Override
    public void encode(final DataOutputExtended output) throws IOException {
        output.writeUTF("test field");
    }

}
