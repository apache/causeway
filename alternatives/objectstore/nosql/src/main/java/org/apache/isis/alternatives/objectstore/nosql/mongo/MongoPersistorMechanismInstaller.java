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


package org.apache.isis.alternatives.objectstore.nosql.mongo;

import org.apache.isis.alternatives.objectstore.nosql.KeyCreator;
import org.apache.isis.alternatives.objectstore.nosql.NoSqlDataDatabase;
import org.apache.isis.alternatives.objectstore.nosql.NoSqlPersistorMechanismInstaller;
import org.apache.isis.alternatives.objectstore.nosql.SerialKeyCreator;
import org.apache.isis.core.metamodel.config.ConfigurationConstants;
import org.apache.isis.core.metamodel.config.IsisConfiguration;

public class MongoPersistorMechanismInstaller extends NoSqlPersistorMechanismInstaller {
    
    private static final String STRING = ConfigurationConstants.ROOT + "nosql.mongodb.";
    private static final String DB_HOST = STRING + "host";
    private static final String DB_PORT = STRING + "port";
    private static final String DB_NAME = STRING + "name";

    public MongoPersistorMechanismInstaller() {
        super("mongodb");
    }

    protected NoSqlDataDatabase createNoSqlDatabase(IsisConfiguration configuration) {
        NoSqlDataDatabase db;
        String host = configuration.getString(DB_HOST, "localhost");
        int port = configuration.getInteger(DB_PORT, 0);
        String name = configuration.getString(DB_NAME, "isis");
        KeyCreator keyCreator = new SerialKeyCreator();
        db = new MongoDb(host, port, name, keyCreator);
        return db;
    }

}


