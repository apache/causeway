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


package org.apache.isis.runtimes.dflt.runtime.testsystem.tests;

import junit.framework.TestCase;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.runtimes.dflt.runtime.testsystem.TestProxyConfiguration;


public class TestProxyConfigurationTest extends TestCase {

    private TestProxyConfiguration configuration;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        configuration = new TestProxyConfiguration();
        configuration.add("test.one", "abc");
        configuration.add("test.two", "10");
        configuration.add("another.one", "ghi");
    }

    public void testGetString() {
        assertEquals("abc", configuration.getString("test.one"));
    }

    public void testGetNumber() throws Exception {
        assertEquals(10, configuration.getInteger("test.two"));
    }

    public void testSubset() throws Exception {
        final IsisConfiguration properties = configuration.getProperties("test.");
        assertEquals("abc", properties.getString("one"));
    }
}

