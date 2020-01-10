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
import java.util.concurrent.atomic.LongAdder;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.events.domain.AbstractDomainEvent;
import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.events.domain.PropertyDomainEvent;
import org.apache.isis.applib.services.HasUniqueId;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.applib.services.metrics.MetricsService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.util.schema.MemberExecutionDtoUtils;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.schema.common.v1.DifferenceDto;
import org.apache.isis.schema.common.v1.InteractionType;
import org.apache.isis.schema.common.v1.PeriodDto;
import org.apache.isis.schema.ixn.v1.ActionInvocationDto;
import org.apache.isis.schema.ixn.v1.MemberExecutionDto;
import org.apache.isis.schema.ixn.v1.MetricsDto;
import org.apache.isis.schema.ixn.v1.ObjectCountsDto;
import org.apache.isis.schema.ixn.v1.PropertyEditDto;
import org.apache.isis.schema.jaxbadapters.JavaSqlTimestampXmlGregorianCalendarAdapter;

import lombok.val;

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
public class Interaction implements HasUniqueId {

    // -- transactionId (property)

    private UUID interactionId;

    @Programmatic
    @Override
    public UUID getUniqueId() {
        return interactionId;
    }

    @Programmatic
    public void setUniqueId(final UUID transactionId) {
        this.interactionId = transactionId;
    }

    // -- push/pop/current/get/clear Execution(s)

    private final List<Execution<?,?>> executionGraphs = _Lists.newArrayList();
    private Execution<?,?> currentExecution;
    private Execution<?,?> priorExecution;


    /**
     * The execution that preceded the current one.
     */
    @Programmatic
    public Execution<?,?> getPriorExecution() {
        return priorExecution;
    }

    /**
     * <b>NOT API</b>: intended only to be implemented by the framework.
     *
     * <p>
     * (Modelled after {@link Callable}), is the implementation
     * by which the framework actually performs the interaction.
     */
    public interface MemberExecutor<T extends Execution<?,?>> {
        @Programmatic
        Object execute(final T currentExecution);
    }

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     *
     * <p>
     * Use the provided {@link MemberExecutor} to invoke an action, with the provided
     * {@link ActionInvocation} capturing the details of said action.
     * </p>
     */
    @Programmatic
    public Object execute(
            final MemberExecutor<ActionInvocation> memberExecutor,
            final ActionInvocation actionInvocation,
            final ClockService clockService,
            final MetricsService metricsService) {

        push(actionInvocation);

        return executeInternal(memberExecutor, actionInvocation, clockService, metricsService);
    }

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     *
     * <p>
     * Use the provided {@link MemberExecutor} to edit a property, with the provided
     * {@link PropertyEdit} capturing the details of said property edit.
     * </p>
     */
    @Programmatic
    public Object execute(
            final MemberExecutor<PropertyEdit> memberExecutor,
            final PropertyEdit propertyEdit,
            final ClockService clockService,
            final MetricsService metricsService) {

        push(propertyEdit);

        return executeInternal(memberExecutor, propertyEdit, clockService, metricsService);
    }

    private <T extends Execution<?,?>> Object executeInternal(
            final MemberExecutor<T> memberExecutor,
            final T execution,
            final ClockService clockService,
            final MetricsService metricsService) {

        // as a convenience, since in all cases we want the command to start when the first 
        // interaction executes, we populate the command here.

        try {
            try {
                Object result = memberExecutor.execute(execution);
                execution.setReturned(result);
                return result;
            } catch (Exception ex) {
                
                //TODO there is an issue with exceptions getting swallowed, unless this is fixed,
                // we rather print all of them, no matter whether recognized or not later on
                // examples are IllegalArgument- or NullPointer- exceptions being swallowed when using the
                // WrapperFactory utilizing async calls
                ex.printStackTrace();
                
                // just because an exception has thrown, does not mean it is that significant; 
                // it could be that it is recognized by an ExceptionRecognizer and is not severe 
                // eg. unique index violation in the DB
                getCurrentExecution().setThrew(ex);

                // propagate (as in previous design); caller will need to trap and decide
                throw ex;
            }
        } finally {
            final Timestamp completedAt = clockService.nowAsJavaSqlTimestamp();
            pop(completedAt, metricsService);
        }
    }

