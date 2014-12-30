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

import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.applib.annotation.Bulk;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.command.CommandDefault;
import org.apache.isis.applib.services.command.spi.CommandService;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.MessageBroker;
import org.apache.isis.core.commons.components.SessionScopedComponent;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.TransactionalResource;
import org.apache.isis.core.runtime.services.RequestScopedService;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSession;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;

public class IsisTransactionManager implements SessionScopedComponent {

    private static final Logger LOG = LoggerFactory.getLogger(IsisTransactionManager.class);

    private final PersistenceSession persistenceSession;

    private int transactionLevel;
    
    private IsisSession session;

    /**
     * Holds the current or most recently completed transaction.
     */
    private IsisTransaction transaction;

    private final TransactionalResource transactionalResource;

    private final ServicesInjector servicesInjector;


    // ////////////////////////////////////////////////////////////////
    // constructor
    // ////////////////////////////////////////////////////////////////

    public IsisTransactionManager(final PersistenceSession persistenceSession, final TransactionalResource transactionalResource, final ServicesInjector servicesInjector) {
        this.persistenceSession = persistenceSession;
        this.transactionalResource = transactionalResource;
        this.servicesInjector = servicesInjector;
    }
    
    
    // ////////////////////////////////////////////////////////////////
    // open, close
    // ////////////////////////////////////////////////////////////////

    @Override
    public void open() {
        ensureThatState(session, is(notNullValue()), "session is required");
    }

