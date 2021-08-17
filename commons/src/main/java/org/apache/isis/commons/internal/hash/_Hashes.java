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

package org.apache.isis.commons.internal.hash;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import org.checkerframework.checker.nullness.qual.Nullable;

import org.apache.isis.commons.internal.base._NullSafe;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
public final class _Hashes {

    @RequiredArgsConstructor
    public static enum Algorithm {
        MD5("MD5"),
        SHA1("SHA"),
        SHA224("SHA-224"),
        SHA256("SHA-256"),
        SHA384("SHA-384"),
        SHA512("SHA-512"),
        ;
        private final String algorithmName;

        public Optional<MessageDigest> getMessageDigest() {
            try {
                return Optional.ofNullable(MessageDigest.getInstance(algorithmName));
            } catch (NoSuchAlgorithmException e) {
                return Optional.empty();
            }
        }

    }

    public static Optional<byte[]> digest(@NonNull Algorithm algorithm, @Nullable byte[] bytes) {
        final int size = _NullSafe.size(bytes);
        if(size==0) {
            return Optional.empty();
        }
        return algorithm.getMessageDigest()
        .map(md->digest(md, new ByteArrayInputStream(bytes), Math.max(size, 4096)));
    }


    // -- HELPER

    @SneakyThrows
    private static byte[] digest(@NonNull MessageDigest md, @NonNull InputStream inputStream, int buffersize) {
        md.reset();
        try(DigestInputStream dis = new DigestInputStream(inputStream, md)){
            byte[] buffer = new byte[buffersize];
            while(dis.read(buffer)>0);
        }
        return md.digest();
    }


}
