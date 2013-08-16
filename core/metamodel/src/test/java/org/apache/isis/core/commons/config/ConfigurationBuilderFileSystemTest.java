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

package org.apache.isis.core.commons.config;

import junit.framework.TestCase;

import org.apache.isis.core.commons.exceptions.IsisException;

public class ConfigurationBuilderFileSystemTest extends TestCase {
    IsisConfigurationBuilderFileSystem loader;

    @Override
    protected void setUp() throws Exception {
        loader = new IsisConfigurationBuilderFileSystem("src/test/config");
        loader.addDefaultConfigurationResources();
    }

    public void testDefaultConfiguration() {
        final IsisConfiguration configuration = loader.getConfiguration();
        assertEquals("one", configuration.getString("properties.example"));
    }

    public void testDefaultConfigurationTrailingWhitespace() {
        final IsisConfiguration configuration = loader.getConfiguration();
        assertEquals("in-memory", configuration.getString("properties.trailingWhitespace"));
    }

    public void testAddConfiguration() {
        loader.addConfigurationResource("another.properties", NotFoundPolicy.FAIL_FAST);
        final IsisConfiguration configuration = loader.getConfiguration();
        assertEquals("added", configuration.getString("additional.example"));
    }

    public void testAddedConfigurationIsIgnoredWhenEarlierPropertyExists() {
        loader.addConfigurationResource("another.properties", NotFoundPolicy.FAIL_FAST);
        final IsisConfiguration configuration = loader.getConfiguration();
        assertEquals("one", configuration.getString("properties.example"));
    }

    public void testAddedConfigurationFailsWhenFileNotFound() {
        try {
            loader.addConfigurationResource("unfound.properties", NotFoundPolicy.FAIL_FAST);
            loader.getConfiguration();
            fail();
        } catch (final IsisException expected) {
        }
    }

    public void testAddedConfigurationIgnoreUnfoundFile() {
        loader.addConfigurationResource("unfound.properties", NotFoundPolicy.CONTINUE);
        loader.getConfiguration();
    }

    public void testAddProperty() throws Exception {
        loader.add("added.property", "added by code");
        final IsisConfiguration configuration = loader.getConfiguration();
        assertEquals("added by code", configuration.getString("added.property"));
    }
}
