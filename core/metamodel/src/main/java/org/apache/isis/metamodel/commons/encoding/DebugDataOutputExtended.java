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

package org.apache.isis.metamodel.commons.encoding;

import java.io.IOException;

import org.apache.isis.commons.internal.encoding.DataOutputExtended;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class DebugDataOutputExtended extends DataOutputExtendedDecorator {

    public DebugDataOutputExtended(final DataOutputExtended underlying) {
        super(underlying);
    }

    @Override
    public void writeBoolean(final boolean flag) throws IOException {
        log.debug("boolean: {}", flag);
        super.writeBoolean(flag);
    }

    @Override
    public void writeBytes(final byte[] value) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("bytes: ({}) {}", value.length, new String(value));
        }
        super.writeBytes(value);
    }

    @Override
    public void writeByte(final int value) throws IOException {
        log.debug("byte: {}", value);
        super.writeByte(value);
    }

    @Override
    public void writeInt(final int value) throws IOException {
        log.debug("int: {}", value);
        super.writeInt(value);
    }

    @Override
    public void writeLong(final long value) throws IOException {
        log.debug("long: {}", value);
        super.writeLong(value);
    }

    @Override
    public void writeEncodable(final Object object) throws IOException {
        log.debug(">>> object: ({})", object);
        super.writeEncodable(object);
    }

    @Override
    public void writeEncodables(final Object[] objects) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug(">>> objects x{}", objects.length);
        }
        super.writeEncodables(objects);
    }

    @Override
    public void writeUTF(final String str) throws IOException {
        log.debug("string: {}", str);
        super.writeUTF(str);
    }

    @Override
    public void writeUTFs(final String[] strings) throws IOException {
        if (log.isDebugEnabled()) {
            final StringBuffer l = new StringBuffer();
            for (int i = 0; i < strings.length; i++) {
                if (i > 0) {
                    l.append(", ");
                }
                l.append(strings[i]);
            }
            log.debug("list: {}", l);
        }
        super.writeUTFs(strings);
    }

}
