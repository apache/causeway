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
package org.apache.isis.core.webapp.impersonation;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.user.ImpersonatedUserHolder;
import org.apache.isis.applib.services.user.UserMemento;

/**
 * Implementation that supports impersonation, using the {@link HttpSession}
 * to store the value.
 *
 * @since 2.0 {@index}
 */
@Component
@RequestScope
@Named("isis.webapp.ImpersonatedUserHolderUsingHttpSession")
@Order(OrderPrecedence.MIDPOINT)
public class ImpersonatedUserHolderUsingHttpSession implements ImpersonatedUserHolder {

    private final Optional<HttpSession> httpSession;

    private static final String HTTP_SESSION_KEY_IMPERSONATED_USER =
            ImpersonatedUserHolderUsingHttpSession.class.getName() + "#userMemento";

    @Inject
    public ImpersonatedUserHolderUsingHttpSession(final HttpServletRequest httpServletRequest) {
        this.httpSession = Optional.ofNullable(httpServletRequest.getSession(false));
    }

    @Override
    public boolean supportsImpersonation() {
        return httpSession.isPresent();
    }

    @Override
    public void setUserMemento(final UserMemento userMemento) {
        httpSession
        .ifPresent(session->
            session.setAttribute(HTTP_SESSION_KEY_IMPERSONATED_USER, userMemento));
    }

    @Override
    public Optional<UserMemento> getUserMemento() {
        return httpSession
            .map(session->session.getAttribute(HTTP_SESSION_KEY_IMPERSONATED_USER))
            .filter(UserMemento.class::isInstance)
            .map(UserMemento.class::cast);
    }

    @Override
    public void clearUserMemento() {
        httpSession.ifPresent(session->
            session.removeAttribute(HTTP_SESSION_KEY_IMPERSONATED_USER));
    }


}
