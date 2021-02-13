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

import java.util.List;
import java.util.UUID;

import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.events.domain.PropertyDomainEvent;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.commons.having.HasUniqueId;
import org.apache.isis.schema.common.v2.InteractionType;
import org.apache.isis.schema.ixn.v2.ActionInvocationDto;
import org.apache.isis.schema.ixn.v2.PropertyEditDto;

import lombok.Getter;

/**
 * Represents an action invocation or property modification, resulting in some
 * state change of the system.  It captures not only the target object and
 * arguments passed, but also builds up the call-graph, and captures metrics,
 * eg for profiling.
 *
 * <p>
 *     The `Interaction` can be used to obtain the  {@link Command} object
 *     representing the top-level invocation action/property edit.
 * </p>
 *
 * <p>
 *     The distinction between {@link Command} and this object is perhaps
 *     subtle: the former represents the intention to invoke an action/edit a
 *     property, whereas this represents the actual invocation/edit itself.
 * </p>
 *
 * <p>
 *     To confuse matters slightly, historically the {@link Command} interface
 *     defines members (specifically: {@link Command#getStartedAt()},
 *     {@link Command#getCompletedAt()}, {@link Command#getResult()},
 *     {@link Command#getException()}) which logically belong to this class
 *     instead; they remain in {@link Command} for backward compatibility only
 *     (and have been deprecated).
 * </p>
 *
 * @since 1.x revised for 2.0 {@index}
 */
public interface Interaction extends HasUniqueId {

    /**
     * The unique identifier of this interaction (inherited from
     * {@link HasUniqueId})
     *
     * <p>
     *     This can be used to correlate audit records and transactions
     *     happening as a consequence or within the interaction.
     * </p>
     *
     * @return
     */
    @Override
    UUID getUniqueId();

    /**
     * Represents the <i>intention</i> to perform this interaction.
     * @return
     */
    Command getCommand();

    /**
     * The current (most recently pushed) {@link Execution}.
     */
    Execution<?,?> getCurrentExecution();

    /**
     * The execution that preceded the current one.
     */
    Execution<?,?> getPriorExecution();


    /**
     * Generates numbers in a named sequence.
     *
     * The name of the sequence can be arbitrary, though note that the framework also uses this capability to
     * generate sequence numbers corresponding to the sequences enumerated by the {@link Sequence} enum.
     */
    int next(final String sequenceId);


}
