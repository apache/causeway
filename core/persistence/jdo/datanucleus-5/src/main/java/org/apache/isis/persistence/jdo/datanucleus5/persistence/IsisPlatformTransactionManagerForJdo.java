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

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.runtime.persistence.session.PersistenceSession;
import org.apache.isis.runtime.persistence.transaction.IsisTransactionAspectSupport;
import org.apache.isis.runtime.persistence.transaction.IsisTransactionObject;
import org.apache.isis.runtime.session.IsisSession;
import org.apache.isis.runtime.session.IsisSessionFactory;
import org.apache.isis.runtime.session.init.InitialisationSession;
import org.apache.isis.security.api.authentication.AuthenticationSession;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Named("isisJdoDn5.IsisPlatformTransactionManagerForJdo")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("JdoDN5")
@Log4j2
public class IsisPlatformTransactionManagerForJdo extends AbstractPlatformTransactionManager {

    private static final long serialVersionUID = 1L;

    private final IsisSessionFactory isisSessionFactory;
    private final ServiceRegistry serviceRegistry;

    @Inject
    public IsisPlatformTransactionManagerForJdo(IsisSessionFactory isisSessionFactory, ServiceRegistry serviceRegistry) {
        this.isisSessionFactory = isisSessionFactory;
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    protected Object doGetTransaction() throws TransactionException {

        val isInSession = IsisSession.isInSession();
        log.debug("doGetTransaction isInSession={}", isInSession);

        if(!isInSession) {

            // get authenticationSession from IoC, or fallback to InitialisationSession 
            val authenticationSession = serviceRegistry.select(AuthenticationSession.class)
                    .getFirst()
                    .orElseGet(InitialisationSession::new);

            log.debug("open new session authenticationSession={}", authenticationSession);

            isisSessionFactory.openSession(authenticationSession);
        }

        val transactionBeforeBegin = 
                IsisTransactionAspectSupport
                .currentTransactionObject()
                .map(x->x.getCurrentTransaction())
                .orElse(null);

        return IsisTransactionObject.of(transactionBeforeBegin);

    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
        IsisTransactionObject txObject = (IsisTransactionObject) transaction;

        log.debug("doBegin {}", definition);

        val tx = transactionManagerJdo().beginTransaction();
        txObject.setCurrentTransaction(tx);
        IsisTransactionAspectSupport.putTransactionObject(txObject);
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
        IsisTransactionObject txObject = (IsisTransactionObject) status.getTransaction();

        log.debug("doCommit {}", status);

        transactionManagerJdo().commitTransaction(txObject);
        txObject.getCountDownLatch().countDown();
        txObject.setCurrentTransaction(null);
        IsisTransactionAspectSupport.clearTransactionObject();
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
        IsisTransactionObject txObject = (IsisTransactionObject) status.getTransaction();

        log.debug("doRollback {}", status);

        transactionManagerJdo().abortTransaction(txObject);
        txObject.getCountDownLatch().countDown();
        txObject.setCurrentTransaction(null);
        IsisTransactionAspectSupport.clearTransactionObject();
    }

    private IsisTransactionManagerJdo transactionManagerJdo() {
        return PersistenceSession.current(IsisPersistenceSessionJdoBase.class)
                    .getFirst()
                    .get()
                    .transactionManager;
    }

}
