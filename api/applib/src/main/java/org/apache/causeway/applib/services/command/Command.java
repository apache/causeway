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
package org.apache.causeway.applib.services.command;

import java.sql.Timestamp;
import java.util.UUID;

import org.apache.causeway.applib.events.domain.ActionDomainEvent;
import org.apache.causeway.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.causeway.applib.mixins.security.HasUsername;
import org.apache.causeway.applib.mixins.system.HasInteractionId;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.commanddto.HasCommandDto;
import org.apache.causeway.applib.services.iactn.Execution;
import org.apache.causeway.applib.services.iactn.Interaction;
import org.apache.causeway.applib.services.publishing.spi.CommandSubscriber;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.applib.services.wrapper.control.AsyncControl;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.schema.cmd.v2.CommandDto;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

/**
 * Represents the <i>intention to</i> invoke either an action or modify a property.  There can be only one such
 * intention per (web) request, so a command is in effect interaction-scoped.
 *
 * <p>
 * Each Command holds a {@link CommandDto} (see Apache Causeway <a href="https://causeway.apache.org/schema/cmd/">cmd</a> schema)
 * which reifies all the details in a serializable form.
 * </p>
 *
 * <p>
 *     It also captures details of the corresponding action invocation (or property edit),
 *     specifically when that action/edit {@link Command#getStartedAt() started} or
 *     {@link Command#getCompletedAt() completed}, and its result, either a {@link Command#getResult() return value}
 *     or an {@link Command#getException() exception}.  Also captures a stack of {@link ActionDomainEvent}s.
 * </p>
 *
 * <p>
 *     Note that when invoking an action, other actions may be invoked courtesy
 *     of the {@link WrapperFactory}.  These "sub-actions" do <i>not</i> modify
 *     the contents of the current command object; in other words think of the command
 *     object as representing the outer-most originating action.
 * </p>
 *
 * <p>
 *     That said, if the sub-action is invoked asynchronously (using
 *     {@link WrapperFactory#asyncWrap(Object, AsyncControl)} or
 *     {@link WrapperFactory#asyncWrapMixin(Class, Object, AsyncControl)}), then
 *     a separate {@link Command} object
 *     is created, and the originating {@link Command} is set to be its
 *     {@link Command#getParentInteractionId() parent}.
 * </p>
 *
 * @since 1.x {@index}
 */
@ToString
@Log4j2
public class Command implements HasInteractionId, HasUsername, HasCommandDto {

    private UUID interactionId;

    public Command(final UUID interactionId) {
        this.interactionId = interactionId;
    }

    /**
     * The unique identifier of this command (inherited from
     * {@link HasInteractionId})
     *
     * <p>
     *     In all cases this be the same as the {@link Interaction} that wraps the command, and can be used
     *     to correlate also to any audit records
     *     ({@link org.apache.causeway.applib.services.publishing.spi.EntityPropertyChange}s resulting from state
     *     changes occurring as a consequence of the command.
     * </p>
     *
     * <p>
     *     Note that this is immutable in almost all cases.  The one exception is if the Command is being executed
     *     through the {@link CommandExecutorService}, for example when executing a async action that has been reified
     *     into a {@link CommandDto}.  In such cases, the {@link CommandExecutorService#executeCommand(CommandDto)}
     *     will <i>replace</i> the original Id with that of the DTO being executed.
     * </p>
     */
    @Override
    public UUID getInteractionId() {
        return interactionId;
    }

    /**
     * The user that created the command.
     *
     * <p>
     *     Derived from {@link #getCommandDto()}'s {@link CommandDto#getUsername()} ()}
     * </p>
     */
    @Override
    public String getUsername() {
        // ...
        return commandDto != null
                ? commandDto.getUsername()
                : null;
    }

    /**
     * The date/time at which this command was created.
     *
     * <p>
     *     Derived from {@link #getCommandDto()}'s {@link CommandDto#getTimestamp()}.
     * </p>
     */
    public Timestamp getTimestamp() {
        // ...
        return commandDto != null
                ? JavaSqlXMLGregorianCalendarMarshalling.toTimestamp(commandDto.getTimestamp())
                : null;
    }

    @ToString.Exclude
    private org.apache.causeway.schema.cmd.v2.CommandDto commandDto;

    /**
     * Serializable representation of the action invocation/property edit.
     *
     * <p>
     *     When the framework sets this (through an internal API), it is
     *     expected that the {@link CommandDto#getUsername() username},
     *     {@link CommandDto#getTimestamp() timestamp}, {@link CommandDto#getTargets() target(s)} and
     *     {@link CommandDto#getMember() member} will be populated.
     *     The {@link #getInteractionId()}, {@link #getUsername()},
     *     {@link #getTimestamp()} and {@link #getTarget()} are all derived
     *     from the provided {@link CommandDto}.
     * </p>
     */
    @Override
    public CommandDto getCommandDto() {
        return commandDto;
    }

    /**
     * Derived from {@link #getCommandDto()}, is the {@link Bookmark} of
     * the target object (entity or service) on which this action/edit was performed.
     */
    @ToString.Include(name = "target")
    public Bookmark getTarget() {
        return commandDto != null
                ? Bookmark.forOidDto(commandDto.getTargets().getOid().get(0))
                : null;
    }

