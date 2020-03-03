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
package org.apache.isis.applib.services.command.spi;

import org.apache.isis.applib.services.command.Command;

/**
 * Default factory service for {@link Command}s.
 */
// tag::refguide[]
public interface CommandService {

    // end::refguide[]
    /**
     * Simply instantiates the appropriate instance of the {@link Command}.
     *
     * <p>
     * Its members will be populated automatically by the framework (the {@link Command}'s
     * {@link Command#getTimestamp()}, {@link Command#getUser()} and {@link Command#getUniqueId()}).
     * </p>
     */
    // tag::refguide[]
    Command create();                               // <.>
    // end::refguide[]

    /**
     * Hint for this implementation to eagerly persist the {@link Command}s if possible; influences the behaviour
     * of actions annotated to execute in the {@link org.apache.isis.applib.annotation.CommandExecuteIn#BACKGROUND}.
     */
    // tag::refguide[]
    boolean persistIfPossible(Command command);     // <.>
    // end::refguide[]

    /**
     * &quot;Complete&quot; the command, typically meaning to indicate that the command is completed, and to
     * persist it if its {@link Command#getPersistence()} and {@link Command#isPersistHint() persistence hint}
     * indicate that it should be.
     *
     * <p>
     * However, not every implementation necessarily {@link #persistIfPossible(Command) supports persistence}.
     *
     * <p>
     *     The framework will automatically have set the {@link Command#getCompletedAt()} property.
     * </p>
     */
    // tag::refguide[]
    void complete(final Command command);           // <.>
}
// end::refguide[]
