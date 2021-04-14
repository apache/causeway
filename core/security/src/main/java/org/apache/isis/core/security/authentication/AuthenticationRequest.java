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

package org.apache.isis.core.security.authentication;

import java.util.stream.Stream;

import javax.annotation.Nullable;

/**
 * Represents a request to authenticate the user identified by
 * {@link AuthenticationRequest#getName()}.
 *
 * <p>
 *     If successful, then the authentication mechanism is expected to add the
 *     {@link AuthenticationRequest#streamRoles() roles} to the resultant
 *     {@link Authentication} (obtained from the
 *     {@link org.apache.isis.applib.services.user.UserMemento} returned by
 *     {@link Authentication#getUser()}).
 * </p>
 *
 * @apiNote This is a framework internal class and so does not constitute a formal API.
 *
 * @since 1.x - refactored/renamed for v2 {@index}
 */
public interface AuthenticationRequest {

    /**
     * The name of the user to be authenticated by the configured
     * {@link Authenticator}.
     * Account's name.
     * @return nullable
     */
    @Nullable String getName();

    /**
     * The roles to be  Account's roles as Stream.
     * @return non-null
     * @since 2.0
     */
    Stream<String> streamRoles();

}