    /**
     * Derived from {@link #getCommandDto()}, holds a string
     * representation of the invoked action, or the edited property.
     */
    @ToString.Include(name = "memberId")
    public String getLogicalMemberIdentifier() {
        return commandDto != null
                    ? commandDto.getMember().getLogicalMemberIdentifier()
                    : null;
    }

    /**
     * For async commands created through the {@link WrapperFactory},
     * captures the {@link Command#getInteractionId() interactionId} of the parent command.
     *
     * <p>
     *     Will return <code>null</code> if there is no parent.
     * </p>
     *
     * @see WrapperFactory#asyncWrap(Object, AsyncControl)
     * @see WrapperFactory#asyncWrapMixin(Class, Object, AsyncControl)
     *
     */
    @ToString.Exclude
    @Getter
    private UUID parentInteractionId;

    /**
     * For an command that has actually been executed, holds the date/time at
     * which the {@link Interaction} that executed the command started.
     *
     * @see Interaction#getCurrentExecution()
     * @see Execution#getStartedAt()
     */
    @Getter
    private Timestamp startedAt;

    /**
     * For an command that has actually been executed, holds the date/time at which the {@link Interaction} that
     * executed the command completed.
     *
     * <p>
     *     Previously this field was deprecated (on the basis that the completedAt is also held in
     *     {@link Execution#getCompletedAt()}). However, this property is now used in master/slave
     *     replay scenarios which may query a persisted Command.
     * </p>
     *
     * See also {@link Interaction#getCurrentExecution()} and
     * {@link Execution#getCompletedAt()}.
     */
    @Getter
    private Timestamp completedAt;

    /**
     * For a command that has actually been executed, holds a {@link Bookmark}
     * to the object returned by the corresponding action/property modification.
     *
     * <p>
     *     This property is used in replay scenarios to verify the outcome of
     *     the replayed command, eg for regression testing.
     * </p>
     *
     * See also  {@link Interaction#getCurrentExecution()} and
     * {@link Execution#getReturned()}.
     */
    @Getter
    private Bookmark result;

    /**
     * For a command that has actually been executed, holds the exception stack
     * trace if the action invocation/property modification threw an exception.
     *
     * <p>
     *     This property is used in replay scenarios to verify the outcome of
     *     the replayed command, eg for regression testing.
     * </p>
     *
     * See also {@link Interaction#getCurrentExecution()} and
     * {@link Execution#getThrew()}.
     */
    @Getter
    private Throwable exception;

    public static enum CommandPublishingPhase {
        /** initial state: do not publish (yet) */
        ONHOLD,
        /**
         * publishing is enabled, and the command will be executed.
         */
        READY,
        /**
         * The command has started to be executed.
         */
        STARTED,
        /**
         * The command has completed its execution.
         */
        COMPLETED;
        public boolean isOnHold() {return this==ONHOLD;}
        public boolean isReady() {return this==READY;}
        public boolean isStarted() {return this==STARTED;}
        public boolean isCompleted() {return this==COMPLETED;}
    }

    /**
     * Whether this command has been enabled for publishing,
     * that is {@link CommandSubscriber}s will be notified when this Command becomes {@link CommandPublishingPhase#READY ready},
     * has {@link CommandPublishingPhase#STARTED started}, and when it {@link CommandPublishingPhase#COMPLETED completes}.
     */
    @Getter private CommandPublishingPhase publishingPhase = CommandPublishingPhase.ONHOLD;

    @ToString.Exclude
    private final Updater UPDATER = new Updater();

    public class Updater implements CommandOutcomeHandler {

        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         *
         * <p>
         * Implementation notes: set when the action is invoked (in the <tt>ActionInvocationFacet</tt>).
         * @param commandDto
         */
        public void setCommandDtoAndIdentifier(final CommandDto commandDto) {
            Command.this.commandDto = commandDto;
            Command.this.interactionId = UUID.fromString(commandDto.getInteractionId());
        }
        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         *
         * <p>
         *     Only populated for async commands created through the
         *     {@link WrapperFactory}.
         * </p>
         */
        public void setParentInteractionId(final UUID parentInteractionId) {
            Command.this.parentInteractionId = parentInteractionId;
        }
        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         */
        @Override
        public Timestamp getStartedAt() {
            return Command.this.getStartedAt();
        }
        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         */
        @Override
        public void setStartedAt(final Timestamp startedAt) {
            Command.this.startedAt = startedAt;
        }
        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         */
        @Override
        public void setCompletedAt(final Timestamp completed) {
            Command.this.completedAt = completed;
        }
        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         */
        @Override
        public void setResult(final Try<Bookmark> resultBookmark) {
            Command.this.result = resultBookmark.getValue().orElse(null);
            Command.this.exception = resultBookmark.getFailure().orElse(null);
        }

        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         */
        public void setPublishingPhase(final @NonNull CommandPublishingPhase publishingPhase) {
            if(Command.this.publishingPhase.isCompleted()) {
                return; // don't ever change when phase is completed
            }
            Command.this.publishingPhase = publishingPhase;
        }
    };

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     */
    public Updater updater() {
        return UPDATER;
    }

}
