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
package org.apache.causeway.extensions.executionoutbox.restclient.integtests;

import java.util.List;
import java.util.stream.IntStream;

import javax.inject.Inject;

import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.core.webapp.CausewayModuleCoreWebapp;
import org.apache.causeway.extensions.executionoutbox.applib.dom.ExecutionOutboxEntry;
import org.apache.causeway.extensions.executionoutbox.applib.dom.ExecutionOutboxEntryRepository;
import org.apache.causeway.extensions.executionoutbox.applib.integtest.model.CounterRepository;
import org.apache.causeway.extensions.executionoutbox.applib.integtest.model.Counter_bumpUsingMixin;
import org.apache.causeway.extensions.executionoutbox.applib.integtest.model.Counter_bumpUsingMixinWithExecutionPublishingDisabled;
import org.apache.causeway.extensions.executionoutbox.jpa.CausewayModuleExtExecutionOutboxPersistenceJpa;
import org.apache.causeway.extensions.executionoutbox.jpa.integtests.model.Counter;
import org.apache.causeway.extensions.executionoutbox.restclient.api.OutboxClient;
import org.apache.causeway.extensions.executionoutbox.restclient.api.OutboxClientConfig;
import org.apache.causeway.persistence.jpa.eclipselink.CausewayModulePersistenceJpaEclipselink;
import org.apache.causeway.schema.ixn.v2.InteractionDto;
import org.apache.causeway.security.bypass.CausewayModuleSecurityBypass;
import org.apache.causeway.testing.fixtures.applib.CausewayModuleTestingFixturesApplib;
import org.apache.causeway.viewer.restfulobjects.client.AuthenticationMode;
import org.apache.causeway.viewer.restfulobjects.client.RestfulClientConfig;
import org.apache.causeway.viewer.restfulobjects.jaxrsresteasy.CausewayModuleViewerRestfulObjectsJaxrsResteasy;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
public class OutboxRestClient_E2eTest {

    OutboxClient outboxClient;

    @BeforeEach
    void beforeEach() {
        outboxClient = new OutboxClient(
                RestfulClientConfig.builder()
                        .restfulBaseUrl("http://localhost:8080/restful")
                        .authenticationMode(AuthenticationMode.OAUTH2_AZURE)
                        .oauthTenantId("xxx")
                        .oauthClientId("xxx")
                        .oauthClientSecret("xxx")
                        .build(),
                OutboxClientConfig.builder()
                        .pendingUri("services/isis.ext.executionOutbox.OutboxRestApi/actions/pending/invoke")
                        .deleteUri("services/isis.ext.executionOutbox.OutboxRestApi/actions/delete/invoke")
                        .deleteManyUri("services/isis.ext.executionOutbox.OutboxRestApi/actions/deleteMany/invoke")
                        .build()
                );
    }

    @Disabled
    @Test
    void pending_when_none() {

        List<InteractionDto> pending = outboxClient.pending();
        assertThat(pending).hasSize(0);
    }

}
