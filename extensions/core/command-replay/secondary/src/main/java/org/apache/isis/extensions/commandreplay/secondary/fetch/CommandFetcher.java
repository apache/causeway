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
import org.apache.isis.commons.collections.Can;
import org.apache.isis.extensions.commandlog.applib.IsisModuleExtCommandLogApplib;
import org.apache.isis.extensions.commandlog.applib.command.ICommandLog;
import org.apache.isis.extensions.commandreplay.secondary.config.SecondaryConfig;
import org.apache.isis.extensions.commandreplay.secondary.status.SecondaryStatus;
import org.apache.isis.extensions.commandreplay.secondary.status.StatusException;
import org.apache.isis.schema.cmd.v2.CommandDto;
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
@Named(IsisModuleExtCommandLogApplib.NAMESPACE_REPLAY_SECONDARY + ".CommandFetcher")
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE) // JUnit Support
@Log4j2
public class CommandFetcher {

    static final String URL_SUFFIX =
            "services/"
            + IsisModuleExtCommandLogApplib.SERVICE_REPLAY_PRIMARY_COMMAND_RETRIEVAL
            + "/actions/findCommandsOnPrimaryAsDto/invoke";

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
    public Can<CommandDto> fetchCommand(
            final @Nullable ICommandLog previousHwmIfAny)
            throws StatusException {

        log.debug("finding command on primary ...");

        return fetchCommands(previousHwmIfAny);
    }

    /**
     * @return - the commands, or <tt>null</tt> if none were found
     * @param previousHwmIfAny
     * @throws StatusException
     */
    private Can<CommandDto> fetchCommands(final ICommandLog previousHwmIfAny)
            throws StatusException {

        final UUID transactionId = previousHwmIfAny != null
                ? previousHwmIfAny.getInteractionId()
                : null;

        log.debug("finding commands on primary ...");

        val commands = callPrimary(transactionId);
        return commands;
    }

    // package private in support of JUnit
    Can<CommandDto> callPrimary(final @Nullable UUID interactionId) throws StatusException {

        val client = newClient(secondaryConfig, useRequestDebugLogging);
        val request = client.request(
                URL_SUFFIX,
                SuppressionType.RO);

        val args = client.arguments()
                .addActionParameter("interactionId", interactionId!=null ? interactionId.toString() : null)
                .addActionParameter("batchSize", secondaryConfig.getBatchSize())
                .build();

        final Response response = request.post(args);
        val digest = client.digestList(response, CommandDto.class, new GenericType<List<CommandDto>>(){})
                .mapFailure(failure->{
                    log.warn("rest call failed", failure);
                    return new StatusException(SecondaryStatus.REST_CALL_FAILING);
                })
                .ifFailureFail();

        return digest.getValue().orElseThrow();
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
