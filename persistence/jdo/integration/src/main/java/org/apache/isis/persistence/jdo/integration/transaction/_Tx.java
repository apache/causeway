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

package org.apache.isis.persistence.jdo.integration.transaction;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import javax.enterprise.inject.Vetoed;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.TransactionScopeListener;
import org.apache.isis.applib.services.TransactionScopeListener.PreCommitPhase;
import org.apache.isis.applib.services.xactn.Transaction;
import org.apache.isis.applib.services.xactn.TransactionId;
import org.apache.isis.applib.services.xactn.TransactionState;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.interaction.session.InteractionTracker;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.transaction.integration.IsisTransactionFlushException;
import org.apache.isis.core.transaction.integration.IsisTransactionManagerException;
import org.apache.isis.persistence.jdo.integration.persistence.JdoPersistenceSession;
import org.apache.isis.persistence.jdo.provider.persistence.HasPersistenceManager;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Vetoed @Log4j2 @ToString
class _Tx implements Transaction {

    public enum State {
        /**
         * Started, still in progress.
         *
         * <p>
         * May {@link IsisTransaction#flush() flush},
         * {@link IsisTransaction#commit() commit} or
         * {@link IsisTransaction#markAsAborted() abort}.
         */
        IN_PROGRESS(TransactionState.IN_PROGRESS),
        /**
         * Started, but has hit an exception.
         *
         * <p>
         * May not {@link IsisTransaction#flush()} or
         * {@link IsisTransaction#commit() commit} (will throw an
         * {@link IllegalStateException}), but can only
         * {@link IsisTransaction#markAsAborted() abort}.
         *
         * <p>
         * Similar to <tt>setRollbackOnly</tt> in EJBs.
         */
        MUST_ABORT(TransactionState.MUST_ABORT),
        /**
         * Completed, having successfully committed.
         *
         * <p>
         * May not {@link IsisTransaction#flush()} or
         * {@link IsisTransaction#markAsAborted() abort}.
         * {@link IsisTransaction#commit() commit} (will throw
         * {@link IllegalStateException}).
         */
        COMMITTED(TransactionState.COMMITTED),
        /**
         * Completed, having aborted.
         *
         * <p>
         * May not {@link IsisTransaction#flush()},
         * {@link IsisTransaction#commit() commit} or
         * {@link IsisTransaction#markAsAborted() abort} (will throw
         * {@link IllegalStateException}).
         */
        ABORTED(TransactionState.ABORTED);

        public final TransactionState transactionState;

        State(TransactionState transactionState){
            this.transactionState = transactionState;
        }


        /**
         * Whether it is valid to {@link _Tx#commit() commit} this
         * {@link _Tx transaction}.
         */
        public boolean canCommit() {
            return this == IN_PROGRESS;
        }

        /**
         * Whether it is valid to {@link _Tx#markAsAborted() abort} this
         * {@link _Tx transaction}.
         */
        public boolean canAbort() {
            return this == IN_PROGRESS || this == MUST_ABORT;
        }

        /**
         * Whether the {@link _Tx transaction} is complete (and so a
         * new one can be started).
         */
        public boolean isComplete() {
            return this == COMMITTED || this == ABORTED;
        }

        public boolean mustAbort() {
            return this == MUST_ABORT;
        }

        public TransactionState getTransactionState() {
            return transactionState;
        }
    }

    // -- constructor, fields

    @Getter @Programmatic
    private final TransactionId id;

    @ToString.Exclude
    private final HasPersistenceManager pmProvider;
    
    @ToString.Exclude
    private final InteractionTracker isisInteractionTracker;

    @ToString.Exclude
    private final Can<TransactionScopeListener> transactionScopeListeners;

    private IsisException abortCause;

    public _Tx(
            final MetaModelContext mmc,
            final HasPersistenceManager pmProvider,
            final UUID interactionId,
            final int sequence) {

        id = TransactionId.of(interactionId, sequence);
        
        this.pmProvider = pmProvider;
        this.isisInteractionTracker = mmc.getServiceRegistry().lookupServiceElseFail(InteractionTracker.class);
        this.transactionScopeListeners = mmc.getServiceRegistry().select(TransactionScopeListener.class);

        this.state = State.IN_PROGRESS;

        log.debug("new transaction {}", this);
        
        for (TransactionScopeListener listener : transactionScopeListeners) {
            listener.onTransactionStarted();
        }
        
    }

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    // -- state

