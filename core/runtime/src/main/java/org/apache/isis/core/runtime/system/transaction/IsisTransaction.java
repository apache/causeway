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

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.Bulk;
import org.apache.isis.applib.services.actinvoc.ActionInvocationContext;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.Command2;
import org.apache.isis.applib.services.command.Command3;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.command.spi.CommandService;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactn.InteractionContext;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.MessageBroker;
import org.apache.isis.core.commons.components.TransactionScopedComponent;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.services.publishing.PublishingServiceInternal;
import org.apache.isis.core.metamodel.transactions.TransactionState;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.core.runtime.services.auditing.AuditingServiceInternal;
import org.apache.isis.core.runtime.services.changes.ChangedObjectsServiceInternal;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Used by the {@link IsisTransactionManager} to captures a set of changes to be
 * applied.
 * 
 * <p>
 * Note that methods such as <tt>flush()</tt>, <tt>commit()</tt> and
 * <tt>abort()</tt> are not part of the API. The place to control transactions
 * is through the {@link IsisTransactionManager transaction manager}, because
 * some implementations may support nesting and such like. It is also the job of
 * the {@link IsisTransactionManager} to ensure that the underlying persistence
 * mechanism (for example, the <tt>ObjectStore</tt>) is also committed.
 */
public class IsisTransaction implements TransactionScopedComponent {

    public static class Placeholder {
        public static Placeholder NEW = new Placeholder("[NEW]");
        public static Placeholder DELETED = new Placeholder("[DELETED]");
        private final String str;
        public Placeholder(String str) {
            this.str = str;
        }
        @Override
        public String toString() {
            return str;
        }
    }

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
         * Whether it is valid to {@link IsisTransaction#flush() flush} this
         * {@link IsisTransaction transaction}.
         */
        public boolean canFlush() {
            return this == IN_PROGRESS;
        }

        /**
         * Whether it is valid to {@link IsisTransaction#commit() commit} this
         * {@link IsisTransaction transaction}.
         */
        public boolean canCommit() {
            return this == IN_PROGRESS;
        }

        /**
         * Whether it is valid to {@link IsisTransaction#markAsAborted() abort} this
         * {@link IsisTransaction transaction}.
         */
        public boolean canAbort() {
            return this == IN_PROGRESS || this == MUST_ABORT;
        }

        /**
         * Whether the {@link IsisTransaction transaction} is complete (and so a
         * new one can be started).
         */
        public boolean isComplete() {
            return this == COMMITTED || this == ABORTED;
        }

        public boolean mustAbort() {
            return this == MUST_ABORT;
        }

        public TransactionState getRuntimeContextState() {
            return transactionState;
        }
    }


    private static final Logger LOG = LoggerFactory.getLogger(IsisTransaction.class);

    private final List<PersistenceCommand> persistenceCommands = Lists.newArrayList();
    private final IsisTransactionManager transactionManager;
    private final MessageBroker messageBroker;

    private final ServicesInjector servicesInjector;

    private final CommandContext commandContext;
    private final CommandService commandService;

    private final InteractionContext interactionContext;
    private final PublishingServiceInternal publishingServiceInternal;
    private final AuditingServiceInternal auditingServiceInternal;
    private final ChangedObjectsServiceInternal changedObjectsServiceInternal;


    private final UUID transactionId;
        
    private State state;
    private IsisException abortCause;




    public IsisTransaction(
            final IsisTransactionManager transactionManager,
            final MessageBroker messageBroker,
            final ServicesInjector servicesInjector,
            final UUID transactionId) {
        
        ensureThatArg(transactionManager, is(not(nullValue())), "transaction manager is required");
        ensureThatArg(messageBroker, is(not(nullValue())), "message broker is required");
        ensureThatArg(servicesInjector, is(not(nullValue())), "services injector is required");

        this.transactionManager = transactionManager;
        this.messageBroker = messageBroker;
        this.servicesInjector = servicesInjector;
        
        this.commandContext = lookupService(CommandContext.class);
        this.commandService = lookupService(CommandService.class);

        this.interactionContext = lookupService(InteractionContext.class);

        this.publishingServiceInternal = lookupService(PublishingServiceInternal.class);
        this.auditingServiceInternal = lookupService(AuditingServiceInternal.class);
        this.changedObjectsServiceInternal = lookupService(ChangedObjectsServiceInternal.class);


        this.transactionId = transactionId;

        this.state = State.IN_PROGRESS;

        if (LOG.isDebugEnabled()) {
            LOG.debug("new transaction " + this);
        }
    }


