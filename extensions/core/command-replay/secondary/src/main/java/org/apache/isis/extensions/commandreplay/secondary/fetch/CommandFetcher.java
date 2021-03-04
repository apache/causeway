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
package org.apache.isis.extensions.commandreplay.secondary.fetch;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.jaxb.JaxbService.Simple;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo;
import org.apache.isis.extensions.commandreplay.secondary.SecondaryStatus;
import org.apache.isis.extensions.commandreplay.secondary.StatusException;
import org.apache.isis.extensions.commandreplay.secondary.config.SecondaryConfig;
import org.apache.isis.extensions.jaxrsclient.applib.client.JaxRsClient;
import org.apache.isis.extensions.jaxrsclient.applib.client.JaxRsResponse;
import org.apache.isis.extensions.jaxrsclient.impl.client.JaxRsClientDefault;
import org.apache.isis.schema.cmd.v2.CommandDto;
import org.apache.isis.schema.cmd.v2.CommandsDto;

import lombok.extern.log4j.Log4j2;


/**
 * @since 2.0 {@index}
 */
@Service()
@Named("isis.ext.commandReplaySecondary.CommandFetcher")
@Order(OrderPrecedence.MIDPOINT)
@Log4j2
public class CommandFetcher {

    static final String URL_SUFFIX =
            "services/isisExtensionsCommandReplayPrimary.CommandRetrievalService/actions/findCommandsOnPrimaryFrom/invoke";


    /**
     * Replicates a single command.
     *
     * @param previousHwmIfAny
     * @throws StatusException
     */
    public List<CommandDto> fetchCommand(
            @Nullable final CommandJdo previousHwmIfAny)
            throws StatusException {

        log.debug("finding command on primary ...");

        final CommandsDto commandsDto = fetchCommands(previousHwmIfAny);
        return commandsDto != null
                ? commandsDto.getCommandDto()
                : Collections.emptyList();
    }

    /**
     * @return - the commands, or <tt>null</tt> if none were found
     * @param previousHwmIfAny
     * @throws StatusException
     */
    private CommandsDto fetchCommands(final CommandJdo previousHwmIfAny)
            throws StatusException {

        final UUID transactionId = previousHwmIfAny != null ? previousHwmIfAny.getInteractionId() : null;

        log.debug("finding commands on primary ...");

        final URI uri = buildUri(transactionId);

        final JaxRsResponse response = callPrimary(uri);

        final CommandsDto commandsDto = unmarshal(response, uri);

        final int size = commandsDto.getCommandDto().size();
        if(size == 0) {
            return null;
        }
        return commandsDto;
    }


    private URI buildUri(final UUID uniqueId) {
        final UriBuilder uriBuilder = UriBuilder.fromUri(
                uniqueId != null
                        ? String.format(
                            "%s%s?uniqueId=%s&batchSize=%d",
                            secondaryConfig.getPrimaryBaseUrlRestful(), URL_SUFFIX, uniqueId, secondaryConfig.getBatchSize())
                        : String.format(
                            "%s%s?batchSize=%d",
                            secondaryConfig.getPrimaryBaseUrlRestful(), URL_SUFFIX, secondaryConfig.getBatchSize())
        );
        final URI uri = uriBuilder.build();
        log.info("uri = {}", uri);
        return uri;
    }

    private JaxRsResponse callPrimary(final URI uri) throws StatusException {
        final JaxRsResponse response;
        final JaxRsClient jaxRsClient = new JaxRsClientDefault();
        try {
            final String user = secondaryConfig.getPrimaryUser();
            final String password = secondaryConfig.getPrimaryPassword();
            response = jaxRsClient.get(uri, CommandsDto.class, JaxRsClient.ReprType.ACTION_RESULT, user, password);
            int status = response.getStatus();
            if(status != Response.Status.OK.getStatusCode()) {
                final String entity = readEntityFrom(response);
                if(entity != null) {
                    log.warn("status: {}, entity: \n{}", status, entity);
                } else {
                    log.warn("status: {}, unable to read entity from response", status);
                }
                throw new StatusException(SecondaryStatus.REST_CALL_FAILING);
            }
        } catch(Exception ex) {
            log.warn("rest call failed", ex);
            throw new StatusException(SecondaryStatus.REST_CALL_FAILING, ex);
        }
        return response;
    }

    private CommandsDto unmarshal(final JaxRsResponse response, final URI uri) throws StatusException {
        CommandsDto commandsDto;
        String entity = "<unable to read from response entity>";
        try {
            entity = readEntityFrom(response);
            final JaxbService jaxbService = new Simple();
            commandsDto = jaxbService.fromXml(CommandsDto.class, entity);
            log.debug("commands:\n{}", entity);
        } catch(Exception ex) {
            log.warn("unable to unmarshal entity from {} to CommandsDto.class; was:\n{}", uri, entity);
            throw new StatusException(SecondaryStatus.FAILED_TO_UNMARSHALL_RESPONSE, ex);
        }
        return commandsDto;
    }

    private static String readEntityFrom(final JaxRsResponse response) {
        try {
            return response.readEntity(String.class);
        } catch(Exception e) {
            return null;
        }
    }

    @Inject
    SecondaryConfig secondaryConfig;

}
