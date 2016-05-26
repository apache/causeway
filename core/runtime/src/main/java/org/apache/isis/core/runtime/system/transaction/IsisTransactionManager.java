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

package org.apache.isis.core.runtime.system.transaction;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.command.spi.CommandService;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactn.InteractionContext;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.MessageBroker;
import org.apache.isis.core.commons.components.SessionScopedComponent;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSession;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

public class IsisTransactionManager implements SessionScopedComponent {

    private static final Logger LOG = LoggerFactory.getLogger(IsisTransactionManager.class);

    private final PersistenceSession persistenceSession;

    private int transactionLevel;
    
    private IsisSession session;

    /**
     * Holds the current or most recently completed transaction.
     */
    private IsisTransaction transaction;

    private final ServicesInjector servicesInjector;

    private final CommandContext commandContext;
    private final InteractionContext interactionContext;


    // ////////////////////////////////////////////////////////////////
    // constructor
    // ////////////////////////////////////////////////////////////////

    public IsisTransactionManager(
            final PersistenceSession persistenceSession,
            final ServicesInjector servicesInjector) {

        this.persistenceSession = persistenceSession;
        this.servicesInjector = servicesInjector;

        this.commandContext = lookupService(CommandContext.class);
        this.interactionContext = lookupService(InteractionContext.class);
    }

    public PersistenceSession getPersistenceSession() {
        return persistenceSession;
    }

    // ////////////////////////////////////////////////////////////////
    // open, close
    // ////////////////////////////////////////////////////////////////

    public void open() {
        ensureThatState(session, is(notNullValue()), "session is required");
    }

    public void close() {
        if (getTransaction() != null) {
            try {
                abortTransaction();
            } catch (final Exception e2) {
                LOG.error("failure during abort", e2);
            }
        }
        session = null;
    }

    // //////////////////////////////////////////////////////
    // current transaction (if any)
    // //////////////////////////////////////////////////////

    /**
     * The current transaction, if any.
     */
    public IsisTransaction getTransaction() {
        return transaction;
    }

    public int getTransactionLevel() {
        return transactionLevel;
    }


    /**
     * Convenience method returning the {@link org.apache.isis.core.commons.authentication.MessageBroker} of the
     * {@link #getTransaction() current transaction}.
     */
    protected MessageBroker getMessageBroker() {
        return getTransaction().getMessageBroker();
    }

    
    // ////////////////////////////////////////////////////////////////
    // Transactional Execution
    // ////////////////////////////////////////////////////////////////

    /**
     * Run the supplied {@link Runnable block of code (closure)} in a
     * {@link IsisTransaction transaction}.
     * 
     * <p>
     * If a transaction is {@link IsisContext#inTransaction() in progress}, then
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
    public void executeWithinTransaction(final TransactionalClosure closure) {
        executeWithinTransaction(null, closure);
    }
    public void executeWithinTransaction(
            final Command existingCommandIfAny,
            final TransactionalClosure closure) {
        final boolean initiallyInTransaction = inTransaction();
        if (!initiallyInTransaction) {
            startTransaction(existingCommandIfAny);
        }
        try {
            closure.execute();
            if (!initiallyInTransaction) {
                endTransaction();
            }
        } catch (final RuntimeException ex) {
            if (!initiallyInTransaction) {
                try {
                    abortTransaction();
                } catch (final Exception e) {
                    LOG.error("Abort failure after exception", e);
                    throw new IsisTransactionManagerException("Abort failure: " + e.getMessage(), ex);
                }
            } else {
                // ensure that this xactn cannot be committed
                getTransaction().setAbortCause(new IsisException(ex));
            }
            throw ex;
        }
    }

    /**
     * Run the supplied {@link Runnable block of code (closure)} in a
     * {@link IsisTransaction transaction}.
     *
     * <p>
     * If a transaction is {@link IsisContext#inTransaction() in progress}, then
     * uses that. Otherwise will {@link #startTransaction() start} a transaction
     * before running the block and {@link #endTransaction() commit} it at the
     * end.
     *  </p>
     *
     * <p>
     *  If the closure throws an exception, then will {@link #abortTransaction() abort} the transaction if was
     *  started here, or will ensure that an already-in-progress transaction cannot commit.
     *  </p>
     */
    public <Q> Q executeWithinTransaction(final TransactionalClosureWithReturn<Q> closure) {
        return executeWithinTransaction(null, closure);
    }
    public <Q> Q executeWithinTransaction(
            final Command existingCommandIfAny,
            final TransactionalClosureWithReturn<Q> closure) {
        final boolean initiallyInTransaction = inTransaction();
        if (!initiallyInTransaction) {
            startTransaction(existingCommandIfAny);
        }
        try {
            final Q retVal = closure.execute();
            if (!initiallyInTransaction) {
                endTransaction();
            }
            return retVal;
        } catch (final RuntimeException ex) {
            if (!initiallyInTransaction) {
                abortTransaction();
            } else {
                // ensure that this xactn cannot be committed (sets state to MUST_ABORT), and capture the cause so can be rendered appropriately by some higher level in the call stack
                getTransaction().setAbortCause(new IsisException(ex));
            }
            throw ex;
        }
    }