    /**
     * The current (most recently pushed) {@link Execution}.
     */
    @Programmatic
    public Execution<?,?> getCurrentExecution() {
        return currentExecution;
    }

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     *
     * <p>
     * Push a new {@link org.apache.isis.applib.events.domain.AbstractDomainEvent}
     * onto the stack of events held by the command.
     * </p>
     */
    @Programmatic
    private Execution<?,?> push(final Execution<?,?> execution) {

        if(currentExecution == null) {
            // new top-level execution
            executionGraphs.add(execution);

        } else {
            // adds to graph of parent
            execution.setParent(currentExecution);
        }


        // update this.currentExecution and this.previousExecution
        moveCurrentTo(execution);

        return execution;
    }

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     *
     * <p>
     * Pops the top-most  {@link org.apache.isis.applib.events.domain.ActionDomainEvent}
     * from the stack of events held by the command.
     * </p>
     */
    @Programmatic
    private Execution<?,?> pop(
            final Timestamp completedAt,
            final MetricsService metricsService) {
        if(currentExecution == null) {
            throw new IllegalStateException("No current execution to pop");
        }
        final Execution<?,?> popped = currentExecution;
        popped.setCompletedAt(completedAt, metricsService);

        moveCurrentTo(currentExecution.getParent());
        return popped;
    }

    private void moveCurrentTo(final Execution<?,?> newExecution) {
        priorExecution = currentExecution;
        currentExecution = newExecution;
    }

