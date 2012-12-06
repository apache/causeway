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

package org.apache.isis.core.commons.io;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

import java.io.IOException;
import java.io.InputStream;

import org.apache.isis.core.commons.ensure.Ensure;

/**
 * An input stream that reads from an underlying {@link InputStream}, deferring
 * the interactions until needed.
 * 
 * <p>
 * This other stream is provided as needed by an {@link InputStreamProvider} so
 * that the underlying stream is not eagerly loaded.
 */
public class LazyInputStream extends InputStream {

    /**
     * An interface to be implemented by clients that wish to utilize
     * {@link LazyInputStream}s. The implementation of this interface should
     * defer obtaining the desired input stream until absolutely necessary.
     */
    public static interface InputStreamProvider {
        InputStream getInputStream() throws IOException;
    }

    private final InputStreamProvider provider;

    private InputStream underlying = null;

    // ///////////////////////////////////////////////////////
    // Constructor
    // ///////////////////////////////////////////////////////

    /**
     * Construct a new lazy stream based off the given provider.
     * 
     * @param provider
     *            the input stream provider. Must not be <code>null</code>.
     */
    public LazyInputStream(final InputStreamProvider provider) {
        Ensure.ensureThatArg(provider, is(not(nullValue())));
        this.provider = provider;
    }

    // ///////////////////////////////////////////////////////
    // InputStream API
    // ///////////////////////////////////////////////////////

    @Override
    public void close() throws IOException {
        obtainUnderlyingIfRequired();
        underlying.close();
    }

    @Override
    public int available() throws IOException {
        obtainUnderlyingIfRequired();
        return underlying.available();
    }

    @Override
    public void mark(final int readlimit) {
        try {
            obtainUnderlyingIfRequired();
            underlying.mark(readlimit);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean markSupported() {
        try {
            obtainUnderlyingIfRequired();
            return underlying.markSupported();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int read() throws IOException {
        obtainUnderlyingIfRequired();
        return underlying.read();
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        obtainUnderlyingIfRequired();
        return underlying.read(b, off, len);
    }

    @Override
    public int read(final byte[] b) throws IOException {
        obtainUnderlyingIfRequired();
        return underlying.read(b);
    }

    @Override
    public long skip(final long n) throws IOException {
        obtainUnderlyingIfRequired();
        return underlying.skip(n);
    }

    @Override
    public void reset() throws IOException {
        obtainUnderlyingIfRequired();
        underlying.reset();
    }

    // ///////////////////////////////////////////////////////
    // helpers
    // ///////////////////////////////////////////////////////

    private void obtainUnderlyingIfRequired() throws IOException {
        if (underlying == null) {
            underlying = provider.getInputStream();
        }
    }

    // ///////////////////////////////////////////////////////
    // equals, hashCode
    // ///////////////////////////////////////////////////////

    @Override
    public boolean equals(final Object obj) {
        try {
            obtainUnderlyingIfRequired();
            return underlying.equals(obj);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int hashCode() {
        try {
            obtainUnderlyingIfRequired();
            return underlying.hashCode();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ///////////////////////////////////////////////////////
    // toString
    // ///////////////////////////////////////////////////////

    @Override
    public String toString() {
        try {
            obtainUnderlyingIfRequired();
            return underlying.toString();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

}
