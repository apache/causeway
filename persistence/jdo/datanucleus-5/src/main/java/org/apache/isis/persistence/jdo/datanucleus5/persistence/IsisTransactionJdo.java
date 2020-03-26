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

package org.apache.isis.persistence.jdo.datanucleus5.persistence;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import javax.enterprise.inject.Vetoed;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.TransactionScopeListener;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.xactn.Transaction;
import org.apache.isis.applib.services.xactn.TransactionId;
import org.apache.isis.applib.services.xactn.TransactionState;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.internal.collections._Inbox;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.commons.ToString;
import org.apache.isis.core.metamodel.services.publishing.PublisherDispatchService;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.iactn.IsisInteractionTracker;
import org.apache.isis.core.runtime.persistence.transaction.AuditerDispatchService;
import org.apache.isis.core.runtime.persistence.transaction.IsisTransactionFlushException;
import org.apache.isis.core.runtime.persistence.transaction.IsisTransactionManagerException;
import org.apache.isis.persistence.jdo.datanucleus5.persistence.command.CreateObjectCommand;
import org.apache.isis.persistence.jdo.datanucleus5.persistence.command.DestroyObjectCommand;
import org.apache.isis.persistence.jdo.datanucleus5.persistence.command.PersistenceCommand;

import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Used by the {@link IsisTransactionManagerJdo} to captures a set of changes to be
 * applied.
 *
 * <p>
 * Note that methods such as <tt>flush()</tt>, <tt>commit()</tt> and
 * <tt>abort()</tt> are not part of the API. The place to control transactions
 * is through the {@link IsisTransactionManagerJdo transaction manager}, because
 * some implementations may support nesting and such like. It is also the job of
 * the {@link IsisTransactionManagerJdo} to ensure that the underlying persistence
 * mechanism (for example, the <tt>ObjectStore</tt>) is also committed.
 */
@Vetoed @Log4j2
public class IsisTransactionJdo implements Transaction {

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
         * Whether it is valid to {@link IsisTransactionJdo#commit() commit} this
         * {@link IsisTransactionJdo transaction}.
         */
        public boolean canCommit() {
            return this == IN_PROGRESS;
        }

        /**
         * Whether it is valid to {@link IsisTransactionJdo#markAsAborted() abort} this
         * {@link IsisTransactionJdo transaction}.
         */
        public boolean canAbort() {
            return this == IN_PROGRESS || this == MUST_ABORT;
        }

        /**
         * Whether the {@link IsisTransactionJdo transaction} is complete (and so a
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

    private final _Inbox<PersistenceCommand> persistenceCommands = new _Inbox<>();

    private final PublisherDispatchService publisherDispatchService;
    private final AuditerDispatchService auditerDispatchService;
    private final IsisInteractionTracker isisInteractionTracker;

    private final Can<TransactionScopeListener> transactionScopeListeners;

    private IsisException abortCause;

    public IsisTransactionJdo(
            final ServiceRegistry serviceRegistry,
            final UUID interactionId,
            final int sequence) {

        id = TransactionId.of(interactionId, sequence);
        
        this.publisherDispatchService = serviceRegistry.lookupServiceElseFail(PublisherDispatchService.class);
        this.auditerDispatchService = serviceRegistry.lookupServiceElseFail(AuditerDispatchService.class);
        this.isisInteractionTracker = serviceRegistry.lookupServiceElseFail(IsisInteractionTracker.class);
        
        this.transactionScopeListeners = serviceRegistry.select(TransactionScopeListener.class);

        this.state = State.IN_PROGRESS;

        log.debug("new transaction {}", this);
        
        for (TransactionScopeListener listener : transactionScopeListeners) {
            listener.onTransactionStarted();;
        }
        
    }

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    // -- state

    @Getter
    private State state;
    private void setState(final State state) {
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

        final TransactionState transactionState = getState().getTransactionState();
        if (transactionState == null) {
            return TransactionState.NONE;
        }

        return transactionState;
    }

    // -- commands

    /**
     * Add the non-null command to the list of commands to execute at the end of
     * the transaction.
     */
    public void addCommand(final PersistenceCommand command) {
        if (command == null) {
            return;
        }

        final ManagedObject onObject = command.onManagedObject();

        // Destroys are ignored when preceded by a create, or another destroy
        if (command instanceof DestroyObjectCommand) {
            if (alreadyHasCreate(onObject)) {
                removeCreate(onObject);
                if (log.isDebugEnabled()) {
                    log.debug("ignored both create and destroy command {}", command);
                }
                return;
            }

            if (alreadyHasDestroy(onObject)) {
                if (log.isDebugEnabled()) {
                    log.debug("ignored command {} as command already recorded", command);
                }
                return;
            }
        }

        log.debug("add command {}", command);
        persistenceCommands.add(command);
    }

    private boolean alreadyHasCommand(final Class<?> commandClass, final ManagedObject onObject) {
        return getCommand(commandClass, onObject) != null;
    }

    private boolean alreadyHasCreate(final ManagedObject onObject) {
        return alreadyHasCommand(CreateObjectCommand.class, onObject);
    }

    private boolean alreadyHasDestroy(final ManagedObject onObject) {
        return alreadyHasCommand(DestroyObjectCommand.class, onObject);
    }

    private PersistenceCommand getCommand(final Class<?> commandClass, final ManagedObject onObject) {
        for (final PersistenceCommand command : persistenceCommands.snapshot()) {
            if (command.onManagedObject().equals(onObject)) {
                if (commandClass.isAssignableFrom(command.getClass())) {
                    return command;
                }
            }
        }
        return null;
    }

    private void removeCommand(final Class<?> commandClass, final ManagedObject onObject) {
        final PersistenceCommand toDelete = getCommand(commandClass, onObject);
        persistenceCommands.remove(toDelete);
    }

    private void removeCreate(final ManagedObject onObject) {
        removeCommand(CreateObjectCommand.class, onObject);
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
            doFlush();
        } catch (final RuntimeException ex) {
            setAbortCause(new IsisTransactionFlushException(ex));
            throw ex;
        }
    }

