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

import java.util.UUID;
import java.util.concurrent.Callable;

import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.iactn.Execution;
import org.apache.causeway.applib.services.iactn.Interaction;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
record InteractionCarrierDefault(
		SimpleInteraction interaction,
		ExecutionState executionState)
implements InteractionCarrier {

	public InteractionCarrierDefault(final ExecutionContext executionContext) {
		this(new ExecutionState(executionContext));
	}

	public InteractionCarrierDefault(final ExecutionState executionState) {
        this(new SimpleInteraction(executionState), executionState);
    }

	UUID interactionId() { return interaction.getInteractionId(); }
	ExecutionContext executionContext() { return executionState.executionContext(); }
	@Override public Command command() { return executionState.command(); }
	Interaction getInteraction() { return interaction; }
	boolean isClosed() { return interaction.isClosed(); }

	/**
	 * Creates a new {@link InteractionCarrier} that is parented by this one.
	 *
	 * <p>The {@link Command} is a shared instance. And so are the counters.
	 */
	InteractionCarrier childCarrier() {
        return new InteractionCarrierDefault(executionState);
    }

	@SneakyThrows
	@Override
    public <E extends Execution<?,?>, R> R execute(final E execution, final Callable<R> callable) {
	    if(isClosed()) {
            throw new IllegalStateException("Cannot execute, as was already closed");
	    }
    	push(execution);
    	start(execution);
    	try {
    		return callable.call();
    	} catch (Exception ex) {
            //TODO there is an issue with exceptions getting swallowed, unless this is fixed,
            // we rather print all of them, no matter whether recognized or not later on
            // examples are IllegalArgument- or NullPointer- exceptions being swallowed when using the
            // WrapperFactory utilizing async calls

            if(executionContext().deadlockRecognizer().isDeadlock(ex)) {
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

            interaction.getCurrentExecution().setThrew(ex);

            // propagate (as in previous design); caller will need to trap and decide
            throw ex;
    	} finally {
    		popAndComplete();
    	}
    }

    /**
     * Generates sequence of numbers for executions.
     *
     * <p>Numbers the transactions within a given {@link Interaction}.
     *
     * <p>Each {@link Interaction} is executed within the context of a transaction, but
     * the (occasionally) the transaction may be committed and a new one
     * started as the result of the domain object using the
     * {@link org.apache.causeway.applib.services.xactn.TransactionService}.
     *
     * @throws IllegalStateException if closed
     * @see #nextTransactionSequence()
     */
    @Override
	public int nextExecutionSequence() {
        if(isClosed())
            throw new IllegalStateException("Cannot calculate next ExecutionSequence, as was already closed");
        executionState.executionSequence().increment();
        return executionState.executionSequence().intValue() - 1;
    }

    /**
     * Generates sequence of numbers for transactions.
     *
     * <p>Numbers the transactions within a given {@link Interaction}.
     *
     * <p>Each {@link Interaction} is executed within the context of a transaction, but
     * the (occasionally) the transaction may be committed and a new one
     * started as the result of the domain object using the
     * {@link org.apache.causeway.applib.services.xactn.TransactionService}.
     *
     * @throws IllegalStateException if closed
     * @see Interaction
     * @see org.apache.causeway.applib.services.xactn.TransactionService
     * @see #nextExecutionSequence()
     */
    @Override
	public int nextTransactionSequence() {
        if(isClosed())
            throw new IllegalStateException("Cannot calculate next TransactionSequence, as was already closed");
        executionState.transactionSequence().increment();
        return executionState.transactionSequence().intValue() - 1;
    }

    void onLayerClosing() {
        if(isClosed())
            return;
        interaction.close();
    }

	// -- HELPER

	private <E extends Execution<?,?>> void push(final E execution) {
		interaction.push(execution);
	}

	private <E extends Execution<?,?>> void start(final E execution) {
		var startedAt = execution.start(executionContext().clockService(), executionContext().metricsService());
        // set the startedAt (and update command if this is the top-most member execution)
        // (this isn't done within Interaction#execute(...) because it requires the DTO
        // to have been set on the current execution).
        if(command().getStartedAt() == null) {
            command().updater().setStartedAt(startedAt);
            command().updater().setPublishingPhase(Command.CommandPublishingPhase.STARTED);
        }
        executionContext().commandPublisher().start(command());
	}

	private Execution<?,?> popAndComplete() {
		final Execution<?,?> popped = interaction.pop();
		popped.setCompletedAt(
        		executionContext().clockService().getClock().nowAsJavaSqlTimestamp(),
        		executionContext().metricsService());
		return popped;
	}

}
