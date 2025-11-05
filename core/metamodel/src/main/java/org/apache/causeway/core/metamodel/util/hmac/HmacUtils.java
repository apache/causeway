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

import java.nio.ByteBuffer;
import java.util.Objects;

import org.springframework.util.Assert;

import org.apache.causeway.applib.services.bookmark.HmacAuthority;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * @since 3.5
 */
@UtilityClass
@Slf4j
public class HmacUtils {

    /**
     * Returns an output byte array, as a concatenation of:<br>
     * (1) the HMAC length as 2 byte short value<br>
     * (2) HMAC as byte array<br>
     * (3) input data as byte array<br>
     */
    public byte[] digitallySign(final HmacAuthority hmacAuthority, final byte[] data) {
        Objects.requireNonNull(hmacAuthority);
        Objects.requireNonNull(data);

        var hmac = hmacAuthority.generateHmac(data);

        // safety guard on the cast to short
        Assert.isTrue(hmac.length<=Short.MAX_VALUE, ()->"unexpected HMAC length encountered: " + hmac.length);
        short hmacLengthAsShort = (short) hmac.length;

        var buf = ByteBuffer.allocate(2 + hmac.length + data.length);

        var digitallySignedPayload = buf
            .putShort(hmacLengthAsShort)
            .put(hmac)
            .put(data)
            .array();

        return digitallySignedPayload;
    }

    /**
     * Interprets an input byte array containing:<br>
     * (1) the HMAC length as 2 byte short value<br>
     * (2) HMAC as byte array<br>
     * (3) actual data as byte array<br>
     * then returns the actual data if it can be verified against given {@link HmacAuthority}, otherwise returns null.
     */
    public byte[] verify(final HmacAuthority hmacAuthority, final byte[] untrustedPayload) {
        Objects.requireNonNull(hmacAuthority);
        Objects.requireNonNull(untrustedPayload);

        var buf = ByteBuffer.wrap(untrustedPayload);

        int hmacLength = buf.getShort();

        if(!hmacAuthority.isValidHmacLength(hmacLength)) return null; // invalid HMAC length encountered in untrusted payload

        var hmacToVerify = new byte[hmacLength];
        buf.get(hmacToVerify);

        var data = new byte[untrustedPayload.length - hmacLength - 2];
        buf.get(data);

        if(!hmacAuthority.verifyHmac(data, hmacToVerify)) {
            log.info("digital verification failed (HMAC either expired or forgery detected)");
            return null; // validation failed
        }

        return data;
    }

}
