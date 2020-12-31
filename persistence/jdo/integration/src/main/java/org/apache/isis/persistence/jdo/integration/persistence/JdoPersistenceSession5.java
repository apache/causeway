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
package org.apache.isis.persistence.jdo.integration.persistence;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Vetoed;
import javax.jdo.PersistenceManager;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.transaction.changetracking.EntityChangeTracker;
import org.apache.isis.persistence.jdo.integration.lifecycles.IsisLifecycleListener;
import org.apache.isis.persistence.jdo.integration.lifecycles.JdoStoreLifecycleListenerForIsis;
import org.apache.isis.persistence.jdo.integration.lifecycles.LoadLifecycleListenerForIsis;
import org.apache.isis.persistence.jdo.spring.integration.TransactionAwarePersistenceManagerFactoryProxy;

import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * A wrapper around the JDO {@link PersistenceManager}.
 */
@Vetoed @Log4j2
public class JdoPersistenceSession5
implements
    JdoPersistenceSession {

    // -- FIELDS

    @Getter(onMethod_ = {@Override}) private PersistenceManager persistenceManager;
    @Getter(onMethod_ = {@Override}) private final MetaModelContext metaModelContext;

    private final PlatformTransactionManager txManager;
    private final TransactionAwarePersistenceManagerFactoryProxy pmf;
    private final List<Runnable> onCloseTasks = new ArrayList<>();
    private TransactionStatus nonParticipatingTransactionalBoundary;

    // -- CONSTRUCTOR
    
    /**
     * Initialize the object store so that calls to this object store access
     * persisted objects and persist changes to the object that are saved.
     * @param pmf 
     */
    public JdoPersistenceSession5(
            final MetaModelContext metaModelContext, 
            final PlatformTransactionManager txManager,
            final TransactionAwarePersistenceManagerFactoryProxy pmf) {

        if (log.isDebugEnabled()) {
            log.debug("creating {}", this);
        }

        this.metaModelContext = metaModelContext;
        this.txManager = txManager;
        this.pmf = pmf;
                
        this.state = State.NOT_INITIALIZED;
    }

    // -- STATE

    protected enum State {
        NOT_INITIALIZED, OPEN, CLOSED
        ;
        protected void ensureNotOpened() {
            if (this != State.NOT_INITIALIZED) {
                throw new IllegalStateException("Persistence session has already been initialized");
            }
        }
        protected void ensureOpened() {
            ensureStateIs(State.OPEN);
        }
        private void ensureStateIs(final State stateRequired) {
            if (this == stateRequired) {
                return;
            }
            throw new IllegalStateException("State is: " + this + "; should be: " + stateRequired);
        }
    }
    
    protected State state = State.NOT_INITIALIZED;

    // -- OPEN

    @Override
    public void open() {
        state.ensureNotOpened();

        if (log.isDebugEnabled()) {
            log.debug("opening {}", this);
        }

        val txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        // either reuse existing or create new
        val txStatus = txManager.getTransaction(txTemplate);
        if(txStatus.isNewTransaction()) {
            // we have created a new transaction, 
            nonParticipatingTransactionalBoundary = txStatus;
        } else {
            // we are participating in an exiting transaction
        }
        
        this.persistenceManager = integrateWithApplicationLayer(pmf.getPersistenceManager());
        
        this.state = State.OPEN;
    }

    // -- CLOSE



    @Override
    public void close() {

        if (state == State.CLOSED) {
            // nothing to do
            return;
        }

        try {
            
            if (nonParticipatingTransactionalBoundary!=null) {
                
                if(nonParticipatingTransactionalBoundary.isRollbackOnly()) {
                    txManager.rollback(nonParticipatingTransactionalBoundary);
                } else {
                    txManager.commit(nonParticipatingTransactionalBoundary);
                }
            }
        
            onCloseTasks.removeIf(task->{
                if(!persistenceManager.isClosed()) {
                    task.run();    
                }
                return true; 
             });
            
        } catch(final Throwable ex) {
            // ignore
            log.error(
                    "close: failed to close JDO persistenceManager; continuing to avoid memory leakage", ex);
        }
        
        persistenceManager = null; // detach

        this.state = State.CLOSED;
    }
    
    // -- HELPER
    
    private PersistenceManager integrateWithApplicationLayer(final PersistenceManager persistenceManager) {
        
        val entityChangeTracker = metaModelContext.getServiceRegistry()
                .lookupServiceElseFail(EntityChangeTracker.class);
        
        val entityChangeEmitter = 
                new JdoEntityChangeEmitter(getMetaModelContext(), persistenceManager, entityChangeTracker);
        
        val isisLifecycleListener = new IsisLifecycleListener(entityChangeEmitter);
        persistenceManager.addInstanceLifecycleListener(isisLifecycleListener, (Class[]) null);

        // install JDO specific entity change listeners ...
        
        val loadLifecycleListener = new LoadLifecycleListenerForIsis();
        val storeLifecycleListener = new JdoStoreLifecycleListenerForIsis();
        
        getServiceInjector().injectServicesInto(loadLifecycleListener);
        getServiceInjector().injectServicesInto(storeLifecycleListener);
            
        persistenceManager.addInstanceLifecycleListener(loadLifecycleListener, (Class[]) null);
        persistenceManager.addInstanceLifecycleListener(storeLifecycleListener, (Class[]) null);
        
        onCloseTasks.add(()->{
            persistenceManager.removeInstanceLifecycleListener(loadLifecycleListener);
            persistenceManager.removeInstanceLifecycleListener(storeLifecycleListener);
        });
        
        return persistenceManager;
    }


}



