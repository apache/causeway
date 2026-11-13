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

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.causeway.core.config.metamodel.facets.DomainObjectConfigOptions.LockingPolicy;

class CausewayConfiguration_locking_Test {
    private final ConfigurationFactory configurations = new ConfigurationFactory();

    @Test
    void defaultsToOptimistic() {
        configurations.test(TestPropertyValues.empty(), config ->
                assertThat(config.applib().annotation().domainObject().locking()).isEqualTo(LockingPolicy.OPTIMISTIC));
    }

    @Test
    void bindsPessimistic() {
        configurations.test(TestPropertyValues.of("causeway.applib.annotation.domain-object.locking=pessimistic"), config ->
                assertThat(config.applib().annotation().domainObject().locking()).isEqualTo(LockingPolicy.PESSIMISTIC));
    }

    @Test
    void rejectsUnresolvedValues() {
        for (var value : new String[] { "DEFAULT", "AS_CONFIGURED" }) {
            new ApplicationContextRunner().withUserConfiguration(CausewayModuleCoreConfig.class)
                    .withPropertyValues("causeway.applib.annotation.domain-object.locking=" + value)
                    .run(context -> assertThat(context).hasFailed());
        }
    }
}
