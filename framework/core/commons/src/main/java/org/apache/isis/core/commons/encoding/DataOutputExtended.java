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

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.Flushable;
import java.io.IOException;

public interface DataOutputExtended extends DataOutput, Flushable {

    public void writeBooleans(boolean[] booleans) throws IOException;

    public void writeChars(char[] chars) throws IOException;

    /**
     * NB: only writes out <tt>byte</tt>.
     */
    @Override
    public void write(int b) throws IOException;

    /**
     * Same as {@link #write(int)}.
     */
    @Override
    public void writeByte(int v) throws IOException;

    public void writeBytes(byte[] bytes) throws IOException;

    public void writeShorts(short[] shorts) throws IOException;

    public void writeInts(int[] ints) throws IOException;

    public void writeLongs(long[] longs) throws IOException;

    public void writeDoubles(double[] doubles) throws IOException;

    public void writeFloats(float[] floats) throws IOException;

    public void writeUTFs(String[] strings) throws IOException;

    public void writeEncodable(Object encodable) throws IOException;

    public void writeEncodables(Object[] encodables) throws IOException;

    public void writeSerializable(Object serializable) throws IOException;

    public void writeSerializables(Object[] serializables) throws IOException;

    DataOutputStream getDataOutputStream();
}
