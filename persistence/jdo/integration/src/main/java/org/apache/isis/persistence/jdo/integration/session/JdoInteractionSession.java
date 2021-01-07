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
package org.apache.isis.persistence.jdo.integration.session;

import javax.enterprise.inject.Vetoed;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.commons.internal.assertions._Assert.OpenCloseState;
import org.apache.isis.core.interaction.session.InteractionSession;
import org.apache.isis.core.metamodel.context.HasMetaModelContext;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.transaction.changetracking.EntityChangeTracker;
import org.apache.isis.persistence.jdo.integration.lifecycles.JdoLifecycleListener;
import org.apache.isis.persistence.jdo.spring.integration.TransactionAwarePersistenceManagerFactoryProxy;

import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * A wrapper around the JDO {@link PersistenceManager}.
 */
@Vetoed @Log4j2
public class JdoInteractionSession
implements HasMetaModelContext {

    // -- FIELDS
    
    @Getter(onMethod_ = {@Override}) private final MetaModelContext metaModelContext;

    private final TransactionAwarePersistenceManagerFactoryProxy pmfFactoryProxy;
    
    private OpenCloseState state = OpenCloseState.NOT_INITIALIZED;

    // -- CONSTRUCTOR
    
    /**
     * Initialize the object store so that calls to this object store access
     * persisted objects and persist changes to the object that are saved.
     * @param pmf 
     */
    public JdoInteractionSession(
            final MetaModelContext metaModelContext, 
            final TransactionAwarePersistenceManagerFactoryProxy pmfFactoryProxy) {

        if (log.isDebugEnabled()) {
            log.debug("creating {}", this);
        }

        this.metaModelContext = metaModelContext;
        this.pmfFactoryProxy = pmfFactoryProxy;
    }

    // -- OPEN

    /**
     * Binds this {@link JdoInteractionSession} to the current {@link InteractionSession}.
     */
    public void open() {
        
        state.assertEquals(OpenCloseState.NOT_INITIALIZED);

        if (log.isDebugEnabled()) {
            log.debug("opening {}", this);
        }
        
        integrateWithApplicationLayer(pmfFactoryProxy.getObject());
        
        this.state = OpenCloseState.OPEN;
    }

    // -- CLOSE

    /**
     * Commits the current transaction and unbinds this 
     * {@link JdoInteractionSession} from the current {@link InteractionSession}.
     */
    public void close() {

        if (state.isClosed()) {
            // nothing to do
            return;
        }
        
        this.state = OpenCloseState.CLOSED;
    }
    
    // -- HELPER
    
    private void integrateWithApplicationLayer(final PersistenceManagerFactory pmf) {
        
        val eventBusService = metaModelContext.getServiceRegistry()
                .lookupServiceElseFail(EventBusService.class);
        
        val entityChangeTracker = metaModelContext.getServiceRegistry()
                .lookupServiceElseFail(EntityChangeTracker.class);
        
        val entityChangeEmitter = 
                new JdoEntityChangeEmitter(getMetaModelContext(), entityChangeTracker);
        
        // install JDO specific entity change listeners ...
        
        val jdoLifecycleListener = new JdoLifecycleListener(
                entityChangeEmitter, entityChangeTracker, eventBusService);
        pmf.addInstanceLifecycleListener(jdoLifecycleListener, (Class[]) null);
        
    }


}



