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
package org.apache.causeway.security.simple.realm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.commons.internal.base._Strings;

import lombok.Value;
import lombok.experimental.Accessors;

@Component
public class SimpleRealm {

    public enum Grant {
        NONE,
        READ,
        CHANGE;
        public boolean grantsRead() {
            return this==READ
                    || this==CHANGE;
        }
        public boolean grantsChange() {
            return this==CHANGE;
        }
        public static Grant valueOf(@Nullable final Grant grant) {
            return grant!=null
                    ? grant
                    : Grant.NONE;
        }
    }

    @Value @Accessors(fluent=true)
    public static class Role {
        String name;
        Function<Identifier, Grant> grants;
    }

    @Value @Accessors(fluent=true)
    public static class User {
        String name;
        String encryptedPass;
        List<Role> roles;
    }

    final Map<String, Role> roles = new HashMap<>();
    final Map<String, User> users = new HashMap<>();

    public SimpleRealm addRole(final String name, final Function<Identifier, Grant> grants) {
        roles.put(name, new Role(name, grants));
        return this;
    }
    public SimpleRealm addUser(final String name, final String pass, final List<String> roleNames) {
        users.put(name, new User(name, pass, roleNames.stream().map(roles::get).collect(Collectors.toList())));
        return this;
    }

    public final Optional<Role> lookupRoleByName(@Nullable final String roleName) {
        return _Strings.isNullOrEmpty(roleName)
                ? Optional.empty()
                : Optional.ofNullable(roles.get(roleName));
    }

    public final Optional<User> lookupUserByName(@Nullable final String userName) {
        return _Strings.isNullOrEmpty(userName)
                ? Optional.empty()
                : Optional.ofNullable(users.get(userName));
    }

}
