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

import java.io.IOException;
import java.util.Arrays;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class DebugDataInputExtended extends DataInputExtendedDecorator {

    public DebugDataInputExtended(final DataInputExtended input) {
        super(input);
    }

    @Override
    public boolean readBoolean() throws IOException {
        final boolean b = super.readBoolean();
        log.debug("boolean: {}", b);
        return b;
    }

    @Override
    public byte readByte() throws IOException {
        final byte b = super.readByte();
        log.debug("byte: {}", b);
        return b;
    }

    @Override
    public byte[] readBytes() throws IOException {
        final byte[] bs = super.readBytes();
        if (log.isDebugEnabled()) {
            log.debug("bytes: {}", new String(bs));
        }
        return bs;
    }

    @Override
    public int readInt() throws IOException {
        final int i = super.readInt();
        log.debug("int: {}", i);
        return i;
    }

    @Override
    public long readLong() throws IOException {
        final long l = super.readLong();
        log.debug("long: {}", l);
        return l;
    }

    @Override
    public String readUTF() throws IOException {
        final String string = super.readUTF();
        log.debug("string: {}", string);
        return string;
    }

    @Override
    public String[] readUTFs() throws IOException {
        final String[] strings = super.readUTFs();
        if (log.isDebugEnabled()) {
            log.debug("list: {}", Arrays.toString(strings));
        }
        return strings;
    }

    @Override
    public <T> T readEncodable(final Class<T> encodableType) throws IOException {
        final T object = super.readEncodable(encodableType);
        if (log.isDebugEnabled()) {
            log.debug(">>> object");
        }
        return object;
    }

    @Override
    public <T> T[] readEncodables(final Class<T> encodableType) throws IOException {
        final T[] objects = super.readEncodables(encodableType);
        log.debug(">>> objects x{}", objects.length);
        return objects;
    }

}
