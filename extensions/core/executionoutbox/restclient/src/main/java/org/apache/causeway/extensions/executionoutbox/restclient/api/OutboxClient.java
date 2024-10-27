/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.causeway.extensions.executionoutbox.restclient.api;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

import org.apache.causeway.applib.util.schema.InteractionsDtoUtils;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.io.JsonUtils;
import org.apache.causeway.extensions.executionoutbox.restclient.api.delete.DeleteMessage;
import org.apache.causeway.extensions.executionoutbox.restclient.api.deleteMany.DeleteManyMessage;
import org.apache.causeway.schema.common.v2.InteractionType;
import org.apache.causeway.schema.ixn.v2.ActionInvocationDto;
import org.apache.causeway.schema.ixn.v2.InteractionDto;
import org.apache.causeway.schema.ixn.v2.InteractionsDto;
import org.apache.causeway.schema.ixn.v2.MemberExecutionDto;
import org.apache.causeway.schema.ixn.v2.PropertyEditDto;
import org.apache.causeway.viewer.restfulobjects.client.AuthenticationMode;
import org.apache.causeway.viewer.restfulobjects.client.RestfulClient;
import org.apache.causeway.viewer.restfulobjects.client.RestfulClientConfig;
import org.apache.causeway.viewer.restfulobjects.client.RestfulClientMediaType;
import org.apache.causeway.viewer.restfulobjects.client.auth.AuthorizationHeaderFactory;

import lombok.extern.log4j.Log4j2;

/**
 * @since 2.x {@index}
 */
@Log4j2
public class OutboxClient {

    private final RestfulClient client;
    private final OutboxClientConfig outboxClientConfig;

    public OutboxClient(
            final String restfulBaseUrl,
            final String username,
            final String password) {
        this(RestfulClientConfig.builder()
                .restfulBaseUrl(restfulBaseUrl)
                .authenticationMode(AuthenticationMode.BASIC)
                .basicAuthUser(username)
                .basicAuthPassword(password)
                .build()
        );
    }

    public OutboxClient(
            final String restfulBaseUrl,
            final String tenantId,
            final String clientId,
            final String clientSecret) {
        this(RestfulClientConfig.builder()
                .restfulBaseUrl(restfulBaseUrl)
                .authenticationMode(AuthenticationMode.OAUTH2_AZURE)
                .oauthTenantId(tenantId)
                .oauthClientId(clientId)
                .oauthClientSecret(clientSecret)
                .build()
        );
    }

    public OutboxClient(final RestfulClientConfig restfulClientConfig) {
        this(restfulClientConfig, new OutboxClientConfig());
    }

    public OutboxClient(final RestfulClientConfig restfulClientConfig, final OutboxClientConfig outboxClientConfig) {
        this.client = RestfulClient.ofConfig(restfulClientConfig);
        this.outboxClientConfig = outboxClientConfig;
    }

    public OutboxClient(
            final RestfulClientConfig restfulClientConfig,
            final AuthorizationHeaderFactory authorizationHeaderFactory) {
        this(restfulClientConfig, authorizationHeaderFactory, new OutboxClientConfig());
    }

    public OutboxClient(
            final RestfulClientConfig restfulClientConfig,
            final AuthorizationHeaderFactory authorizationHeaderFactory,
            final OutboxClientConfig outboxClientConfig) {
        this.outboxClientConfig = outboxClientConfig;
        this.client = RestfulClient.ofConfig(restfulClientConfig, authorizationHeaderFactory);
    }

    public OutboxClient withConnectTimeoutInSecs(int connectTimeoutInSecs) {
        client.getConfig().setConnectTimeoutInMillis(1000L * connectTimeoutInSecs);
        return this;
    }

    public OutboxClient withReadTimeoutInSecs(int readTimeoutInSecs) {
        client.getConfig().setReadTimeoutInMillis(1000L * readTimeoutInSecs);
        return this;
    }

    public List<InteractionDto> pending() {

        Invocation.Builder invocationBuilder = client.request(outboxClientConfig.getPendingUri())
                .accept(RestfulClientMediaType.RO_XML.mediaTypeFor(InteractionsDto.class));
        var response = invocationBuilder.get();

        final Try<InteractionsDto> digest = client.digest(response, InteractionsDto.class);

        digest.ifFailureFail();
        return digest.getValue()
                .map(InteractionsDto::getInteractionDto)
                .orElseGet(Collections::emptyList);
    }

    public void delete(final String interactionId, final int sequence) {
        invoke(outboxClientConfig.getDeleteUri(),
                new DeleteMessage(interactionId, sequence));
    }

    public void deleteMany(final List<InteractionDto> interactionDtos) {
        var interactionsDto = new InteractionsDto();
        interactionDtos.forEach(interactionDto -> {
            addTo(interactionsDto, interactionDto);
        });
        invoke(outboxClientConfig.getDeleteManyUri(),
                new DeleteManyMessage(InteractionsDtoUtils.dtoMapper().toString(interactionsDto)));
    }

    // -- HELPER

    private void addTo(final InteractionsDto interactionsDto, final InteractionDto orig) {
        var copy = new InteractionDto();
        copy.setInteractionId(orig.getInteractionId());
        setMemberExecution(copy, orig);
        interactionsDto.getInteractionDto().add(copy);
    }

    private void setMemberExecution(final InteractionDto copy, final InteractionDto orig) {
        var memberExecutionDto = newMemberExecutionDto(orig);
        memberExecutionDto.setSequence(orig.getExecution().getSequence());
        copy.setExecution(memberExecutionDto);
    }

    private MemberExecutionDto newMemberExecutionDto(final InteractionDto orig) {
        var execution = orig.getExecution();
        return execution.getInteractionType() == InteractionType.ACTION_INVOCATION
                ? new ActionInvocationDto()
                : new PropertyEditDto();
    }

    private void invoke(final String path, final Object dto) {

        var invocationBuilder = client.request(path);

        var invocation = invocationBuilder.buildPut(
                Entity.entity(JsonUtils.toStringUtf8(dto), MediaType.APPLICATION_JSON_TYPE));

        var response = invocation.invoke();

        var responseStatus = response.getStatus();
        if (responseStatus != 200) {
            // if failed to log message via REST service, then fallback by logging to slf4j
            log.warn(dto.toString());
        }
    }

}
