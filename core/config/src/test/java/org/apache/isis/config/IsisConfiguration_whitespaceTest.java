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
package org.apache.isis.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.isis.unittestsupport.config.IsisConfigurationLegacy;
import org.apache.isis.unittestsupport.config.internal._Config;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IsisConfiguration_whitespaceTest {

    IsisConfigurationLegacy configuration;

    @BeforeEach
    void setUp() throws Exception {

        _Config.clear();

        _Config.put("properties.leadingSpaces", "  twoSpacesBeforeThis");
        _Config.put("properties.leadingTab", "\toneTabBeforeThis");
        _Config.put("properties.trailingSpaces", "twoSpacesAfterThis  ");
        _Config.put("properties.trailingTab", "oneTabAfterThis\t");
        _Config.put("properties.trailingTabAndSpaces", "oneTabAndTwoSpacesAfterThis\t  ");

        configuration = _Config.getConfiguration();

    }

    @Test
    void testLeadingSpaces() {
        assertEquals("twoSpacesBeforeThis", configuration.getString("properties.leadingSpaces"));
    }

    @Test
    void testLeadingTab() {
        assertEquals("oneTabBeforeThis", configuration.getString("properties.leadingTab"));
    }

    @Test
    void testTrailingSpaces() {
        assertEquals("twoSpacesAfterThis", configuration.getString("properties.trailingSpaces"));
    }

    @Test
    void testTrailingTab() {
        assertEquals("oneTabAfterThis", configuration.getString("properties.trailingTab"));
    }

    @Test
    void testTrailingTabSpaces() {
        assertEquals("oneTabAndTwoSpacesAfterThis", configuration.getString("properties.trailingTabAndSpaces"));
    }

}
