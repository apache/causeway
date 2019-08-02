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
package org.apache.isis.runtime.services.background;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;

import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.runtime.system.internal.InitialisationSession;
import org.apache.isis.runtime.system.session.IsisSession;
import org.apache.isis.runtime.system.transaction.IsisTransactionAspectSupport;

import static org.apache.isis.commons.internal.functions._Functions.uncheckedSupplier;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Package private invocation handler that executes actions in the background 
 * using a ExecutorService.
 * @since 2.0
 */
@Log4j2 @AllArgsConstructor
class ForkingInvocationHandler<T> implements InvocationHandler {

    @NonNull private final T target;
    private final Object mixedInIfAny;
    @NonNull private final ExecutorService backgroundExecutorService;
    @NonNull private final TransactionService transactionService;

    @Override
    public Object invoke(
            final Object proxied,
            final Method proxyMethod,
            final Object[] args) throws Throwable {

        val inheritedFromObject = proxyMethod.getDeclaringClass().equals(Object.class);
        if(inheritedFromObject) {
            return proxyMethod.invoke(target, args);
        }

        val domainObject = mixedInIfAny != null
                ? mixedInIfAny
                        : target;

        val authenticationSession = 
                IsisSession.current()
                .map(IsisSession::getAuthenticationSession)
                .orElse(new InitialisationSession());

        val transactionLatch = IsisTransactionAspectSupport.transactionLatch();

        //unfortunately there is no easy way to make use of this future
        //would be nice if users had access to it via the background-service
        val future = backgroundExecutorService.submit(()->{

            try {

                transactionLatch.await(); // wait for transaction of the calling thread to complete

                return IsisContext.getSessionFactory().doInSession(
                        ()->transactionService.executeWithinTransaction(
                                uncheckedSupplier(()->proxyMethod.invoke(domainObject, args))
                                ),
                        authenticationSession);

            } catch (Exception e) {

                log.error(
                        String.format("Background execution of action '%s' on object '%s' failed.",
                                proxyMethod.getName(),
                                domainObject.getClass().getName()),
                        e);
                return null;
            }
        });

        return null;

    }

}
