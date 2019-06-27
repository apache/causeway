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

package org.apache.isis.core.commons.encoding;

import java.io.DataInputStream;
import java.io.IOException;

import org.apache.isis.commons.internal.encoding.DataInputExtended;

public class DataInputExtendedDecorator implements DataInputExtended {

    private final DataInputExtended underlying;

    public DataInputExtendedDecorator(final DataInputExtended underlying) {
        this.underlying = underlying;
    }

    @Override
    public DataInputStream getDataInputStream() {
        return underlying.getDataInputStream();
    }

    // ////////////////////////////////////////
    // Boolean, Char
    // ////////////////////////////////////////

    @Override
    public boolean readBoolean() throws IOException {
        return underlying.readBoolean();
    }

    @Override
    public boolean[] readBooleans() throws IOException {
        return underlying.readBooleans();
    }

    @Override
    public char readChar() throws IOException {
        return underlying.readChar();
    }

    @Override
    public char[] readChars() throws IOException {
        return underlying.readChars();
    }

    // ////////////////////////////////////////
    // Integral Numbers
    // ////////////////////////////////////////

    @Override
    public byte readByte() throws IOException {
        return underlying.readByte();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return underlying.readUnsignedByte();
    }

    @Override
    public byte[] readBytes() throws IOException {
        return underlying.readBytes();
    }

    @Override
    public short readShort() throws IOException {
        return underlying.readShort();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return underlying.readUnsignedShort();
    }

    @Override
    public short[] readShorts() throws IOException {
        return underlying.readShorts();
    }

    @Override
    public int readInt() throws IOException {
        return underlying.readInt();
    }

    @Override
    public int[] readInts() throws IOException {
        return underlying.readInts();
    }

    @Override
    public long[] readLongs() throws IOException {
        return underlying.readLongs();
    }

    @Override
    public long readLong() throws IOException {
        return underlying.readLong();
    }

    // ////////////////////////////////////////
    // Floating Point Numbers
    // ////////////////////////////////////////

    @Override
    public float readFloat() throws IOException {
        return underlying.readFloat();
    }

    @Override
    public float[] readFloats() throws IOException {
        return underlying.readFloats();
    }

    @Override
    public double readDouble() throws IOException {
        return underlying.readDouble();
    }

    @Override
    public double[] readDoubles() throws IOException {
        return underlying.readDoubles();
    }

    // ////////////////////////////////////////
    // Strings
    // ////////////////////////////////////////

    @Override
    public String readUTF() throws IOException {
        return underlying.readUTF();
    }

    @Override
    public String[] readUTFs() throws IOException {
        return underlying.readUTFs();
    }

    // ////////////////////////////////////////
    // Encodable and Serializable
    // ////////////////////////////////////////

    @Override
    public <T> T readEncodable(final Class<T> encodableType) throws IOException {
        return underlying.readEncodable(encodableType);
    }

    @Override
    public <T> T[] readEncodables(final Class<T> encodableType) throws IOException {
        return underlying.readEncodables(encodableType);
    }

    @Override
    public <T> T readSerializable(final Class<T> serializableType) throws IOException {
        return underlying.readSerializable(serializableType);
    }

    @Override
    public <T> T[] readSerializables(final Class<T> serializableType) throws IOException {
        return underlying.readSerializables(serializableType);
    }

    // ////////////////////////////////////////
    // Other
    // ////////////////////////////////////////

    @Override
    public void readFully(final byte[] b) throws IOException {
        underlying.readFully(b);
    }

    @Override
    public void readFully(final byte[] b, final int off, final int len) throws IOException {
        underlying.readFully(b, off, len);
    }

    @Override
    public String readLine() throws IOException {
        return underlying.readLine();
    }

    @Override
    public int skipBytes(final int n) throws IOException {
        return underlying.skipBytes(n);
    }


}
