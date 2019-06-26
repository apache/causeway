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
package org.apache.isis.core.webserver;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.internal._Config;

import static org.assertj.core.api.Assertions.assertThat;

class HelloWorldAppConfigTest {

    @BeforeEach
    void setUp() throws Exception {
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test @Disabled("TODO[2112] AppConfigLocator is deprecated")
    void test() {

        // when
        IsisConfiguration isisConfiguration = _Config.getConfiguration();

        // then
        Assertions.assertNotNull(isisConfiguration);
        
        Map<String, String> config = isisConfiguration.copyToMap();
        Assertions.assertNotNull(config);
        assertThat(config).hasSize(1);
        //assertThat(config.get("isis.appManifest")).isEqualTo(DummyAppManifest.class.getName());
    }

}
