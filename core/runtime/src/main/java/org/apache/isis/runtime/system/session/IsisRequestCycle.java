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
package org.apache.isis.runtime.system.session;

import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.runtime.system.persistence.PersistenceSession;
import org.apache.isis.security.authentication.AuthenticationSession;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * TODO [2125] intent is to remove direct dependencies upon Persistence/Transaction for the viewer-modules.   
 * 
 * @since 2.0
 */
@RequiredArgsConstructor(staticName = "next")
public class IsisRequestCycle {

    // -- SUPPORTING ISIS TRANSACTION FILTER FOR RESTFUL OBJECTS ...

    private final TransactionTemplate transactionTemplate;
    private TransactionStatus txStatus;

    // -- SUPPORTING WEB REQUEST CYCLE FOR ISIS ...

    public void onBeginRequest(AuthenticationSession authenticationSession) {

        val isisSessionFactory = IsisContext.getSessionFactory();
        isisSessionFactory.openSession(authenticationSession);

        txStatus = transactionTemplate.getTransactionManager().getTransaction(null);

        //		IsisContext.getTransactionManagerJdo()
        //				.ifPresent(txMan->txMan.startTransaction());
    }

    public void onRequestHandlerExecuted() {

        if(txStatus==null) {
            return;    
        }

        txStatus.flush();
    }

    public void onEndRequest() {

        if(txStatus==null) {
            return;    
        }

        try {
            
            transactionTemplate.getTransactionManager().commit(txStatus);
            
        } finally {
            val isisSessionFactory = IsisContext.getSessionFactory();
            isisSessionFactory.closeSession();
        }

    }

    // -- SUPPORTING FORM EXECUTOR DEFAULT ...

    public static void onResultAdapterObtained() {
        val isisSession = IsisSession.currentOrElseNull();
        if (isisSession==null) {
            return;
        }

        PersistenceSession.current(PersistenceSession.class)
        .stream()
        .forEach(ps->ps.flush());

        //isisSession.flush();
    }

    // --


}
