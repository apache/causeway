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

package org.apache.isis.jdo.persistence;

import java.util.UUID;
import java.util.function.Supplier;

import javax.enterprise.inject.Vetoed;

import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactn.InteractionContext;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.commons.internal.components.SessionScopedComponent;
import org.apache.isis.jdo.persistence.IsisTransactionJdo.State;
import org.apache.isis.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtime.system.session.IsisSession;
import org.apache.isis.runtime.system.transaction.IsisTransactionManagerException;

import lombok.extern.log4j.Log4j2;

@Vetoed @Log4j2
class IsisTransactionManagerJdoInternal implements SessionScopedComponent {

    private int transactionLevel;

    private IsisSession session;

    /**
     * Holds the current or most recently completed transaction.
     */
    private IsisTransactionJdo currentTransaction;

    // -- constructor, fields

    private final PersistenceSession persistenceSession;
    private final ServiceRegistry serviceRegistry;

    private final CommandContext commandContext;
    private final InteractionContext interactionContext;

    public IsisTransactionManagerJdoInternal(PersistenceSession persistenceSession) {

        this.persistenceSession = persistenceSession;
        this.serviceRegistry = IsisContext.getServiceRegistry();

        this.commandContext = this.serviceRegistry.lookupServiceElseFail(CommandContext.class);
        this.interactionContext = this.serviceRegistry.lookupServiceElseFail(InteractionContext.class);
    }

    public PersistenceSession getPersistenceSession() {
        return persistenceSession;
    }

    // -- open, close

    public void open() {
        assert session != null;
    }

    public void close() {
        if (getCurrentTransaction() != null) {
            try {
                abortTransaction();
            } catch (final Exception e2) {
                log.error("failure during abort", e2);
            }
        }
        session = null;
    }


    // -- current transaction (if any)
    /**
     * The current transaction, if any.
     */
    public IsisTransactionJdo getCurrentTransaction() {
        return currentTransaction;
    }

    public int getTransactionLevel() {
        return transactionLevel;
    }




    // -- Transactional Execution
    /**
     * Run the supplied {@link Runnable block of code (closure)} in a
     * {@link IsisTransactionJdo transaction}.
     *
     * <p>
     * If a transaction is in progress, then
     * uses that. Otherwise will {@link #startTransaction() start} a transaction
     * before running the block and {@link #endTransaction() commit} it at the
     * end.
     *  </p>
     *
     * <p>
     *  If the closure throws an exception, then will {@link #abortTransaction() abort} the transaction if was
     *  started here, or will ensure that an already-in-progress transaction cannot commit.
     * </p>
     */
    public void executeWithinTransaction(final Runnable task) {
        executeWithinTransaction(null, task);
    }
    
    public void executeWithinTransaction(
            final Command existingCommandIfAny,
            final Runnable task) {
        final boolean initiallyInTransaction = inTransaction();
        if (!initiallyInTransaction) {
            startTransaction(existingCommandIfAny);
        }
        try {
            task.run();
            if (!initiallyInTransaction) {
                endTransaction();
            }
        } catch (final RuntimeException ex) {
            if (!initiallyInTransaction) {
                try {
                    abortTransaction();
                } catch (final Exception e) {
                    log.error("Abort failure after exception", e);
                    throw new IsisTransactionManagerException("Abort failure: " + e.getMessage(), ex);
                }
            } else {
                // ensure that this xactn cannot be committed
                getCurrentTransaction().setAbortCause(new IsisException(ex));
            }
            throw ex;
        }
    }

    /**
     * Run the supplied {@link Runnable block of code (closure)} in a
     * {@link IsisTransactionJdo transaction}.
     *
     * <p>
     * If a transaction is in progress, then uses that. Otherwise will {@link #startTransaction() start} a transaction
     * before running the block and {@link #endTransaction() commit} it at the
     * end.
     *  </p>
     *
     * <p>
     *  If the closure throws an exception, then will {@link #abortTransaction() abort} the transaction if was
     *  started here, or will ensure that an already-in-progress transaction cannot commit.
     *  </p>
     */
    public <Q> Q executeWithinTransaction(final Supplier<Q> task) {
        return executeWithinTransaction(null, task);
    }
    
