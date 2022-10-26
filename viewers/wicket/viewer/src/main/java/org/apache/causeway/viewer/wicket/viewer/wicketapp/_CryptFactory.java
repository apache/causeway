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
package org.apache.causeway.viewer.wicket.viewer.wicketapp;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Random;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.wicket.core.random.DefaultSecureRandomSupplier;
import org.apache.wicket.core.random.ISecureRandomSupplier;
import org.apache.wicket.core.util.crypt.AESCrypt;
import org.apache.wicket.util.crypt.ICrypt;
import org.apache.wicket.util.crypt.NoCrypt;
import org.apache.wicket.util.crypt.SunJceCrypt;

import org.apache.causeway.commons.internal.os._OsUtil;

import lombok.SneakyThrows;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
class _CryptFactory {

    static final String FIXED_SALT_FOR_PROTOTYPING = "PrototypingEncryptionKey";

    ICrypt sunJceCrypt(final String encryptionKey) {
        final byte[] salt = getSalt(8, encryptionKey);
        val crypt = new SunJceCrypt(salt, 1000);
        crypt.setKey(encryptionKey);
        return crypt;
    }

    @SneakyThrows
    ICrypt aesCrypt(final String encryptionKey) {

        final byte[] salt = getSalt(8, encryptionKey);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(encryptionKey.toCharArray(), salt, 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

        ISecureRandomSupplier rGen = new DefaultSecureRandomSupplier();

        return new AESCrypt(secret, rGen);
    }

    ICrypt noCrypt(final String encryptionKey) {
        return new NoCrypt();
    }

    //XXX what about BCryptPasswordEncoder (Spring);

    // -- HELPER

    /**
     * @param size
     *      must be 8 bytes - for anything else PBES1Core throws
     *      InvalidAlgorithmParameterException: Salt must be 8 bytes long
     */
    private byte[] getSalt(final int size, final String encryptionKey) {
        final byte[] salt = FIXED_SALT_FOR_PROTOTYPING.equals(encryptionKey)
                ? machineFixedSalt(size)
                : secureSalt(size);
        return salt;
    }

    /**
     * Cloned from {@link SunJceCrypt#randomSalt()},
     * but using {@link SecureRandom} instead of {@link Random}.
     */
    private byte[] secureSalt(final int size) {
        val salt = new byte[size];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    private byte[] machineFixedSalt(final int size) {
        val machineFixedSeed = _OsUtil.machineId();
        if(machineFixedSeed.isEmpty()){
            return secureSalt(size);
        }
        val salt = new byte[size];
        /*sonar-ignore-on*/
        new Random(machineFixedSeed.getAsLong()).nextBytes(salt); // not required to be secure
        /*sonar-ignore-off*/
        return salt;
    }

}
