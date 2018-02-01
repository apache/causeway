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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebugDataOutputExtended extends DataOutputExtendedDecorator {

    private static final Logger LOG = LoggerFactory.getLogger(DebugDataOutputExtended.class);

    public DebugDataOutputExtended(final DataOutputExtended underlying) {
        super(underlying);
    }

    @Override
    public void writeBoolean(final boolean flag) throws IOException {
        LOG.debug("boolean: {}", flag);
        super.writeBoolean(flag);
    }

    @Override
    public void writeBytes(final byte[] value) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("bytes: ({}) {}", value.length, new String(value));
        }
        super.writeBytes(value);
    }

    @Override
    public void writeByte(final int value) throws IOException {
        LOG.debug("byte: {}", value);
        super.writeByte(value);
    }

    @Override
    public void writeInt(final int value) throws IOException {
        LOG.debug("int: {}", value);
        super.writeInt(value);
    }

    @Override
    public void writeLong(final long value) throws IOException {
        LOG.debug("long: {}", value);
        super.writeLong(value);
    }

    @Override
    public void writeEncodable(final Object object) throws IOException {
        LOG.debug(">>> object: ({})", object);
        super.writeEncodable(object);
    }

    @Override
    public void writeEncodables(final Object[] objects) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(">>> objects x{}", objects.length);
        }
        super.writeEncodables(objects);
    }

    @Override
    public void writeUTF(final String str) throws IOException {
        LOG.debug("string: {}", str);
        super.writeUTF(str);
    }

    @Override
    public void writeUTFs(final String[] strings) throws IOException {
        if (LOG.isDebugEnabled()) {
            final StringBuffer l = new StringBuffer();
            for (int i = 0; i < strings.length; i++) {
                if (i > 0) {
                    l.append(", ");
                }
                l.append(strings[i]);
            }
            LOG.debug("list: {}", l);
        }
        super.writeUTFs(strings);
    }

}
