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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.causeway.extensions.executionoutbox.restclient.api.delete.DeleteMessage;
import org.apache.causeway.extensions.executionoutbox.restclient.api.deleteMany.DeleteManyMessage;
import org.apache.causeway.schema.common.v2.InteractionType;
import org.apache.causeway.schema.ixn.v2.ActionInvocationDto;
import org.apache.causeway.schema.ixn.v2.InteractionDto;
import org.apache.causeway.schema.ixn.v2.InteractionsDto;
import org.apache.causeway.schema.ixn.v2.MemberExecutionDto;
import org.apache.causeway.schema.ixn.v2.PropertyEditDto;

import lombok.Setter;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 * @since 2.x {@index}
 */
@Slf4j
public class OutboxClient {

    private final ClientBuilder clientBuilder;

    public OutboxClient() {
        clientBuilder = ClientBuilder.newBuilder();
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
    public OutboxClient withConnectTimeoutInSecs(int connectTimeoutInSecs) {
        clientBuilder.connectTimeout(connectTimeoutInSecs, TimeUnit.SECONDS);
        return this;
    }

    /**
     * for debugging
     * @param readTimeoutInSecs
     */
    public OutboxClient withReadTimeoutInSecs(int readTimeoutInSecs) {
        clientBuilder.readTimeout(readTimeoutInSecs, TimeUnit.SECONDS);
        return this;
    }

    private UriBuilder pendingUriBuilder;
    private UriBuilder deleteUriBuilder;
    private UriBuilder deleteManyUriBuilder;

    @Setter private String base;
    @Setter private String username;
    @Setter private String password;


    /**
     * Should be called once all properties have been injected.
     */
    public void init() {
        this.pendingUriBuilder = UriBuilder.fromUri(base + "services/causeway.ext.executionOutbox.OutboxRestApi/actions/pending/invoke");
        this.deleteUriBuilder = UriBuilder.fromUri(base + "services/causeway.ext.executionOutbox.OutboxRestApi/actions/delete/invoke");
        this.deleteManyUriBuilder = UriBuilder.fromUri(base + "services/causeway.ext.executionOutbox.OutboxRestApi/actions/deleteMany/invoke");
    }

    private void ensureInitialized() {
        if(username == null || password == null || base == null) {
            throw new IllegalStateException("Must initialize 'username', 'password' and 'base' properties");
        }
    }


    public List<InteractionDto> pending() {

        ensureInitialized();

        val uri = pendingUriBuilder.build();

        Client client = null;
        try {
            client = clientBuilder.build();

            val webTarget = client.target(uri);

            val invocationBuilder = webTarget.request()
                    .header("Authorization", "Basic " + encode(username, password))
                    .accept(mediaTypeFor(InteractionsDto.class))
                    ;

            val invocation = invocationBuilder.buildGet();
            val response = invocation.invoke();

            val responseStatus = response.getStatus();
            if (responseStatus != 200) {
                log.warn(invocation.toString());
            }

            final InteractionsDto interactionsDto = response.readEntity(InteractionsDto.class);
            return interactionsDto.getInteractionDto();

        } catch(Exception ex) {
            log.error(String.format("Failed to GET from %s", uri.toString()), ex);
        } finally {
            closeQuietly(client);
        }
        return Collections.emptyList();
    }

    private static MediaType mediaTypeFor(final Class<?> dtoClass) {

        val headers = new HashMap<String,String>();
        headers.put("profile", "urn:org.restfulobjects:repr-types/action-result");
        headers.put("x-ro-domain-type", dtoClass.getName());
        return new MediaType("application", "xml", headers);
    }


    public void delete(final String interactionId, final int sequence) {
        val jsonable = new DeleteMessage(interactionId, sequence);
        invoke(jsonable, deleteUriBuilder);
    }

    public void deleteMany(final List<InteractionDto> interactionDtos) {

        InteractionsDto interactionsDto = new InteractionsDto();
        interactionDtos.forEach(interactionDto -> {
            addTo(interactionsDto, interactionDto);
        });

        val jsonable = new DeleteManyMessage(_Jaxb.toXml(interactionsDto));
        invoke(jsonable, deleteManyUriBuilder);
    }

    private void addTo(InteractionsDto interactionsDto, InteractionDto orig) {
        InteractionDto copy = new InteractionDto();
        copy.setInteractionId(orig.getInteractionId());
        setMemberExecution(copy, orig);
        interactionsDto.getInteractionDto().add(copy);
    }

    private void setMemberExecution(InteractionDto copy, InteractionDto orig) {
        val memberExecutionDto = newMemberExecutionDto(orig);
        memberExecutionDto.setSequence(orig.getExecution().getSequence());
        copy.setExecution(memberExecutionDto);
    }

    private MemberExecutionDto newMemberExecutionDto(InteractionDto orig) {
        val execution = orig.getExecution();
        return execution.getInteractionType() == InteractionType.ACTION_INVOCATION
                ? new ActionInvocationDto()
                : new PropertyEditDto();
    }

    private void invoke(Jsonable entity, UriBuilder uriBuilder) {

        ensureInitialized();

        val json = entity.asJson();

        Client client = null;
        try {
            client = clientBuilder.build();

            val webTarget = client.target(uriBuilder.build());

            val invocationBuilder = webTarget.request();
            invocationBuilder.header("Authorization", "Basic " + encode(username, password));

            val invocation = invocationBuilder.buildPut(
                    Entity.entity(json, MediaType.APPLICATION_JSON_TYPE));

            val response = invocation.invoke();

            val responseStatus = response.getStatus();
            if (responseStatus != 200) {
                // if failed to log message via REST service, then fallback by logging to slf4j
                log.warn(entity.toString());
            }
        } catch(Exception ex) {
            log.error(entity.toString(), ex);
        } finally {
            closeQuietly(client);
        }
    }

    private static String encode(final String username, final String password) {
        return java.util.Base64.getEncoder().encodeToString(asBytes(username, password));
    }

    private static byte[] asBytes(final String username, final String password) {
        return String.format("%s:%s", username, password).getBytes();
    }

    private static void closeQuietly(final Client client) {
        if (client == null) {
            return;
        }
        try {
            client.close();
        } catch (Exception ex) {
            // ignore so as to avoid overriding any pending exceptions in calling 'finally' block.
        }
    }


}
