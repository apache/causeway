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

import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.isis.commons.internal.encoding.DataOutputExtended;

public class DataOutputExtendedDecorator implements DataOutputExtended {

    private final DataOutputExtended underlying;

    public DataOutputExtendedDecorator(final DataOutputExtended underlying) {
        this.underlying = underlying;
    }

    @Override
    public DataOutputStream getDataOutputStream() {
        return underlying.getDataOutputStream();
    }

    // ////////////////////////////////////////
    // Boolean, Char
    // ////////////////////////////////////////

    @Override
    public void writeBoolean(final boolean v) throws IOException {
        underlying.writeBoolean(v);
    }

    @Override
    public void writeBooleans(final boolean[] booleans) throws IOException {
        underlying.writeBooleans(booleans);
    }

    @Override
    public void writeChar(final int v) throws IOException {
        underlying.writeChar(v);
    }

    @Override
    public void writeChars(final char[] chars) throws IOException {
        underlying.writeChars(chars);
    }

    // ////////////////////////////////////////
    // Integral Numbers
    // ////////////////////////////////////////

    @Override
    public void write(final int b) throws IOException {
        underlying.write(b);
    }

    @Override
    public void writeByte(final int v) throws IOException {
        underlying.writeByte(v);
    }

    @Override
    public void write(final byte[] b) throws IOException {
        underlying.write(b);
    }

    @Override
    public void writeBytes(final byte[] bytes) throws IOException {
        underlying.writeBytes(bytes);
    }

    @Override
    public void writeShort(final int v) throws IOException {
        underlying.writeShort(v);
    }

    @Override
    public void writeShorts(final short[] shorts) throws IOException {
        underlying.writeShorts(shorts);
    }

    @Override
    public void writeInt(final int v) throws IOException {
        underlying.writeInt(v);
    }

    @Override
    public void writeInts(final int[] ints) throws IOException {
        underlying.writeInts(ints);
    }

    @Override
    public void writeLong(final long v) throws IOException {
        underlying.writeLong(v);
    }

    @Override
    public void writeLongs(final long[] longs) throws IOException {
        underlying.writeLongs(longs);
    }

    // ////////////////////////////////////////
    // Floating Point Numbers
    // ////////////////////////////////////////

    @Override
    public void writeFloat(final float v) throws IOException {
        underlying.writeFloat(v);
    }

    @Override
    public void writeFloats(final float[] floats) throws IOException {
        underlying.writeFloats(floats);
    }

    @Override
    public void writeDouble(final double v) throws IOException {
        underlying.writeDouble(v);
    }

    @Override
    public void writeDoubles(final double[] doubles) throws IOException {
        underlying.writeDoubles(doubles);
    }

    // ////////////////////////////////////////
    // Strings
    // ////////////////////////////////////////

    @Override
    public void writeUTF(final String str) throws IOException {
        underlying.writeUTF(str);
    }

    @Override
    public void writeUTFs(final String[] strings) throws IOException {
        underlying.writeUTFs(strings);
    }

    // ////////////////////////////////////////
    // Encodable and Serializable
    // ////////////////////////////////////////

    @Override
    public void writeEncodable(final Object encodable) throws IOException {
        underlying.writeEncodable(encodable);
    }

    @Override
    public void writeEncodables(final Object[] encodables) throws IOException {
        underlying.writeEncodables(encodables);
    }

    @Override
    public void writeSerializable(final Object serializable) throws IOException {
        underlying.writeSerializable(serializable);
    }

    @Override
    public void writeSerializables(final Object[] serializables) throws IOException {
        underlying.writeSerializables(serializables);
    }

    // ////////////////////////////////////////
    // Other
    // ////////////////////////////////////////

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        underlying.write(b, off, len);
    }

    @Override
    public void writeBytes(final String s) throws IOException {
        underlying.writeBytes(s);
    }

    @Override
    public void writeChars(final String s) throws IOException {
        underlying.writeChars(s);
    }

    // ////////////////////////////////////////
    // Flush
    // ////////////////////////////////////////

    @Override
    public void flush() throws IOException {
        underlying.flush();
    }

}
