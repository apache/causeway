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
package org.apache.causeway.core.runtimeservices.user;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.keyvaluestore.KeyValueSessionStore;
import org.apache.causeway.applib.services.user.ImpersonatedUserHolder;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;

@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".ImpersonatedUserHolderDefault")
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
public class ImpersonatedUserHolderDefault implements ImpersonatedUserHolder {

    private static final String SESSION_KEY_IMPERSONATED_USER =
            ImpersonatedUserHolderDefault.class.getName() + "#userMemento";

    @Inject private Optional<KeyValueSessionStore> keyValueSessionStore = Optional.empty();

    @Override
    public boolean supportsImpersonation() {
        return keyValueSessionStore
            .map(KeyValueSessionStore::isSessionAvailable)
            .orElse(false);
    }

    @Override
    public void setUserMemento(final UserMemento userMemento) {
        keyValueSessionStore
            .ifPresent(store->store.put(SESSION_KEY_IMPERSONATED_USER, userMemento));
    }

    @Override
    public Optional<UserMemento> getUserMemento() {
        return keyValueSessionStore
            .flatMap(store->store.lookupAs(SESSION_KEY_IMPERSONATED_USER, UserMemento.class));
    }

    @Override
    public void clearUserMemento() {
        keyValueSessionStore
            .ifPresent(store->store.clear(SESSION_KEY_IMPERSONATED_USER));
    }

}