    @Getter
    private State state;
    private void setState(final @NonNull State state) {
        if(this.state == state) {
            return;
        }
        this.state = state;
        if(state.isComplete()) {
            countDownLatch.countDown();
        }
    }

    @Override
    public TransactionState getTransactionState() {

        if (getState() == null) {
            return TransactionState.NONE;
        }

        val transactionState = getState().getTransactionState();
        return transactionState == null
                ? TransactionState.NONE
                : transactionState;
    }

    // -- flush

    @Override
    public final void flush() {

        // have removed THIS guard because we hit a situation where a xactn is aborted
        // from a no-arg action, the Wicket viewer attempts to render a new page that (of course)
        // contains the service menu items, and some of the 'disableXxx()' methods of those
        // service actions perform repository queries (while xactn is still in a state of ABORTED)
        //
        // ensureThatState(getState().canFlush(), is(true), "state is: " + getState());
        //
        log.debug("flush transaction {}", this);

        try {
            flushTransaction();
        } catch (final RuntimeException ex) {
            setAbortCause(new IsisTransactionFlushException(ex));
            throw ex;
        }
    }
    
    private void flushTransaction() {
        pmProvider.flushTransaction();
    }

    protected JdoPersistenceSession getPersistenceSession() {
        return isisInteractionTracker.currentInteractionSession()
                .map(interaction->interaction.getAttribute(JdoPersistenceSession.class))
                .orElseThrow(()->_Exceptions.unrecoverable("no current JdoPersistenceSession available"));
    }


    // -- preCommit, commit

    void preCommit() {
        _Assert.assertTrue(getState().canCommit());
        _Assert.assertTrue(abortCause == null);

        log.debug("preCommit transaction {}", this);

        if (getState() == State.COMMITTED) {
            log.info("already committed; ignoring");
            return;
        }

        try {
            
            flushTransaction();
            
            notifyPreCommit(PreCommitPhase.PRE_PUBLISHING);
            notifyPreCommit(PreCommitPhase.WHILE_PUBLISHING);

        } catch (RuntimeException ex) {
            setAbortCause(new IsisTransactionManagerException(ex));
            throw ex;
        } finally {
            notifyPreCommit(PreCommitPhase.POST_PUBLISHING);
        }
    }

    private void notifyPreCommit(PreCommitPhase preCommitPhase) {
        for (val listener : transactionScopeListeners) {
            listener.onPreCommit(preCommitPhase);
        }
    }

    void commit() {
        assert getState().canCommit();
        assert abortCause == null;

        log.debug("postCommit transaction {}", this);

        if (getState() == State.COMMITTED) {
            log.info("already committed; ignoring");
            return;
        }

        setState(State.COMMITTED);
        
    }

    // -- abortCause, markAsAborted

    /**
     * internal API called by IsisTransactionManager only
     */
    final void markAsAborted() {
        assert getState().canAbort();

        log.info("abort transaction {}", this);
        setState(State.ABORTED);
    }


    /**
     * Indicate that the transaction must be aborted, and that there is
     * an unhandled exception to be rendered somehow.
     *
     * <p>
     * If the cause is subsequently rendered by code higher up the stack, then the
     * cause can be {@link #clearAbortCause() cleared}.  Note that this keeps the transaction in a state of
     * {@link State#MUST_ABORT}.
     *
     * <p>
     * If the cause is to be discarded completely (eg background command execution), then
     * {@link #clearAbortCauseAndContinue()} can be used.
     */
    public void setAbortCause(IsisException abortCause) {
        setState(State.MUST_ABORT);
        this.abortCause = abortCause;
    }

    public IsisException getAbortCause() {
        return abortCause;
    }

    /**
     * If the cause has been rendered higher up in the stack, then clear the cause so that
     * it won't be picked up and rendered elsewhere.
     *
     * <p>
     *     for framework internal use only.
     * </p>
     *
     */
    public void clearAbortCause() {
        abortCause = null;
    }

    public void clearAbortCauseAndContinue() {
        setState(State.IN_PROGRESS);
        clearAbortCause();
    }
    

}


