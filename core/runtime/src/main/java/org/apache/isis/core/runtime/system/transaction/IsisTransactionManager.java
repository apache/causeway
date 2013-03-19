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

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;

import java.util.List;

import org.apache.log4j.Logger;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.PublishedAction;
import org.apache.isis.applib.annotation.PublishedObject;
import org.apache.isis.applib.services.audit.AuditingService;
import org.apache.isis.applib.services.publish.EventPayload;
import org.apache.isis.applib.services.publish.EventPayloadForActionInvocation;
import org.apache.isis.applib.services.publish.EventPayloadForChangedObject;
import org.apache.isis.applib.services.publish.EventSerializer;
import org.apache.isis.applib.services.publish.ObjectStringifier;
import org.apache.isis.applib.services.publish.PublishingService;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.components.SessionScopedComponent;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.services.ServicesInjectorSpi;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PublishingServiceWithDefaultPayloadFactories;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.TransactionalResource;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSession;

public class IsisTransactionManager implements SessionScopedComponent {

    private static final Logger LOG = Logger.getLogger(IsisTransactionManager.class);

    private final PersistenceSession persistenceSession;

    private int transactionLevel;
    
    /**
     * Could be null.
     */
    private final AuditingService auditingService;
    /**
     * Could be null.
     */
    private final PublishingServiceWithDefaultPayloadFactories publishingService;

    private IsisSession session;

    /**
     * Holds the current or most recently completed transaction.
     */
    private IsisTransaction transaction;

    private final TransactionalResource transactionalResource;


    // ////////////////////////////////////////////////////////////////
    // constructor
    // ////////////////////////////////////////////////////////////////

