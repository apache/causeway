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
package org.apache.isis.objectstore.nosql.encryption.rot13;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.objectstore.nosql.encryption.DataEncryption;

public class Rot13Encryption implements DataEncryption {

    @Override
    public String getType() {
        return "rot13";
    }

    @Override
    public void init(final IsisConfiguration configuration) {
    }

    @Override
    public String encrypt(final String plainText) {
        return encode(plainText);
    }

    @Override
    public String decrypt(final String encryptedText) {
        return encode(encryptedText);
    }

    private String encode(final String plainText) {
        if (plainText == null) {
            return plainText;
        }

        // encode plainText
        String encodedMessage = "";
        for (int i = 0; i < plainText.length(); i++) {
            char c = plainText.charAt(i);
            if (c >= 'a' && c <= 'm') {
                c += 13;
            } else if (c >= 'n' && c <= 'z') {
                c -= 13;
            } else if (c >= 'A' && c <= 'M') {
                c += 13;
            } else if (c >= 'N' && c <= 'Z') {
                c -= 13;
            }
            encodedMessage += c;
        }
        return encodedMessage;
    }

}
