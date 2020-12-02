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

import org.apache.isis.applib.events.domain.AbstractDomainEvent;
import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.events.domain.PropertyDomainEvent;
import org.apache.isis.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.applib.services.metrics.MetricsService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.util.schema.MemberExecutionDtoUtils;
import org.apache.isis.commons.having.HasUniqueId;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.schema.common.v2.DifferenceDto;
import org.apache.isis.schema.common.v2.InteractionType;
import org.apache.isis.schema.common.v2.PeriodDto;
import org.apache.isis.schema.ixn.v2.ActionInvocationDto;
import org.apache.isis.schema.ixn.v2.MemberExecutionDto;
import org.apache.isis.schema.ixn.v2.MetricsDto;
import org.apache.isis.schema.ixn.v2.ObjectCountsDto;
import org.apache.isis.schema.ixn.v2.PropertyEditDto;

import lombok.Getter;
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
// tag::refguide[]
public interface Interaction extends HasUniqueId {

    Command getCommand();                  // <.>

    // end::refguide[]

    /**
     * The current (most recently pushed) {@link Execution}.
     */
    // tag::refguide[]
    Execution<?,?> getCurrentExecution();        // <.>

    // end::refguide[]
    /**
     * The execution that preceded the current one.
     */
    // tag::refguide[]
    Execution<?,?> getPriorExecution();          // <.>

    // end::refguide[]

    /**
     * Enumerates the different reasons why multiple occurrences of a certain type might occur within a single
     * (top-level) interaction.
     */
    // tag::refguide-1[]
    public enum Sequence {

        // end::refguide-1[]
        /**
         * Each interaction is either an action invocation or a property edit.  There could be multiple of these,
         * typically as the result of a nested calls using the {@link WrapperFactory}.  Another reason is
         * support for bulk action invocations within a single transaction.
         */
        // tag::refguide-1[]
        INTERACTION,

        // end::refguide-1[]
        /**
         * For objects: multiple such could be dirtied and thus published as separate events.  For actions
         * invocations/property edits : multiple sub-invocations could occur if sub-invocations are made through the
         * {@link WrapperFactory}.
         */
        // tag::refguide-1[]
        PUBLISHED_EVENT,

        // end::refguide-1[]
        /**
         * There may be multiple transactions within a given interaction.
         */
        // tag::refguide-1[]
        TRANSACTION,
        ;
        // end::refguide-1[]

        public String id() {
            return Interaction.Sequence.class.getName() + "#" + name();
        }
        // tag::refguide-1[]
    }
    // end::refguide-1[]

    /**
     * Generates numbers in a named sequence.
     *
     * The name of the sequence can be arbitrary, though note that the framework also uses this capability to
     * generate sequence numbers corresponding to the sequences enumerated by the {@link Sequence} enum.
     */
    // tag::refguide[]
    int next(final String sequenceId);      // <.>
    
    // end::refguide[]


    /**
     * Represents an action invocation/property edit as a node in a call-stack execution graph, with sub-interactions
     * being made by way of the {@link WrapperFactory}).
     */
    // tag::refguide-2[]
    public static abstract class Execution<T extends MemberExecutionDto, E extends AbstractDomainEvent<?>> {

        @Getter
        private final Interaction interaction;
        @Getter
        private final InteractionType interactionType;
        @Getter
        private final String memberIdentifier;

        // end::refguide-2[]
        /**
         * The target of the action invocation.  If this interaction is for a mixin action, then will be the
         * mixed-in target (not the transient mixin itself).
         */
        // tag::refguide-2[]
        @Getter
        private final Object target;

        // end::refguide-2[]
        /**
         * A human-friendly description of the class of the target object.
         */
        // tag::refguide-2[]
        @Getter
        private final String targetClass;

        // end::refguide-2[]
        /**
         * The human-friendly name of the action invoked/property edited on the target object.
         */
        // tag::refguide-2[]
        @Getter
        private final String targetMember;
        // end::refguide-2[]


        /**
         * Captures metrics before the Execution Dto is present.
         */
        private int numberObjectsLoadedBefore;
        /**
         * Captures metrics before the Execution Dto is present.
         */
        private int numberObjectsDirtiedBefore;

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


        // -- parent, children

        private final List<Execution<?,?>> children = _Lists.newArrayList();
        /**
         * The action/property that invoked this action/property edit (if any).
         */
        // tag::refguide-2[]
        @Getter
        private Execution<?,?> parent;

        // end::refguide-2[]
        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         */
        public void setParent(final Execution<?,?> parent) {
            this.parent = parent;
            if(parent != null) {
                parent.children.add(this);
            }
        }

        /**
         * The actions/property edits made in turn via the {@link WrapperFactory}.
         */
        // tag::refguide-2[]
        public List<Execution<?,?>> getChildren() {
            // end::refguide-2[]

            return Collections.unmodifiableList(children);

            // tag::refguide-2[]
            // ...
        }

        // end::refguide-2[]
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
        // tag::refguide-2[]
        @Getter
        private E event;

