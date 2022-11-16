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

import lombok.val;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import javax.inject.Inject;

import org.apache.causeway.applib.jaxb.JavaSqlJaxbAdapters;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.applib.services.wrapper.callable.AsyncCallable;
import org.apache.causeway.applib.services.wrapper.control.AsyncControl;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.common.v2.PeriodDto;
import org.springframework.stereotype.Service;

/**
 * Allows the execution of action invocations or property edits to be deferred so that they can be executed later in
 * another thread of execution.
 *
 * <p>
 *     Typically this other thread of execution would be scheduled from quartz or similar.  The
 *     {@link org.apache.causeway.extensions.commandlog.applib.job.RunBackgroundCommandsJob} provides a ready-made
 *     implementation to do this for quartz.
 * </p>
 *
 * @see WrapperFactory
 * @since 2.0 {@index}
 */
@Service
public class BackgroundService {

    @Inject WrapperFactory wrapperFactory;
    @Inject PersistCommandExecutorService persistCommandExecutorService;

    /**
     * Wraps the domain object in a proxy whereby any actions invoked through the proxy will instead be persisted as a
     * {@link ExecuteIn#BACKGROUND background} {@link CommandLogEntry command log entry}.
     *
     * @see #executeMixin(Class, Object) - to invoke actions that are implemented as mixins
     */
    public <T> T execute(T object) {
        return wrapperFactory.asyncWrap(object, AsyncControl.returningVoid().withCheckRules()
                .with(persistCommandExecutorService)
        );
    }
    /**
     * Wraps the domain object in a proxy whereby any actions invoked through the proxy will instead be persisted as a
     * {@link ExecuteIn#BACKGROUND background} {@link CommandLogEntry command log entry}.
     *
     * @see #executeMixin(Class, Object) - to invoke actions that are implemented as mixins
     */
    public <T> T executeSkipRules(T object) {
        return wrapperFactory.asyncWrap(object, AsyncControl.returningVoid().withSkipRules()
                .with(persistCommandExecutorService)
        );
    }

    /**
     * Wraps a mixin object in a proxy whereby invoking that mixin will instead be persisted as a
     * {@link ExecuteIn#BACKGROUND background} {@link CommandLogEntry command log entry}.
     *
     * @see #execute(Object) - to invoke actions that are implemented directly within the object
     */
    public <T> T executeMixin(Class<T> mixinClass, Object mixedIn) {
        return wrapperFactory.asyncWrapMixin(mixinClass, mixedIn, AsyncControl.returningVoid().withCheckRules()
                .with(persistCommandExecutorService)
        );
    }

    /**
     * Wraps a mixin object in a proxy whereby invoking that mixin will instead be persisted as a
     * {@link ExecuteIn#BACKGROUND background} {@link CommandLogEntry command log entry}.
     *
     * @see #execute(Object) - to invoke actions that are implemented directly within the object
     */
    public <T> T executeMixinSkipRules(Class<T> mixinClass, Object mixedIn) {
        return wrapperFactory.asyncWrapMixin(mixinClass, mixedIn, AsyncControl.returningVoid().withSkipRules()
                .with(persistCommandExecutorService)
        );
    }

    /**
     * @since 2.0 {@index}
     */
    @Service
    public static class PersistCommandExecutorService implements ExecutorService {

        @Inject CommandLogEntryRepository<? extends CommandLogEntry> commandLogEntryRepository;
        @Inject InteractionService interactionService;

        private final static JavaSqlJaxbAdapters.TimestampToXMLGregorianCalendarAdapter gregorianCalendarAdapter  = new JavaSqlJaxbAdapters.TimestampToXMLGregorianCalendarAdapter();;

        @Override
        public <T> Future<T> submit(Callable<T> task) {
            val callable = (AsyncCallable<T>) task;
            val commandDto = callable.getCommandDto();

            // we'll mutate the commandDto in line with the callable, then
            // create the CommandLogEntry from that commandDto
            val childInteractionId = UUID.randomUUID();
            commandDto.setInteractionId(childInteractionId.toString());

            // copy details from requested interaction context into the commandDto
            val interactionContext = callable.getInteractionContext();
            val timestamp = interactionContext.getClock().nowAsJavaSqlTimestamp();
            commandDto.setTimestamp(gregorianCalendarAdapter.marshal(timestamp));

            val username = interactionContext.getUser().getName();
            commandDto.setUsername(username);

            val periodDto = new PeriodDto();
            periodDto.setStartedAt(null);
            periodDto.setCompletedAt(null);
            commandDto.setTimings(periodDto);

            val childCommand = newCommand(commandDto);

            commandLogEntryRepository.createEntryAndPersist(childCommand, callable.getParentInteractionId(), ExecuteIn.BACKGROUND);

            // a more sophisticated implementation could perhaps return a Future that supports these methods by
            // querying the CommandLogEntryRepository
            return new Future<T>() {
                @Override
                public boolean cancel(boolean mayInterruptIfRunning) {
                    throw new IllegalStateException("Not implemented");
                }
                @Override
                public boolean isCancelled() {
                    throw new IllegalStateException("Not implemented");
                }

                @Override
                public boolean isDone() {
                    throw new IllegalStateException("Not implemented");
                }

                @Override
                public T get() {
                    throw new IllegalStateException("Not implemented");
                }

                @Override
                public T get(long timeout, TimeUnit unit) {
                    throw new IllegalStateException("Not implemented");
                }
            };
        }

        private static Command newCommand(CommandDto commandDto) {
            return new Command(UUID.fromString(commandDto.getInteractionId())) {
                @Override public String getUsername() {return commandDto.getUsername();}
                @Override public Timestamp getTimestamp() {return gregorianCalendarAdapter.unmarshal(commandDto.getTimestamp());}
                @Override public CommandDto getCommandDto() {return commandDto;}
                @Override public String getLogicalMemberIdentifier() {return commandDto.getMember().getLogicalMemberIdentifier();}
                @Override public Bookmark getTarget() {return Bookmark.forOidDto(commandDto.getTargets().getOid().get(0));}
                @Override public Timestamp getStartedAt() {return gregorianCalendarAdapter.unmarshal(commandDto.getTimings().getStartedAt());}
                @Override public Timestamp getCompletedAt() {return gregorianCalendarAdapter.unmarshal(commandDto.getTimings().getCompletedAt());}
                @Override public Bookmark getResult() {return null;}
                @Override public Throwable getException() {return null;}
            };
        }


        @Override
        public <T> Future<T> submit(Runnable task, T result) {
            throw new IllegalStateException("Not implemented");
        }

        @Override
        public Future<?> submit(Runnable task) {
            throw new IllegalStateException("Not implemented");
        }

        @Override
        public void execute(Runnable command) {
            throw new IllegalStateException("Not implemented");
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) {
            throw new IllegalStateException("Not implemented");
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
            throw new IllegalStateException("Not implemented");
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
            throw new IllegalStateException("Not implemented");
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            throw new IllegalStateException("Not implemented");
        }

        @Override
        public void shutdown() {
            throw new IllegalStateException("Not implemented");
        }

        @Override
        public List<Runnable> shutdownNow() {
            throw new IllegalStateException("Not implemented");
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            throw new IllegalStateException("Not implemented");
        }

        @Override
        public boolean isShutdown() {
            return false;
        }

        @Override
        public boolean isTerminated() {
            return false;
        }

    }
}
