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
package org.apache.isis.applib.services.command;

import java.time.Instant;
import java.util.function.BiFunction;

import org.apache.isis.applib.clock.VirtualClock;
import org.apache.isis.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.applib.services.user.UserMemento;
import org.apache.isis.schema.cmd.v2.CommandDto;

import lombok.val;

/**
 * Provides a mechanism to execute a {@link Command}.
 *
 * @see org.apache.isis.applib.services.iactnlayer.InteractionService
 * @see org.apache.isis.applib.services.iactnlayer.InteractionLayerTracker
 *
 * @since 1.x {@index}
 */
public interface CommandExecutorService {

    /**
     * Determines the {@link org.apache.isis.applib.services.iactnlayer.InteractionContext} (the &quot;who&quot;
     * &quot;when&quot; and &quot;where&quot;) within which the {@link Command} should be executed.
     *
     * @since 1.x {@index}
     */
    enum SudoPolicy {

        /**
         * Execute within the same {@link org.apache.isis.applib.services.iactnlayer.InteractionContext} as the
         * thread calling the {@link CommandExecutorService}.
         *
         * <p>
         * For example, regular background commands.
         * </p>
         */
        NO_SWITCH((interactionContext, commandDto) -> interactionContext),

        /**
         * Execute using an {@link org.apache.isis.applib.services.iactnlayer.InteractionContext}, with the apparent
         * user being taken from the {@link Command}.
         */
        SWITCH_USER_ONLY((interactionContext, commandDto) -> {
            return interactionContext.withUser(UserMemento.ofName(commandDto.getUser()));
        }),

        /**
         * Execute using an {@link org.apache.isis.applib.services.iactnlayer.InteractionContext}, with the apparent
         * user and time being taken from the {@link Command}.
         *
         * <p>
         * For example, replayable commands.
         * </p>
         */
        SWITCH_USER_AND_TIME((interactionContext, commandDto) -> {
            return interactionContext.withUser(UserMemento.ofName(commandDto.getUser()))
                                     .withClock(VirtualClock.nowAt(timestampOf(commandDto)));
        }),
        ;

        private static Instant timestampOf(CommandDto commandDto) {
            val timestampGc = commandDto.getTimestamp();
            val javaSqlTimestamp = JavaSqlXMLGregorianCalendarMarshalling.toTimestamp(timestampGc);
            return javaSqlTimestamp.toInstant();
        }

        public final BiFunction<InteractionContext, CommandDto, InteractionContext> mapper;

        private SudoPolicy(BiFunction<InteractionContext, CommandDto, InteractionContext> mapper) {
            this.mapper = mapper;
        }
    }

    /**
     * Executes the specified {@link Command} using the required {@link SudoPolicy}, updating the Command (or its
     * persistent equivalent) afterwards (for example, setting its {@link Command#getCommandDto() commandDto} field.
     *
     * @param sudoPolicy - policy to use
     * @param command - the {@link Command} to be executed
     * @return - a bookmark representing the result of executing the command (could be null)
     */
    Bookmark executeCommand(
            SudoPolicy sudoPolicy,
            Command command
    );

    /**
     * Executes the specified command (represented as a {@link CommandDto} using the required {@link SudoPolicy}.
     *
     * <p>
     *     IMPORTANT: THIS METHOD HAS SIGNIFICANT SIDE-EFFECTS.  Specifically, the {@link Command} of the executing
     *     thread (obtained using {@link org.apache.isis.applib.services.iactn.InteractionProvider} to obtain the
     *     {@link Interaction}, and then {@link Interaction#getCommand()} to obtain the {@link Command}) will be
     *     UPDATED to hold the {@link CommandDto} passed in.
     * </p>
     *
     * <p>
     * Optionally an {@link CommandOutcomeHandler outcome handler} can be provided to process the result.  This is
     * used by the persistent implementations to update their respective persistent equivalents of {@link Command}.
     * </p>
     *
     * @param sudoPolicy - policy to use
     * @param commandDto - the {@link CommandDto} to be executed
     * @param outcomeHandler - callback to handle the result
     *
     * @return - a bookmark representing the result of executing the command (could be null)
     */
    Bookmark executeCommand(
            SudoPolicy sudoPolicy,
            CommandDto commandDto,
            CommandOutcomeHandler outcomeHandler);

    /**
     * As per {@link #executeCommand(SudoPolicy, Command)}, with a policy of {@link SudoPolicy#NO_SWITCH no switch}.
     *
     * <p>
     *     Note that this method updates the Command as a side-effect.
     * </p>
     *
     * @see #executeCommand(SudoPolicy, Command)
     */
    Bookmark executeCommand(
            Command command
    );

    /**
     * As per {@link #executeCommand(SudoPolicy, CommandDto, CommandOutcomeHandler)}, with a policy of {@link SudoPolicy#NO_SWITCH no switch}.
     *
     * <p>
     *     Note that this method has significant side-effects.
     * </p>
     *
     * @see #executeCommand(SudoPolicy, CommandDto, CommandOutcomeHandler)
     *
     * @param commandDto
     * @param outcomeHandler
     * @return
     */
    Bookmark executeCommand(
            CommandDto commandDto,
            CommandOutcomeHandler outcomeHandler);

}
