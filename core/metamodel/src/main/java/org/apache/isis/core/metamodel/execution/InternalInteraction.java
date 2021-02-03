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
package org.apache.isis.core.metamodel.execution;

import java.util.concurrent.Callable;

import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.metrics.MetricsService;

/**
 * @since 2.0
 */
public interface InternalInteraction extends Interaction {

    /**
     * (Modeled after {@link Callable}), is the implementation
     * by which the framework actually performs the interaction.
     */
    public interface MemberExecutor<T extends Execution<?,?>> {
        Object execute(final T currentExecution);
    }

    /**
     * Use the provided {@link MemberExecutor} to invoke an action, with the provided
     * {@link org.apache.isis.applib.services.iactn.Interaction.ActionInvocation} capturing 
     * the details of said action.
     * <p>
     * Because this both pushes an {@link org.apache.isis.applib.services.iactn.Interaction.Execution} to
     * represent the action invocation and then pops it, that completed
     * execution is accessible at {@link Interaction#getPriorExecution()}.
     */
    Object execute(
            final MemberExecutor<ActionInvocation> memberExecutor,
            final ActionInvocation actionInvocation,
            final ClockService clockService,
            final MetricsService metricsService,
            final Command command);

    /**
     * Use the provided {@link MemberExecutor} to edit a property, with the provided
     * {@link org.apache.isis.applib.services.iactn.Interaction.PropertyEdit} 
     * capturing the details of said property edit.
     * <p>
     * Because this both pushes an {@link org.apache.isis.applib.services.iactn.Interaction.Execution} to
     * represent the property edit and then pops it, that completed
     * execution is accessible at {@link Interaction#getPriorExecution()}.
     */
    Object execute(
            final MemberExecutor<PropertyEdit> memberExecutor,
            final PropertyEdit propertyEdit,
            final ClockService clockService,
            final MetricsService metricsService,
            final Command command);
    
}
