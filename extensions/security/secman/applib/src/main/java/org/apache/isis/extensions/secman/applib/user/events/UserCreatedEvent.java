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
package org.apache.isis.extensions.secman.applib.user.events;

import org.apache.isis.extensions.secman.applib.user.dom.AccountType;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUser;

import lombok.NonNull;
import lombok.Value;

/**
 * SecMan fires this event when a new user entity just got persisted.
 * <p>
 * Users may subscribe to this event in order to apply business
 * logic to the newly created user. eg. add default roles
 * <p>
 * <pre>
 * &#64;Component
 * public class Listener {
 *     &#64;EventListener(UserCreatedEvent.class)
 *     public void listenOn(UserCreatedEvent<String> event) {
 *         // business logic ...
 *     }
 * }
 *
 * </pre>
 *
 * @since 2.0 {@index}
 */
@Value(staticConstructor="of")
public class UserCreatedEvent {

    @NonNull private ApplicationUser user;

    // -- SHORTCUTS

    public AccountType getAccountType() {
        return user.getAccountType();
    }

    public String getUserName() {
        return user.getUsername();
    }

    public boolean isDelegated() {
        return getAccountType()!=null && getAccountType().isDelegated();
    }

}
