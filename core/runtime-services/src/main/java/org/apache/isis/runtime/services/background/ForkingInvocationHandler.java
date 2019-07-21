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
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.runtime.system.internal.InitialisationSession;
import org.apache.isis.runtime.system.session.IsisSession;
import org.apache.isis.runtime.system.transaction.IsisTransaction;
import org.apache.isis.security.authentication.AuthenticationSession;

import static org.apache.isis.commons.internal.base._With.requires;

import lombok.extern.log4j.Log4j2;

/**
 * Package private invocation handler that executes actions in the background using a ExecutorService
 * @since 2.0
 */
@Log4j2
class ForkingInvocationHandler<T> implements InvocationHandler {

    private final T target;
    private final Object mixedInIfAny;
    private final ExecutorService backgroundExecutorService;

    ForkingInvocationHandler(
            T target,
            Object mixedInIfAny,
            ExecutorService backgroundExecutorService ) {
        this.target = requires(target, "target");
        this.mixedInIfAny = mixedInIfAny;
        this.backgroundExecutorService = requires(backgroundExecutorService, "backgroundExecutorService");
    }

    @Override
    public Object invoke(
            final Object proxied,
            final Method proxyMethod,
            final Object[] args) throws Throwable {

        final boolean inheritedFromObject = proxyMethod.getDeclaringClass().equals(Object.class);
        if(inheritedFromObject) {
            return proxyMethod.invoke(target, args);
        }

        final Object domainObject;
        if (mixedInIfAny == null) {
            domainObject = target;
        } else {
            domainObject = mixedInIfAny;
        }

        final Optional<IsisSession> currentSession = IsisSession.current();

        final AuthenticationSession authSession = currentSession
                .map(IsisSession::getAuthenticationSession)
                .orElse(new InitialisationSession());

        final CountDownLatch countDownLatch = currentSession
                .map(IsisSession::getCurrentTransaction)
                .map(IsisTransaction::countDownLatch)
                .orElse(new CountDownLatch(0));

        backgroundExecutorService.submit(()->{

            try {
                countDownLatch.await(); // wait for current transaction of the calling thread to complete

                IsisContext.getSessionFactory().doInSession(
                        ()->proxyMethod.invoke(domainObject, args),
                        authSession	);

            } catch (Exception e) {

                log.error(
                        String.format("Background execution of action '%s' on object '%s' failed.",
                                proxyMethod.getName(),
                                domainObject.getClass().getName()),
                        e);
            }
        });

        return null;
    }

}
