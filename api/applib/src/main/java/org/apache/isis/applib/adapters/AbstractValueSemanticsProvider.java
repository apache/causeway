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

package org.apache.isis.applib.adapters;

public abstract class AbstractValueSemanticsProvider<T> implements ValueSemanticsProvider<T> {

    private boolean immutable;
    private boolean equalByContent;

    /**
     * Defaults {@link #isImmutable()} to <tt>true</tt> and
     * {@link #isEqualByContent()} to <tt>true</tt> also.
     */
    public AbstractValueSemanticsProvider() {
        this(true, true);
    }

    public AbstractValueSemanticsProvider(final boolean immutable, final boolean equalByContent) {
        this.immutable = immutable;
        this.equalByContent = equalByContent;
    }

    @SuppressWarnings("unchecked")
    @Override
    public EncoderDecoder<T> getEncoderDecoder() {
        return (EncoderDecoder<T>) (this instanceof EncoderDecoder ? this : null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Parser<T> getParser() {
        return (Parser<T>) (this instanceof Parser ? this : null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public DefaultsProvider<T> getDefaultsProvider() {
        return (DefaultsProvider<T>) (this instanceof DefaultsProvider ? this : null);
    }

    /**
     * Defaults to <tt>true</tt> if no-arg constructor is used.
     */
    @Override
    public boolean isEqualByContent() {
        return equalByContent;
    }

    /**
     * Defaults to <tt>true</tt> if no-arg constructor is used.
     */
    @Override
    public boolean isImmutable() {
        return immutable;
    }

}
