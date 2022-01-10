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
package org.apache.isis.core.runtimeservices.user;

import java.util.Optional;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.user.ImpersonatedUserHolder;
import org.apache.isis.applib.services.user.UserMemento;

/**
 * Used by the framework's default implementation of {@link org.apache.isis.applib.services.user.UserService} to
 * allow the current user to be temporarily impersonated.
 *
 * <p>
 *     The intention is that viewers provide an implementation of this service..
 *     Note that the Wicket viewer <i>does</i> implement this service and
 *     uses an {@link javax.servlet.http.HttpSession}; this will have the
 *     side-effect of making REST API potentially non stateful.
 * </p>
 *
 * <p>
 *     The default implementation does <i>not</i> support impersonation.
 * </p>
 */
@Service
@Named("isis.runtimeservices.ImpersonatedUserHolderDefault")
@javax.annotation.Priority(PriorityPrecedence.LAST)
@Qualifier("Default")
public class ImpersonatedUserHolderDefault implements ImpersonatedUserHolder {

    /**
     * Returns <code>false</code>, as this implementation does <i>not</i>
     * support impersonation.
     */
    @Override
    public boolean supportsImpersonation() {
        return false;
    }

    /**
     * Simply throws an exception.
     */
    @Override
    public void setUserMemento(final UserMemento userMemento) {
        throw new RuntimeException("This implementation does not support impersonation");
    }

    /**
     * Simply returns an empty Optional.
     */
    @Override
    public Optional<UserMemento> getUserMemento() {
        return Optional.empty();
    }

    /**
     * No-op
     */
    @Override
    public void clearUserMemento() {
    }

}
