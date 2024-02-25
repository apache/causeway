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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.test.context.ActiveProfiles;

import org.apache.causeway.extensions.executionoutbox.restclient.api.OutboxClient;
import org.apache.causeway.extensions.executionoutbox.restclient.api.OutboxClientConfig;
import org.apache.causeway.schema.ixn.v2.InteractionDto;
import org.apache.causeway.viewer.restfulobjects.client.AuthenticationMode;
import org.apache.causeway.viewer.restfulobjects.client.RestfulClientConfig;

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
                        .pendingUri("services/causeway.ext.executionOutbox.OutboxRestApi/actions/pending/invoke")
                        .deleteUri("services/causeway.ext.executionOutbox.OutboxRestApi/actions/delete/invoke")
                        .deleteManyUri("services/causeway.ext.executionOutbox.OutboxRestApi/actions/deleteMany/invoke")
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
