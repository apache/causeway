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
package org.apache.causeway.core.interaction.session;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;

import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.iactn.ActionInvocation;
import org.apache.causeway.applib.services.iactn.Execution;
import org.apache.causeway.applib.services.iactn.PropertyEdit;
import org.apache.causeway.applib.services.metrics.MetricsService;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.execution.InteractionInternal;

import lombok.Getter;
import org.jspecify.annotations.NonNull;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class CausewayInteraction
implements InteractionInternal {

    public CausewayInteraction(final @NonNull UUID interactionId) {
        this.startedAtSystemNanos = System.nanoTime(); // used to measure time periods, so not using ClockService here
        this.command = new Command(interactionId);
        if(log.isDebugEnabled()) {
            log.debug("new CausewayInteraction id={}", interactionId);
        }
    }

    @Getter(onMethod_ = {@Override})
    private final Command command;

    @Override
    public UUID getInteractionId() {
        return command.getInteractionId();
    }

    @Getter private final long startedAtSystemNanos;

    // -- INTERACTION ON CLOSE HANDLER

    @Setter private Runnable onClose;

    private boolean closed = false;

    /** Do not use, is called by the framework internally. */
    public void close() {
        if(!closed
                && onClose!=null) {
            onClose.run();
            onClose = null;
        }
        closed = true;
    }

    // --

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
            final Context context) {

        push(actionInvocation);
        start(actionInvocation, context);
        try {
            return executeInternal(memberExecutor, actionInvocation, context);
        } finally {
            popAndComplete(context.clockService(), context.metricsService());
        }
    }

    @Override
    public Object execute(
            final MemberExecutor<PropertyEdit> memberExecutor,
            final PropertyEdit propertyEdit,
            final Context context) {

        push(propertyEdit);
        start(propertyEdit, context);
        try {
            return executeInternal(memberExecutor, propertyEdit, context);
        } finally {
            popAndComplete(context.clockService(), context.metricsService());
        }
    }

    private <T extends Execution<?,?>> Object executeInternal(
            final MemberExecutor<T> memberExecutor,
            final T execution,
            final Context context) {

        try {
            Object result = memberExecutor.execute(execution);
            execution.setReturned(result);
            return result;
        } catch (Exception ex) {

            //TODO there is an issue with exceptions getting swallowed, unless this is fixed,
            // we rather print all of them, no matter whether recognized or not later on
            // examples are IllegalArgument- or NullPointer- exceptions being swallowed when using the
            // WrapperFactory utilizing async calls

            if(context.deadlockRecognizer().isDeadlock(ex)) {
                if(log.isDebugEnabled()) {
                    log.debug("failed to execute an interaction due to a deadlock", ex);
                } else if(log.isInfoEnabled()) {
                    log.info("failed to execute an interaction due to a deadlock");
                }
            } else {
                if(log.isErrorEnabled()) {
                    log.error("failed to execute an interaction", _Exceptions.getRootCause(ex).orElse(null));
                }
            }

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
     * Push a new {@link org.apache.causeway.applib.events.domain.AbstractDomainEvent}
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
            final Context context) {
        // set the startedAt (and update command if this is the top-most member execution)
        // (this isn't done within Interaction#execute(...) because it requires the DTO
        // to have been set on the current execution).
        var startedAt = execution.start(context.clockService(), context.metricsService());
        if(getCommand().getStartedAt() == null) {
            getCommand().updater().setStartedAt(startedAt);
            getCommand().updater().setPublishingPhase(Command.CommandPublishingPhase.STARTED);
        }
        context.commandPublisher().start(getCommand());
    }

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     *
     * <p>
     * Pops the top-most  {@link org.apache.causeway.applib.events.domain.ActionDomainEvent}
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

        final Timestamp completedAt = clockService.getClock().nowAsJavaSqlTimestamp();
        popped.setCompletedAt(completedAt, metricsService);

        moveCurrentTo(currentExecution.getParent());
        return popped;
    }

    private void moveCurrentTo(final Execution<?,?> newExecution) {
        priorExecution = currentExecution;
        currentExecution = newExecution;
    }

    @Getter(onMethod_ = {@Override})
    private final LongAdder executionSequence = new LongAdder();

    @Getter(onMethod_ = {@Override})
    private final LongAdder transactionSequence = new LongAdder();

    // -- INTERACTION SCOPED ATTRIBUTES

    // not thread-safe
    private final Map<Class<?>, Object> attributes = new HashMap<>();

    @Override
    public <T> T putAttribute(final Class<? super T> type, final T value) {
        return _Casts.uncheckedCast(attributes.put(type, value));
    }

    @Override
    public <T> T computeAttributeIfAbsent(final Class<? super T> type, final Function<Class<?>, ? extends T> mappingFunction) {
        return _Casts.uncheckedCast(attributes.computeIfAbsent(type, mappingFunction));
    }

    @Override
    public <T> T getAttribute(final Class<T> type) {
        return _Casts.uncheckedCast(attributes.get(type));
    }

    @Override
    public void removeAttribute(final Class<?> type) {
        attributes.remove(type);
    }

}
