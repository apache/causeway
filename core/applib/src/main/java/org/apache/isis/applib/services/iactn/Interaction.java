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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.HasTransactionId;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.eventbus.AbstractDomainEvent;
import org.apache.isis.applib.services.eventbus.ActionDomainEvent;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.applib.services.eventbus.PropertyDomainEvent;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.schema.common.v1.InteractionType;
import org.apache.isis.schema.common.v1.PeriodDto;
import org.apache.isis.schema.ixn.v1.ActionInvocationDto;
import org.apache.isis.schema.ixn.v1.InteractionExecutionDto;
import org.apache.isis.schema.ixn.v1.PropertyModificationDto;
import org.apache.isis.schema.utils.jaxbadapters.JavaSqlTimestampXmlGregorianCalendarAdapter;

/**
 * Represents an action invocation or property modification, resulting in some state change of the system.  It captures
 * not only the target object and arguments passed, but also builds up the call-graph, and captures metrics, eg
 * for profiling.
 *
 * <p>
 *     The distinction between {@link Command} and this object is perhaps subtle: the former represents the
 *     intention to invoke an action/edit a property, whereas this represents the actual invocation/edit itself.
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
 *     NOTE: you could also think of this interface as being analogous to the (database) transaction.  The name
 *     &quot;Transaction&quot; has not been used for the interface not chosen however because there is also the
 *     system-level transaction that manages the persistence of
 *     the {@link Command} object itself.
 * </p>
 *
 */
public class Interaction implements HasTransactionId {

    //region > transactionId (property)

    @Override
    public UUID getTransactionId() {
        return null;
    }

    @Override
    public void setTransactionId(final UUID transactionId) {

    }
    //endregion

    //region > push/pop/current/get/clear Execution(s)

    private final List<Execution> executionGraphs = Lists.newArrayList();
    private Execution currentExecution;
    private Execution priorExecution;


    /**
     * The execution that preceded the current one.
     */
    public Execution getPriorExecution() {
        return priorExecution;
    }

    /**
     * Implemented by the framework (and modelled after {@link Callable}), is the implementation
     * by which the framework actually performs the interaction.
     * @param <T>
     */
    public interface MemberExecutor<T extends Execution> {
        Object execute(final T currentExecution);
    }



    public Object execute(
            final MemberExecutor<ActionInvocation> memberExecutor,
            final ActionInvocation actionInvocation,
            final ClockService clockService,
            final Command command) {

        pushAndUpdateCommand(actionInvocation, clockService, command);

        return execute(memberExecutor, actionInvocation, clockService);
    }

    public Object execute(
            final MemberExecutor<PropertyModification> memberExecutor,
            final PropertyModification propertyModification,
            final ClockService clockService,
            final Command command) {

        pushAndUpdateCommand(propertyModification, clockService, command);
        return execute(memberExecutor, propertyModification, clockService);
    }


    private Execution pushAndUpdateCommand(
            final Execution execution,
            final ClockService clockService,
            final Command command) {

        final Timestamp startedAt = clockService.nowAsJavaSqlTimestamp();
        push(startedAt, execution);

        if(command.getStartedAt() == null) {
            command.setStartedAt(startedAt);
        }
        return execution;
    }


