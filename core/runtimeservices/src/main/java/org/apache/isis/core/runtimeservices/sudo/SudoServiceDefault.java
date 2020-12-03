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

package org.apache.isis.core.runtimeservices.sudo;

import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.sudo.SudoService;
import org.apache.isis.applib.services.user.UserMemento;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.runtime.iactn.IsisInteractionFactory;
import org.apache.isis.core.security.authentication.standard.SimpleSession;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import jakarta.annotation.PostConstruct;

@Service
@Named("isisRuntimeServices.SudoServiceDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class SudoServiceDefault implements SudoService {

    private final IsisInteractionFactory interactionFactory;

    private Can<SudoService.Listener> sudoListeners = Can.empty();

    @Override
    public <T> T call(final @NonNull UserMemento user, final @NonNull Callable<T> callable) {

        try {
            beforeCall(user);
            return interactionFactory.callAuthenticated(SimpleSession.validOf(user), callable);
        } finally {
            afterCall();
        }
    }

    @PostConstruct @Inject
    public void init(final ServiceRegistry serviceRegistry) {
        this.sudoListeners = serviceRegistry.select(SudoService.Listener.class);
    }

    // -- HELPER

    private void beforeCall(final @NonNull UserMemento user) {
        for (val sudoListener : sudoListeners) {
            sudoListener.beforeCall(user);
        }
    }

    private void afterCall() {
        for (val sudoListener : sudoListeners) {
            sudoListener.afterCall();
        }
    }





}
