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

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebugDataInputExtended extends DataInputExtendedDecorator {

    private static final Logger LOG = LoggerFactory.getLogger(DebugDataInputExtended.class);

    public DebugDataInputExtended(final DataInputExtended input) {
        super(input);
    }

    @Override
    public boolean readBoolean() throws IOException {
        final boolean b = super.readBoolean();
        if (LOG.isDebugEnabled()) {
            LOG.debug("boolean: " + b);
        }
        return b;
    }

    @Override
    public byte readByte() throws IOException {
        final byte b = super.readByte();
        if (LOG.isDebugEnabled()) {
            LOG.debug("byte: " + b);
        }
        return b;
    }

    @Override
    public byte[] readBytes() throws IOException {
        final byte[] bs = super.readBytes();
        if (LOG.isDebugEnabled()) {
            LOG.debug("bytes: " + new String(bs));
        }
        return bs;
    }

    @Override
    public int readInt() throws IOException {
        final int i = super.readInt();
        if (LOG.isDebugEnabled()) {
            LOG.debug("int: " + i);
        }
        return i;
    }

    @Override
    public long readLong() throws IOException {
        final long l = super.readLong();
        if (LOG.isDebugEnabled()) {
            LOG.debug("long: " + l);
        }
        return l;
    }

    @Override
    public String readUTF() throws IOException {
        final String string = super.readUTF();
        if (LOG.isDebugEnabled()) {
            LOG.debug("string: " + string);
        }
        return string;
    }

    @Override
    public String[] readUTFs() throws IOException {
        final String[] strings = super.readUTFs();
        if (LOG.isDebugEnabled()) {
            LOG.debug("list: " + Arrays.toString(strings));
        }
        return strings;
    }

    @Override
    public <T> T readEncodable(final Class<T> encodableType) throws IOException {
        final T object = super.readEncodable(encodableType);
        if (LOG.isDebugEnabled()) {
            LOG.debug(">>> object");
        }
        return object;
    }

    @Override
    public <T> T[] readEncodables(final Class<T> encodableType) throws IOException {
        final T[] objects = super.readEncodables(encodableType);
        if (LOG.isDebugEnabled()) {
            LOG.debug(">>> objects x" + objects.length);
        }
        return objects;
    }

}
