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

import java.util.HashMap;
import java.util.Map;

import org.apache.isis.applib.services.xactn.TransactionId;
import org.apache.isis.applib.services.xactn.TransactionState;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.metamodel.commons.ToString;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.runtime.context.session.RuntimeContextBase;
import org.apache.isis.core.runtime.persistence.session.PersistenceSession;
import org.apache.isis.core.security.authentication.AuthenticationSession;

import lombok.Getter;
import lombok.NonNull;

/**
 * Holds the current set of components for a specific execution context (such as on a thread).
 *
 * @see IsisInteractionFactory
 */
public class IsisInteraction extends RuntimeContextBase {

    @Getter private final AuthenticationSession authenticationSession;

    /**
     * Set to System.nanoTime() when session opens.
     * @deprecated use command timestamp instead?
     */
    @Getter private long openedAtSystemNanos = -1L;

    public IsisInteraction(
            @NonNull final MetaModelContext mmc,
            @NonNull final AuthenticationSession authenticationSession) {

        super(mmc);
        this.authenticationSession = authenticationSession; // binds this session to given authenticationSession
        
        openedAtSystemNanos = System.nanoTime();
    }

    // -- FLUSH
    //    void flush() {
    //    	runtimeEventService.fireSessionFlushing(this);
    //    }

    // -- TRANSACTION

    public TransactionId getCurrentTransactionId() {
        return transactionService.currentTransactionId();
    }

    public TransactionState getCurrentTransactionState() {
        return transactionService.currentTransactionState();
    }


    // -- toString
    @Override
    public String toString() {
        final ToString asString = new ToString(this);
        asString.append("persistenceSession", PersistenceSession.current(PersistenceSession.class));
        asString.append("transaction", getCurrentTransactionId());
        return asString.toString();
    }

    // -- INCUBATING INTERACTION SCOPED USER DATA ... 
    
    private final Map<Class, Can> map = new HashMap<>();

    public <T> Runnable put(Class<? super T> type, T variant) {
        map
                .compute(type, (k, v) -> v == null
                        ? Can.<T>ofSingleton(variant)
                        : Can.<T>concat(_Casts.uncheckedCast(v), variant));

        return () -> map.remove(type);
    }

    public <T> Can<T> get(Class<? super T> type) {
        return map.get(type);
    }
}
