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
package org.apache.isis.metamodel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.isis.metamodel.context.MetaModelContext;
import org.apache.isis.unittestsupport.config.IsisConfigurationLegacy;
import org.apache.isis.unittestsupport.config.internal._Config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import lombok.val;

class MetaModelContext_configTest {

    private MetaModelContext mmc;
    
    @BeforeEach
    void setUp() {
        _Config.clear();
        mmc = MetaModelContext_forTesting.buildDefault();
    }

    @Test
    void shouldReturnEmptyValue() {

        val config = config();
        assertEquals(null, config.getString("test"));
    }

    @Test
    void shouldNotAllowChangeAfterFinalizedConfig() {

        @SuppressWarnings("unused")
        val config = config();

        assertThrows(IllegalStateException.class, ()->{
            _Config.put("test", "Hello World!");    
        });
    }

    @Test
    void shouldReturnPrimedValue() {

        _Config.put("test", "Hello World!");

        val config = config();

        assertEquals("Hello World!", config.getString("test"));
    }

    // -- HELPER

    private IsisConfigurationLegacy config() {
        return ((MetaModelContext_forTesting) mmc).getConfigurationLegacy();
    }

}