    // ////////////////////////////////////////////////////////////////
    // GUID
    // ////////////////////////////////////////////////////////////////

    public final UUID getTransactionId() {
        return transactionId;
    }
    
    
    // ////////////////////////////////////////////////////////////////
    // State
    // ////////////////////////////////////////////////////////////////

    public State getState() {
        return state;
    }

    private void setState(final State state) {
        this.state = state;
    }

    
    // //////////////////////////////////////////////////////////
    // Commands
    // //////////////////////////////////////////////////////////

    /**
     * Add the non-null command to the list of commands to execute at the end of
     * the transaction.
     */
    public void addCommand(final PersistenceCommand command) {
        if (command == null) {
            return;
        }

        final ObjectAdapter onObject = command.onAdapter();

        // Destroys are ignored when preceded by a create, or another destroy
        if (command instanceof DestroyObjectCommand) {
            if (alreadyHasCreate(onObject)) {
                removeCreate(onObject);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("ignored both create and destroy command " + command);
                }
                return;
            }

            if (alreadyHasDestroy(onObject)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("ignored command " + command + " as command already recorded");
                }
                return;
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("add command " + command);
        }
        persistenceCommands.add(command);
    }



    // ////////////////////////////////////////////////////////////////
    // flush
    // ////////////////////////////////////////////////////////////////

    public synchronized final void flush() {

        // have removed THIS guard because we hit a situation where a xactn is aborted
        // from a no-arg action, the Wicket viewer attempts to render a new page that (of course)
        // contains the service menu items, and some of the 'disableXxx()' methods of those
        // service actions perform repository queries (while xactn is still in a state of ABORTED)
        //
        // ensureThatState(getState().canFlush(), is(true), "state is: " + getState());
        //
        if (LOG.isDebugEnabled()) {
            LOG.debug("flush transaction " + this);
        }

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
            final List<PersistenceCommand> persistenceCommandList = Lists.newArrayList(persistenceCommands);

            if(!persistenceCommandList.isEmpty()) {
                // so won't be processed again if a flush is encountered subsequently
                persistenceCommands.removeAll(persistenceCommandList);
                try {
                    this.transactionManager.getPersistenceSession().execute(persistenceCommandList);
                    for (PersistenceCommand persistenceCommand : persistenceCommandList) {
                        if (persistenceCommand instanceof DestroyObjectCommand) {
                            final ObjectAdapter adapter = persistenceCommand.onAdapter();
                            adapter.setVersion(null);
                        }
                    }
                } catch (final RuntimeException ex) {
                    // if there's an exception, we want to make sure that
                    // all commands are cleared and propagate
                    persistenceCommands.clear();
                    throw ex;
                }
            }
        } while(!persistenceCommands.isEmpty());
        
    }


    // ////////////////////////////////////////////////////////////////
    // preCommit, commit
    // ////////////////////////////////////////////////////////////////

    synchronized void preCommit() {
        ensureThatState(getState().canCommit(), is(true), "state is: " + getState());
        ensureThatState(abortCause, is(nullValue()), "cannot commit: an abort cause has been set");

        if (LOG.isDebugEnabled()) {
            LOG.debug("preCommit transaction " + this);
        }

        if (getState() == State.COMMITTED) {
            if (LOG.isInfoEnabled()) {
                LOG.info("already committed; ignoring");
            }
            return;
        }

        try {
            // ensureCommandsPersistedIfDirtyXactn
            final Command command = commandContext.getCommand();

            // ensure that any changed objects means that the command should be persisted
            final boolean hasChangedAdapters = changedObjectsServiceInternal.hasChangedAdapters();
            if(hasChangedAdapters && command.getMemberIdentifier() != null) {
                command.setPersistHint(true);
            }

            auditingServiceInternal.audit();

            publishingServiceInternal.publishObjects();
            doFlush();

            final ActionInvocationContext actionInvocationContext = lookupService(ActionInvocationContext.class);
            if(actionInvocationContext != null) {
                Bulk.InteractionContext.current.set(null);
            }

            completeCommandAndInteractionAndClearDomainEvents();
            doFlush();


        } catch (final RuntimeException ex) {
            setAbortCause(new IsisTransactionManagerException(ex));
            completeCommandAndInteractionAndClearDomainEvents();
            throw ex;
        } finally {
            changedObjectsServiceInternal.clearChangedObjectProperties();
        }
    }

    private void completeCommandAndInteractionAndClearDomainEvents() {

        final Command command = commandContext.getCommand();
        final Interaction interaction = interactionContext.getInteraction();

        if(command.getStartedAt() != null && command.getCompletedAt() == null) {
            // the guard is in case we're here as the result of a redirect following a previous exception;just ignore.

            // copy over from the most recent interaction
            Interaction.Execution priorExecution = interaction.getPriorExecution();
            final Timestamp completedAt = priorExecution.getCompletedAt();
            command.setCompletedAt(completedAt);
        }

        commandService.complete(command);

        if(command instanceof Command3) {
            final Command3 command3 = (Command3) command;
            command3.flushActionDomainEvents();
        } else
        if(command instanceof Command2) {
            final Command2 command2 = (Command2) command;
            command2.flushActionInteractionEvents();
        }

        interaction.clear();
    }


    // ////////////////////////////////////////////////////////////////

    public synchronized void commit() {
        ensureThatState(getState().canCommit(), is(true), "state is: " + getState());
        ensureThatState(abortCause, is(nullValue()), "cannot commit: an abort cause has been set");

        if (LOG.isDebugEnabled()) {
            LOG.debug("postCommit transaction " + this);
        }

        if (getState() == State.COMMITTED) {
            if (LOG.isInfoEnabled()) {
                LOG.info("already committed; ignoring");
            }
            return;
        }

        setState(State.COMMITTED);
    }


    
    // ////////////////////////////////////////////////////////////////
    // markAsAborted
    // ////////////////////////////////////////////////////////////////

    public synchronized final void markAsAborted() {
        ensureThatState(getState().canAbort(), is(true), "state is: " + getState());
        if (LOG.isInfoEnabled()) {
            LOG.info("abort transaction " + this);
        }

        setState(State.ABORTED);
    }

    
    
    /////////////////////////////////////////////////////////////////////////
    // handle exceptions on load, flush or commit
    /////////////////////////////////////////////////////////////////////////


    /**
     * Indicate that the transaction must be aborted, and that there is
     * an unhandled exception to be rendered somehow.
     * 
     * <p>
     * If the cause is subsequently rendered by code higher up the stack, then the
     * cause can be {@link #clearAbortCause() cleared}.  However, it is not possible
     * to change the state from {@link State#MUST_ABORT}.
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
     */
    public void clearAbortCause() {
        abortCause = null;
    }

    
    // //////////////////////////////////////////////////////////
    // Helpers
    // //////////////////////////////////////////////////////////

    private boolean alreadyHasCommand(final Class<?> commandClass, final ObjectAdapter onObject) {
        return getCommand(commandClass, onObject) != null;
    }

    private boolean alreadyHasCreate(final ObjectAdapter onObject) {
        return alreadyHasCommand(CreateObjectCommand.class, onObject);
    }

    private boolean alreadyHasDestroy(final ObjectAdapter onObject) {
        return alreadyHasCommand(DestroyObjectCommand.class, onObject);
    }

    private PersistenceCommand getCommand(final Class<?> commandClass, final ObjectAdapter onObject) {
        for (final PersistenceCommand command : persistenceCommands) {
            if (command.onAdapter().equals(onObject)) {
                if (commandClass.isAssignableFrom(command.getClass())) {
                    return command;
                }
            }
        }
        return null;
    }

    private void removeCommand(final Class<?> commandClass, final ObjectAdapter onObject) {
        final PersistenceCommand toDelete = getCommand(commandClass, onObject);
        persistenceCommands.remove(toDelete);
    }

    private void removeCreate(final ObjectAdapter onObject) {
        removeCommand(CreateObjectCommand.class, onObject);
    }

    // ////////////////////////////////////////////////////////////////
    // toString
    // ////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return appendTo(new ToString(this)).toString();
    }

    protected ToString appendTo(final ToString str) {
        str.append("state", state);
        str.append("commands", persistenceCommands.size());
        return str;
    }


    // ////////////////////////////////////////////////////////////////
    // Depenendencies (from constructor)
    // ////////////////////////////////////////////////////////////////


    /**
     * The {@link org.apache.isis.core.commons.authentication.MessageBroker} for this transaction.
     * 
     * <p>
     * Injected in constructor
     *
     * @deprecated - obtain the {@link org.apache.isis.core.commons.authentication.MessageBroker} instead from the {@link AuthenticationSession}.
     */
    public MessageBroker getMessageBroker() {
        return messageBroker;
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

    private <T> T lookupServiceIfAny(final Class<T> serviceType) {
        return servicesInjector.lookupService(serviceType);
    }
}
