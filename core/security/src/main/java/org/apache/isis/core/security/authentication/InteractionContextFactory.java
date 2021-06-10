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

import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.applib.services.user.UserMemento;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class InteractionContextFactory {

    public static InteractionContext anonymous() {
        return InteractionContext
                .ofUserWithSystemDefaults(
                        UserMemento
                            .system());
    }

    public static InteractionContext health() {
        return InteractionContext
                .ofUserWithSystemDefaults(
                        UserMemento
                            .ofNameAndRoleNames(
                                "__health", // user name
                                "__health-role")); // role(s)
    }

    public static InteractionContext testing() {
        return InteractionContext
                .ofUserWithSystemDefaults(
                        UserMemento
                            .ofName("prototyping"));
    }

    public static InteractionContext testing(final @NonNull String authenticationCode) {
        return InteractionContext
                .ofUserWithSystemDefaults(
                        UserMemento
                            .ofName("prototyping")
                            .withAuthenticationCode(authenticationCode));
    }


}
