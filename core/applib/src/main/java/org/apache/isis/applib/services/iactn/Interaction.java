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
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.HasTransactionId;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.eventbus.AbstractDomainEvent;
import org.apache.isis.applib.services.eventbus.EventBusService;
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

    //region > push/pop/current/get/clear (Abstract)DomainEvents

    private List<Execution> executionGraphs = Lists.newArrayList();
    private Execution currentExecution;
    private Execution priorExecution;


    /**
     * The execution that preceded the current one.
     */
    public Execution getPriorExecution() {
        return priorExecution;
    }

    public interface MemberCallable<T extends MemberArgs> {
        Object call(T args);
    }

    public static abstract class MemberArgs {

        public enum Type {
            PROPERTY,
            ACTION
        }

        private final Command command;
        private final Object target;
        private final Type type;

        protected MemberArgs(
                final Command command,
                final Object target, final Type type) {
            this.command = command;
            this.target = target;
            this.type = type;
        }

        public Command getCommand() {
            return command;
        }

        public Object getTarget() {
            return target;
        }

        public Type getType() {
            return type;
        }
    }


    public static class ActionArgs extends MemberArgs {
        private final List<Object> args;

        public ActionArgs(
                final Command command,
                final Object target,
                final List<Object> args) {
            super(command, target, Type.ACTION);
            this.args = args;
        }

        public List<Object> getArgs() {
            return args;
        }
    }

    public static class PropertyArgs extends MemberArgs {
        private final Object argValue;

        public PropertyArgs(
                final Command command,
                final Object target,
                final Object argValue) {
            super(command, target, Type.PROPERTY);
            this.argValue = argValue;
        }

        public Object getArgValue() {
            return argValue;
        }
    }

    public <T> T execute(
            final MemberCallable memberCallable,
            final ActionArgs actionArgs,
            final ClockService clockService) {

        final Timestamp startedAt = clockService.nowAsJavaSqlTimestamp();
        final Execution execution = push(startedAt, actionArgs);

        final Command command = actionArgs.getCommand();
        return execute(memberCallable, actionArgs, clockService, command, startedAt, execution);

    }

    public <T> T execute(
            final MemberCallable memberCallable,
            final PropertyArgs propertyArgs,
            final ClockService clockService) {

        final Timestamp startedAt = clockService.nowAsJavaSqlTimestamp();
        Execution execution = push(startedAt, propertyArgs);

        final Command command = propertyArgs.getCommand();
        return execute(memberCallable, propertyArgs, clockService, command, startedAt, execution);

    }

    private <T> T execute(
            final MemberCallable memberCallable,
            final MemberArgs memberArgs,
            final ClockService clockService,
            final Command command,
            final Timestamp startedAt,
            final Execution currentExecution) {
        // as a convenience, since in all cases we want the command to start when the first interaction executes,
        // we populate the command here.
        if(command.getStartedAt() == null) {
            command.setStartedAt(startedAt);
        }

        try {
            try {
                Object result = memberCallable.call(memberArgs);
                currentExecution.setResult(result);
                return (T)result;
            } catch (Exception e) {

                // just because an exception has thrown, does not mean it is that significant; it could be that
                // it is recognized by an ExceptionRecognizer and is not severe, eg unique index violation in the DB.
                RuntimeException re = e instanceof RuntimeException? (RuntimeException) e : new RuntimeException(e);
                currentExecution.setException(re);

                // propagate (as in previous design); caller will need to trap and decide
                throw re;
            }
        } finally {
            final Timestamp completedAt = clockService.nowAsJavaSqlTimestamp();
            pop(completedAt);
        }
    }

    /**
     * Represents an action invocation/property edit as a node in a call-stack execution graph, with sub-interactions
     * being made by way of the {@link WrapperFactory}).
     */
    public static class Execution {

        private final Timestamp startedAt;
        private final MemberArgs memberArgs;
        private final Execution parent;
        private final List<Execution> children = Lists.newArrayList();

        public Execution(
                final MemberArgs memberArgs, final Timestamp startedAt) {
            this.startedAt = startedAt;
            this.memberArgs = memberArgs;
            this.parent = null;
        }

        public Execution(
                final MemberArgs memberArgs, final Timestamp startedAt, final Execution parent) {
            this.startedAt = startedAt;
            this.parent = parent;
            this.memberArgs = memberArgs;
            parent.children.add(this);
        }

        /**
         * The action/property that invoked this action/property edit (if any).
         */
        public Execution getParent() {
            return parent;
        }

        /**
         * The actions/property edits made in turn via the {@link WrapperFactory}.
         */
        public List<Execution> getChildren() {
            return Collections.unmodifiableList(children);
        }

        public MemberArgs getMemberArgs() {
            return memberArgs;
        }

        //region > event

        private AbstractDomainEvent<?> event;
        /**
         * The domain event fired on the {@link EventBusService event bus} representing the execution of
         * this action invocation/property edit.
         */
        public AbstractDomainEvent<?> getEvent() {
            return event;
        }

        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         */
        public void setEvent(final AbstractDomainEvent<?> event) {
            this.event = event;
        }
        //endregion

        //region > startedAt


        /**
         * The date/time at which this execution started.
         */
        public Timestamp getStartedAt() {
            return startedAt;
        }


        //endregion

        //region > completedAt

        private Timestamp completedAt;

        /**
         * The date/time at which this execution completed.
         */
        public Timestamp getCompletedAt() {
            return completedAt;
        }

        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         */
        void setCompletedAt(Timestamp completedAt) {
            this.completedAt = completedAt;
        }

        //endregion



        //region > exception (property)

        private RuntimeException exception;
        @Programmatic
        public RuntimeException getException() {
            return exception;
        }

        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         */
        public void setException(RuntimeException exception) {
            this.exception = exception;
        }

        //endregion

        //region > result (property)


        private Object result;
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
        public Object getResult() {
            return result;
        }

        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         */
        public void setResult(Object result) {
            this.result =  result;
        }

        //endregion



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
    Execution push(final Timestamp startedAt, final MemberArgs memberArgs) {

        final Execution newExecution;
        if(currentExecution == null) {
            // new top-level execution
            newExecution = new Execution(memberArgs, startedAt);
            executionGraphs.add(newExecution);

        } else {
            // adds to graph of parent
            newExecution = new Execution(memberArgs, startedAt, currentExecution);
        }

        // set
        moveCurrentTo(newExecution);

        return currentExecution;
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
    Execution pop(final Timestamp completedAt) {
        if(currentExecution == null) {
            throw new IllegalStateException("No current execution graph to pop");
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
     * Returns a (list of) graph(es) indicating the domain events in the order that they were
     * {@link #push(Timestamp, MemberArgs pushed}.
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
     * Clears the set of {@link AbstractDomainEvent}s that have been {@link #push(Timestamp, MemberArgs push)}ed.
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

}
