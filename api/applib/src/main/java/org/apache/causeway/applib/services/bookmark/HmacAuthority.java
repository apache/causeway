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
package org.apache.causeway.applib.services.bookmark;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.jspecify.annotations.Nullable;

import lombok.SneakyThrows;

/**
 * Can be registered with Spring, to override the build in default, which has an application scoped random secret.
 *
 * <pre>
 * {@code @Configuration}
 * class EnableHmacAuthority {
 *     {@code @Bean}
 *     public HmacAuthority hmacAuthority() {
 *         return HmacAuthority.HmacSHA256.randomInstance();
 *     }
 * }
 * </pre>
 *
 * <p>Note that bookmark's validity is bound to the (server-side) secret key of the {@link HmacAuthority}.
 * Once the secret changes, bookmarks that are stored client-side for later use, will be rendered invalid.
 *
 * @apiNote the default bean is auto-configured with 'CausewayModuleCoreRuntimeServices.HmacAutorityAutoconfigure'
 *
 * @since 3.5
 */
public interface HmacAuthority {

    /**
     * HMAC as byte array, for given input data.
     */
    byte[] generateHmac(byte[] data);

    /**
     * Verifies that given dataToVerify when passed to {@link #generateHmac(byte[])} yields given hmacToVerify.
     *
     * <p>If any of the arguments is null returns false.
     *
     * <p>If hmacToVerify does not conform with {@link #isValidHmacLength(int)} returns false.
     */
    default boolean verifyHmac(final @Nullable byte[] data, final @Nullable byte[] hmacToVerify) {
        if(data == null || hmacToVerify == null) return false; // invalid by definition
        if(!isValidHmacLength(hmacToVerify.length)) return false; // shortcut
        return Arrays.equals(generateHmac(data), hmacToVerify);
    }

    /**
     * Whether HMAC length in bytes is expected/valid.
     */
    boolean isValidHmacLength(final int hmacLength);

    // -- IMPL

    record HmacSHA256(
        SecretKeySpec secretKey) implements HmacAuthority {

        private final static String ALGORITHM = "HmacSHA256";

        public HmacSHA256(final byte[] secret) {
            this(new SecretKeySpec(secret, ALGORITHM));
        }

        @SneakyThrows
        public static HmacSHA256 randomInstance() {
            var secret = new byte[32]; // double the minimum requirement of 16
            SecureRandom.getInstanceStrong().nextBytes(secret);
            return new HmacSHA256(secret);
        }

        @Override
        public byte[] generateHmac(final byte[] data) {
            Objects.requireNonNull(data);
            var mac = newMac();
            return mac.doFinal(data);
        }

        @Override
        public boolean isValidHmacLength(final int hmacLength) {
            return 32 == hmacLength;
        }

        // -- HELPER

        @SneakyThrows
        private Mac newMac() {
            var mac = Mac.getInstance(ALGORITHM);
            mac.init(secretKey);
            return mac;
        }
    }

    // JUNIT SUPPORT

    static HmacAuthority forTesting() {
        return new HmacSHA256("secret for testing onyl".getBytes());
    }
}