    public <Q> Q executeWithinTransaction(
            final Command existingCommandIfAny,
            final Supplier<Q> task) {
        final boolean initiallyInTransaction = inTransaction();
        if (!initiallyInTransaction) {
            startTransaction(existingCommandIfAny);
        }
        try {
            final Q retVal = task.get();
            if (!initiallyInTransaction) {
                endTransaction();
            }
            return retVal;
        } catch (final RuntimeException ex) {
            if (!initiallyInTransaction) {
                abortTransaction();
            } else {
                // ensure that this xactn cannot be committed (sets state to MUST_ABORT), and capture the cause so can be rendered appropriately by some higher level in the call stack
                getCurrentTransaction().setAbortCause(new IsisException(ex));
            }
            throw ex;
        }
    }

    public boolean inTransaction() {
        return getCurrentTransaction() != null && !getCurrentTransaction().getState().isComplete();
    }



    // -- startTransaction

    public IsisTransactionJdo startTransaction() {
        startTransaction(null);
        return getCurrentTransaction();
    }

    /**
     * @param existingCommandIfAny - specifically, a previously persisted background {@link Command}, now being executed by a background execution service.
     */
    public void startTransaction(final Command existingCommandIfAny) {
        boolean noneInProgress = false;
        if (getCurrentTransaction() == null || getCurrentTransaction().getState().isComplete()) {
            noneInProgress = true;

            // previously we called __isis_startRequest here on all RequestScopedServices.  This is now
            // done earlier, in PersistenceSession#open(). If we introduce support for @TransactionScoped
            // services, then this would be the place to initialize them.


            // allow the command to be overridden (if running as a background command with a parent command supplied)

            final Interaction interaction = interactionContext.getInteraction();

            final Command command;
            if (existingCommandIfAny != null) {
                commandContext.setCommand(existingCommandIfAny);
                interaction.setUniqueId(existingCommandIfAny.getUniqueId());
            }
            command = commandContext.getCommand();
            final UUID transactionId = command.getUniqueId();

            this.currentTransaction = new IsisTransactionJdo(transactionId,
                    interaction.next(Interaction.Sequence.TRANSACTION.id()));
            transactionLevel = 0;

            persistenceSession.startTransaction();
        }

        transactionLevel++;

        if (log.isDebugEnabled()) {
            log.debug("startTransaction: level {}->{}{}", (transactionLevel - 1), (transactionLevel), (noneInProgress ? " (no transaction in progress or was previously completed; transaction created)" : ""));
        }
    }



    // -- flushTransaction
    public boolean flushTransaction() {

        if (log.isDebugEnabled()) {
            log.debug("flushTransaction");
        }

        if (getCurrentTransaction() != null) {
            getCurrentTransaction().flush();
        }
        return false;
    }



    // -- endTransaction, abortTransaction
    /**
     * Ends the transaction if nesting level is 0 (but will abort the transaction instead,
     * even if nesting level is not 0, if an {@link IsisTransactionJdo#getAbortCause() abort cause}
     * has been {@link IsisTransactionJdo#setAbortCause(IsisException) set}.
     *
     * <p>
     * If in the process of committing the transaction an exception is thrown, then this will
     * be handled and will abort the transaction instead.
     *
     * <p>
     * If an abort cause has been set (or an exception occurs), then will throw this
     * exception in turn.
     */
    public void endTransaction() {

        final IsisTransactionJdo transaction = getCurrentTransaction();
        if (transaction == null) {
            // allow this method to be called >1 with no adverse affects

            if (log.isDebugEnabled()) {
                log.debug("endTransaction: level {} (no transaction exists)", transactionLevel);
            }

            return;
        } else if (transaction.getState().isComplete()) {
            // allow this method to be called >1 with no adverse affects

            if (log.isDebugEnabled()) {
                log.debug("endTransaction: level {} (previous transaction completed)", transactionLevel);
            }

            return;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("endTransaction: level {}->{}", transactionLevel, transactionLevel - 1);
            }
        }


