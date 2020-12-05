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
package org.apache.isis.core.runtime.session;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.runtime.iactn.InteractionFactory;
import org.apache.isis.core.security.authentication.Authentication;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * 
 * @since 2.0
 */
@RequiredArgsConstructor(staticName = "next")
public class IsisRequestCycle {

    // -- SUPPORTING ISIS TRANSACTION FILTER FOR RESTFUL OBJECTS ...

    private final InteractionFactory isisInteractionFactory;
    private final TransactionTemplate transactionTemplate;
    private TransactionStatus txStatus;

    // -- SUPPORTING WEB REQUEST CYCLE FOR ISIS ...

    public void onBeginRequest(Authentication authentication) {

        isisInteractionFactory.openInteraction(authentication);

        txStatus = getTransactionManager().getTransaction(null);

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

            getTransactionManager().commit(txStatus);

        } finally {
            isisInteractionFactory.closeSessionStack();
        }

    }
    
    // -- HELPER
    
    private PlatformTransactionManager getTransactionManager() {
        val txMan = transactionTemplate.getTransactionManager();
        if(txMan == null) {
            throw _Exceptions.illegalState("IsisRequestCycle needs a PlatformTransactionManager (Spring)");
        }
        return txMan;
    }


}
