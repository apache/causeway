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
package org.apache.isis.core.runtime.services.background;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandExecutorService;
import org.apache.isis.applib.services.command.CommandWithDto;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;

/**
 * Intended to be used as a base class for executing queued up {@link Command background action}s.
 *
 * <p>
 * This implementation uses the {@link #findBackgroundCommandsToExecute() hook method} so that it is
 * independent of the location where the actions have actually been persisted to.
 */
public abstract class BackgroundCommandExecution extends CommandExecutionAbstract {

    private final static Logger LOG = LoggerFactory.getLogger(BackgroundCommandExecution.class);

    /**
     * Defaults to the historical defaults * for running background commands.
     */
    public BackgroundCommandExecution() {
        this(CommandExecutorService.SudoPolicy.NO_SWITCH);
    }

    public BackgroundCommandExecution(final CommandExecutorService.SudoPolicy sudoPolicy) {
        super(sudoPolicy);
    }

    // //////////////////////////////////////

    @Override
    protected void doExecute(Object context) {

        final PersistenceSession persistenceSession = getPersistenceSession();
        final IsisTransactionManager transactionManager = getTransactionManager(persistenceSession);
        final List<Command> commands = _Lists.newArrayList();
        transactionManager.executeWithinTransaction(() -> {
            commands.addAll(findBackgroundCommandsToExecute());
        });

        LOG.debug("Found {} to execute", commands.size());

        for (final Command command : commands) {
            execute(transactionManager, (CommandWithDto) command);
        }
    }

    /**
     * Mandatory hook method
     */
    protected abstract List<? extends Command> findBackgroundCommandsToExecute();

}
