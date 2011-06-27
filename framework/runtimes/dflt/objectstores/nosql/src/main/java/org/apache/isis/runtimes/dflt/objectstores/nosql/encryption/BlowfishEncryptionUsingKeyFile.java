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
package org.apache.isis.runtimes.dflt.objectstores.nosql.encryption;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.runtimes.dflt.objectstores.nosql.NoSqlStoreException;


public class BlowfishEncryptionUsingKeyFile extends BlowfishEncryptionAbstract {

    private static final String ENCRYPTION_KEY_FILE = ConfigurationConstants.ROOT + "nosql.encryption.blowfish-key-file";

    public byte[] secretKey(IsisConfiguration configuration) {
        String fileName = configuration.getString(ENCRYPTION_KEY_FILE, "./blowfish.key");
        File file = new File(fileName);
        if (file.exists()) {
            try {
                InputStream fileInput = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                int length = fileInput.read(buffer);
                byte[] key = new byte[length];
                System.arraycopy(buffer, 0, key, 0, length);
                return key;
            } catch (IOException e) {
                throw new NoSqlStoreException("Failed to read in encryption file: " + file.getAbsolutePath(), e);
            }
        } else {
            throw new NoSqlStoreException("Cannot find encryption file: " + file.getAbsolutePath());
        }
    }

}
