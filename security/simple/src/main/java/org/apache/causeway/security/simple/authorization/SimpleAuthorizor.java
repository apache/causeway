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
package org.apache.causeway.security.simple.authorization;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.core.security.authorization.Authorizor;
import org.apache.causeway.security.simple.CausewayModuleSecuritySimple;
import org.apache.causeway.security.simple.realm.SimpleRealm;
import org.apache.causeway.security.simple.realm.SimpleRealm.Grant;

import lombok.RequiredArgsConstructor;

/**
 * Simple in-memory {@link Authorizor} implementation.
 *
 * @since 2.x {@index}
 */
@Service
@Named(CausewayModuleSecuritySimple.NAMESPACE + ".SimpleAuthorizor")
@javax.annotation.Priority(PriorityPrecedence.LATE - 10) // ensure earlier than bypass
@Qualifier("Simple")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class SimpleAuthorizor implements Authorizor {

    protected final SimpleRealm realm;

    @Override
    public boolean isVisible(final InteractionContext ctx, final Identifier identifier) {
        return roles(ctx.getUser()).stream()
            .anyMatch(role->Grant.valueOf(role.grants().apply(identifier)).grantsRead());
    }

    @Override
    public boolean isUsable(final InteractionContext ctx, final Identifier identifier) {
        return roles(ctx.getUser()).stream()
            .anyMatch(role->Grant.valueOf(role.grants().apply(identifier)).grantsChange());
    }

    protected List<SimpleRealm.Role> roles(final UserMemento userMemento){
        var roles = realm.lookupUserByName(userMemento.getName())
            .map(SimpleRealm.User::roles)
            .orElseGet(List::of);
        return roles;
    }

}