    public boolean inTransaction() {
        return getTransaction() != null && !getTransaction().getState().isComplete();
    }


    // //////////////////////////////////////////////////////
    // startTransaction
    // //////////////////////////////////////////////////////

    public synchronized void startTransaction() {
        startTransaction(null);
    }

    /**
     * @param existingCommandIfAny - specifically, a previously persisted background {@link Command}, now being executed by a background execution service.
     */
    public void startTransaction(final Command existingCommandIfAny) {
        boolean noneInProgress = false;
        if (getTransaction() == null || getTransaction().getState().isComplete()) {
            noneInProgress = true;

            // previously we called __isis_startRequest here on all RequestScopedServices.  This is now
            // done earlier, in PersistenceSession#open(). If we introduce support for @TransactionScoped
            // services, then this would be the place to initialize them.


            // allow the command to be overridden (if running as a background command with a parent command supplied)

            final Interaction interaction = interactionContext.getInteraction();

            final Command command;
            if (existingCommandIfAny != null) {
                commandContext.setCommand(existingCommandIfAny);
                interaction.setTransactionId(existingCommandIfAny.getTransactionId());
            }
            command = commandContext.getCommand();
            final UUID transactionId = command.getTransactionId();


            final MessageBroker messageBroker = MessageBroker.acquire(getAuthenticationSession());
            this.transaction = new IsisTransaction(this, messageBroker, servicesInjector, transactionId);
            transactionLevel = 0;

            persistenceSession.startTransaction();
        }

        transactionLevel++;

        if (LOG.isDebugEnabled()) {
            LOG.debug("startTransaction: level " + (transactionLevel - 1) + "->" + (transactionLevel) + (noneInProgress ? " (no transaction in progress or was previously completed; transaction created)" : ""));
        }
    }

    // //////////////////////////////////////////////////////
    // flush
    // //////////////////////////////////////////////////////

    public synchronized boolean flushTransaction() {

        if (LOG.isDebugEnabled()) {
            LOG.debug("flushTransaction");
        }

        if (getTransaction() != null) {
            getTransaction().flush();
        }
        return false;
    }

    // //////////////////////////////////////////////////////
    // end, abort
    // //////////////////////////////////////////////////////

