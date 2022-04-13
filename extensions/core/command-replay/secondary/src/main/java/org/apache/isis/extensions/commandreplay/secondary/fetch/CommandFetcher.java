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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.client.SuppressionType;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.jaxb.JaxbService.Simple;
import org.apache.isis.extensions.commandlog.model.IsisModuleExtCommandLogApplib;
import org.apache.isis.extensions.commandlog.model.command.CommandModel;
import org.apache.isis.extensions.commandreplay.secondary.SecondaryStatus;
import org.apache.isis.extensions.commandreplay.secondary.StatusException;
import org.apache.isis.extensions.commandreplay.secondary.config.SecondaryConfig;
import org.apache.isis.schema.cmd.v2.CommandDto;
import org.apache.isis.schema.cmd.v2.CommandsDto;
import org.apache.isis.viewer.restfulobjects.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.client.RestfulClientConfig;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;


/**
 * @since 2.0 {@index}
 */
@Service()
@Named(IsisModuleExtCommandLogApplib.NAMESPACE_SECONDARY + ".CommandFetcher")
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE) // JUnit Support
@Log4j2
public class CommandFetcher {

    static final String URL_SUFFIX =
            "services/"
            + IsisModuleExtCommandLogApplib.COMMAND_REPLAY_ON_PRIMARY_SERVICE
            + "/actions/findCommandsOnPrimaryFrom/invoke";

    private final SecondaryConfig secondaryConfig;
    private final boolean useRequestDebugLogging;

    @Inject
    public CommandFetcher(final SecondaryConfig secondaryConfig) {
        this(secondaryConfig, false);
    }

    /**
     * Replicates a single command.
     *
     * @param previousHwmIfAny
     * @throws StatusException
     */
    public List<CommandDto> fetchCommand(
            final @Nullable CommandModel previousHwmIfAny)
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
    private CommandsDto fetchCommands(final CommandModel previousHwmIfAny)
            throws StatusException {

        final UUID transactionId = previousHwmIfAny != null ? previousHwmIfAny.getInteractionId() : null;

        log.debug("finding commands on primary ...");

        final CommandsDto commandsDto = callPrimary(transactionId);

        final int size = commandsDto.getCommandDto().size();
        if(size == 0) {
            return null;
        }
        return commandsDto;
    }

    //TODO simplify
    private String buildUri(final UUID interactionId) {

//        val args = client.arguments()
//                .build();

        val uri =
                interactionId != null
                        ? String.format(
                            "%s?interactionId=%s&batchSize=%d",
                            URL_SUFFIX, interactionId, secondaryConfig.getBatchSize())
                        : String.format(
                            "%s?batchSize=%d",
                            URL_SUFFIX, secondaryConfig.getBatchSize());
        log.info("uri = {}", uri);
        return uri;
    }

    // package private in support of JUnit
    CommandsDto callPrimary(final UUID transactionId) throws StatusException {

        val endpointUri = buildUri(transactionId);
        val client = newClient(secondaryConfig, useRequestDebugLogging );
        val request = client.request(
                endpointUri,
                SuppressionType.RO);

        final Response response = request.get();
        val digest = client.digestList(response, CommandModel.class, new GenericType<List<CommandModel>>(){})
                .mapFailure(failure->{
                    log.warn("rest call failed", failure);
                    return new StatusException(SecondaryStatus.REST_CALL_FAILING);
                })
                .ifFailureFail();

        System.err.printf("%s%n", digest.getValue());

        return null;//unmarshal(digest.getValue().orElse("<unable to read from response entity>"), endpointUri);
    }

    private CommandsDto unmarshal(final String rawValue, final String endpointUri) throws StatusException {
        CommandsDto commandsDto;
        try {
            final JaxbService jaxbService = new Simple();
            commandsDto = jaxbService.fromXml(CommandsDto.class, rawValue);
            log.debug("commands:\n{}", rawValue);
        } catch(Exception ex) {
            log.warn("unable to unmarshal entity from {} to CommandsDto.class; was:\n{}", endpointUri, rawValue);
            throw new StatusException(SecondaryStatus.FAILED_TO_UNMARSHALL_RESPONSE, ex);
        }
        return commandsDto;
    }

    private static RestfulClient newClient(
            final SecondaryConfig secondaryConfig,
            final boolean useRequestDebugLogging) {

        val clientConfig = new RestfulClientConfig();
        clientConfig.setRestfulBase(secondaryConfig.getPrimaryBaseUrlRestful());
        // setup basic-auth
        clientConfig.setUseBasicAuth(true);
        clientConfig.setRestfulAuthUser(secondaryConfig.getPrimaryUser());
        clientConfig.setRestfulAuthPassword(secondaryConfig.getPrimaryPassword());
        // setup request/response debug logging
        clientConfig.setUseRequestDebugLogging(useRequestDebugLogging);

        val client = RestfulClient.ofConfig(clientConfig);
        return client;
    }

}
