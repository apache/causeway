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


package org.apache.isis.remoting.transport;

import java.io.IOException;
import java.io.InputStream;


/**
 * Collection of timing and quantity data for the stream. Note that as the reads block the clock does not
 * start until the first read has completed.
 */
public class TailingInputStream extends InputStream {
    private final InputStream wrapped;

    public TailingInputStream(final InputStream wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public int read() throws IOException {
        final int read = wrapped.read();
        System.out.println("byte " + read);
        return read;
    }

    @Override
    public int read(final byte[] b) throws IOException {
        final int read = wrapped.read(b);
        System.out.println("bytes (" + read + ")" + new String(b));
        return read;
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        final int read = wrapped.read(b, off, len);
        System.out.println("bytes (" + read + ")" + new String(b, off, len));
        return read;
    }

}

