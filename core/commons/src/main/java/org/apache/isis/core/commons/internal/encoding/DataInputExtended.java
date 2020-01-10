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

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;

public interface DataInputExtended extends DataInput {

    public boolean[] readBooleans() throws IOException;

    public char[] readChars() throws IOException;

    public byte[] readBytes() throws IOException;

    public short[] readShorts() throws IOException;

    public int[] readInts() throws IOException;

    public long[] readLongs() throws IOException;

    public float[] readFloats() throws IOException;

    public double[] readDoubles() throws IOException;

    public String[] readUTFs() throws IOException;

    public <T> T readEncodable(Class<T> encodableType) throws IOException;

    public <T> T[] readEncodables(Class<T> elementType) throws IOException;

    public <T> T readSerializable(Class<T> serializableType) throws IOException;

    public <T> T[] readSerializables(Class<T> elementType) throws IOException;

    /**
     * Underlying {@link DataInputStream} to read in primitives.
     */
    DataInputStream getDataInputStream();
}
