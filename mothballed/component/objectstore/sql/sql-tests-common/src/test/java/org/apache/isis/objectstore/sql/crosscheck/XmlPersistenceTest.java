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

/**
 * 
 */
package org.apache.isis.objectstore.sql.crosscheck;

import java.util.Properties;

import org.apache.isis.core.unittestsupport.files.Files;
import org.apache.isis.core.unittestsupport.files.Files.Recursion;
import org.apache.isis.objectstore.sql.common.SqlIntegrationTestData;
import org.apache.isis.objectstore.sql.common.SqlIntegrationTestFixtures;
import org.apache.isis.objectstore.sql.common.SqlIntegrationTestFixtures.State;

public class XmlPersistenceTest extends SqlIntegrationTestData {

    @Override
    public void resetPersistenceStoreDirectlyIfRequired() {
        Files.deleteFiles("xml/objects", ".xml", Recursion.DO_RECURSE);
    }

    @Override
    protected void testSetup() {
        resetPersistenceStoreDirectlyIfRequired();
        SqlIntegrationTestFixtures.recreate();
        try {
            SqlIntegrationTestFixtures.getInstance().initSystem(getProperties());
        } catch (Exception e) {
            e.printStackTrace();
        }
        getSqlIntegrationTestFixtures().setState(State.INITIALIZE);
    }

    @Override
    public Properties getProperties() {
        final Properties properties = new Properties();
        properties.put("isis.persistor", "xml");
        properties.put("isis.logging.objectstore", "off");
        return properties;
    }

    @Override
    public String getPropertiesFilename() {
        return "xml.properties";
    }

}
