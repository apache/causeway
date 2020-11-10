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

package org.apache.isis.core.runtime.iactn;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.isis.applib.services.xactn.TransactionId;
import org.apache.isis.applib.services.xactn.TransactionState;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.commons.ToString;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.runtime.context.RuntimeContextBase;
import org.apache.isis.core.security.authentication.AuthenticationSession;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Holds the current set of components for a specific execution context
 * (such as on a thread).
 *
 * <p>
 *     Not to be confused with the applib's
 *     {@link org.apache.isis.applib.services.iactn.Interaction} object, which
 *     captures the details of an action invocation or property edit (along
 *     with any nested calls to other actions through wrapper factory) for
 *     the purpose of publishing using
 *     {@link org.apache.isis.applib.services.publish.PublisherService}.
 * </p>
 *
 * @see IsisInteractionFactory
 */
public class InteractionSession extends RuntimeContextBase {

    @Getter private final AuthenticationSession authenticationSession;
    @Getter private final long lifecycleStartedAtSystemNanos;

    public InteractionSession(
            @NonNull final MetaModelContext mmc,
            @NonNull final AuthenticationSession authenticationSession) {

        super(mmc);
        this.authenticationSession = authenticationSession; // binds this session to given authenticationSession
        this.lifecycleStartedAtSystemNanos = System.nanoTime(); // used to measure time periods, so not using ClockService here
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

    // -- INTERACTION ON CLOSE HANDLER
    
    @Setter private Runnable onClose;
    
    // -- INTERACTION SCOPED SESSION ATTRIBUTE
    
    private Map<Class<?>, Object> attributes = null;
    private boolean closed = false;

    /** add type specific session data */
    public <T> T putAttribute(Class<? super T> type, T value) {
        return _Casts.uncheckedCast(attributes().put(type, value));
    }
    
    /** conditionally add type specific session data */
    public <T> T computeAttributeIfAbsent(Class<? super T> type, Function<Class<?>, ? extends T> mappingFunction) {
        return _Casts.uncheckedCast(attributes().computeIfAbsent(type, mappingFunction));
    }

    /** get type specific session data */
    public <T> T getAttribute(Class<T> type) {
        return (attributes!=null)
                ? _Casts.uncheckedCast(attributes.get(type))
                : null;
    }
    
    /** remove type specific session data */
    public void removeUserData(Class<?> type) {
        if(attributes!=null) {
            attributes.remove(type);
        }
    }
    
    /** Do not use, is called by the framework internally. */
    public void close() {
        if(onClose!=null) {
            onClose.run();
            onClose = null;
        }
        attributes = null;
        closed = true;
    }
    
    /**
     * Copies all attributes to the target session.
     * @param target
     */
    public void copyAttributesTo(final @NonNull InteractionSession target) {
        if(_NullSafe.isEmpty(attributes)) {
            return;
        }
        target.attributes().putAll(attributes);
    }
    
    private Map<Class<?>, Object> attributes() {
        if(closed) {
            throw _Exceptions.illegalState(
                    "IsisInteraction was already closed, cannot access UserData any longer.");
        }
        return (attributes==null) 
                ? attributes = new HashMap<>() 
                : attributes;
    }
    
    // -- TO STRING
    
    @Override
    public String toString() {
        final ToString asString = new ToString(this);
        asString.append("transaction", getCurrentTransactionId());
        return asString.toString();
    }




}
