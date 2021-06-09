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

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.applib.services.user.ImpersonatedUserHolder;
import org.apache.isis.applib.services.user.UserMemento;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.core.interaction.session.InteractionTracker;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This default implementation delegates to {@link ImpersonatedUserHolder} to
 * hold an impersonated user (if supported).
 */
@Service
@Named("isis.runtimeservices.UserServiceDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class UserServiceDefault implements UserService {

    private final InteractionTracker isisInteractionTracker;
    private final List<ImpersonatedUserHolder> impersonatedUserHolders;

    /**
     * Either the current user or the one being impersonated.
     */
    @Override
    public Optional<UserMemento> currentUser() {
        Optional<UserMemento> optional = getImpersonatedUser();
        return optional.isPresent()
                ? optional
                : isisInteractionTracker.currentExecutionContext().map(InteractionContext::getUser);
    }

    @Override
    public boolean supportsImpersonation() {
        return impersonatingHolder()
                .isPresent();
    }

    private Optional<ImpersonatedUserHolder> impersonatingHolder() {
        return impersonatedUserHolders.stream()
                .filter(ImpersonatedUserHolder::supportsImpersonation)
                .findFirst();
    }

    @Override
    public void impersonateUser(final String userName, final List<String> roles) {
        impersonatingHolder().ifPresent(x ->
                {
                    val userMemento = UserMemento.ofNameAndRoleNames(userName, roles)
                                        .withImpersonating();
                    x.setUserMemento(userMemento);
                }
        );
    }

    @Override
    public void stopImpersonating() {
        impersonatingHolder().ifPresent(ImpersonatedUserHolder::clearUserMemento);
    }

    @Override
    public Optional<UserMemento> getImpersonatedUser() {
        return impersonatingHolder().flatMap(ImpersonatedUserHolder::getUserMemento);
    }

}
