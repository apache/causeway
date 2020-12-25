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

import javax.annotation.Nullable;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.transaction.changetracking.EntityChangeTracker;
import org.apache.isis.persistence.jdo.applib.fixturestate.FixturesInstalledStateHolder;
import org.apache.isis.persistence.jdo.integration.lifecycles.FetchResultHandler;
import org.apache.isis.persistence.jdo.integration.transaction.TransactionalProcessor;
import org.apache.isis.persistence.jdo.integration.transaction.TxManagerInternalFactory;

import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
abstract class _JdoPersistenceSessionBase 
implements JdoPersistenceSession {

    // -- FIELDS

    protected final FixturesInstalledStateHolder fixturesInstalledStateHolder;
    protected final TransactionalProcessor txCommandProcessor;
    
    @Getter protected final MetaModelContext metaModelContext;

    /**
     * Used to create the {@link #persistenceManager} when {@link #open()}ed.
     */
    protected final PersistenceManagerFactory jdoPersistenceManagerFactory;
    
    /**
     * populated only when {@link #open()}ed.
     */
    protected PersistenceManager persistenceManager;

    // -- CONSTRUCTOR

    /**
     * Initialize the object store so that calls to this object store access
     * persisted objects and persist changes to the object that are saved.
     */
    protected _JdoPersistenceSessionBase(
            final MetaModelContext metaModelContext,
            final PersistenceManagerFactory jdoPersistenceManagerFactory,
            final FixturesInstalledStateHolder fixturesInstalledStateHolder) {

        if (log.isDebugEnabled()) {
            log.debug("creating {}", this);
        }

        this.metaModelContext = metaModelContext;
        this.jdoPersistenceManagerFactory = jdoPersistenceManagerFactory;
        this.fixturesInstalledStateHolder = fixturesInstalledStateHolder;

        // sub-components
        this.txCommandProcessor = TxManagerInternalFactory.newCommandQueue(
                metaModelContext, 
                this,
                (FetchResultHandler)this); 

        this.state = State.NOT_INITIALIZED;
    }

    // -- GETTERS

    public EntityChangeTracker getEntityChangeTracker() {
        return metaModelContext.getServiceRegistry()
                .lookupServiceElseFail(EntityChangeTracker.class);
    }
    
    /**
     * Only populated once {@link #open()}'d
     */
    @Override
    public PersistenceManager getPersistenceManager() {
        return persistenceManager;
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

    // -- OID
    
    /**
     * @param pojo
     * @return oid for the given domain object 
     */
    protected @Nullable RootOid oidFor(@Nullable Object pojo) {
        if(pojo==null) {
            return null;
        }
        val spec = getSpecificationLoader().loadSpecification(pojo.getClass());
        val adapter = ManagedObject.of(spec, pojo);
        return ManagedObjects.identify(adapter).orElse(null);
    }

}
