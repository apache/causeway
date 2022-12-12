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
package org.apache.causeway.testdomain.interact;

import lombok.val;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;

import javax.inject.Inject;

import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.DateTimeFormat;
import org.apache.causeway.testdomain.conf.Configuration_headless;
import org.apache.causeway.testdomain.model.interaction.Configuration_usingInteractionDomain;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;
import org.apache.causeway.testing.integtestsupport.applib.NoPermissionChecks;
import org.apache.causeway.testing.integtestsupport.applib.annotation.InteractAs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
                Configuration_usingInteractionDomain.class
        },
        properties = {
        })
@TestPropertySource({
    CausewayPresets.SilenceMetaModel,
    CausewayPresets.SilenceProgrammingModel
})
@ExtendWith(NoPermissionChecks.class)
@DirtiesContext
class CustomContextWithNoPermissionChecksTest extends CausewayIntegrationTestAbstract {

    @Inject InteractionService interactionService;

    @Test
    void shouldRunWithDefaultContext() {

        val iaCtx = interactionService.currentInteractionContextElseFail();
        assertThat(iaCtx.getUser().hasSudoAccessAllRole()).isTrue();
    }



}
