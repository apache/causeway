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
package org.apache.isis.core.runtime.session.template;

import javax.inject.Inject;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.core.runtime.session.IsisSession;
import org.apache.isis.core.runtime.session.IsisSessionFactory;
import org.apache.isis.core.security.authentication.AuthenticationSession;

public abstract class AbstractIsisSessionTemplate {
    
    @Inject protected TransactionService transactionService;
    @Inject protected IsisSessionFactory isisSessionFactory;
    @Inject protected ServiceInjector serviceInjector;

    /**
     * Sets up an {@link IsisSession} then passes along any calling framework's context.
     */
    public void execute(final AuthenticationSession authSession, final Object context) {
        
        isisSessionFactory.runAuthenticated(authSession, ()->{
            serviceInjector.injectServicesInto(this);
            doExecute(context);
        });
        
    }

    // //////////////////////////////////////

    /**
     * Either override {@link #doExecute(Object)} (this method) or alternatively override
     * {@link #doExecuteWithTransaction(Object)}.
     *
     * <p>
     * This method is called within a current {@link IsisSession session},
     * but with no current transaction.  The default implementation sets up a transaction
     * and then calls {@link #doExecuteWithTransaction(Object)}.  Override if you require more sophisticated
     * transaction handling.
     */
    protected void doExecute(final Object context) {
        transactionService.executeWithinTransaction(()->{
            doExecuteWithTransaction(context);
        });
    }

    /**
     * Either override {@link #doExecuteWithTransaction(Object)} (this method) or alternatively override
     * {@link #doExecuteWithTransaction(Object)}.
     *
     * <p>
     * This method is called within a current transaction, by the default
     * implementation of {@link #doExecute(Object)}.
     */
    protected void doExecuteWithTransaction(final Object context) {}


}
