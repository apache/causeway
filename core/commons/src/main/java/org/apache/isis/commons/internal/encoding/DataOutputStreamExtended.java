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

package org.apache.isis.commons.internal.encoding;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

public class DataOutputStreamExtended implements DataOutputExtended {

    private final DataOutputStream dataOutputStream;

    public DataOutputStreamExtended(final OutputStream output) {
        dataOutputStream = new DataOutputStream(output);
    }

    @Override
    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    // ////////////////////////////////////////
    // Boolean, Char
    // ////////////////////////////////////////

    @Override
    public void writeBoolean(final boolean value) throws IOException {
        FieldType.BOOLEAN.write(this, value);
    }

    @Override
    public void writeBooleans(final boolean[] value) throws IOException {
        FieldType.BOOLEAN_ARRAY.write(this, value);
    }

    @Override
    public void writeChar(final int value) throws IOException {
        FieldType.CHAR.write(this, (char) value);
    }

    @Override
    public void writeChars(final char[] value) throws IOException {
        FieldType.CHAR_ARRAY.write(this, value);
    }


    // ////////////////////////////////////////
    // Integral Numbers
    // ////////////////////////////////////////

    @Override
    public void write(final int value) throws IOException {
        writeByte((byte) value);
    }

    @Override
    public void writeByte(final int value) throws IOException {
        FieldType.BYTE.write(this, (byte) value);
    }

    @Override
    public void write(final byte[] value) throws IOException {
        writeBytes(value);
    }

    @Override
    public void writeBytes(final byte[] value) throws IOException {
        FieldType.BYTE_ARRAY.write(this, value);
    }

    @Override
    public void writeShort(final int value) throws IOException {
        FieldType.SHORT.write(this, (short) value);
    }

    @Override
    public void writeShorts(final short[] value) throws IOException {
        FieldType.SHORT_ARRAY.write(this, value);
    }

    @Override
    public void writeInt(final int value) throws IOException {
        FieldType.INTEGER.write(this, value);
    }

    @Override
    public void writeInts(final int[] value) throws IOException {
        FieldType.INTEGER_ARRAY.write(this, value);
    }

    @Override
    public void writeLong(final long value) throws IOException {
        FieldType.LONG.write(this, value);
    }

    @Override
    public void writeLongs(final long[] value) throws IOException {
        FieldType.LONG_ARRAY.write(this, value);
    }

    // ////////////////////////////////////////
    // Floating Point Numbers
    // ////////////////////////////////////////

    @Override
    public void writeFloat(final float value) throws IOException {
        FieldType.FLOAT.write(this, value);
    }

    @Override
    public void writeFloats(final float[] value) throws IOException {
        FieldType.FLOAT_ARRAY.write(this, value);
    }

    @Override
    public void writeDouble(final double value) throws IOException {
        FieldType.DOUBLE.write(this, value);
    }

    @Override
    public void writeDoubles(final double[] value) throws IOException {
        FieldType.DOUBLE_ARRAY.write(this, value);
    }

    // ////////////////////////////////////////
    // Strings
    // ////////////////////////////////////////

    @Override
    public void writeUTF(final String value) throws IOException {
        FieldType.STRING.write(this, value);
    }

    @Override
    public void writeUTFs(final String[] value) throws IOException {
        FieldType.STRING_ARRAY.write(this, value);
    }

    // ////////////////////////////////////////
    // Encodable and Serializable
    // ////////////////////////////////////////

    @Override
    public void writeEncodable(final Object encodable) throws IOException {
        FieldType.ENCODABLE.write(this, (Encodable) encodable);
    }

    @Override
    public void writeEncodables(final Object[] objects) throws IOException {
        Encodable[] encodables;
        if (objects == null) {
            encodables = null;
        } else {
            encodables = new Encodable[objects.length];
            for (int i = 0; i < encodables.length; i++) {
                encodables[i] = (Encodable) objects[i];
            }
        }
        FieldType.ENCODABLE_ARRAY.write(this, encodables);
    }

    @Override
    public void writeSerializable(final Object serializable) throws IOException {
        FieldType.SERIALIZABLE.write(this, (Serializable) serializable);
    }

    @Override
    public void writeSerializables(final Object[] objects) throws IOException {
        Serializable[] serializeables;
        if (objects == null) {
            serializeables = null;
        } else {
            serializeables = new Serializable[objects.length];
            for (int i = 0; i < serializeables.length; i++) {
                serializeables[i] = (Serializable) objects[i];
            }
        }
        FieldType.SERIALIZABLE_ARRAY.write(this, serializeables);
    }

    // ////////////////////////////////////////
    // Other
    // ////////////////////////////////////////

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        dataOutputStream.write(b, off, len);
    }

    @Override
    public void writeBytes(final String str) throws IOException {
        dataOutputStream.writeBytes(str);
    }

    @Override
    public void writeChars(final String str) throws IOException {
        dataOutputStream.writeChars(str);
    }

    // ////////////////////////////////////////
    // Flushable
    // ////////////////////////////////////////

    @Override
    public void flush() throws IOException {
        dataOutputStream.flush();
    }

}