    @Override
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
        final boolean initiallyInTransaction = inTransaction();
        if (!initiallyInTransaction) {
            startTransaction();
        }
        try {
            closure.preExecute();
            closure.execute();
            closure.onSuccess();
            if (!initiallyInTransaction) {
                endTransaction();
            }
        } catch (final RuntimeException ex) {
            closure.onFailure();
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
        final boolean initiallyInTransaction = inTransaction();
        if (!initiallyInTransaction) {
            startTransaction();
        }
        try {
            closure.preExecute();
            final Q retVal = closure.execute();
            closure.onSuccess();
            if (!initiallyInTransaction) {
                endTransaction();
            }
            return retVal;
        } catch (final RuntimeException ex) {
            closure.onFailure();
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

    // ////////////////////////////////////////////////////////////////
    // create transaction, + hooks
    // ////////////////////////////////////////////////////////////////

    /**
     * Creates a new transaction and saves, to be accessible in
     * {@link #getTransaction()}.
     */
    protected final IsisTransaction createTransaction() {
        MessageBroker messageBroker = createMessageBroker();
        return this.transaction = createTransaction(messageBroker, transactionalResource);
    }


    /**
     * The provided {@link org.apache.isis.core.commons.authentication.MessageBroker} is
     * obtained from the {@link #createMessageBroker()} hook method.
     * @param transactionalResource
     *
     * @see #createMessageBroker()
     */
    private IsisTransaction createTransaction(
            final MessageBroker messageBroker,
            final TransactionalResource transactionalResource) {
        ensureThatArg(messageBroker, is(not(nullValue())));

        return new IsisTransaction(this, messageBroker, transactionalResource, servicesInjector);
    }
    

    // //////////////////////////////////////////////////////
    // start
    // //////////////////////////////////////////////////////

    public synchronized void startTransaction() {

        boolean noneInProgress = false;
        if (getTransaction() == null || getTransaction().getState().isComplete()) {
            noneInProgress = true;


            startRequestOnRequestScopedServices();
            createCommandIfConfigured();
            initOtherApplibServicesIfConfigured();
            
            IsisTransaction isisTransaction = createTransaction();
            transactionLevel = 0;

            transactionalResource.startTransaction();

            startTransactionOnCommandIfConfigured(isisTransaction.getTransactionId());
        }

        transactionLevel++;

        if (LOG.isDebugEnabled()) {
            LOG.debug("startTransaction: level " + (transactionLevel - 1) + "->" + (transactionLevel) + (noneInProgress ? " (no transaction in progress or was previously completed; transaction created)" : ""));
        }
    }

    
    private void initOtherApplibServicesIfConfigured() {
        
        final Bulk.InteractionContext bic = getServiceOrNull(Bulk.InteractionContext.class);
        if(bic != null) {
            Bulk.InteractionContext.current.set(bic);
        }
    }

    private void startRequestOnRequestScopedServices() {

        final List<Object> registeredServices = servicesInjector.getRegisteredServices();

        // tell the proxy of all request-scoped services to instantiate the underlying
        // services, store onto the thread-local and inject into them...
        for (final Object service : registeredServices) {
            if(service instanceof RequestScopedService) {
                ((RequestScopedService)service).__isis_startRequest(servicesInjector);
            }
        }
        // ... and invoke all @PostConstruct
        for (final Object service : registeredServices) {
            if(service instanceof RequestScopedService) {
                ((RequestScopedService)service).__isis_postConstruct();
            }
        }
    }

    private void endRequestOnRequestScopeServices() {
        // tell the proxy of all request-scoped services to invoke @PreDestroy
        // (if any) on all underlying services stored on their thread-locals...
        for (final Object service : servicesInjector.getRegisteredServices()) {
            if(service instanceof RequestScopedService) {
                ((RequestScopedService)service).__isis_preDestroy();
            }
        }

        // ... and then remove those underlying services from the thread-local
        for (final Object service : servicesInjector.getRegisteredServices()) {
            if(service instanceof RequestScopedService) {
                ((RequestScopedService)service).__isis_endRequest();
            }
        }
    }

    private void createCommandIfConfigured() {
        final CommandContext commandContext = getServiceOrNull(CommandContext.class);
        if(commandContext == null) {
            return;
        } 
        final CommandService commandService = getServiceOrNull(CommandService.class);
        final Command command = 
                commandService != null 
                    ? commandService.create() 
                    : new CommandDefault();
        commandContext.setCommand(command);

        if(command.getTimestamp() == null) {
            command.setTimestamp(Clock.getTimeAsJavaSqlTimestamp());
        }
        if(command.getUser() == null) {
            command.setUser(getAuthenticationSession().getUserName());
        }
        
        // the remaining properties are set further down the call-stack, if an action is actually performed
    }

    /**
     * Called by IsisTransactionManager on start
     */
    public void startTransactionOnCommandIfConfigured(final UUID transactionId) {
        final CommandContext commandContext = getServiceOrNull(CommandContext.class);
        if(commandContext == null) {
            return;
        } 
        final CommandService commandService = getServiceOrNull(CommandService.class);
        if(commandService == null) {
            return;
        } 
        final Command command = commandContext.getCommand();
        commandService.startTransaction(command, transactionId);
    }


    /**
     * @return - the service, or <tt>null</tt> if no service registered of specified type.
     */
    public <T> T getServiceOrNull(Class<T> serviceType) {
        return servicesInjector.lookupService(serviceType);
    }



    // //////////////////////////////////////////////////////
    // flush
    // //////////////////////////////////////////////////////

    public synchronized boolean flushTransaction() {

        if (LOG.isDebugEnabled()) {
            LOG.debug("flushTransaction");
        }

        if (getTransaction() != null) {
            persistenceSession.objectChangedAllDirty();
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
                    persistenceSession.objectChangedAllDirty();
                } catch(RuntimeException ex) {
                    // just in case any new exception was raised...
                    abortCause = ex;
                    transactionLevel = 1; // because the transactionLevel was decremented earlier
                }
            }
            
            if(abortCause == null) {
                
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
                    transactionalResource.endTransaction();
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
            
            
            endRequestOnRequestScopeServices();

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
            transactionalResource.abortTransaction();
        }
    }

    public void addCommand(final PersistenceCommand command) {
        getTransaction().addCommand(command);
    }

    

    // //////////////////////////////////////////////////////////////
    // Hooks
    // //////////////////////////////////////////////////////////////


    /**
     * Overridable hook, used in
     * {@link #createTransaction(org.apache.isis.core.commons.authentication.MessageBroker, org.apache.isis.core.runtime.persistence.objectstore.transaction.TransactionalResource)}
     * 
     * <p> Called when a new {@link IsisTransaction} is created.
     */
    protected MessageBroker createMessageBroker() {
        return MessageBroker.acquire(getAuthenticationSession());
    }

    // ////////////////////////////////////////////////////////////////
    // helpers
    // ////////////////////////////////////////////////////////////////

    protected void ensureTransactionInProgress() {
        ensureThatState(getTransaction() != null && !getTransaction().getState().isComplete(), is(true), "No transaction in progress");
    }

    protected void ensureTransactionNotInProgress() {
        ensureThatState(getTransaction() != null && !getTransaction().getState().isComplete(), is(false), "Transaction in progress");
    }


    // //////////////////////////////////////////////////////
    // debugging
    // //////////////////////////////////////////////////////

    public void debugData(final DebugBuilder debug) {
        debug.appendln("Transaction", getTransaction());
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
    
    protected AdapterManager getAdapterManager() {
        return IsisContext.getPersistenceSession().getAdapterManager();
    }

    protected OidMarshaller getOidMarshaller() {
        return IsisContext.getOidMarshaller();
    }

}
