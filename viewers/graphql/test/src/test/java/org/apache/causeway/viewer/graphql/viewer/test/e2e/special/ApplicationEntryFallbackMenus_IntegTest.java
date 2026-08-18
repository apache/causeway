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
package org.apache.causeway.viewer.graphql.viewer.test.e2e.special;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.apache.causeway.applib.services.iactn.InteractionService;
import org.apache.causeway.viewer.graphql.model.application.ApplicationEntryService;
import org.apache.causeway.viewer.graphql.viewer.test.e2e.Abstract_IntegTest;

import static org.assertj.core.api.Assertions.assertThat;

@Order(69)
@ActiveProfiles("test")
public class ApplicationEntryFallbackMenus_IntegTest extends Abstract_IntegTest {

    @jakarta.inject.Inject ApplicationEntryService applicationEntryService;
    @jakarta.inject.Inject InteractionService interactionService;

    @DynamicPropertySource
    static void generatedMenus(final DynamicPropertyRegistry registry) {
        registry.add(
                "causeway.viewer.common.application.menubars-layout-file",
                () -> "missing-menubars.layout.xml");
    }

    @Test
    void exposesGeneratedEffectiveMenusThroughTheSameCapability() {
        var snapshot = interactionService.callAnonymous(
                () -> applicationEntryService.applicationSnapshot(true));
        assertThat(snapshot.menuBars()).isNotNull();
        assertThat(snapshot.menuBars().href()).isEqualTo("/graphql/application/menu-bars");
        var resource = interactionService.callAnonymous(applicationEntryService::menuBarsResource);
        assertThat(resource.xml())
                .contains("<mb:primary")
                .contains("<mb:secondary")
                .contains("<mb:tertiary");
    }
}