    /**
     * Ends the transaction if nesting level is 0 (but will abort the transaction instead, 
     * even if nesting level is not 0, if an {@link IsisTransaction#getAbortCause() abort cause}
     * has been {@link IsisTransaction#setAbortCause(IsisException) set}.
     * 
     * <p>
     * If in the process of committing the transaction an exception is thrown, then this will
     * be handled and will abort the transaction instead.
     * 
     * <p>
     * If an abort cause has been set (or an exception occurs), then will throw this
     * exception in turn.
     */
    public synchronized void endTransaction() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("endTransaction: level " + (transactionLevel) + "->" + (transactionLevel - 1));
        }

        final IsisTransaction transaction = getTransaction();
        if (transaction == null || transaction.getState().isComplete()) {
            // allow this method to be called >1 with no adverse affects
            return;
        }

        // terminate the transaction early if an abort cause was already set.
        RuntimeException abortCause = this.getTransaction().getAbortCause();
        if(transaction.getState().mustAbort()) {
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("endTransaction: aborting instead [EARLY TERMINATION], abort cause '" + abortCause.getMessage() + "' has been set");
            }
            try {
                abortTransaction();

                // just in case any different exception was raised...
                abortCause = this.getTransaction().getAbortCause();
            } catch(RuntimeException ex) {
                
                // ... or, capture this most recent exception
                abortCause = ex;
            }
            
            
            if(abortCause != null) {
                // hasn't been rendered lower down the stack, so fall back
                throw abortCause;
            } else {
                // assume that any rendering of the problem has been done lower down the stack.
                return;
            }
        }

        
        transactionLevel--;
        if (transactionLevel == 0) {

            //
            // TODO: granted, this is some fairly byzantine coding.  but I'm trying to account for different types
            // of object store implementations that could start throwing exceptions at any stage.
            // once the contract/API for the objectstore is better tied down, hopefully can simplify this...
            //
            
            if(abortCause == null) {
            
                if (LOG.isDebugEnabled()) {
                    LOG.debug("endTransaction: committing");
                }

                try {
                    getTransaction().preCommit();
                } catch(RuntimeException ex) {
                    // just in case any new exception was raised...
                    abortCause = ex;
                    transactionLevel = 1; // because the transactionLevel was decremented earlier
                }
            }
            
            if(abortCause == null) {
                try {
                    persistenceSession.endTransaction();
                } catch(RuntimeException ex) {
                    // just in case any new exception was raised...
                    abortCause = ex;
                    
                    // hacky... moving the transaction back to something other than COMMITTED
                    transactionLevel = 1; // because the transactionLevel was decremented earlier
                    getTransaction().setAbortCause(new IsisTransactionManagerException(ex));
                }
            }

            if(abortCause == null) {
                try {
                    getTransaction().commit();
                } catch(RuntimeException ex) {
                    // just in case any new exception was raised...
                    abortCause = ex;
                    transactionLevel = 1; // because the transactionLevel was decremented earlier
                }
            }


            // previously we called __isis_endRequest here on all RequestScopedServices.  This is now
            // done later, in PersistenceSession#close(). If we introduce support for @TransactionScoped
            // services, then this would be the place to finalize them.

            if(abortCause != null) {
                
                if (LOG.isDebugEnabled()) {
                    LOG.debug("endTransaction: aborting instead, abort cause has been set");
                }
                try {
                    abortTransaction();
                } catch(RuntimeException ex) {
                    // ignore; nothing to do:
                    // * we want the existing abortCause to be available
                    // * the transactionLevel is correctly now at 0.
                }
                
                throw abortCause;
            }
        } else if (transactionLevel < 0) {
            LOG.error("endTransaction: transactionLevel=" + transactionLevel);
            transactionLevel = 0;
            throw new IllegalStateException(" no transaction running to end (transactionLevel < 0)");
        }
    }


    public synchronized void abortTransaction() {
        if (getTransaction() != null) {
            getTransaction().markAsAborted();
            transactionLevel = 0;
            persistenceSession.abortTransaction();
        }
    }

    public void addCommand(final PersistenceCommand command) {
        getTransaction().addCommand(command);
    }


    ////////////////////////////////////////////////////////////////////////
    // Dependencies (lookup)
    ////////////////////////////////////////////////////////////////////////

    private <T> T lookupService(Class<T> serviceType) {
        T service = lookupServiceIfAny(serviceType);
        if(service == null) {
            throw new IllegalStateException("Could not locate service of type '" + serviceType + "'");
        }
        return service;
    }

    /**
     * @return - the service, or <tt>null</tt> if no service registered of specified type.
     */
    private <T> T lookupServiceIfAny(Class<T> serviceType) {
        return servicesInjector.lookupService(serviceType);
    }


    // ////////////////////////////////////////////////////////////////
    // Dependencies (injected)
    // ////////////////////////////////////////////////////////////////

    /**
     * The owning {@link IsisSession}.
     * 
     * <p>
     * Will be non-<tt>null</tt> when {@link #open() open}ed, but <tt>null</tt>
     * if {@link #close() close}d .
     */
    public IsisSession getSession() {
        return session;
    }

    /**
     * Should be injected prior to {@link #open() opening}
     */
    public void setSession(final IsisSession session) {
        this.session = session;
    }

    
    // ////////////////////////////////////////////////////////////////
    // Dependencies (from context)
    // ////////////////////////////////////////////////////////////////

    /**
     * Called back by {@link IsisTransaction}.
     */
    protected AuthenticationSession getAuthenticationSession() {
        return IsisContext.getAuthenticationSession();
    }

    protected OidMarshaller getOidMarshaller() {
        return IsisContext.getOidMarshaller();
    }

}
