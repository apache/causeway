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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.net.MediaType;

import org.apache.log4j.Logger;

import org.apache.isis.applib.annotation.PublishedAction;
import org.apache.isis.applib.annotation.PublishedObject;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.services.audit.AuditingService;
import org.apache.isis.applib.services.publish.EventMetadata;
import org.apache.isis.applib.services.publish.EventType;
import org.apache.isis.applib.services.publish.ObjectStringifier;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.components.TransactionScopedComponent;
import org.apache.isis.core.commons.ensure.Ensure;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.facets.actions.invoke.ActionInvocationFacet;
import org.apache.isis.core.metamodel.facets.actions.invoke.ActionInvocationFacet.CurrentInvocation;
import org.apache.isis.core.metamodel.facets.actions.publish.PublishedActionFacet;
import org.apache.isis.core.metamodel.facets.object.audit.AuditableFacet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.object.publish.PublishedObjectFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PublishingServiceWithDefaultPayloadFactories;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.SaveObjectCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.TransactionalResource;
import org.apache.isis.core.runtime.system.context.IsisContext;

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

    public static enum State {
        /**
         * Started, still in progress.
         * 
         * <p>
         * May {@link IsisTransaction#flush() flush},
         * {@link IsisTransaction#commit() commit} or
         * {@link IsisTransaction#abort() abort}.
         */
        IN_PROGRESS,
        /**
         * Started, but has hit an exception.
         * 
         * <p>
         * May not {@link IsisTransaction#flush()} or
         * {@link IsisTransaction#commit() commit} (will throw an
         * {@link IllegalStateException}), but can only
         * {@link IsisTransaction#abort() abort}.
         * 
         * <p>
         * Similar to <tt>setRollbackOnly</tt> in EJBs.
         */
        MUST_ABORT,
        /**
         * Completed, having successfully committed.
         * 
         * <p>
         * May not {@link IsisTransaction#flush()} or
         * {@link IsisTransaction#abort() abort} or
         * {@link IsisTransaction#commit() commit} (will throw
         * {@link IllegalStateException}).
         */
        COMMITTED,
        /**
         * Completed, having aborted.
         * 
         * <p>
         * May not {@link IsisTransaction#flush()},
         * {@link IsisTransaction#commit() commit} or
         * {@link IsisTransaction#abort() abort} (will throw
         * {@link IllegalStateException}).
         */
        ABORTED;

        private State(){}

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
    }


    private static final Logger LOG = Logger.getLogger(IsisTransaction.class);


    private final TransactionalResource objectStore;
    private final List<PersistenceCommand> commands = Lists.newArrayList();
    private final IsisTransactionManager transactionManager;
    private final org.apache.isis.core.commons.authentication.MessageBroker messageBroker;
    private final UpdateNotifier updateNotifier;
    private IsisException abortCause;

    /**
     * could be null if none has been registered
     */
    private final AuditingService auditingService;
    /**
     * could be null if none has been registered
     */
    private final PublishingServiceWithDefaultPayloadFactories publishingService;

    private State state;

    private final UUID guid;

    private int eventSequence;

    public IsisTransaction(final IsisTransactionManager transactionManager, final org.apache.isis.core.commons.authentication.MessageBroker messageBroker, final UpdateNotifier updateNotifier, final TransactionalResource objectStore, final AuditingService auditingService, PublishingServiceWithDefaultPayloadFactories publishingService) {
        
        ensureThatArg(transactionManager, is(not(nullValue())), "transaction manager is required");
        ensureThatArg(messageBroker, is(not(nullValue())), "message broker is required");
        ensureThatArg(updateNotifier, is(not(nullValue())), "update notifier is required");

        this.transactionManager = transactionManager;
        this.messageBroker = messageBroker;
        this.updateNotifier = updateNotifier;
        this.auditingService = auditingService;
        this.publishingService = publishingService;
        
        this.guid = UUID.randomUUID();
        this.eventSequence = 0;

        this.state = State.IN_PROGRESS;

        this.objectStore = objectStore;
        if (LOG.isDebugEnabled()) {
            LOG.debug("new transaction " + this);
        }
    }

    // ////////////////////////////////////////////////////////////////
    // GUID
    // ////////////////////////////////////////////////////////////////

    public final UUID getGuid() {
        return guid;
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

        // Saves are ignored when preceded by another save, or a delete
        if (command instanceof SaveObjectCommand) {
            if (alreadyHasCreate(onObject) || alreadyHasSave(onObject)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("ignored command as object already created/saved" + command);
                }
                return;
            }

            if (alreadyHasDestroy(onObject)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("ignored command " + command + " as object no longer exists");
                }
                return;
            }
        }

        // Destroys are ignored when preceded by a create, or another destroy
        if (command instanceof DestroyObjectCommand) {
            if (alreadyHasCreate(onObject)) {
                removeCreate(onObject);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("ignored both create and destroy command " + command);
                }
                return;
            }

            if (alreadyHasSave(onObject)) {
                removeSave(onObject);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("removed prior save command " + command);
                }
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
        commands.add(command);
    }



    // ////////////////////////////////////////////////////////////////
    // flush
    // ////////////////////////////////////////////////////////////////

    public synchronized final void flush() {
        // have removed the guard below because not every objectstore necessarily 
        // wraps up every change inside a command.
        
        // for example, the JDO object store just lets DataNucleus do the change tracking
        // itself
        
        ensureThatState(getState().canFlush(), is(true), "state is: " + getState());
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
     * Mandatory hook method for subclasses to persist all pending changes.
     * 
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
        
        try {
            
            objectStore.execute(Collections.unmodifiableList(commands));
            
            for (final PersistenceCommand command : commands) {
                if (command instanceof DestroyObjectCommand) {
                    final ObjectAdapter adapter = command.onAdapter();
                    adapter.setVersion(null);
                    adapter.changeState(ResolveState.DESTROYED);
                }
            }
        } finally {
            // even if there's an exception, we want to clear the commands
            // this is because the Wicket viewer uses an implementation of IsisContext 
            // whereby there are several threads which could be sharing the same context
            // if the first fails, we don't want the others to pick up the same command list
            // and try again
            commands.clear();
        }
    }

    
    protected void doAudit(final Set<Entry<AdapterAndProperty, PreAndPostValues>> changedObjectProperties) {
        if(auditingService == null) {
            return;
        }
        
        // else
        final String currentUser = getTransactionManager().getAuthenticationSession().getUserName();
        final long currentTimestampEpoch = currentTimestampEpoch();
        for (Entry<AdapterAndProperty, PreAndPostValues> auditEntry : changedObjectProperties) {
            auditChangedProperty(currentUser, currentTimestampEpoch, auditEntry);
        }
    }

    protected void doPublish(final Set<ObjectAdapter> changedAdapters) {
        if(publishingService == null) {
            return;
        }

        // else
        final String currentUser = getTransactionManager().getAuthenticationSession().getUserName();
        final long currentTimestampEpoch = currentTimestampEpoch();
        
        publishActionIfRequired(currentUser, currentTimestampEpoch);
        publishedChangedObjects(changedAdapters, currentUser, currentTimestampEpoch);
    }

    protected void publishActionIfRequired(final String currentUser, final long currentTimestampEpoch) {
        // TODO: need some transaction handling here
        
        try {
            final CurrentInvocation currentInvocation = ActionInvocationFacet.currentInvocation.get();
            if(currentInvocation == null) {
                return;
            } 
            final PublishedActionFacet publishedActionFacet = currentInvocation.getAction().getFacet(PublishedActionFacet.class);
            if(publishedActionFacet == null) {
                return;
            } 
            final PublishedAction.PayloadFactory payloadFactory = publishedActionFacet.value();
            
            final RootOid adapterOid = (RootOid) currentInvocation.getTarget().getOid();
            final String oidStr = getOidMarshaller().marshal(adapterOid);
            final String title = oidStr + ": " + currentInvocation.getAction().getIdentifier().toNameParmsIdentityString();
            
            final EventMetadata metadata = newEventMetadata(EventType.ACTION_INVOCATION, currentUser, currentTimestampEpoch, title);
            publishingService.publishAction(payloadFactory, metadata, currentInvocation, objectStringifier());
        } finally {
            ActionInvocationFacet.currentInvocation.set(null);
        }
    }

    protected void publishedChangedObjects(final Set<ObjectAdapter> changedAdapters, final String currentUser, final long currentTimestampEpoch) {
        for (final ObjectAdapter changedAdapter : changedAdapters) {
            final PublishedObjectFacet publishedObjectFacet = changedAdapter.getSpecification().getFacet(PublishedObjectFacet.class);
            if(publishedObjectFacet == null) {
                continue;
            }
            final PublishedObject.PayloadFactory payloadFactory = publishedObjectFacet.value();

            final RootOid adapterOid = (RootOid) changedAdapter.getOid();
            final String oidStr = getOidMarshaller().marshal(adapterOid);
            final String title = oidStr;

            final EventMetadata metadata = newEventMetadata(EventType.OBJECT_CHANGED, currentUser, currentTimestampEpoch, title);

            publishingService.publishObject(payloadFactory, metadata, changedAdapter, objectStringifier());
        }
    }

    protected ObjectStringifier objectStringifier() {
        if(objectStringifier == null) {
            // lazily created; is threadsafe so no need to guard against race conditions
            objectStringifier = new ObjectStringifier() {
                @Override
                public String toString(Object object) {
                    if(object == null) {
                        return null;
                    }
                    final ObjectAdapter adapter = getAdapterManager().adapterFor(object);
                    Oid oid = adapter.getOid();
                    return oid != null? oid.enString(getOidMarshaller()): encodedValueOf(adapter);
                }
                private String encodedValueOf(ObjectAdapter adapter) {
                    EncodableFacet facet = adapter.getSpecification().getFacet(EncodableFacet.class);
                    return facet != null? facet.toEncodedString(adapter): adapter.toString();
                }
                @Override
                public String classNameOf(Object object) {
                    final ObjectAdapter adapter = getAdapterManager().adapterFor(object);
                    final String className = adapter.getSpecification().getFullIdentifier();
                    return className;
                }
            };
        }
        return objectStringifier;
    }

    private static long currentTimestampEpoch() {
        return Clock.getTime();
    }

    private EventMetadata newEventMetadata(EventType eventType, final String currentUser, final long currentTimestampEpoch, String title) {
        return new EventMetadata(getGuid(), nextEventSequence(), eventType, currentUser, currentTimestampEpoch, title);
    }

    private int nextEventSequence() {
        return eventSequence++;
    }
    
    private void auditChangedProperty(final String currentUser, final long currentTimestampEpoch, final Entry<AdapterAndProperty, PreAndPostValues> auditEntry) {
        final AdapterAndProperty aap = auditEntry.getKey();
        final ObjectAdapter adapter = aap.getAdapter();
        if(!adapter.getSpecification().containsFacet(AuditableFacet.class)) {
            return;
        }
        final RootOid oid = (RootOid) adapter.getOid();
        final String objectType = oid.getObjectSpecId().asString();
        final String identifier = oid.getIdentifier();
        final PreAndPostValues papv = auditEntry.getValue();
        final String preValue = asString(papv.getPre());
        final String postValue = asString(papv.getPost());
        auditingService.audit(currentUser, currentTimestampEpoch, objectType, identifier, preValue, postValue);
    }

    private static String asString(Object object) {
        return object != null? object.toString(): null;
    }


    protected AuthenticationSession getAuthenticationSession() {
        return IsisContext.getAuthenticationSession();
    }


    
    // ////////////////////////////////////////////////////////////////
    // commit
    // ////////////////////////////////////////////////////////////////

    public synchronized final void commit() {

        ensureThatState(getState().canCommit(), is(true), "state is: " + getState());
        ensureThatState(abortCause, is(nullValue()), "cannot commit: an abort cause has been set");

        if (LOG.isDebugEnabled()) {
            LOG.debug("commit transaction " + this);
        }

        if (getState() == State.COMMITTED) {
            if (LOG.isInfoEnabled()) {
                LOG.info("already committed; ignoring");
            }
            return;
        }
        
        try {
            doAudit(getChangedObjectProperties());
            doFlush();
            doPublish(getChangedObjects());
            doFlush();
            setState(State.COMMITTED);
        } catch (final RuntimeException ex) {
            setAbortCause(new IsisTransactionManagerException(ex));
            throw ex;
        }
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

    @Deprecated
    public void ensureNoAbortCause() {
        Ensure.ensureThatArg(abortCause, is(nullValue()), "abort cause has been set");
    }

    
    
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

    private boolean alreadyHasSave(final ObjectAdapter onObject) {
        return alreadyHasCommand(SaveObjectCommand.class, onObject);
    }

    private PersistenceCommand getCommand(final Class<?> commandClass, final ObjectAdapter onObject) {
        for (final PersistenceCommand command : commands) {
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
        commands.remove(toDelete);
    }

    private void removeCreate(final ObjectAdapter onObject) {
        removeCommand(CreateObjectCommand.class, onObject);
    }

    private void removeSave(final ObjectAdapter onObject) {
        removeCommand(SaveObjectCommand.class, onObject);
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
        str.append("commands", commands.size());
        return str;
    }


    // ////////////////////////////////////////////////////////////////
    // Depenendencies (from constructor)
    // ////////////////////////////////////////////////////////////////

    /**
     * The owning {@link IsisTransactionManager transaction manager}.
     * 
     * <p>
     * Injected in constructor
     */
    public IsisTransactionManager getTransactionManager() {
        return transactionManager;
    }

    /**
     * The {@link MessageBroker} for this transaction.
     * 
     * <p>
     * Injected in constructor
     *
     * @deprecated - obtain the {@link org.apache.isis.core.commons.authentication.MessageBroker} instead from the {@link AuthenticationSession}.
     */
    @Deprecated
    public MessageBroker getMessageBroker() {
        return (MessageBroker) messageBroker;
    }

    /**
     * The {@link UpdateNotifier} for this transaction.
     * 
     * <p>
     * Injected in constructor
     */
    public UpdateNotifier getUpdateNotifier() {
        return updateNotifier;
    }

    public static class AdapterAndProperty {
        
        private final ObjectAdapter objectAdapter;
        private final ObjectAssociation property;
        
        public static AdapterAndProperty of(ObjectAdapter adapter, ObjectAssociation property) {
            return new AdapterAndProperty(adapter, property);
        }

        private AdapterAndProperty(ObjectAdapter adapter, ObjectAssociation property) {
            this.objectAdapter = adapter;
            this.property = property;
        }
        
        public ObjectAdapter getAdapter() {
            return objectAdapter;
        }
        public ObjectAssociation getProperty() {
            return property;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((objectAdapter == null) ? 0 : objectAdapter.hashCode());
            result = prime * result + ((property == null) ? 0 : property.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            AdapterAndProperty other = (AdapterAndProperty) obj;
            if (objectAdapter == null) {
                if (other.objectAdapter != null)
                    return false;
            } else if (!objectAdapter.equals(other.objectAdapter))
                return false;
            if (property == null) {
                if (other.property != null)
                    return false;
            } else if (!property.equals(other.property))
                return false;
            return true;
        }
        
        @Override
        public String toString() {
            return getAdapter().getOid().enStringNoVersion(getMarshaller()) + " , " + getProperty().getId();
        }

        protected OidMarshaller getMarshaller() {
            return new OidMarshaller();
        }

        private Object getPropertyValue() {
            ObjectAdapter referencedAdapter = property.get(objectAdapter);
            return referencedAdapter == null ? null : referencedAdapter.getObject();
        }
    }
   
    
    ////////////////////////////////////////////////////////////////////////
    // Auditing/Publishing object tracking
    ////////////////////////////////////////////////////////////////////////

    public static class PreAndPostValues {
        
        private final static Predicate<Entry<?, PreAndPostValues>> CHANGED = new Predicate<Entry<?, PreAndPostValues>>(){
            @Override
            public boolean apply(Entry<?, PreAndPostValues> input) {
                final PreAndPostValues papv = input.getValue();
                return papv.differ();
            }};
            
        private final Object pre;
        private Object post;
        
        public static PreAndPostValues pre(Object preValue) {
            return new PreAndPostValues(preValue, null);
        }

        private PreAndPostValues(Object pre, Object post) {
            this.pre = pre;
            this.post = post;
        }
        public Object getPre() {
            return pre;
        }
        
        public Object getPost() {
            return post;
        }
        
        public void setPost(Object post) {
            this.post = post;
        }
        
        @Override
        public String toString() {
            return getPre() + " -> " + getPost();
        }

        public boolean differ() {
            return !Objects.equal(getPre(), getPost());
        }
    }
    
   
    private final Map<AdapterAndProperty, PreAndPostValues> changedObjectProperties = Maps.newLinkedHashMap();
    private final Set<ObjectAdapter> changedObjects = Sets.newLinkedHashSet();


    private ObjectStringifier objectStringifier;
    

    /**
     * For object stores to record the current values of an {@link ObjectAdapter} that has enlisted
     * into the transaction, prior to updating its values.
     * 
     * <p>
     * The values of the {@link ObjectAdapter} after being updated are captured when the
     * audit entries are requested, in {@link #getChangedObjectProperties()}.
     * 
     * <p>
     * Supported by the JDO object store; check documentation for support in other objectstores.
     */
    public void auditDirty(ObjectAdapter adapter) {
        for (ObjectAssociation property : adapter.getSpecification().getAssociations(ObjectAssociationFilters.PROPERTIES)) {
            changedObjectProperty(adapter, property);
        }
    }
    
    private void changedObjectProperty(ObjectAdapter adapter, ObjectAssociation property) {
        final AdapterAndProperty aap = AdapterAndProperty.of(adapter, property);
        PreAndPostValues papv = PreAndPostValues.pre(aap.getPropertyValue());
        changedObjectProperties.put(aap, papv);
        changedObjects.add(adapter);
    }


    /**
     * Returns the pre- and post-values of all {@link ObjectAdapter}s that were enlisted and dirtied
     * in this transaction.
     * 
     * <p>
     * This requires that the object store called {@link #auditDirty(ObjectAdapter)} for each object being
     * enlisted.
     * 
     * <p>
     * Supported by the JDO object store (since it calls {@link #auditDirty(ObjectAdapter)}); 
     * check documentation for support in other object stores.
     */
    public Set<Entry<AdapterAndProperty, PreAndPostValues>> getChangedObjectProperties() {
        updatePostValues(changedObjectProperties.entrySet());

        return Collections.unmodifiableSet(Sets.filter(changedObjectProperties.entrySet(), PreAndPostValues.CHANGED));
    }

    private static void updatePostValues(Set<Entry<AdapterAndProperty, PreAndPostValues>> entrySet) {
        for (Entry<AdapterAndProperty, PreAndPostValues> entry : entrySet) {
            final AdapterAndProperty aap = entry.getKey();
            final PreAndPostValues papv = entry.getValue();
            
            papv.setPost(aap.getPropertyValue());
        }
    }

    private Set<ObjectAdapter> getChangedObjects() {
        return changedObjects;
    }

    
    ////////////////////////////////////////////////////////////////////////
    // Dependencies (from context)
    ////////////////////////////////////////////////////////////////////////

    protected AdapterManager getAdapterManager() {
        return IsisContext.getPersistenceSession().getAdapterManager();
    }

    protected OidMarshaller getOidMarshaller() {
        return IsisContext.getOidMarshaller();
    }


    
}
