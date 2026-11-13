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

import static org.assertj.core.api.Assertions.assertThat;

class CausewayConfiguration_CommandReplayMapping_Test {

    private final ConfigurationFactory configurationFactory = new ConfigurationFactory();

    @Test
    void commandExecutorAdvisorPolicyDefaultsToNoCheck() {
        configurationFactory.test(
                TestPropertyValues.empty(),
                causeway -> assertThat(causeway.core().runtimeServices().commandExecutorService().interactionAdvisorPolicy())
                        .isEqualTo(CausewayConfiguration.Core.RuntimeServices.CommandExecutorService.InteractionAdvisorPolicy.NO_CHECK));
    }

    @Test
    void commandExecutorAdvisorPolicyCanBeConfigured() {
        configurationFactory.test(
                TestPropertyValues.of(
                        "causeway.core.runtime-services.command-executor-service.interaction-advisor-policy=CHECK_BUT_IGNORE"),
                causeway -> assertThat(causeway.core().runtimeServices().commandExecutorService().interactionAdvisorPolicy())
                        .isEqualTo(CausewayConfiguration.Core.RuntimeServices.CommandExecutorService.InteractionAdvisorPolicy.CHECK_BUT_IGNORE));
    }

    @Test
    void replayResultMappingDefaultsToInMemoryAndThrowException() {
        configurationFactory.test(
                TestPropertyValues.empty(),
                causeway -> {
                    var mapping = causeway.extensions().commandLog().replayResultMapping();
                    assertThat(mapping.storageStrategy())
                            .isEqualTo(CausewayConfiguration.Extensions.CommandLog.ReplayResultMapping.StorageStrategy.IN_MEMORY);
                    assertThat(mapping.onConflictPolicy())
                            .isEqualTo(CausewayConfiguration.Extensions.CommandLog.ReplayResultMapping.OnConflictPolicy.THROW_EXCEPTION);
                });
    }

    @Test
    void replayResultMappingCanSelectPersistentAndLogConflicts() {
        configurationFactory.test(
                TestPropertyValues.of(
                        "causeway.extensions.command-log.replay-result-mapping.storage-strategy=PERSISTENT",
                        "causeway.extensions.command-log.replay-result-mapping.on-conflict-policy=LOG_AND_CONTINUE"),
                causeway -> {
                    var mapping = causeway.extensions().commandLog().replayResultMapping();
                    assertThat(mapping.storageStrategy())
                            .isEqualTo(CausewayConfiguration.Extensions.CommandLog.ReplayResultMapping.StorageStrategy.PERSISTENT);
                    assertThat(mapping.onConflictPolicy())
                            .isEqualTo(CausewayConfiguration.Extensions.CommandLog.ReplayResultMapping.OnConflictPolicy.LOG_AND_CONTINUE);
                });
    }
}
