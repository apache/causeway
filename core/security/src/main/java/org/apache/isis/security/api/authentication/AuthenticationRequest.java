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

package org.apache.isis.security.api.authentication;

import java.util.Collection;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import static org.apache.isis.core.commons.internal.base._NullSafe.stream;

public interface AuthenticationRequest {

    /**
     * Account's name. 
     * @return nullable
     */
    public @Nullable String getName();

    /**
     * Account's roles as Stream.
     * @return non-null
     * @since 2.0
     */
    public Stream<String> streamRoles();

    /**
     * Add a role to associate with the account. Null or empty roles are ignored.
     * @param role
     * @since 2.0
     */
    public void addRole(@Nullable String role);

    /**
     * Add a roles to associate with the account. Null or empty roles are ignored.
     * @param roles
     * @since 2.0
     */
    public default void addRoles(@Nullable Collection<String> roles) {
        stream(roles)
        .forEach(this::addRole);
    }

}
