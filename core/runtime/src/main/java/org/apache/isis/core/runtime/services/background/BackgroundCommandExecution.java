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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService2;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandExecutorService;
import org.apache.isis.applib.services.command.CommandWithDto;
import org.apache.isis.applib.services.sessmgmt.SessionManagementService;
import org.apache.isis.core.runtime.services.background.CommandExecutionAbstract;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.runtime.system.transaction.TransactionalClosure;

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


    protected void doExecute(Object context) {

        final PersistenceSession persistenceSession = getPersistenceSession();
        final IsisTransactionManager transactionManager = getTransactionManager(persistenceSession);
        final List<Command> commands = Lists.newArrayList();
        transactionManager.executeWithinTransaction(new TransactionalClosure() {
            @Override
            public void execute() {
                commands.addAll(findBackgroundCommandsToExecute());
            }
        });

        LOG.debug("Found {} to execute", commands.size());

        // convert Commands to Bookmarks so that we can close the initial session.
        final List<Bookmark> bookmarks = new ArrayList<>();
        for (final Command command : commands) {
            bookmarks.add(bookmarkService.bookmarkFor(command));
        }

        // now, for each bookmark (= Command), obtain a new session and perform the work within a xactn in that session.
        for (final Bookmark bookmark: bookmarks) {

            sessionManagementService.nextSession();

            final PersistenceSession persistenceSessionForEachCommand = getPersistenceSession();
            final IsisTransactionManager transactionManagerForEachCommand =
                    getTransactionManager(persistenceSessionForEachCommand);

            transactionManagerForEachCommand.executeWithinTransaction(new TransactionalClosure() {
                @Override
                public void execute() {
                    final CommandWithDto command = bookmarkService
                            .lookup(bookmark, BookmarkService2.FieldResetPolicy.DONT_RESET, CommandWithDto.class);
                    executeForEachCommand(transactionManagerForEachCommand, command);
                }
            });
        }
    }

    protected void executeForEachCommand(final IsisTransactionManager transactionManager, final CommandWithDto command) {
        super.execute(transactionManager, command);
    }

    /**
     * Mandatory hook method
     */
    protected abstract List<? extends Command> findBackgroundCommandsToExecute();

    @Inject
    BookmarkService2 bookmarkService;

    @Inject
    SessionManagementService sessionManagementService;

}
