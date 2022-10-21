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
package org.apache.causeway.core.webapp.keyvaluestore;

import java.io.Serializable;
import java.util.Optional;

import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.keyvaluestore.KeyValueSessionStore;

import lombok.NonNull;

/**
 * Implementation that uses the {@link HttpSession}
 * to store key/value pairs.
 *
 * @since 2.0 {@index}
 */
@Component
@Named("causeway.webapp.KeyValueStoreUsingHttpSession")
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
public class KeyValueStoreUsingHttpSession implements KeyValueSessionStore {

    @Override
    public boolean isSessionAvailable() {
        return httpSession().isPresent();
    }

    @Override
    public void put(final @NonNull String key, final @Nullable Serializable value) {
        if(value==null) {
            clear(key);
            return;
        }
        httpSession()
        .ifPresent(session->
            session.setAttribute(key, value));
    }

    @Override
    public <T extends Serializable>
    Optional<T> lookupAs(final @NonNull String key, final @NonNull Class<T> requiredType) {
        return httpSession()
                .map(session->session.getAttribute(key))
                .filter(requiredType::isInstance)
                .map(requiredType::cast);
    }

    @Override
    public void clear(final @NonNull String key) {
        httpSession()
        .ifPresent(session->
            session.removeAttribute(key));
    }

    // -- HELPER

    private static Optional<HttpSession> httpSession() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .map(x -> x.getSession(false)); // asks for session without side-effects
    }

}
