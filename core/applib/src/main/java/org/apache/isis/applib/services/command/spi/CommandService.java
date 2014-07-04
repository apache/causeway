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
 * Factory and persistence service for {@link Command}s.
 *
 * <p>
 * There is currently only one implementation, <tt>CommandServiceJdo</tt>, part of the
 * <tt>o.a.i.module:isis-module-command-jdo</tt>.  To use, must both include on the classpath and also
 * register its services (eg in <tt>isis.properties</tt>).
 */
public interface CommandService {

    @Programmatic
    Command create();
    
    /**
     * Although the transactionId is also provided in the
     * {@link #complete(Command)} callback, it is passed in here as well
     * so that an implementation can ensure that the {@link Command} is fully populated in order
     * to persist if required.
     * 
     * <p>
     * One case where this may be supported (for example, by the <tt>CommandServiceJdo</tt> implementation)
     * is to flush still-running {@link Command}s to the database on-demand.
     */
    @Programmatic
    void startTransaction(final Command command, final UUID transactionId);
    
    /**
     * &quot;Complete&quot; the command, typically meaning to indicate that the command is completed, and to 
     * persist it if its {@link Command#getPersistence()} and {@link Command#isPersistHint() persistence hint} 
     * indicate that it should be.
     * 
     * <p>
     * However, not every implementation necessarily {@link #persistIfPossible(Command) supports persistence}.
     */
    @Programmatic
    void complete(final Command command);

    /**
     * Hint for this implementation to eagerly persist the {@link Command}s if possible; influences the behaviour 
     * of actions annotated to execute in the {@link org.apache.isis.applib.annotation.Command.ExecuteIn#BACKGROUND}.
     */
    @Programmatic
    boolean persistIfPossible(Command command);
    
}
