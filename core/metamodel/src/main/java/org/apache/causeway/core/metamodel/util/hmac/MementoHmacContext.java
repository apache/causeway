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
package org.apache.causeway.core.metamodel.util.hmac;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.exceptions.unrecoverable.DigitalVerificationException;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.metamodel.valuesemantics.ValueCodec;

public record MementoHmacContext(
    HmacUrlCodec hmacUrlCodec,
    ValueCodec valueCodec) {

    public MementoHmacContext {
        Objects.requireNonNull(hmacUrlCodec);
        Objects.requireNonNull(valueCodec);
    }

    public Memento newMemento() {
        return new SecureMemento(this);
    }

    public Memento parseTrustedMemento(final byte[] trustedInput) {
        return SecureMemento.parseTrustedMemento(this, trustedInput);
    }

    public Memento parseDigitallySignedMemento(final String untrustedInput) {
        return SecureMemento.parseDigitallySignedMemento(this, untrustedInput);
    }

    public Memento parseMemento(final @Nullable Bookmark untrustedBookmark) {
        if(untrustedBookmark==null) throw new DigitalVerificationException("invalid memento data");
        return parseDigitallySignedMemento(untrustedBookmark.identifier());
    }

}