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
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.iactn.Execution;
import org.apache.causeway.applib.services.iactn.Interaction;
import org.apache.causeway.core.metamodel.execution.InteractionCarrier.InteractionCarrierForTesting;

import lombok.SneakyThrows;

/**
 * Carries an {@link Interaction} through its life-cycle.
 *
 * @since 4.0
 */
public sealed interface InteractionCarrier
permits InteractionCarrierDefault, InteractionCarrierForTesting {

	Interaction interaction();

	int nextExecutionSequence();
	int nextTransactionSequence();

	<E extends Execution<?, ?>, R> R execute(E execution, Callable<R> callable);

	// -- SHORTCUT

	default Command command() { return interaction().getCommand(); }

	// -- TESTING

	record InteractionCarrierForTesting(Interaction interaction, AtomicBoolean closed) implements InteractionCarrier {
	    public InteractionCarrierForTesting(final Interaction interaction) {
	        this(interaction, new AtomicBoolean(false));
	    }
	    @Override public int nextExecutionSequence() { return 0; }
	    @Override public int nextTransactionSequence() { return 0; }
	    void close() { closed.set(true); }
	    boolean isClosed() { return closed.get(); }
	    @SneakyThrows @Override
	    public <E extends Execution<?, ?>, R> R execute(final E execution, final Callable<R> callable) {
	        return callable.call();
	    }
	}

}
