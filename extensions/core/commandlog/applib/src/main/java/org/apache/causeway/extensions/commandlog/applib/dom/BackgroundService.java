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
package org.apache.causeway.extensions.commandlog.applib.dom;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;

import jakarta.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.causeway.applib.jaxb.JavaSqlJaxbAdapters;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.applib.services.wrapper.WrapperFactory.AsyncProxy;
import org.apache.causeway.applib.services.wrapper.control.AsyncControl;
import org.apache.causeway.applib.services.wrapper.control.SyncControl;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.common.v2.PeriodDto;

/**
 * Allows the execution of action invocations or property edits to be deferred so that they can be executed later in
 * another thread of execution.
 *
 * <p>Typically this other thread of execution would be scheduled from quartz or similar.  The
 * {@link org.apache.causeway.extensions.commandlog.applib.job.RunBackgroundCommandsJob} provides a ready-made
 * implementation to do this for quartz.
 *
 * @see WrapperFactory
 * @see org.apache.causeway.extensions.commandlog.applib.fakescheduler.FakeScheduler
 * @since 2.0 revised for 3.4 {@index}
 */
@Service
public class BackgroundService {

    @Inject WrapperFactory wrapperFactory;
    @Inject CommandLogEntryRepository commandLogEntryRepository;

    /**
     * Wraps the domain object in a proxy whereby any actions invoked through the proxy will instead be persisted as a
     * {@link ExecuteIn#BACKGROUND background} {@link CommandLogEntry command log entry}.
     *
     * @see #executeMixin(Class, Object) - to invoke actions that are implemented as mixins
     */
    public <T> AsyncProxy<T> execute(final T object) {
        return wrapperFactory.asyncWrap(object, asyncControl().withCheckRules());
    }
    /**
     * Wraps the domain object in a proxy whereby any actions invoked through the proxy will instead be persisted as a
     * {@link ExecuteIn#BACKGROUND background} {@link CommandLogEntry command log entry}.
     *
     * @see #executeMixin(Class, Object) - to invoke actions that are implemented as mixins
     */
    public <T> AsyncProxy<T> executeSkipRules(final T object) {
        return wrapperFactory.asyncWrap(object, asyncControl().withSkipRules());
    }

    /**
     * Wraps a mixin object in a proxy whereby invoking that mixin will instead be persisted as a
     * {@link ExecuteIn#BACKGROUND background} {@link CommandLogEntry command log entry}.
     *
     * @see #execute(Object) - to invoke actions that are implemented directly within the object
     */
    public <T> AsyncProxy<T> executeMixin(final Class<T> mixinClass, final Object mixedIn) {
        return wrapperFactory.asyncWrapMixin(mixinClass, mixedIn, asyncControl().withCheckRules());
    }

    /**
     * Wraps a mixin object in a proxy whereby invoking that mixin will instead be persisted as a
     * {@link ExecuteIn#BACKGROUND background} {@link CommandLogEntry command log entry}.
     *
     * @see #execute(Object) - to invoke actions that are implemented directly within the object
     */
    public <T> AsyncProxy<T> executeMixinSkipRules(final Class<T> mixinClass, final Object mixedIn) {
        return wrapperFactory.asyncWrapMixin(mixinClass, mixedIn, asyncControl().withSkipRules());
    }

    // -- HELPER

    AsyncControl asyncControl() {
        return AsyncControl.defaults()
            .with(ForkJoinPool.commonPool())
            .withNoExecute()
            .listen(new CommandPersistor(commandLogEntryRepository));
    }

    record CommandPersistor(CommandLogEntryRepository commandLogEntryRepository) implements SyncControl.CommandListener {

        @Override
        public void onCommand(
                final UUID parentInteractionId,
                final InteractionContext interactionContext,
                final CommandDto commandDto) {

            // we'll mutate the commandDto in line with the callable, then
            // create the CommandLogEntry from that commandDto
            commandDto.setInteractionId(UUID.randomUUID().toString());

            // copy details from requested interaction context into the commandDto
            commandDto.setTimestamp(GREGORIAN_CALENDAR_ADAPTER.marshal(interactionContext.getClock().nowAsJavaSqlTimestamp()));
            commandDto.setUsername(interactionContext.getUser().name());

            var periodDto = new PeriodDto();
            periodDto.setStartedAt(null);
            periodDto.setCompletedAt(null);
            commandDto.setTimings(periodDto);

            var childCommand = newCommand(commandDto);

            commandLogEntryRepository.createEntryAndPersist(childCommand, parentInteractionId, ExecuteIn.BACKGROUND);
        }

        // -- HELPER

        private final static JavaSqlJaxbAdapters.TimestampToXMLGregorianCalendarAdapter GREGORIAN_CALENDAR_ADAPTER
            = new JavaSqlJaxbAdapters.TimestampToXMLGregorianCalendarAdapter();

        private static Command newCommand(final CommandDto commandDto) {
            return new Command(UUID.fromString(commandDto.getInteractionId())) {
                @Override public String getUsername() {return commandDto.getUsername();}
                @Override public Timestamp getTimestamp() {return GREGORIAN_CALENDAR_ADAPTER.unmarshal(commandDto.getTimestamp());}
                @Override public CommandDto getCommandDto() {return commandDto;}
                @Override public String getLogicalMemberIdentifier() {return commandDto.getMember().getLogicalMemberIdentifier();}
                @Override public Bookmark getTarget() {return Bookmark.forOidDto(commandDto.getTargets().getOid().get(0));}
                @Override public Timestamp getStartedAt() {return GREGORIAN_CALENDAR_ADAPTER.unmarshal(commandDto.getTimings().getStartedAt());}
                @Override public Timestamp getCompletedAt() {return GREGORIAN_CALENDAR_ADAPTER.unmarshal(commandDto.getTimings().getCompletedAt());}
                @Override public Bookmark getResult() {return null;}
                @Override public Throwable getException() {return null;}
            };
        }

    }
}