    public IsisTransactionManager(final PersistenceSession persistenceSession, final TransactionalResource transactionalResource, final ServicesInjectorSpi servicesInjectorSpi) {
        this.persistenceSession = persistenceSession;
        this.transactionalResource = transactionalResource;
        
        this.auditingService = (AuditingService) servicesInjectorSpi.lookupService(AuditingService.class);
        this.publishingService = getPublishingServiceIfAny(servicesInjectorSpi);
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
     * Convenience method returning the {@link UpdateNotifier} of the
     * {@link #getTransaction() current transaction}.
     */
    protected UpdateNotifier getUpdateNotifier() {
        return getTransaction().getUpdateNotifier();
    }

    /**
     * Convenience method returning the {@link MessageBroker} of the
     * {@link #getTransaction() current transaction}.
     */
    protected org.apache.isis.core.commons.authentication.MessageBroker getMessageBroker() {
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
     * end. If the closure throws an exception, then will
     * {@link #abortTransaction() abort} the transaction.
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
                // temp TODO fix swallowing of exception
                // System.out.println(ex.getMessage());
                // ex.printStackTrace();
                try {
                    abortTransaction();
                } catch (final Exception e) {
                    LOG.error("Abort failure after exception", e);
                    // System.out.println(e.getMessage());
                    // e.printStackTrace();
                    throw new IsisTransactionManagerException("Abort failure: " + e.getMessage(), ex);
                }
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
     * end. If the closure throws an exception, then will
     * {@link #abortTransaction() abort} the transaction.
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
        org.apache.isis.core.commons.authentication.MessageBroker messageBroker = createMessageBroker();
        UpdateNotifier updateNotifier = createUpdateNotifier();
        return this.transaction = createTransaction(messageBroker, updateNotifier, transactionalResource);
    }


    /**
     * The provided {@link MessageBroker} and {@link UpdateNotifier} are
     * obtained from the hook methods ( {@link #createMessageBroker()} and
     * {@link #createUpdateNotifier()}).
     * @param transactionalResource 
     * 
     * @see #createMessageBroker()
     * @see #createUpdateNotifier()
     */
    private IsisTransaction createTransaction(final org.apache.isis.core.commons.authentication.MessageBroker messageBroker, final UpdateNotifier updateNotifier, TransactionalResource transactionalResource) {
        ensureThatArg(messageBroker, is(not(nullValue())));
        ensureThatArg(updateNotifier, is(not(nullValue())));

        return new IsisTransaction(this, messageBroker, updateNotifier, transactionalResource, auditingService, publishingService);
    }
    

    // //////////////////////////////////////////////////////
    // start, flush, abort, end
    // //////////////////////////////////////////////////////

    public synchronized void startTransaction() {

        boolean noneInProgress = false;
        if (getTransaction() == null || getTransaction().getState().isComplete()) {
            noneInProgress = true;

            createTransaction();
            transactionLevel = 0;
            transactionalResource.startTransaction();
        }

        transactionLevel++;

        if (LOG.isDebugEnabled()) {
            LOG.debug("startTransaction: level " + (transactionLevel - 1) + "->" + (transactionLevel) + (noneInProgress ? " (no transaction in progress or was previously completed; transaction created)" : ""));
        }
    }

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
            abortTransaction();
            
            // just in case any different exception was raised...
            abortCause = this.getTransaction().getAbortCause();
            
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
                }
            }
            
            if(abortCause == null) {
                
                try {
                    getTransaction().commit();
                } catch(RuntimeException ex) {
                    // just in case any new exception was raised...
                    abortCause = ex;
                }
            }
            
            if(abortCause == null) {
                try {
                    transactionalResource.endTransaction();
                } catch(RuntimeException ex) {
                    // just in case any new exception was raised...
                    abortCause = ex;
                }
            }
            
            if(abortCause != null) {
                
                if (LOG.isDebugEnabled()) {
                    LOG.debug("endTransaction: aborting instead, abort cause has been set");
                }
                try {
                    abortTransaction();
                } catch(RuntimeException ex) {
                    // just in case any new exception was raised...
                    abortCause = ex;
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

    
    // ///////////////////////////////////////////
    // Publishing service
    // ///////////////////////////////////////////

    public PublishingServiceWithDefaultPayloadFactories getPublishingServiceIfAny(ServicesInjectorSpi servicesInjectorSpi) {
        final PublishingService publishingService = servicesInjectorSpi.lookupService(PublishingService.class);
        if(publishingService == null) {
            return null;
        }

        EventSerializer eventSerializer = servicesInjectorSpi.lookupService(EventSerializer.class);
        if(eventSerializer == null) {
            eventSerializer = newSimpleEventSerializer();
        }

        PublishedObject.PayloadFactory objectPayloadFactory = servicesInjectorSpi.lookupService(PublishedObject.PayloadFactory.class);
        if(objectPayloadFactory == null) {
            objectPayloadFactory = newDefaultObjectPayloadFactory();
        }
        
        PublishedAction.PayloadFactory actionPayloadFactory = servicesInjectorSpi.lookupService(PublishedAction.PayloadFactory.class);
        if(actionPayloadFactory == null) {
            actionPayloadFactory = newDefaultActionPayloadFactory();
        }
        
        return new PublishingServiceWithDefaultPayloadFactories(publishingService, objectPayloadFactory, actionPayloadFactory);
    }

    protected EventSerializer newSimpleEventSerializer() {
        return new EventSerializer.Simple();
    }


    protected PublishedObject.PayloadFactory newDefaultObjectPayloadFactory() {
        return new PublishedObject.PayloadFactory() {
            @Override
            public EventPayload payloadFor(final Object changedObject) {
                return new EventPayloadForChangedObject(changedObject)
                            .with(objectStringifier());
            }

        };
    }

    protected PublishedAction.PayloadFactory newDefaultActionPayloadFactory() {
        return new PublishedAction.PayloadFactory(){
            @Override
            public EventPayload payloadFor(Identifier actionIdentifier, Object target, List<Object> arguments, Object result) {
                return new EventPayloadForActionInvocation(
                        actionIdentifier, 
                        target, 
                        arguments, 
                        result).with(objectStringifier());
            }
        };
    }

    protected ObjectStringifier objectStringifier() {
        return new ObjectStringifier() {
            @Override
            public String toString(Object object) {
                if(object == null) {
                    return null;
                }
                final ObjectAdapter adapter = getAdapterManager().adapterFor(object);
                Oid oid = adapter.getOid();
                return oid != null? oid.enString(IsisContext.getOidMarshaller()): encodedValueOf(adapter);
            }
            private String encodedValueOf(ObjectAdapter adapter) {
                EncodableFacet facet = adapter.getSpecification().getFacet(EncodableFacet.class);
                return facet != null? facet.toEncodedString(adapter): adapter.toString();
            }
        };
    }



    // //////////////////////////////////////////////////////////////
    // Hooks
    // //////////////////////////////////////////////////////////////


    /**
     * Overridable hook, used in
     * {@link #createTransaction(MessageBroker, UpdateNotifier)
     * 
     * <p> Called when a new {@link IsisTransaction} is created.
     */
    protected org.apache.isis.core.commons.authentication.MessageBroker createMessageBroker() {
        return MessageBrokerDefault.acquire(getAuthenticationSession());
    }

    /**
     * Overridable hook, used in
     * {@link #createTransaction(MessageBroker, UpdateNotifier)
     * 
     * <p> Called when a new {@link IsisTransaction} is created.
     */
    protected UpdateNotifier createUpdateNotifier() {
        return new UpdateNotifierDefault();
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

}