        // end::refguide-2[]
        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         */
        public void setEvent(final E event) {
            this.event = event;
        }

        /**
         * The date/time at which this execution started.
         */
        // tag::refguide-2[]
        @Getter
        private Timestamp startedAt;

        // end::refguide-2[]
        /**
         * The date/time at which this execution completed.
         */
        // tag::refguide-2[]
        @Getter
        private Timestamp completedAt;

        public Timestamp start(
                final ClockService clockService,
                final MetricsService metricsService) {
            // end::refguide-2[]

            val startedAt = clockService.nowAsJavaSqlTimestamp();
            syncMetrics(When.BEFORE, startedAt, metricsService);
            return startedAt;

            // tag::refguide-2[]
            // ...
        }

        // end::refguide-2[]
        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         */
        public void setCompletedAt(
                final Timestamp completedAt,
                final MetricsService metricsService) {
            syncMetrics(When.AFTER, completedAt, metricsService);
        }

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
        // tag::refguide-2[]
        @Getter
        private Object returned;

        // end::refguide-2[]
        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         */
        public void setReturned(Object returned) {
            this.returned = returned;
        }

        // tag::refguide-2[]
        @Getter
        private Exception threw;

        // end::refguide-2[]
        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         */
        public void setThrew(Exception threw) {
            this.threw = threw;
        }


        // -- dto (property)

        /**
         * A serializable representation of this action invocation/property edit.
         *
         * <p>
         *     This <i>will</i> be populated (by the framework) during the method call itself (representing the
         *     action invocation/property edit), though some fields ({@link Execution#getCompletedAt()},
         *     {@link Execution#getReturned()}) will (obviously) still be null.
         * </p>
         */
        // tag::refguide-2[]
        @Getter
        private T dto;

        // end::refguide-2[]
        /**
         * <b>NOT API</b>: Set by framework (implementation of {@link MemberExecutor})
         */
        public void setDto(final T executionDto) {
            this.dto = executionDto;
        }


        // -- helpers (syncMetrics)

        // tag::refguide-2a[]
        enum When {
            BEFORE {
                // end::refguide-2a[]

                @Override
                void syncMetrics(
                        final Execution<?, ?> execution,
                        final Timestamp timestamp,
                        final int numberObjectsLoaded,
                        final int numberObjectsDirtied) {

                    execution.startedAt = timestamp;
                    execution.numberObjectsLoadedBefore = numberObjectsLoaded;
                    execution.numberObjectsDirtiedBefore = numberObjectsLoaded;
                }

                // tag::refguide-2a[]
                // ....
            },
            AFTER {
                // end::refguide-2a[]

                @Override void syncMetrics(
                        final Execution<?, ?> execution,
                        final Timestamp timestamp,
                        final int numberObjectsLoaded,
                        final int numberObjectsDirtied) {

                    execution.completedAt = timestamp;

                    final MetricsDto metricsDto = metricsFor(execution);

                    final PeriodDto periodDto = timingsFor(metricsDto);
                    periodDto.setStartedAt(JavaSqlXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(execution.startedAt));
                    periodDto.setCompletedAt(JavaSqlXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(execution.completedAt));

                    final ObjectCountsDto objectCountsDto = objectCountsFor(metricsDto);
                    numberObjectsLoadedFor(objectCountsDto).setBefore(execution.numberObjectsLoadedBefore);
                    numberObjectsDirtiedFor(objectCountsDto).setBefore(execution.numberObjectsDirtiedBefore);

                    numberObjectsLoadedFor(objectCountsDto).setAfter(numberObjectsLoaded);
                    numberObjectsDirtiedFor(objectCountsDto).setAfter(numberObjectsDirtied);
                }

                // tag::refguide-2a[]
                // ....
            };
            // end::refguide-2a[]

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
            // tag::refguide-2a[]
        }
        // end::refguide-2a[]

        private void syncMetrics(
                final When when,
                final Timestamp timestamp,
                final MetricsService metricsService) {

            final int numberObjectsLoaded = metricsService.numberEntitiesLoaded();
            final int numberObjectsDirtied = metricsService.numberEntitiesDirtied();

            when.syncMetrics(this, timestamp, numberObjectsLoaded, numberObjectsDirtied);
        }

        // tag::refguide-2[]
    }
    // end::refguide-2[]

    // tag::refguide-3[]
    public static class ActionInvocation extends Execution<ActionInvocationDto, ActionDomainEvent<?>> {

        @Getter
        private final List<Object> args;
        // end::refguide-3[]

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
        // tag::refguide-3[]
        // ...
    }
    // end::refguide-3[]

    // tag::refguide-4[]
    public static class PropertyEdit extends Execution<PropertyEditDto, PropertyDomainEvent<?,?>> {

        @Getter
        private final Object newValue;
        // end::refguide-4[]

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

        // tag::refguide-4[]
        // ...
    }
    // end::refguide-4[]

    // tag::refguide[]
}
// end::refguide[]
