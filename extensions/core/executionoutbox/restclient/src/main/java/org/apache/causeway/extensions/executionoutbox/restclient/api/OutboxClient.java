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
import javax.ws.rs.core.MediaType;

import org.apache.causeway.applib.util.schema.InteractionsDtoUtils;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.resources._Json;
import org.apache.causeway.extensions.executionoutbox.restclient.api.delete.DeleteMessage;
import org.apache.causeway.extensions.executionoutbox.restclient.api.deleteMany.DeleteManyMessage;
import org.apache.causeway.schema.common.v2.InteractionType;
import org.apache.causeway.schema.ixn.v2.ActionInvocationDto;
import org.apache.causeway.schema.ixn.v2.InteractionDto;
import org.apache.causeway.schema.ixn.v2.InteractionsDto;
import org.apache.causeway.schema.ixn.v2.MemberExecutionDto;
import org.apache.causeway.schema.ixn.v2.PropertyEditDto;
import org.apache.causeway.viewer.restfulobjects.client.RestfulClient;
import org.apache.causeway.viewer.restfulobjects.client.RestfulClientConfig;
import org.apache.causeway.viewer.restfulobjects.client.RestfulClientMediaType;

import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * @since 2.x {@index}
 */
@Log4j2
public class OutboxClient {

    private final RestfulClientConfig restfulClientConfig;

    public OutboxClient() {
        this.restfulClientConfig = new RestfulClientConfig();
    }

    /**
     * Will automatically call {@link #init()} since all properties already supplied.
     */
    public OutboxClient(final String base, final String username, final String password) {
        this();

        setBase(base);
        setUsername(username);
        setPassword(password);

        init();
    }

    /**
     * for debugging
     * @param connectTimeoutInSecs
     */
    public OutboxClient withConnectTimeoutInSecs(final int connectTimeoutInSecs) {
        setConnectTimeoutInSecs(connectTimeoutInSecs);
        return this;
    }

    /**
     * for debugging
     * @param readTimeoutInSecs
     */
    public OutboxClient withReadTimeoutInSecs(final int readTimeoutInSecs) {
        setReadTimeoutInSecs(readTimeoutInSecs);
        return this;
    }

    @Setter private String base;
    @Setter private String username;
    @Setter private String password;
    @Setter private int connectTimeoutInSecs;
    @Setter private int readTimeoutInSecs;

    /**
     * Should be called once all properties have been injected.
     */
    public void init() {
        restfulClientConfig.setRestfulBase(base);
        restfulClientConfig.setUseBasicAuth(true);
        restfulClientConfig.setRestfulAuthUser(username);
        restfulClientConfig.setRestfulAuthPassword(password);
        restfulClientConfig.setConnectTimeoutInMillis(1000L * connectTimeoutInSecs);
        restfulClientConfig.setReadTimeoutInMillis(1000L * readTimeoutInSecs);
        //restfulClientConfig.setUseRequestDebugLogging(true); //for debugging
    }

    private void ensureInitialized() {
        if(username == null || password == null || base == null) {
            throw new IllegalStateException("Must initialize 'username', 'password' and 'base' properties");
        }
    }

    public List<InteractionDto> pending() {

        ensureInitialized();

        try(val client = RestfulClient.ofConfig(restfulClientConfig)) {

            var response = client.request(PENDING_URI)
                    .accept(RestfulClientMediaType.RO_XML.mediaTypeFor(InteractionsDto.class))
                    .get();

            final Try<InteractionsDto> digest = client.digest(response, InteractionsDto.class);

            if(digest.isSuccess()) {
                return digest.getValue()
                        .map(InteractionsDto::getInteractionDto)
                        .orElseGet(Collections::emptyList);
            } else {
                log.error("Failed to GET from {}: {}", client.uri(PENDING_URI), digest.getFailure().get());
                return Collections.emptyList();
            }
        }

    }

    public void delete(final String interactionId, final int sequence) {
        invoke(DELETE_URI,
                new DeleteMessage(interactionId, sequence));
    }

    public void deleteMany(final List<InteractionDto> interactionDtos) {
        val interactionsDto = new InteractionsDto();
        interactionDtos.forEach(interactionDto -> {
            addTo(interactionsDto, interactionDto);
        });
        invoke(DELETE_MANY_URI,
                new DeleteManyMessage(InteractionsDtoUtils.toXml(interactionsDto)));
    }

    // -- HELPER

    private static String PENDING_URI = "services/causeway.ext.executionOutbox.OutboxRestApi/actions/pending/invoke";
    private static String DELETE_URI = "services/causeway.ext.executionOutbox.OutboxRestApi/actions/delete/invoke";
    private static String DELETE_MANY_URI = "services/causeway.ext.executionOutbox.OutboxRestApi/actions/deleteMany/invoke";

    private void addTo(final InteractionsDto interactionsDto, final InteractionDto orig) {
        val copy = new InteractionDto();
        copy.setInteractionId(orig.getInteractionId());
        setMemberExecution(copy, orig);
        interactionsDto.getInteractionDto().add(copy);
    }

    private void setMemberExecution(final InteractionDto copy, final InteractionDto orig) {
        val memberExecutionDto = newMemberExecutionDto(orig);
        memberExecutionDto.setSequence(orig.getExecution().getSequence());
        copy.setExecution(memberExecutionDto);
    }

    private MemberExecutionDto newMemberExecutionDto(final InteractionDto orig) {
        val execution = orig.getExecution();
        return execution.getInteractionType() == InteractionType.ACTION_INVOCATION
                ? new ActionInvocationDto()
                : new PropertyEditDto();
    }

    private void invoke(final String path, final Object dto) {

        ensureInitialized();

        try(val client = RestfulClient.ofConfig(restfulClientConfig)) {

            var invocationBuilder = client.request(path);

            val invocation = invocationBuilder.buildPut(
                    Entity.entity(_Json.toString(dto), MediaType.APPLICATION_JSON_TYPE));

            val response = invocation.invoke();

            val responseStatus = response.getStatus();
            if (responseStatus != 200) {
                // if failed to log message via REST service, then fallback by logging to slf4j
                log.warn(dto.toString());
            }
        }

    }

}
