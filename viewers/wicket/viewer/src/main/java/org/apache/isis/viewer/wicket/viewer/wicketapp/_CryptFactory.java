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
package org.apache.isis.viewer.wicket.viewer.wicketapp;

import java.security.spec.KeySpec;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.wicket.core.random.DefaultSecureRandomSupplier;
import org.apache.wicket.core.random.ISecureRandomSupplier;
import org.apache.wicket.core.util.crypt.AESCrypt;
import org.apache.wicket.util.crypt.ICrypt;
import org.apache.wicket.util.crypt.SunJceCrypt;

import lombok.SneakyThrows;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
class _CryptFactory {

    ICrypt sunJceCrypt(final String encryptionKey) {
        final byte[] salt = SunJceCrypt.randomSalt();
        val crypt = new SunJceCrypt(salt, 1000);
        crypt.setKey(encryptionKey);
        return crypt;
    }

    @SneakyThrows
    ICrypt aesCrypt(final String encryptionKey) {

        final byte[] salt = SunJceCrypt.randomSalt();

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(encryptionKey.toCharArray(), salt, 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

        ISecureRandomSupplier rGen = new DefaultSecureRandomSupplier();

        return new AESCrypt(secret, rGen);
    }

    //XXX BCryptPasswordEncoder (Spring);

}