    /**
     * <p>
     * Called by both {@link #commit()} and by {@link #flush()}:
     * <table>
     * <tr>
     * <th>called from</th>
     * <th>next {@link #getState() state} if ok</th>
     * <th>next {@link #getState() state} if exception</th>
     * </tr>
     * <tr>
     * <td>{@link #commit()}</td>
     * <td>{@link State#COMMITTED}</td>
     * <td>{@link State#ABORTED}</td>
     * </tr>
     * <tr>
     * <td>{@link #flush()}</td>
     * <td>{@link State#IN_PROGRESS}</td>
     * <td>{@link State#MUST_ABORT}</td>
     * </tr>
     * </table>
     */
    private void doFlush() {

        //
        // it's possible that in executing these commands that more will be created.
        // so we keep flushing until no more are available (ISIS-533)
        //
        // this is a do...while rather than a while... just for backward compatibilty
        // with previous algorithm that always went through the execute phase at least once.
        //
        do {
            // this algorithm ensures that we never execute the same command twice,
            // and also allow new commands to be added to end
            val pc_snapshot = persistenceCommands.snapshotThenClear();

            if(!pc_snapshot.isEmpty()) {
                try {
                    
                    getPersistenceSession().execute(pc_snapshot);
                    
                } catch (final RuntimeException ex) {
                    // if there's an exception, we want to make sure that
                    // all commands are cleared and propagate
                    persistenceCommands.clear();
                    throw ex;
                }
            }
        } while(!persistenceCommands.isEmpty());

    }

    protected IsisPersistenceSessionJdo getPersistenceSession() {
        return isisInteractionTracker.currentInteraction()
                .map(interaction->interaction.getUserData(IsisPersistenceSessionJdo.class))
                .orElseThrow(()->_Exceptions.unrecoverable("no current IsisPersistenceSessionJdo available"));
    }


    // -- preCommit, commit

    void preCommit() {
        assert getState().canCommit();
        assert abortCause == null;

        log.debug("preCommit transaction {}", this);

        if (getState() == State.COMMITTED) {
            log.info("already committed; ignoring");
            return;
        }

        try {
            
            auditerDispatchService.audit();
            publisherDispatchService.publishObjects();
            
            doFlush();

        } catch (final RuntimeException ex) {
            setAbortCause(new IsisTransactionManagerException(ex));
            throw ex;
        } finally {
            for (TransactionScopeListener listener : transactionScopeListeners) {
                listener.onTransactionEnded();
            }
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

    // -- toString

    @Override
    public String toString() {
        return appendTo(new ToString(this)).toString();
    }

    private ToString appendTo(final ToString str) {
        str.append("state", state);
        str.append("commands", persistenceCommands.size());
        return str;
    }
    

}


