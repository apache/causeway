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

package org.apache.isis.legacy.commons.internal.encoding;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DataInputStreamExtended implements DataInputExtended {

    private final DataInputStream dataInputStream;

    public DataInputStreamExtended(final InputStream inputStream) {
        this.dataInputStream = new DataInputStream(inputStream);
    }

    @Override
    public DataInputStream getDataInputStream() {
        return dataInputStream;
    }

    // ////////////////////////////////////////
    // Boolean, Char
    // ////////////////////////////////////////

    @Override
    public boolean readBoolean() throws IOException {
        return FieldType.BOOLEAN.read(this);
    }

    @Override
    public boolean[] readBooleans() throws IOException {
        return FieldType.BOOLEAN_ARRAY.read(this);
    }

    @Override
    public char readChar() throws IOException {
        return FieldType.CHAR.read(this);
    }

    @Override
    public char[] readChars() throws IOException {
        return FieldType.CHAR_ARRAY.read(this);
    }

    // ////////////////////////////////////////
    // Integral Numbers
    // ////////////////////////////////////////

    @Override
    public byte readByte() throws IOException {
        return FieldType.BYTE.read(this);
    }

    @Override
    public byte[] readBytes() throws IOException {
        return FieldType.BYTE_ARRAY.read(this);
    }

    @Override
    public short readShort() throws IOException {
        return FieldType.SHORT.read(this);
    }

    @Override
    public short[] readShorts() throws IOException {
        return FieldType.SHORT_ARRAY.read(this);
    }

    @Override
    public int readInt() throws IOException {
        return FieldType.INTEGER.read(this);
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return FieldType.UNSIGNED_BYTE.read(this);
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return FieldType.UNSIGNED_SHORT.read(this);
    }

    @Override
    public int[] readInts() throws IOException {
        return FieldType.INTEGER_ARRAY.read(this);
    }

    @Override
    public long readLong() throws IOException {
        return FieldType.LONG.read(this);
    }

    @Override
    public long[] readLongs() throws IOException {
        return FieldType.LONG_ARRAY.read(this);
    }

    // ////////////////////////////////////////
    // Floating Point Numbers
    // ////////////////////////////////////////

    @Override
    public float readFloat() throws IOException {
        return FieldType.FLOAT.read(this);
    }

    @Override
    public float[] readFloats() throws IOException {
        return FieldType.FLOAT_ARRAY.read(this);
    }

    @Override
    public double readDouble() throws IOException {
        return FieldType.DOUBLE.read(this);
    }

    @Override
    public double[] readDoubles() throws IOException {
        return FieldType.DOUBLE_ARRAY.read(this);
    }

    // ////////////////////////////////////////
    // Strings
    // ////////////////////////////////////////

    @Override
    public String readUTF() throws IOException {
        return FieldType.STRING.read(this);
    }

    @Override
    public String[] readUTFs() throws IOException {
        return FieldType.STRING_ARRAY.read(this);
    }

    // ////////////////////////////////////////
    // Encodable and Serializable
    // ////////////////////////////////////////

    @Override
    @SuppressWarnings("unchecked")
    public <T> T readEncodable(final Class<T> encodableType) throws IOException {
        return (T) FieldType.ENCODABLE.read(this);
    }

    @Override
    public <T> T[] readEncodables(final Class<T> elementType) throws IOException {
        return FieldType.ENCODABLE_ARRAY.readArray(this, elementType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T readSerializable(final Class<T> serializableType) throws IOException {
        return (T) FieldType.SERIALIZABLE.read(this);
    }

    @Override
    public <T> T[] readSerializables(final Class<T> elementType) throws IOException {
        return FieldType.SERIALIZABLE_ARRAY.readArray(this, elementType);
    }

    // ////////////////////////////////////////
    // Other
    // ////////////////////////////////////////

    @Override
    public void readFully(final byte[] b) throws IOException {
        dataInputStream.readFully(b);
    }

    @Override
    public void readFully(final byte[] b, final int off, final int len) throws IOException {
        dataInputStream.readFully(b, off, len);
    }

    @Override
    @SuppressWarnings("deprecation")
    public String readLine() throws IOException {
        return dataInputStream.readLine();
    }

    @Override
    public int skipBytes(final int n) throws IOException {
        return dataInputStream.skipBytes(n);
    }

}