    private <T extends Execution> Object execute(
            final MemberExecutor<T> memberExecutor,
            final T execution,
            final ClockService clockService) {

        // as a convenience, since in all cases we want the command to start when the first interaction executes,
        // we populate the command here.

        try {
            try {
                Object result = memberExecutor.execute(execution);
                execution.setReturned(result);
                return result;
            } catch (Exception ex) {

                // just because an exception has thrown, does not mean it is that significant; it could be that
                // it is recognized by an ExceptionRecognizer and is not severe, eg unique index violation in the DB.
                currentExecution.setThrew(ex);

                // propagate (as in previous design); caller will need to trap and decide
                throw ex;
            }
        } finally {
            final Timestamp completedAt = clockService.nowAsJavaSqlTimestamp();
            pop(completedAt);
        }
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
    public Execution getCurrentExecution() {
        return currentExecution;
    }

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     *
     * <p>
     * Push a new {@link org.apache.isis.applib.services.eventbus.AbstractDomainEvent}
     * onto the stack of events held by the command.
     * </p>
     */
    @Programmatic
    private Execution push(final Timestamp startedAt, final Execution execution) {

        if(currentExecution == null) {
            // new top-level execution
            executionGraphs.add(execution);

        } else {
            // adds to graph of parent
            execution.setParent(currentExecution);
        }

        execution.setStartedAt(startedAt);

        // update this.currentExecution and this.previousExecution
        moveCurrentTo(execution);

        return execution;
    }

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     *
     * <p>
     * Pops the top-most  {@link org.apache.isis.applib.services.eventbus.ActionDomainEvent}
     * from the stack of events held by the command.
     * </p>
     * @param completedAt
     */
    @Programmatic
    private Execution pop(final Timestamp completedAt) {
        if(currentExecution == null) {
            throw new IllegalStateException("No current execution to pop");
        }
        final Execution popped = currentExecution;
        popped.setCompletedAt(completedAt);

        moveCurrentTo(currentExecution.getParent());
        return popped;
    }

    private void moveCurrentTo(final Execution newExecution) {
        priorExecution = currentExecution;
        currentExecution = newExecution;
    }

    /**
     * Returns a (list of) graph(es) indicating the domain events in the order that they were pushed.
     *
     * <p>
     *     Each {@link Execution} represents a call stack of domain events (action invocations or property edits),
     *     that may in turn cause other domain events to be fired (by virtue of the {@link WrapperFactory}).
     *     The reason that a list is returned is to support bulk command/actions (against multiple targets).  A non-bulk
     *     action will return a list of just one element.
     * </p>
     */
    @Programmatic
    public List<Execution> getExecutions() {
        return Collections.unmodifiableList(executionGraphs);
    }

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     *
     * Clears the set of {@link Execution}s that may have been {@link #push(Timestamp, Execution)}ed.
     */
    @Programmatic
    public void clear() {
        executionGraphs.clear();
    }
    //endregion

    //region > next (programmatic)

    public enum SequenceName {

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


    private final Map<String, AtomicInteger> sequenceByName = Maps.newHashMap();

    /**
     * Generates numbers in a named sequence
     *
     * @param sequenceAbbr - should be {@link SequenceName#abbr()}.
     */
    @Programmatic
    public int next(String sequenceAbbr) {
        AtomicInteger next = sequenceByName.get(sequenceAbbr);
        if(next == null) {
            next = new AtomicInteger(0);
            sequenceByName.put(sequenceAbbr, next);
        } else {
            next.incrementAndGet();
        }
        return next.get();
    }

    //endregion

    /**
     * Represents an action invocation/property edit as a node in a call-stack execution graph, with sub-interactions
     * being made by way of the {@link WrapperFactory}).
     */
    public static class Execution<T extends InteractionExecutionDto, E extends AbstractDomainEvent<?>> {

        //region > fields, constructor

        private final String memberId;
        private final Object target;
        private final InteractionType interactionType;

        public Execution(
                final InteractionType interactionType,
                final String memberId,
                final Object target) {
            this.interactionType = interactionType;
            this.memberId = memberId;
            this.target = target;
        }
        //endregion

        //region > via constructor: interactionType, memberId, target

        public InteractionType getInteractionType() {
            return interactionType;
        }

        public String getMemberId() {
            return memberId;
        }

        public Object getTarget() {
            return target;
        }

        //endregion

        //region > parent, children

        private final List<Execution> children = Lists.newArrayList();
        private Execution parent;

        /**
         * The action/property that invoked this action/property edit (if any).
         */
        public Execution getParent() {
            return parent;
        }

        public void setParent(final Execution parent) {
            this.parent = parent;
            if(parent != null) {
                parent.children.add(this);
            }
        }

        /**
         * The actions/property edits made in turn via the {@link WrapperFactory}.
         */
        public List<Execution> getChildren() {
            return Collections.unmodifiableList(children);
        }
        //endregion


        //region > event

        private E event;
        /**
         * The domain event fired on the {@link EventBusService event bus} representing the execution of
         * this action invocation/property edit.
         *
         * <p>
         *     This event field is called by the framework before the action invocation/property edit itself;
         *     if read by the executing action/property edit method it will be in the
         *     {@link AbstractDomainEvent.Phase#EXECUTING executing} phase.
         * </p>
         */
        public E getEvent() {
            return event;
        }

        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         */
        public void setEvent(final E event) {
            this.event = event;
        }
        //endregion

        //region > startedAt, completedAt

        private Timestamp startedAt;
        private Timestamp completedAt;

        /**
         * The date/time at which this execution started.
         */
        public Timestamp getStartedAt() {
            return startedAt;
        }

        public void setStartedAt(final Timestamp startedAt) {
            this.startedAt = startedAt;
            syncMetrics();
        }


        /**
         * The date/time at which this execution completed.
         */
        public Timestamp getCompletedAt() {
            return completedAt;
        }

        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         */
        void setCompletedAt(final Timestamp completedAt) {
            this.completedAt = completedAt;
            syncMetrics();
        }

        //endregion

        //region > returned, threw (properties)

        private Object returned;
        /**
         * The object returned by the action invocation/property edit.
         *
         * <p>
         * If the action returned either a domain entity or a simple value (and did not throw an
         * exception) then this object is provided here.
         *
         * <p>
         * For <tt>void</tt> methods and for actions returning collections, the value
         * will be <tt>null</tt>.
         */
        public Object getReturned() {
            return returned;
        }

        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         */
        public void setReturned(Object returned) {
            this.returned = returned;
        }

        private Exception threw;
        @Programmatic
        public Exception getThrew() {
            return threw;
        }

        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         */
        public void setThrew(Exception threw) {
            this.threw = threw;
        }


        //endregion

        //region > dto (property)

        private T dto;

        /**
         * A serializable representation of this action invocation/property edit.
         *
         * <p>
         *     This <i>will</i> be populated (by the framework) during the method call itself (representing the
         *     action invocation/property edit), though some fields ({@link Execution#getCompletedAt()},
         *     {@link Execution#getReturned()}) will (obviously) still be null.
         * </p>
         */
        public T getDto() {
            return dto;
        }

        /**
         * Set by framework (implementation of {@link MemberExecutor})
         */
        public void setDto(final T executionDto) {
            this.dto = executionDto;
            syncMetrics();
        }

        //endregion

        //region > helpers (syncMetrics)
        private void syncMetrics() {
            if (this.dto == null) {
                return;
            }
            final PeriodDto periodDto = periodDtoFor(this.dto);
            periodDto.setStart(JavaSqlTimestampXmlGregorianCalendarAdapter.print(getStartedAt()));
            periodDto.setComplete(JavaSqlTimestampXmlGregorianCalendarAdapter.print(getCompletedAt()));
        }

        private static PeriodDto periodDtoFor(final InteractionExecutionDto executionDto) {
            PeriodDto timings = executionDto.getTimings();
            if(timings == null) {
                timings = new PeriodDto();
            }
            return timings;
        }
        //endregion

    }

    public static class ActionInvocation extends Execution<ActionInvocationDto, ActionDomainEvent<?>> {

        private final List<Object> args;

        public ActionInvocation(
                final String memberId,
                final Object target,
                final List<Object> args) {
            super(InteractionType.ACTION_INVOCATION, memberId, target);
            this.args = args;
        }

        public List<Object> getArgs() {
            return args;
        }
    }

    public static class PropertyModification extends Execution<PropertyModificationDto, PropertyDomainEvent<?,?>> {

        private final Object newValue;

        public PropertyModification(
                final String memberId,
                final Object target,
                final Object newValue) {
            super(InteractionType.PROPERTY_MODIFICATION, memberId, target);
            this.newValue = newValue;
        }

        public Object getNewValue() {
            return newValue;
        }
    }
}
