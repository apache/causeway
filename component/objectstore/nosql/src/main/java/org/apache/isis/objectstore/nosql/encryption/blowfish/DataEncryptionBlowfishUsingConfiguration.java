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
package org.apache.isis.objectstore.nosql.encryption.blowfish;

import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.objectstore.nosql.NoSqlStoreException;

public class DataEncryptionBlowfishUsingConfiguration extends DataEncryptionBlowfishAbstract {

    private static final String ENCRYPTION_KEY = ConfigurationConstants.ROOT + "nosql.encryption.blowfish-key";

    @Override
    public byte[] secretKey(final IsisConfiguration configuration) {
        final String key = configuration.getString(ENCRYPTION_KEY);
        if (key == null) {
            throw new NoSqlStoreException("No blowfish encryption key specified in the configuration file (key: " + ENCRYPTION_KEY + ")");
        }
        return key.getBytes();
    }

}
