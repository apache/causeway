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

import java.sql.Timestamp;
import java.util.UUID;

import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.isis.applib.mixins.security.HasUsername;
import org.apache.isis.applib.mixins.system.HasInteractionId;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.commanddto.HasCommandDto;
import org.apache.isis.applib.services.iactn.Execution;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.publishing.spi.CommandSubscriber;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.control.AsyncControl;
import org.apache.isis.commons.functional.Result;
import org.apache.isis.schema.cmd.v2.CommandDto;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Represents the <i>intention to</i> invoke either an action or modify a property.  There can be only one such
 * intention per (web) request, so a command is in effect interaction-scoped.
 *
 * <p>
 * Each Command holds a {@link CommandDto} (see Apache Isis <a href="http://isis.apache.org/schema/cmd/">cmd</a> schema)
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
 *     {@link Command#getParent() parent}.
 * </p>
 *
 * @since 1.x {@index}
 */
@RequiredArgsConstructor
@ToString
@Log4j2
public class Command implements HasInteractionId, HasUsername, HasCommandDto {

    /**
     * Unique identifier for the command.
     *
     * <p>
     *     Derived from {@link #getCommandDto()}'s {@link CommandDto#getInteractionId()}
     * </p>
     */
    @Getter
        (onMethod_ = {@Override})
    private final UUID interactionId;

    /**
     * The user that created the command.
     *
     * <p>
     *     Derived from {@link #getCommandDto()}'s {@link CommandDto#getUser()}
     * </p>
     */
    @Override
    public String getUsername() {
        // ...
        return commandDto != null
                ? commandDto.getUser()
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

    /**
     * Serializable representation of the action invocation/property edit.
     *
     * <p>
     *     When the framework sets this (through an internal API), it is
     *     expected to have {@link CommandDto#getInteractionId()},
     *     {@link CommandDto#getUser()}, {@link CommandDto#getTimestamp()},
     *     {@link CommandDto#getTargets()} and {@link CommandDto#getMember()}
     *     to be populated.  The {@link #getInteractionId()}, {@link #getUsername()},
     *     {@link #getTimestamp()} and {@link #getTarget()} are all derived
     *     from the provided {@link CommandDto}.
     * </p>
     */
    @ToString.Exclude
    @Getter
    private CommandDto commandDto;

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
     * captures the parent command.
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
    private Command parent;

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

    /**
     * Whether this command resulted in a change of state to the system.
     *
     * <p>
     *     This can be used as a hint to decide whether to persist the command
     *     to a datastore, for example for auditing (though
     *     {@link org.apache.isis.applib.services.publishing.spi.ExecutionSubscriber} is
     *     an alternative for that use case) or so that it can be retrieved
     *     and replayed on another system, eg for regression testing.
     * </p>
     *
     */
    @Getter
    private boolean systemStateChanged;

    public static enum CommandPublishingPhase {
        /** initial state: do not publish (yet) */
        ONHOLD,
        /** publishing is enabled */
        READY,
        /** publishing has completed */
        COMPLETED;
        public boolean isOnhold() {return this==ONHOLD;}
        public boolean isReady() {return this==READY;}
        public boolean isCompleted() {return this==COMPLETED;}
    }

    /**
     * Whether this command has been enabled for publishing,
     * that is {@link CommandSubscriber}s will be notified when this Command completes.
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
        public void setCommandDto(final CommandDto commandDto) {
            Command.this.commandDto = commandDto;

            // should be redundant, but we ensure commandInteractionId == dtoInteractionId
            val commandInteractionId = Command.this.getInteractionId().toString();
            val dtoInteractionId = commandDto.getInteractionId();

            if(!commandInteractionId.equals(dtoInteractionId)) {
                log.warn("setting CommandDto on a Command has side-effects if "
                        + "their InteractionIds don't match; forcing CommandDto's Id to be same as Command's");
                commandDto.setInteractionId(commandInteractionId);
            }

        }
        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         *
         * <p>
         *     Only populated for async commands created through the
         *     {@link WrapperFactory}.
         * </p>
         */
        public void setParent(final Command parent) {
            Command.this.parent = parent;
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
        public void setResult(final Result<Bookmark> resultBookmark) {
            Command.this.result = resultBookmark.getValue().orElse(null);
            Command.this.exception = resultBookmark.getFailure().orElse(null);
        }

        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         *
         * <p>
         * Hint that this {@link Command} has resulted in a change of state to the system.
         * Implementations can use this to persist the command, for example.
         * </p>
         */
        public void setSystemStateChanged(final boolean systemStateChanged) {
            Command.this.systemStateChanged = systemStateChanged;
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
