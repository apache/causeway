/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.runtime.sessiontemplate;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSession;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.runtime.system.transaction.TransactionalClosureAbstract;

public abstract class AbstractIsisSessionTemplate {

    /**
     * Sets up an {@link IsisSession} then passes along any calling framework's context. 
     */
    public void execute(final AuthenticationSession authSession, final Object context) {
        try {
            IsisContext.openSession(authSession);
            PersistenceSession persistenceSession = getPersistenceSession();
            persistenceSession.getServicesInjector().injectServicesInto(this);
            doExecute(context);
        } finally {
            IsisContext.closeSession();
        }
    }


    // //////////////////////////////////////
    
    /**
     * Either override {@link #doExecute(Object)} (this method) or alternatively override
     * {@link #doExecuteWithTransaction(Object)}.
     *
     * <p>
     * This method is called within a current {@link org.apache.isis.core.runtime.system.session.IsisSession session},
     * but with no current transaction.  The default implementation sets up a
     * {@link org.apache.isis.core.runtime.system.transaction.IsisTransaction transaction}
     * and then calls {@link #doExecuteWithTransaction(Object)}.  Override if you require more sophisticated
     * transaction handling.
     */
    protected void doExecute(final Object context) {
        final PersistenceSession persistenceSession = getPersistenceSession();
        final IsisTransactionManager transactionManager = getTransactionManager(persistenceSession);
        transactionManager.executeWithinTransaction(new TransactionalClosureAbstract() {
            @Override
            public void execute() {
                doExecuteWithTransaction(context);
            }
        });
    }

    /**
     * Either override {@link #doExecuteWithTransaction(Object)} (this method) or alternatively override
     * {@link #doExecuteWithTransaction(Object)}.
     *
     * <p>
     * This method is called within a current
     * {@link org.apache.isis.core.runtime.system.transaction.IsisTransaction transaction}, by the default
     * implementation of {@link #doExecute(Object)}.
     */
    protected void doExecuteWithTransaction(final Object context) {}

    // //////////////////////////////////////

    protected ObjectAdapter adapterFor(final Object targetObject) {
        return getAdapterManager().adapterFor(targetObject);
    }
    protected ObjectAdapter adapterFor(final RootOid rootOid) {
        return getAdapterManager().adapterFor(rootOid);
    }
    
    // //////////////////////////////////////
    
    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected IsisTransactionManager getTransactionManager(PersistenceSession persistenceSession) {
        return persistenceSession.getTransactionManager();
    }

    protected AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }


}
