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

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.applib.services.bookmark.HmacAuthority;
import org.apache.causeway.applib.services.urlencoding.UrlEncodingService;

/**
 * Thread-safe coder/decoder with digital signing and validity verification support.
 *
 * <p> can be used as an application scoped singleton
 *
 * @since 3.5
 */
public record HmacUrlCodec(
        HmacAuthority hmacAuthority,
        UrlEncodingService urlSafeCodec) {

    public HmacUrlCodec {
        Objects.requireNonNull(hmacAuthority);
        Objects.requireNonNull(urlSafeCodec);
    }

    public String encodeForUrl(final @NonNull byte[] byteArray) {
        var digitallySignedPayload = HmacUtils.digitallySign(hmacAuthority, byteArray);
        return urlSafeCodec.encode(digitallySignedPayload);
    }

    public Optional<byte[]> decodeFromUrl(final @NonNull String untrustedUrlEncodedString) {
        var trustedBytes = HmacUtils.verify(hmacAuthority, urlSafeCodec.decode(untrustedUrlEncodedString));
        return Optional.ofNullable(trustedBytes);
    }

    // -- STRING SUPPORT

    public String encodeForUrlAsUtf8(final @NonNull String string) {
        return encodeForUrl(string.getBytes(StandardCharsets.UTF_8));
    }

    public Optional<String> decodeFromUrlAsUtf8(final @NonNull String untrustedUrlEncodedString) {
        return decodeFromUrl(untrustedUrlEncodedString)
            .map(trustedBytes->new String(trustedBytes, StandardCharsets.UTF_8));
    }

}
