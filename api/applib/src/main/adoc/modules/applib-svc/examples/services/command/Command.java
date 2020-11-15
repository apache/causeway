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
import org.apache.isis.applib.services.HasUniqueId;
import org.apache.isis.applib.services.HasUsername;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.commanddto.HasCommandDto;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.control.AsyncControl;
import org.apache.isis.schema.cmd.v2.CommandDto;

import lombok.Getter;

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
 */
// tag::refguide[]
public class Command implements HasUniqueId, HasUsername, HasCommandDto {

    // end::refguide[]
    /**
     * Unique identifier for the command.
     *
     * <p>
     *     Derived from {@link #getCommandDto()}'s {@link CommandDto#getTransactionId()}
     * </p>
     */
    @Override
    // tag::refguide[]
    public UUID getUniqueId() {                 // <.>
        // ...
        // end::refguide[]
        return commandDto != null
                ? UUID.fromString(commandDto.getTransactionId())
                : null;
    }
    /**
     * The user that created the command.
     *
     * <p>
     *     Derived from {@link #getCommandDto()}'s {@link CommandDto#getUser()}
     * </p>
     */
    @Override
    // tag::refguide[]
    public String getUsername() {               // <.>
        // ...
        // end::refguide[]
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
    // tag::refguide[]
    public Timestamp getTimestamp() {           // <.>
        // ...
        // end::refguide[]
        return commandDto != null
                ? JavaSqlXMLGregorianCalendarMarshalling.toTimestamp(commandDto.getTimestamp())
                : null;
    }

    /**
     * Serializable representation of the action invocation/property edit.
     *
     * <p>
     *     When the framework sets this (through an internal API), it is
     *     expected to have {@link CommandDto#getTransactionId()},
     *     {@link CommandDto#getUser()}, {@link CommandDto#getTimestamp()},
     *     {@link CommandDto#getTargets()} and {@link CommandDto#getMember()}
     *     to be populated.  The {@link #getUniqueId()}, {@link #getUsername()},
     *     {@link #getTimestamp()} and {@link #getTarget()} are all derived
     *     from the provided {@link CommandDto}.
     * </p>
     */
    // tag::refguide[]
    @Getter
    private CommandDto commandDto;              // <.>
    // end::refguide[]

    /**
     * Derived from {@link #getCommandDto()}, is the {@link Bookmark} of
     * the target object (entity or service) on which this action/edit was performed.
     */
    // tag::refguide[]
    public Bookmark getTarget() {               // <.>
        return commandDto != null
                ? Bookmark.from(commandDto.getTargets().getOid().get(0))
                : null;
    }
    // end::refguide[]

    /**
     * Derived from {@link #getCommandDto()}, holds a string
     * representation of the invoked action, or the edited property.
     */
    // tag::refguide[]
    public String getLogicalMemberIdentifier() {    // <.>
        return commandDto != null
                    ? commandDto.getMember().getLogicalMemberIdentifier()
                    : null;
    }
    // end::refguide[]

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
    // tag::refguide[]
    @Getter
    private Command parent;                     // <.>
    // end::refguide[]

    /**
     * For an command that has actually been executed, holds the date/time at
     * which the {@link Interaction} that executed the command started.
     *
     * @see Interaction#getCurrentExecution()
     * @see Interaction.Execution#getStartedAt()
     */
    // tag::refguide[]
    @Getter
    private Timestamp startedAt;                // <.>
    // end::refguide[]

    /**
     * For an command that has actually been executed, holds the date/time at which the {@link Interaction} that
     * executed the command completed.
     *
     * <p>
     *     Previously this field was deprecated (on the basis that the completedAt is also held in
     *     {@link Interaction.Execution#getCompletedAt()}). However, this property is now used in master/slave
     *     replay scenarios which may query a persisted Command.
     * </p>
     *
     * See also {@link Interaction#getCurrentExecution()} and
     * {@link Interaction.Execution#getCompletedAt()}.
     */
    // tag::refguide[]
    @Getter
    private Timestamp completedAt;              // <.>
    // end::refguide[]

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
     * {@link org.apache.isis.applib.services.iactn.Interaction.Execution#getReturned()}.
     */
    // tag::refguide[]
    @Getter
    private Bookmark result;                    // <.>
    // end::refguide[]

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
     * {@link org.apache.isis.applib.services.iactn.Interaction.Execution#getThrew()}.
     */
    // tag::refguide[]
    @Getter
    private Throwable exception;                    // <.>
    // end::refguide[]

    /**
     * Whether this command resulted in a change of state to the system.
     *
     * <p>
     *     This can be used as a hint to decide whether to persist the command
     *     to a datastore, for example for auditing (though
     *     {@link org.apache.isis.applib.services.publish.PublisherService} is
     *     an alternative for that use case) or so that it can be retrieved
     *     and replayed on another system, eg for regression testing.
     * </p>
     *
     */
    // tag::refguide[]
    @Getter
    private boolean systemStateChanged;                // <.>
    // end::refguide[]


    /**
     * Whether this command has been reified
     */
    // tag::refguide[]
    @Getter
    private boolean reified;
    // end::refguide[]

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
        }
        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         *
         * <p>
         *     Only populated for async commands created through the
         *     {@link WrapperFactory}.
         * </p>
         */
        public void setParent(Command parent) {
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
        public void setStartedAt(Timestamp startedAt) {
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
        public void setResult(final Bookmark result) {
            Command.this.result = result;
        }

        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         */
        @Override
        public void setException(final Throwable exception) {
            Command.this.exception = exception;
        }
        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         *
         * <p>
         * Hint that this {@link Command} has resulted in a change of state to the system.
         * Implementations can use this to persist the command, for example.
         * </p>
         */
        public void setSystemStateChanged(boolean systemStateChanged) {
            Command.this.systemStateChanged = systemStateChanged;
        }

        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         *
         * <p>
         * Hint that this {@link Command} has resulted in a change of state to the system.
         * Implementations can use this to persist the command, for example.
         * </p>
         */
        public void setReified(boolean reified) {
            Command.this.reified = reified;
        }

    };

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     */
    public Updater updater() {
        return UPDATER;
    }


// tag::refguide[]

}
// end::refguide[]
