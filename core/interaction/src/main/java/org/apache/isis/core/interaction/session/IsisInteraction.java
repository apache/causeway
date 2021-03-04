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
package org.apache.isis.core.interaction.session;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.LongAdder;

import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.iactn.ActionInvocation;
import org.apache.isis.applib.services.iactn.Execution;
import org.apache.isis.applib.services.iactn.PropertyEdit;
import org.apache.isis.applib.services.iactn.SequenceType;
import org.apache.isis.applib.services.metrics.MetricsService;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.execution.InternalInteraction;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class IsisInteraction implements InternalInteraction {

    public IsisInteraction(final @NonNull UUID uniqueId) {
        this.command = new Command(uniqueId);
    }

    @Getter(onMethod_ = {@Override})
    private final Command command;

    @Override
    public UUID getInteractionId() {
        return command.getInteractionId();
    }

    private final List<Execution<?,?>> executionGraphs = _Lists.newArrayList();

    @Getter(onMethod_ = {@Override})
    private Execution<?,?> currentExecution;

    @Getter(onMethod_ = {@Override})
    private Execution<?,?> priorExecution;

    public void clear() {
        executionGraphs.clear();
    }

    @Override
    public Object execute(
            final MemberExecutor<ActionInvocation> memberExecutor,
            final ActionInvocation actionInvocation,
            final ClockService clockService,
            final MetricsService metricsService,
            final Command command) {

        pushAndStart(actionInvocation, clockService, metricsService, command);
        try {
            return executeInternal(memberExecutor, actionInvocation);
        } finally {
            popAndComplete(clockService, metricsService);
        }
    }

    private void pushAndStart(ActionInvocation actionInvocation, ClockService clockService, MetricsService metricsService, Command command) {
        push(actionInvocation);
        start(actionInvocation, clockService, metricsService, command);
    }

    @Override
    public Object execute(
            final MemberExecutor<PropertyEdit> memberExecutor,
            final PropertyEdit propertyEdit,
            final ClockService clockService,
            final MetricsService metricsService,
            final Command command) {

        push(propertyEdit);
        start(propertyEdit, clockService, metricsService, command);
        try {
            return executeInternal(memberExecutor, propertyEdit);
        } finally {
            popAndComplete(clockService, metricsService);
        }
    }

    private <T extends Execution<?,?>> Object executeInternal(MemberExecutor<T> memberExecutor, T execution) {

        try {
            Object result = memberExecutor.execute(execution);
            execution.setReturned(result);
            return result;
        } catch (Exception ex) {

            //TODO there is an issue with exceptions getting swallowed, unless this is fixed,
            // we rather print all of them, no matter whether recognized or not later on
            // examples are IllegalArgument- or NullPointer- exceptions being swallowed when using the
            // WrapperFactory utilizing async calls
            log.error("failed to execute an interaction", _Exceptions.getRootCause(ex));

            // just because an exception has thrown, does not mean it is that significant;
            // it could be that it is recognized by an ExceptionRecognizer and is not severe
            // eg. unique index violation in the DB
            getCurrentExecution().setThrew(ex);

            // propagate (as in previous design); caller will need to trap and decide
            throw ex;
        }
    }


    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     *
     * <p>
     * Push a new {@link org.apache.isis.applib.events.domain.AbstractDomainEvent}
     * onto the stack of events held by the command.
     * </p>
     */
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

    private void start(
            final Execution<?,?> execution,
            final ClockService clockService,
            final MetricsService metricsService,
            final Command command) {
        // set the startedAt (and update command if this is the top-most member execution)
        // (this isn't done within Interaction#execute(...) because it requires the DTO
        // to have been set on the current execution).
        val startedAt = execution.start(clockService, metricsService);
        if(command.getStartedAt() == null) {
            command.updater().setStartedAt(startedAt);
        }
    }

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     *
     * <p>
     * Pops the top-most  {@link org.apache.isis.applib.events.domain.ActionDomainEvent}
     * from the stack of events held by the command.
     * </p>
     */
    private Execution<?,?> popAndComplete(
            final ClockService clockService,
            final MetricsService metricsService) {

        if(currentExecution == null) {
            throw new IllegalStateException("No current execution to pop");
        }
        final Execution<?,?> popped = currentExecution;

        final Timestamp completedAt = clockService.getClock().javaSqlTimestamp();
        popped.setCompletedAt(completedAt, metricsService);

        moveCurrentTo(currentExecution.getParent());
        return popped;
    }

    private void moveCurrentTo(final Execution<?,?> newExecution) {
        priorExecution = currentExecution;
        currentExecution = newExecution;
    }

    private final Map<SequenceType, LongAdder> maxBySequence = _Maps.newHashMap();

    @Override
    public int next(final SequenceType sequenceType) {
        final LongAdder adder = maxBySequence.computeIfAbsent(sequenceType, this::newAdder);
        adder.increment();
        return adder.intValue();
    }

    private LongAdder newAdder(final SequenceType ignore) {
        final LongAdder adder = new LongAdder();
        adder.decrement();
        return adder;
    }

}
