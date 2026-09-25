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
package org.apache.causeway.core.config;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class CausewayConfiguration_executionModeKey_Test {

    @Test
    void defaultsToCausewayExecutionMode() {
        final CausewayConfiguration configuration = newConfiguration(new StandardEnvironment());

        assertThat(configuration.getExecution().getMode().getKey())
                .isEqualTo("causeway.execution.mode");
    }

    @Test
    void bindsConfiguredExecutionModeKey() {
        final StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource(
                "test",
                Map.of("causeway.execution.mode.key", "application.execution.mode")));
        final CausewayConfiguration configuration = newConfiguration(environment);

        Binder.get(environment).bind(
                CausewayConfiguration.ROOT_PREFIX,
                Bindable.ofInstance(configuration));

        assertThat(configuration.getExecution().getMode().getKey())
                .isEqualTo("application.execution.mode");
    }

    private static CausewayConfiguration newConfiguration(final StandardEnvironment environment) {
        return new CausewayConfiguration(environment, Optional.empty());
    }
}
