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

package org.apache.isis.applib.services.iactn;

import java.sql.Timestamp;
import java.util.List;

import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.HasTransactionId;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.eventbus.AbstractDomainEvent;
import org.apache.isis.applib.services.wrapper.WrapperFactory;

/**
 * Represents an action invocation or property modification, resulting in some state change of the system.
 *
 * <p>
 *     The distinction between {@link Command} and this object is perhaps subtle: the former represents the
 *     intention to invoke an action/edit a property, whereas this represents the actual invocation itself.
 * </p>
 *
 * <p>
 *     To confuse matters slightly, historically the {@link Command} interface defines members (specifically:
 *     {@link Command#getStartedAt()}, {@link Command#getCompletedAt()}, {@link Command#getResult()},
 *     {@link Command#getException()}) which logically belong to this class instead; they remain in {@link Command}
 *     for backward compatibility only (and have been deprecated).
 * </p>
 *
 * <p>
 *     NOTE: this interface might also be considered as representing the (persistence) transaction.  That name was
 *     not chosen however because there is also the system-level transaction that also manages the persistence of
 *     the {@link Command} object.
 * </p>
 *
 */
public interface Interaction extends HasTransactionId {

    //region > startedAt (property)

    /**
     * The date/time at which this interaction started.
     */
    Timestamp getStartedAt();

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     */
    void setStartedAt(Timestamp startedAt);

    //endregion

    //region > completedAt (property)

    /**
     * The date/time at which this interaction completed.
     */
    Timestamp getCompletedAt();

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     */
    void setCompletedAt(Timestamp completedAt);

    //endregion

    //region > push/pop/peek/get/clear (Abstract)DomainEvents

    interface ExecutionGraph {
        AbstractDomainEvent<?> getEvent();
        List<AbstractDomainEvent<?>> getChildEvents();
    }

    /**
     * The current (most recently pushed) {@link org.apache.isis.applib.services.eventbus.AbstractDomainEvent}.
     *
     * <p>
     *     Note that the {@link org.apache.isis.applib.services.eventbus.AbstractDomainEvent} itself is mutable,
     *     as its {@link AbstractDomainEvent#getEventPhase()} phase} changes from
     *     {@link org.apache.isis.applib.services.eventbus.AbstractDomainEvent.Phase#EXECUTING executing} to
     *     {@link org.apache.isis.applib.services.eventbus.AbstractDomainEvent.Phase#EXECUTED executed}.  The
     *     event returned from this method will always be in one or other of these phases.
     * </p>
     */
    @Programmatic
    AbstractDomainEvent<?> peekDomainEvent();

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     *
     * <p>
     * Push a new {@link org.apache.isis.applib.services.eventbus.AbstractDomainEvent}
     * onto the stack of events held by the command.
     * </p>
     */
    @Programmatic
    void pushDomainEvent(final AbstractDomainEvent domainEvent);

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     *
     * <p>
     * Pops the top-most  {@link org.apache.isis.applib.services.eventbus.ActionDomainEvent}
     * from the stack of events held by the command.
     * </p>
     */
    @Programmatic
    AbstractDomainEvent<?> popDomainEvent();

    /**
     * Returns a (list of) graph(es) indicating the domain events in the order that they were
     * {@link #pushDomainEvent(AbstractDomainEvent) pushed}.
     *
     * <p>
     *     Each {@link ExecutionGraph} represents a call stack of domain events (action invocations or property edits),
     *     that may in turn cause other domain events to be fired (by virtue of the {@link WrapperFactory}).
     *     The reason that a list is returned is to support bulk command/actions (against multiple targets).  A non-bulk
     *     action will return a list of just one element.
     * </p>
     */
    @Programmatic
    List<ExecutionGraph> getExecutionGraphs();

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     *
     * Clears the set of {@link AbstractDomainEvent}s that have been {@link #pushDomainEvent(AbstractDomainEvent) push}ed.
     */
    @Programmatic
    void clearDomainEvents();

    //endregion


    //region > exception (property)


    @Optional
    String getException();

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     */
    void setException(String stackTrace);

    //endregion

    //region > result (property)


    /**
     * A {@link Bookmark} to the object returned by the action.
     *
     * <p>
     * If the action returned either a domain entity or a simple value (and did not throw an
     * exception) then this object is provided here.
     *
     * <p>
     * For <tt>void</tt> methods and for actions returning collections, the value
     * will be <tt>null</tt>.
     */
    Bookmark getResult();

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     */
    void setResult(Bookmark resultBookmark);

    //endregion


    //region > next (programmatic)

    enum SequenceName {

        /**
         * &quot;pe&quot; - published event.  For objects: multiple such could be dirtied and thus published as
         * separate events.  For actions invocations/property edits : multiple sub-invocations could occur if sub-invocations are made through the {@link WrapperFactory}.
         */
        PUBLISHED_EVENT("pe");

        private final String abbr;
        SequenceName(final String abbr) {
            this.abbr = abbr;
        }
        public String abbr() { return abbr; }
    }


    /**
     * Generates numbers in a named sequence
     *
     * <p>
     * Used to support the <tt>PublishingServiceJdo</tt> implementation whose
     * persisted entities are uniquely identified by a ({@link #getTransactionId() transactionId}, <tt>sequence</tt>)
     * tuple.
     *
     * @param sequenceAbbr - should be {@link SequenceName#abbr()}.
     */
    @Programmatic
    int next(final String sequenceAbbr);

    //endregion

}