    /**
     * Returns a (list of) {@link Execution}s in the order that they were pushed.  Generally there will be just one entry in this list, but additional entries may arise from the use of mixins/contributions when re-rendering a modified object.
     *
     * <p>
     *     Each {@link Execution} represents a call stack of domain events (action invocations or property edits),
     *     that may in turn cause other domain events to be fired (by virtue of the {@link WrapperFactory}).
     *     The reason that a list is returned is to support bulk command/actions (against multiple targets).  A non-bulk
     *     action will return a list of just one element.
     * </p>
     */
    @Programmatic
    public List<Execution<?,?>> getExecutions() {
        return Collections.unmodifiableList(executionGraphs);
    }

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     *
     * Clears the set of {@link Execution}s that may have been {@link #push(Execution)}ed.
     */
    @Programmatic
    public void clear() {
        executionGraphs.clear();
    }


    // -- next (programmatic)

    /**
     * Enumerates the different reasons why multiple occurrences of a certain type might occur within a single
     * (top-level) interaction.
     */
    public enum Sequence {

        /**
         * Each interaction is either an action invocation or a property edit.  There could be multiple of these,
         * typically as the result of a nested calls using the {@link WrapperFactory}.  Another reason is
         * support for bulk action invocations within a single transaction.
         */
        INTERACTION,
        /**
         * For objects: multiple such could be dirtied and thus published as separate events.  For actions
         * invocations/property edits : multiple sub-invocations could occur if sub-invocations are made through the
         * {@link WrapperFactory}.
         */
        PUBLISHED_EVENT,
        /**
         * There may be multiple transactions within a given interaction.
         */
        TRANSACTION,
        ;

        @Programmatic
        public String id() {
            return Interaction.Sequence.class.getName() + "#" + name();
        }
    }

    private final Map<String, LongAdder> maxBySequence = _Maps.newHashMap();

    /**
     * Generates numbers in a named sequence.  The name of the sequence can be arbitrary, though note that the
     * framework also uses this capability to generate sequence numbers corresponding to the sequences enumerated by
     * the {@link Sequence} enum.
     */
    @Programmatic
    public int next(final String sequenceId) {
        final LongAdder adder = maxBySequence.computeIfAbsent(sequenceId, this::newAdder);
        adder.increment();
        return adder.intValue();
    }

    private LongAdder newAdder(String ignore) {
        final LongAdder adder = new LongAdder();
        adder.decrement();
        return adder;
    }

    /**
     * Represents an action invocation/property edit as a node in a call-stack execution graph, with sub-interactions
     * being made by way of the {@link WrapperFactory}).
     */
    public static abstract class Execution<T extends MemberExecutionDto, E extends AbstractDomainEvent<?>> {

        // -- fields, constructor

        private final String memberIdentifier;
        private final Object target;
        private final String targetMember;
        private final String targetClass;
        private final Interaction interaction;
        private final InteractionType interactionType;

        protected Execution(
                final Interaction interaction,
                final InteractionType interactionType,
                final String memberIdentifier,
                final Object target,
                final String targetMember,
                final String targetClass) {
            
            this.interaction = interaction;
            this.interactionType = interactionType;
            this.memberIdentifier = memberIdentifier;
            this.target = target;
            this.targetMember = targetMember;
            this.targetClass = targetClass;
        }


        // -- via constructor: interaction, interactionType, memberId, target, targetMember, targetClass

        @Programmatic
        public Interaction getInteraction() {
            return interaction;
        }

        @Programmatic
        public InteractionType getInteractionType() {
            return interactionType;
        }

        @Programmatic
        public String getMemberIdentifier() {
            return memberIdentifier;
        }

        /**
         * The target of the action invocation.  If this interaction is for a mixin action, then will be the
         * mixed-in target (not the transient mixin itself).
         */
        @Programmatic
        public Object getTarget() {
            return target;
        }

        /**
         * A human-friendly description of the class of the target object.
         */
        @Programmatic
        public String getTargetClass() {
            return targetClass;
        }

        /**
         * The human-friendly name of the action invoked/property edited on the target object.
         */
        @Programmatic
        public String getTargetMember() {
            return targetMember;
        }



        // -- parent, children

        private final List<Execution<?,?>> children = _Lists.newArrayList();
        private Execution<?,?> parent;

        /**
         * The action/property that invoked this action/property edit (if any).
         */
        @Programmatic
        public Execution<?,?> getParent() {
            return parent;
        }

        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         */
        @Programmatic
        public void setParent(final Execution<?,?> parent) {
            this.parent = parent;
            if(parent != null) {
                parent.children.add(this);
            }
        }

        /**
         * The actions/property edits made in turn via the {@link WrapperFactory}.
         */
        @Programmatic
        public List<Execution<?,?>> getChildren() {
            return Collections.unmodifiableList(children);
        }



        // -- event

        private E event;
        /**
         * The domain event fired on the {@link EventBusService event bus} representing the execution of
         * this action invocation/property edit.
         *
         * <p>
         *     This event field is set by the framework before the action invocation/property edit itself;
         *     if read by the executing action/property edit method it will be in the
         *     {@link AbstractDomainEvent.Phase#EXECUTING executing} phase.
         * </p>
         */
        @Programmatic
        public E getEvent() {
            return event;
        }

        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         */
        @Programmatic
        public void setEvent(final E event) {
            this.event = event;
        }


        // -- startedAt, completedAt

        private Timestamp startedAt;
        private Timestamp completedAt;

        /**
         * The date/time at which this execution started.
         */
        @Programmatic
        public Timestamp getStartedAt() {
            return startedAt;
        }

        @Programmatic
        public Timestamp start(
                final ClockService clockService,
                final MetricsService metricsService) {
            val startedAt = clockService.nowAsJavaSqlTimestamp();
            syncMetrics(When.BEFORE, startedAt, metricsService);
            return startedAt;
        }


        /**
         * The date/time at which this execution completed.
         */
        @Programmatic
        public Timestamp getCompletedAt() {
            return completedAt;
        }

        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         */
        void setCompletedAt(
                final Timestamp completedAt,
                final MetricsService metricsService) {
            syncMetrics(When.AFTER, completedAt, metricsService);
        }



        // -- returned, threw (properties)

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
        @Programmatic
        public Object getReturned() {
            return returned;
        }

        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         */
        @Programmatic
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
        @Programmatic
        public void setThrew(Exception threw) {
            this.threw = threw;
        }




        // -- dto (property)

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
        @Programmatic
        public T getDto() {
            return dto;
        }

        /**
         * Set by framework (implementation of {@link MemberExecutor})
         */
        @Programmatic
        public void setDto(final T executionDto) {
            this.dto = executionDto;
        }



        // -- helpers (syncMetrics)

        enum When {
            BEFORE {
                @Override
                void syncMetrics(
                        final Execution<?, ?> execution,
                        final Timestamp timestamp,
                        final int numberObjectsLoaded,
                        final int numberObjectsDirtied) {

                    execution.startedAt = timestamp;

                    final MetricsDto metricsDto = metricsFor(execution);

                    final PeriodDto periodDto = timingsFor(metricsDto);
                    periodDto.setStartedAt(JavaSqlTimestampXmlGregorianCalendarAdapter.print(timestamp));

                    final ObjectCountsDto objectCountsDto = objectCountsFor(metricsDto);
                    numberObjectsLoadedFor(objectCountsDto).setBefore(numberObjectsLoaded);
                    numberObjectsDirtiedFor(objectCountsDto).setBefore(numberObjectsDirtied);
                }

            },
            AFTER {
                @Override void syncMetrics(
                        final Execution<?, ?> execution,
                        final Timestamp timestamp,
                        final int numberObjectsLoaded,
                        final int numberObjectsDirtied) {

                    execution.completedAt = timestamp;

                    final MetricsDto metricsDto = metricsFor(execution);

                    final PeriodDto periodDto = timingsFor(metricsDto);
                    periodDto.setCompletedAt(JavaSqlTimestampXmlGregorianCalendarAdapter.print(timestamp));

                    final ObjectCountsDto objectCountsDto = objectCountsFor(metricsDto);
                    numberObjectsLoadedFor(objectCountsDto).setAfter(numberObjectsLoaded);
                    numberObjectsDirtiedFor(objectCountsDto).setAfter(numberObjectsDirtied);
                }

            };

            // -- helpers

            private static DifferenceDto numberObjectsDirtiedFor(final ObjectCountsDto objectCountsDto) {
                return MemberExecutionDtoUtils.numberObjectsDirtiedFor(objectCountsDto);
            }

            private static DifferenceDto numberObjectsLoadedFor(final ObjectCountsDto objectCountsDto) {
                return MemberExecutionDtoUtils.numberObjectsLoadedFor(objectCountsDto);
            }

            private static ObjectCountsDto objectCountsFor(final MetricsDto metricsDto) {
                return MemberExecutionDtoUtils.objectCountsFor(metricsDto);
            }

            private static MetricsDto metricsFor(final Execution<?, ?> execution) {
                return MemberExecutionDtoUtils.metricsFor(execution.dto);
            }

            private static PeriodDto timingsFor(final MetricsDto metricsDto) {
                return MemberExecutionDtoUtils.timingsFor(metricsDto);
            }


            abstract void syncMetrics(
                    final Execution<?, ?> teExecution,
                    final Timestamp timestamp,
                    final int numberObjectsLoaded,
                    final int numberObjectsDirtied);
        }
        private void syncMetrics(
                final When when,
                final Timestamp timestamp,
                final MetricsService metricsService) {

            final int numberObjectsLoaded = metricsService.numberObjectsLoaded();
            final int numberObjectsDirtied = metricsService.numberObjectsDirtied();

            when.syncMetrics(this, timestamp, numberObjectsLoaded, numberObjectsDirtied);
        }



    }

    public static class ActionInvocation extends Execution<ActionInvocationDto, ActionDomainEvent<?>> {

        private final List<Object> args;

        public ActionInvocation(
                final Interaction interaction,
                final String memberId,
                final Object target,
                final List<Object> args,
                final String targetMember,
                final String targetClass) {
            super(interaction, InteractionType.ACTION_INVOCATION, memberId, target, targetMember, targetClass);
            this.args = args;
        }

        @Programmatic
        public List<Object> getArgs() {
            return args;
        }
    }

    public static class PropertyEdit extends Execution<PropertyEditDto, PropertyDomainEvent<?,?>> {

        private final Object newValue;

        public PropertyEdit(
                final Interaction interaction,
                final String memberId,
                final Object target,
                final Object newValue,
                final String targetMember,
                final String targetClass) {
            super(interaction, InteractionType.PROPERTY_EDIT, memberId, target, targetMember, targetClass);
            this.newValue = newValue;
        }

        @Programmatic
        public Object getNewValue() {
            return newValue;
        }
    }

}
