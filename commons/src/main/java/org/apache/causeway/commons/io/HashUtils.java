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
package org.apache.causeway.commons.io;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.functional.ThrowingSupplier;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._Bytes;
import org.apache.causeway.commons.internal.base._NullSafe;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;

/**
 * Utilities related to byte data hashing algorithms, at least providing MD5.
 * <p>
 * Consider <a href="https://commons.apache.org/codec/">Apache Commons Codec</a>
 * for a more comprehensive suite of digest utilities.
 *
 * @since 2.0 {@index}
 */
@UtilityClass
public class HashUtils {

    //XXX record candidate
    @Value @Accessors(fluent=true)
    public static class Hash {
        private final String algorithmName;
        private final byte[] bytes;

        public byte[] bytes() {
            // defensive copy
            return bytes!=null
                    ? bytes.clone()
                    : null;
        }

        public String asHexString() {
            return _Bytes.hexDump(bytes, "");
        }
    }

    @RequiredArgsConstructor
    public static enum HashAlgorithm {
        MD5(()->MessageDigest.getInstance("MD5")),
        SHA1(()->MessageDigest.getInstance("SHA-1")),
        SHA256(()->MessageDigest.getInstance("SHA-256"))
        ;

        @Getter(value = AccessLevel.PRIVATE)
        private final ThrowingSupplier<MessageDigest> messageDigestSupplier;

        public Try<MessageDigest> tryGetMessageDigest(){
            return Try.call(getMessageDigestSupplier()::get);
        }
    }

    /**
     * Optimized for when the input byte array is already present in memory.
     */
    public Try<Hash> tryDigest(
            final @NonNull HashAlgorithm algorithm,
            final @Nullable byte[] bytes,
            final int buffersize) {
        return tryDigestAsBytes(algorithm, bytes, buffersize)
                .mapSuccessAsNullable(digestBytes -> new Hash(algorithm.name(), digestBytes));
    }

    /**
     * Optimized for direct {@link InputStream} processing,
     * if possible, not reading all data into memory at once.
     */
    public Try<Hash> tryDigest(
            final @NonNull HashAlgorithm algorithm,
            final @NonNull DataSource dataSource,
            final int buffersize) {
        return tryDigestAsBytes(algorithm, dataSource, buffersize)
                .mapSuccessAsNullable(digestBytes -> new Hash(algorithm.name(), digestBytes));
    }

    // -- HELPER

    /**
     * Optimized for when the input byte array is already present in memory.
     */
    private Try<byte[]> tryDigestAsBytes(
            final HashAlgorithm algorithm,
            final byte[] bytes,
            final int buffersize) {

        return Try.call(()->{
            if(_NullSafe.isEmpty(bytes)) {
                return bytes;
            }
            var messageDigest = algorithm.tryGetMessageDigest().valueAsNonNullElseFail();
            try(DigestInputStream dis = new DigestInputStream(new ByteArrayInputStream(bytes), messageDigest)){
                byte[] buffer = new byte[buffersize];
                while(dis.read(buffer)>0);
            }
            var digestBytes = messageDigest.digest();
            return digestBytes;
        });
    }

    /**
     * Optimized for direct {@link InputStream} processing,
     * if possible, not reading all data into memory at once.
     */
    private Try<byte[]> tryDigestAsBytes(
            final HashAlgorithm algorithm,
            final DataSource dataSource,
            final int buffersize) {

        return Try.call(()->{
            var messageDigest = algorithm.tryGetMessageDigest().valueAsNonNullElseFail();
            var digestBytes = dataSource.tryReadAndApply(inputStream->{
                if(inputStream==null) {
                    return null;
                }
                try(DigestInputStream dis = new DigestInputStream(inputStream, messageDigest)){
                    byte[] buffer = new byte[buffersize];
                    while(dis.read(buffer)>0);
                }
                return messageDigest.digest();
            })
            .valueAsNullableElseFail();
            return digestBytes; // null-able
        });
    }

}
