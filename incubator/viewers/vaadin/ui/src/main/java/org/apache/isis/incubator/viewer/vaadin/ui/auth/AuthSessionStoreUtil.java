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
package org.apache.isis.incubator.viewer.vaadin.ui.auth;

import java.util.Optional;

import javax.annotation.Nullable;
import javax.servlet.http.HttpSession;

import com.vaadin.flow.server.VaadinSession;

import org.apache.isis.core.security.authentication.AuthenticationSession;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * @implNote Stores authentication information (AuthenticationSession) on the HttpSession that is associated
 * with the current thread's VaadinSession or directly on the provided HttpSession if given as argument.
 * 
 * @since Mar 15, 2020
 *
 */
@UtilityClass
public class AuthSessionStoreUtil {

    public static void put(
            @NonNull final HttpSession httpSession, 
            @Nullable final AuthenticationSession authSession) {
        httpSession.setAttribute(AuthenticationSession.class.getName(), authSession);
    }
    
    public static Optional<AuthenticationSession> get(
            @NonNull final HttpSession httpSession) {
        return Optional.ofNullable(
                (AuthenticationSession)httpSession
                .getAttribute(AuthenticationSession.class.getName()));
    }
    
    /** when within a VaadinSession */
    public static void put(
            @Nullable final AuthenticationSession authSession) {
        Optional.ofNullable(VaadinSession.getCurrent())
        .map(VaadinSession::getSession)
        .ifPresent(sessionVaa->{
            sessionVaa.setAttribute(AuthenticationSession.class.getName(), authSession);    
        });
    }
    
    /** when within a VaadinSession */
    public static Optional<AuthenticationSession> get() {
        return Optional.ofNullable(
                (AuthenticationSession)VaadinSession.getCurrent().getSession()
                .getAttribute(AuthenticationSession.class.getName()));
    }
    
    /** when within a VaadinSession */
    public static void clear() {
        put(null);
    }

}