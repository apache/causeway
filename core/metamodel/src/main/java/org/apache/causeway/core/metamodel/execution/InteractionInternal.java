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
package org.apache.causeway.core.metamodel.execution;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.LongAdder;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.iactn.ActionInvocation;
import org.apache.causeway.applib.services.iactn.Execution;
import org.apache.causeway.applib.services.iactn.Interaction;
import org.apache.causeway.applib.services.iactn.PropertyEdit;
import org.apache.causeway.applib.services.metrics.MetricsService;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.core.metamodel.services.deadlock.DeadlockRecognizer;
import org.apache.causeway.core.metamodel.services.publishing.CommandPublisher;

/**
 * @since 2.0
 */
public interface InteractionInternal
extends Interaction {

    /**
     * (Modeled after {@link Callable}), is the implementation
     * by which the framework actually performs the interaction.
     */
    interface MemberExecutor<T extends Execution<?,?>> {
        Object execute(final T currentExecution);
    }

    record Context(
            ClockService clockService,
            MetricsService metricsService,
            CommandPublisher commandPublisher,
            DeadlockRecognizer deadlockRecognizer) {
    }

    /**
     * Use the provided {@link MemberExecutor} to invoke an action, with the provided
     * {@link ActionInvocation} capturing
     * the details of said action.
     * <p>
     * Because this both pushes an {@link Execution} to
     * represent the action invocation and then pops it, that completed
     * execution is accessible at {@link Interaction#getPriorExecution()}.
     */
    Object execute(
            final MemberExecutor<ActionInvocation> memberExecutor,
            final ActionInvocation actionInvocation,
            final Context context);

    /**
     * Use the provided {@link MemberExecutor} to edit a property, with the provided
     * {@link PropertyEdit}
     * capturing the details of said property edit.
     * <p>
     * Because this both pushes an {@link Execution} to
     * represent the property edit and then pops it, that completed
     * execution is accessible at {@link Interaction#getPriorExecution()}.
     */
    Object execute(
            final MemberExecutor<PropertyEdit> memberExecutor,
            final PropertyEdit propertyEdit,
            final Context context);

    /**
     * Numbers the executions (an action invocation or property edit) within
     * a given {@link Interaction}.
     *
     * <p>
     * Each {@link Interaction} is initiated by an execution of action
     * invocation or a property edit.  Thereafter there could be multiple
     * other executions as the result of nested calls using the
     * {@link WrapperFactory}.
     * </p>
     *
     * <p>
     * Another possible reason is support for bulk action invocations.
     * </p>
     *
     * @see Interaction
     * @see WrapperFactory
     */
    LongAdder getExecutionSequence();

    /**
     * Framework internal use: generates sequence of numbers for executions.
     * @see #getExecutionSequence()
     */
    default int getThenIncrementExecutionSequence() {
        final int counter = getExecutionSequence().intValue();
        getExecutionSequence().increment();
        return counter;
    }

    /**
     * Numbers the transactions within a given {@link Interaction}.
     *
     * <p>
     * Each {@link Interaction} is executed within the context of a transaction, but
     * the (occasionally) the transaction may be committed and a new one
     * started as the result of the domain object using the
     * {@link org.apache.causeway.applib.services.xactn.TransactionService}.
     * </p>
     *
     * @see Interaction
     * @see org.apache.causeway.applib.services.xactn.TransactionService
     */
    LongAdder getTransactionSequence();

    /**
     * Framework internal use: generates sequence of numbers for transactions.
     * @see #getTransactionSequence()
     */
    default int getThenIncrementTransactionSequence() {
        final int counter = getTransactionSequence().intValue();
        getTransactionSequence().increment();
        return counter;
    }

    /**
     * throws if there was any exception, otherwise returns the prior execution
     */
    default Execution<?, ?> getPriorExecutionOrThrowIfAnyException(
            final @NonNull ActionInvocation actionInvocation) {
        var priorExecution = getPriorExecution();
        var executionExceptionIfAny = getPriorExecution().getThrew();
        actionInvocation.setThrew(executionExceptionIfAny);
        if(executionExceptionIfAny != null) {
            throw executionExceptionIfAny instanceof RuntimeException
                ? ((RuntimeException)executionExceptionIfAny)
                : new RuntimeException(executionExceptionIfAny);
        }
        return priorExecution;
    }

}
