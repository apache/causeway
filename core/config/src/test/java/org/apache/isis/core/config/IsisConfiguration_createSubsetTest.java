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
package org.apache.isis.core.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.isis.core.config.unittestsupport.IsisConfigurationLegacy;
import org.apache.isis.core.config.unittestsupport.internal._Config;

import lombok.val;

class IsisConfiguration_createSubsetTest {

    @BeforeEach
    public void setUp() throws Exception {
        _Config.clear();
    }

    @Test
    void empty() {

        val configuration = _Config.getConfiguration();

        final IsisConfigurationLegacy subset = configuration.subsetWithNamesStripped("foo");
        assertEquals(true, subset.isEmpty());
    }

    @Test
    void nonEmptyButNoneInSubset() {

        _Config.put("bar", "barValue");

        val configuration = _Config.getConfiguration();

        final IsisConfigurationLegacy subset = configuration.subsetWithNamesStripped("foo");
        assertEquals(true, subset.isEmpty());
    }

    @Test
    void nonEmptyButSingleKeyedInSubset() {

        _Config.put("foo", "fooValue");

        val configuration = _Config.getConfiguration();

        final IsisConfigurationLegacy subset = configuration.subsetWithNamesStripped("foo");
        assertEquals(true, subset.isEmpty());
    }

    @Test
    void nonEmptyAndMultiKeyedInSubset() {

        _Config.put("foo.foz", "fozValue");

        val configuration = _Config.getConfiguration();

        final IsisConfigurationLegacy subset = configuration.subsetWithNamesStripped("foo");

        assertEquals(1, subset.copyToMap().size());
        assertEquals("fozValue", subset.getString("foz"));
    }

    @Test
    void propertiesOutsideOfSubsetAreIgnored() {
        _Config.put("foo.foz", "fozValue");
        _Config.put("foo.faz", "fazValue");
        _Config.put("bar.baz", "bazValue");

        val configuration = _Config.getConfiguration();

        final IsisConfigurationLegacy subset = configuration.subsetWithNamesStripped("foo");

        assertEquals("fozValue", subset.getString("foz"));
        assertEquals("fazValue", subset.getString("faz"));

        assertEquals(2, subset.copyToMap().size());


    }

}
