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
package org.apache.isis.core.runtimeservices.background;

import javax.inject.Inject;

import org.apache.isis.applib.services.command.CommandExecutorService;
import org.apache.isis.applib.services.command.CommandWithDto;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.core.runtime.session.template.AbstractIsisSessionTemplate;

/**
 */
public abstract class CommandExecutionAbstract extends AbstractIsisSessionTemplate {

    @Inject protected CommandExecutorService commandExecutorService;
    
    private final CommandExecutorService.SudoPolicy sudoPolicy;

    protected CommandExecutionAbstract(final CommandExecutorService.SudoPolicy sudoPolicy) {
        this.sudoPolicy = sudoPolicy;
    }

    /**
     * Executes the command within a transaction, and with respect to the {@link CommandExecutorService.SudoPolicy}
     * specified in the constructor.
     *
     * <p>
     *     Uses {@link CommandExecutorService} to actually execute the command.
     * </p>
     */
    protected final void execute(final CommandWithDto commandWithDto, TransactionService transactionService) {
        transactionService.executeWithinTransaction(()->{

            // the executor service will handle any exceptions thrown.
            commandExecutorService.executeCommand(sudoPolicy, commandWithDto);

        });
    }


}
