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
package org.apache.causeway.core.security.authentication;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Sets;

import static org.apache.causeway.commons.internal.base._NullSafe.stream;

public abstract class AuthenticationRequestAbstract
implements AuthenticationRequest {

    private final String name;
    private final Set<String> roles = _Sets.newHashSet();

    public AuthenticationRequestAbstract(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Stream<String> streamRoles() {
        return roles.stream();
    }

    /**
     * Add a role to associate with the account.
     *
     * <p>
     * Null or empty roles are ignored.
     * </p>
     *
     * @apiNote this is not part of the {@link AuthenticationRequest} API.
     *
     * @param role
     * @since 2.0
     */
    public void addRole(final String role) {
        if(_Strings.isNullOrEmpty(role)) {
            return; // ignore
        }
        this.roles.add(role);
    }

    public void addRoles(final @Nullable Collection<String> roles) {
        stream(roles)
                .forEach(this::addRole);
    }

}
