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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.iactn.Execution;
import org.apache.causeway.applib.services.iactn.Interaction;
import org.apache.causeway.commons.internal.base._Casts;

import lombok.extern.slf4j.Slf4j;

/**
 * Captures {@link Execution} sequences and their parent child relation ship.
 *
 * @since 4.0
 */
@Slf4j
record SimpleInteraction(
		ExecutionContext executionContext,
		UUID interactionId,
		Command command, // shared with parent if any
		Map<Class<?>, Object> attributes,
		List<Execution<?,?>> executionGraphs,
		Execution<?, ?>[] executionBuffer,
		AtomicBoolean closed) implements Interaction {

    /**
     * To be used for root layers, when we need a new {@link Command}
     */
	SimpleInteraction(
			final ExecutionContext executionContext) {
		this(executionContext, new Command(executionContext.idGenerator().interactionId()));
	}

	/**
	 * To be used for parented layers, so the {@link Command} can be shared
	 */
	SimpleInteraction(
            final ExecutionContext executionContext,
            final Command command) {
	    this(executionContext, command.getInteractionId(), command,
                new HashMap<>(), // not thread-safe
                new ArrayList<>(),
                new Execution<?, ?>[2],
                new AtomicBoolean(false));
    }

	@Override public UUID getInteractionId() { return interactionId; }
	@Override public Command getCommand() { return command; }
	@Override public Execution<?, ?> getCurrentExecution() { return executionBuffer[0]; }
	@Override public Execution<?, ?> getPriorExecution() { return executionBuffer[1]; }

	boolean isClosed() { return closed().get(); }

    /**
     * Push a new {@link org.apache.causeway.applib.events.domain.AbstractDomainEvent}
     * onto the stack of events held by the command.
     *
     * @throws IllegalStateException if closed
     */
	<E extends Execution<?,?>> E push(final E execution) {
	    if(isClosed())
            throw new IllegalStateException("Cannot push, as was already closed");
		if(getCurrentExecution() == null) {
            // new root-level execution
			executionGraphs.add(execution);
        } else {
        	// new parented execution
            execution.setParent(getCurrentExecution());
        }
        // advance buffer
        return next(execution);
	}

	/**
     * @throws IllegalStateException if closed or no current execution to pop
     */
	Execution<?,?> pop() {
	    if(isClosed())
	        throw new IllegalStateException("Cannot pop, as was already closed");
		final Execution<?,?> popped = getCurrentExecution();
		if(popped == null)
            throw new IllegalStateException("No current execution to pop");
        // advance buffer
        next(popped.getParent());
        return popped;
	}

	/**
	 * Marks closed, clears all {@link Execution} references and clears the attribute map.
	 */
	void close() {
	    closed.set(true);
	    executionBuffer[0] = null;
	    executionBuffer[1] = null;
	    executionGraphs.clear();
	    attributes.clear();
	}

    // -- ATTRIBUTES

    @Override
    public <T> T putAttribute(final Class<? super T> type, final T value) {
        if(isClosed())
            throw new IllegalStateException("Cannot put an attribute, as was already closed");
        return _Casts.uncheckedCast(attributes.put(type, value));
    }

    @Override
    public <T> T computeAttributeIfAbsent(final Class<? super T> type, final Function<Class<?>, ? extends T> mappingFunction) {
        if(isClosed())
            throw new IllegalStateException("Cannot compute an attribute, as was already closed");
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

    // -- HELPER

	private <E extends Execution<?,?>> E next(final E execution) {
		executionBuffer[1] = executionBuffer[0];
		executionBuffer[0] = execution;
		return execution;
	}

}
