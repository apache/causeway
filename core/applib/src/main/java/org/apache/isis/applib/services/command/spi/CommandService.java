/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.applib.services.command.spi;

import java.util.UUID;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.command.Command;

/**
 * Default factory service for {@link Command}s.
 */
public interface CommandService {

    /**
     * Simply instantiate the appropriate instance of the {@link Command}.  Its members will be populated automatically
     * by the framework.
     */
    @Programmatic
    Command create();
    
    /**
     * DEPRECATED - this method is no longer called by the framework.
     *
     * @deprecated - the framework automatically populates the {@link Command}'s {@link Command#getTimestamp()}, {@link Command#getUser()}  and {@link Command#getTransactionId()}, so there is no need for the service implementation to initialize any of these.  In particular, the {@link Command} will already have been initialized with the provided <tt>transactionId</tt> argument.
     */
    @Deprecated
    @Programmatic
    void startTransaction(final Command command, final UUID transactionId);
    
    /**
     * Hint for this implementation to eagerly persist the {@link Command}s if possible; influences the behaviour 
     * of actions annotated to execute in the {@link org.apache.isis.applib.annotation.Command.ExecuteIn#BACKGROUND}.
     */
    @Programmatic
    boolean persistIfPossible(Command command);

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
    @Programmatic
    void complete(final Command command);

}