        try {
            endTransactionInternal();
        } finally {
            final IsisTransactionJdo.State state = getCurrentTransaction().getState();
            int transactionLevel = this.transactionLevel;
            if(transactionLevel == 0 && !state.isComplete()) {
                log.error("endTransaction: inconsistency detected between transactionLevel {} and transactionState '{}'", transactionLevel, state);
            }
        }
    }

    private void endTransactionInternal() {

        final IsisTransactionJdo transaction = getCurrentTransaction();

        // terminate the transaction early if an abort cause was already set.
        RuntimeException abortCause = this.getCurrentTransaction().getAbortCause();
        if(transaction.getState().mustAbort()) {

            if (log.isDebugEnabled()) {
                log.debug("endTransaction: aborting instead [EARLY TERMINATION], abort cause '{}' has been set", abortCause.getMessage());
            }
            try {
                abortTransaction();

                // just in case any different exception was raised...
                abortCause = this.getCurrentTransaction().getAbortCause();

            } catch(RuntimeException ex) {

                abortCause = ex;

            }


            if(abortCause != null) {

                // re-introduced the throwing of exceptions in 1.15.1 (same as 1.14.x)

                // in 1.15.0 we were not throwing exceptions at this point, resulting in JDOUserException errors
                // (eg malformed SQL) simply being silently ignored

                // the reason that no exceptions were being thrown in 1.15.0 was because it was observed that
                // throwing exceptions always resulted in forwarding to the error page, even if the error had been
                // recognised at the UI layer.  This was the rationale given, at least.
                //
                // Not certain now it is correct; if it was to improve the UI experience.
                //
                // Certainly swallowing severe exceptions is much less acceptable.  Therefore reverting.

                throw abortCause;


            } else {
                // assume that any rendering of the problem has been done lower down the stack.
                return;
            }
        }

        // we don't decrement the transactionLevel just yet, because an exception might end up being thrown
        // (meaning there would be more faffing around to ensure that the transactionLevel
        // and state of the currentTransaction remain in sync)
        int nextTransactionLevel = transactionLevel - 1;
        if ( nextTransactionLevel > 0) {
            transactionLevel --;
        } else if ( nextTransactionLevel == 0) {

            //
            // TODO: granted, this is some fairly byzantine coding.  but I'm trying to account for different types
            // of object store implementations that could start throwing exceptions at any stage.
            // once the contract/API for the objectstore is better tied down, hopefully can simplify this...
            //

            if(abortCause == null) {

                if (log.isDebugEnabled()) {
                    log.debug("endTransaction: committing");
                }

                try {
                    getCurrentTransaction().preCommit();
                } catch(Exception ex) {
                    // just in case any new exception was raised...

                    // this bizarre code because an InvocationTargetException (which is not a RuntimeException) was
                    // being thrown due to a coding error in a domain object
                    abortCause = ex instanceof RuntimeException ? (RuntimeException) ex : new RuntimeException(ex);

                    getCurrentTransaction().setAbortCause(new IsisTransactionManagerException(ex));
                }
            }

            if(abortCause == null) {
                try {
                    persistenceSession.endTransaction();
                } catch(Exception ex) {
                    // just in case any new exception was raised...
                    abortCause = ex instanceof RuntimeException ? (RuntimeException) ex : new RuntimeException(ex);

                    // hacky... moving the transaction back to something other than COMMITTED
                    getCurrentTransaction().setAbortCause(new IsisTransactionManagerException(ex));
                }
            }


            //
            // ok, everything that could have thrown an exception is now done,
            // so  it's safe to decrement the transaction level
            //
            transactionLevel = 0;


            // previously we called __isis_endRequest here on all RequestScopedServices.  This is now
            // done later, in PersistenceSession#close(). If we introduce support for @TransactionScoped
            // services, then this would be the place to finalize them.


            //
            // finally, if an exception was thrown, we rollback the transaction
            //
            if(abortCause != null) {

                if (log.isDebugEnabled()) {
                    log.debug("endTransaction: aborting instead, abort cause has been set");
                }
                try {
                    abortTransaction();
                } catch(RuntimeException ex) {
                    // ignore; nothing to do:
                    // * we want the existing abortCause to be available
                    // * the transactionLevel is correctly now at 0.
                }

                throw abortCause;
            } else {

                // keeping things in sync
                getCurrentTransaction().commit();
            }

        } else {
            // transactionLevel < 0
            log.error("endTransaction: transactionLevel={}", transactionLevel);
            transactionLevel = 0;
            IllegalStateException ex = new IllegalStateException(" no transaction running to end (transactionLevel < 0)");
            getCurrentTransaction().setAbortCause(new IsisException(ex));
            throw ex;
        }
    }

    public void abortTransaction() {
        if (getCurrentTransaction() != null) {
            getCurrentTransaction().markAsAborted();
            transactionLevel = 0;
            persistenceSession.abortTransaction();
        }
    }



    // -- addCommand
    public void addCommand(final PersistenceCommand command) {
        getCurrentTransaction().addCommand(command);
    }


}
