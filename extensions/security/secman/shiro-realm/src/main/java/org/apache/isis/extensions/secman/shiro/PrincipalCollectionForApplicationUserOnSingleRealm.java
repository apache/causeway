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
package org.apache.isis.extensions.secman.shiro;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.shiro.subject.PrincipalCollection;

import org.apache.isis.core.commons.internal.base._Casts;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @since 2.0
 */
@RequiredArgsConstructor(staticName = "of")
public class PrincipalCollectionForApplicationUserOnSingleRealm implements PrincipalCollection {

    private static final long serialVersionUID = -8778415093248238553L;
    
    @NonNull private final PrincipalForApplicationUser principal;
    @NonNull private final String realmName;
    
    @Override
    public Iterator<?> iterator() {
        return asList().iterator();
    }

    @Override
    public Object getPrimaryPrincipal() {
        return principal;
    }

    @Override
    public <T> T oneByType(Class<T> type) {
        if(type == PrincipalForApplicationUser.class) {
            return _Casts.uncheckedCast(principal);
        }
        return null;
    }

    @Override
    public <T> Collection<T> byType(Class<T> type) {
        if(type == PrincipalForApplicationUser.class) {
            return _Casts.uncheckedCast(asList());
        }
        return null;
    }

    @Override
    public List<?> asList() {
        return Collections.singletonList(principal);
    }

    @Override
    public Set<?> asSet() {
        return Collections.singleton(principal);
    }

    @Override
    public Collection<?> fromRealm(String realmName2) {
        if(realmName.equals(realmName2)) {
            return asList();
        }
        return Collections.emptyList();
    }

    @Override
    public Set<String> getRealmNames() {
        return Collections.singleton(realmName);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
    
}
