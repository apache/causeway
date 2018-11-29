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

package org.apache.isis.config.builder;

import java.util.Properties;

import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.builder.IsisConfigurationDefault;

import junit.framework.TestCase;

public class IsisConfigurationDefault_PropertNamesTest extends TestCase {

    private IsisConfigurationDefault configuration;

    public IsisConfigurationDefault_PropertNamesTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {

        configuration = new IsisConfigurationDefault();

        final Properties p = new Properties();
        p.put("isis.bool", "on");
        p.put("isis.str", "original");
        configuration.add(p, IsisConfiguration.ContainsPolicy.IGNORE);

        final Properties p1 = new Properties();
        p1.put("isis.int", "1");
        p1.put("isis.str", "replacement");
        configuration.add(p1, IsisConfiguration.ContainsPolicy.IGNORE);
    }

    public void testDuplicatedPropertyIsNotReplaced() {
        assertEquals("original", configuration.getString("isis.str"));
    }

    public void testUniqueEntries() {
        assertEquals(1, configuration.getInteger("isis.int"));
        assertEquals(true, configuration.getBoolean("isis.bool"));
    }

}
