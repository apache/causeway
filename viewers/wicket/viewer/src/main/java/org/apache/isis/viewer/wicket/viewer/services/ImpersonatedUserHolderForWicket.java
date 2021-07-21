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
package org.apache.isis.viewer.wicket.viewer.services;

import java.util.Optional;

import javax.inject.Named;

import org.apache.wicket.Session;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.user.ImpersonatedUserHolder;
import org.apache.isis.applib.services.user.UserMemento;

/**
 * Implementation that supports impersonation, using the Wicket {@link Session}
 * to store the value.
 *
 * @since 2.0 {@index}
 */
@Component
@Named("isis.webapp.ImpersonatedUserHolderForWicket")
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT - 100)
public class ImpersonatedUserHolderForWicket implements ImpersonatedUserHolder {

    private static final String HTTP_SESSION_KEY_IMPERSONATED_USER =
            ImpersonatedUserHolderForWicket.class.getName() + "#userMemento";

    @Override
    public boolean supportsImpersonation() {
        return session().isPresent();
    }

    @Override
    public void setUserMemento(final UserMemento userMemento) {
        session()
        .ifPresent(session->
            session.setAttribute(HTTP_SESSION_KEY_IMPERSONATED_USER, userMemento));
    }

    @Override
    public Optional<UserMemento> getUserMemento() {
        return session()
            .map(session->session.getAttribute(HTTP_SESSION_KEY_IMPERSONATED_USER))
            .filter(UserMemento.class::isInstance)
            .map(UserMemento.class::cast);
    }

    @Override
    public void clearUserMemento() {
        session()
        .ifPresent(session->
            session.removeAttribute(HTTP_SESSION_KEY_IMPERSONATED_USER));
    }

    private static Optional<Session> session() {
        return Optional.ofNullable(Session.get());
    }

}
