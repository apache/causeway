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
package org.apache.causeway.security.spring.authconverters;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.user.UserMemento;

import lombok.NonNull;

/**
 * Applies if {@link Authentication} holds a principal of type {@link String}.
 */
@Component
@javax.annotation.Priority(PriorityPrecedence.LATE + 100)
public class AuthenticationConverterOfStringPrincipal
extends AuthenticationConverter.Abstract<String> {

    public AuthenticationConverterOfStringPrincipal() {
        super(String.class);
    }

    @Override
    protected UserMemento convertPrincipal(final @NonNull String name) {
        return name.isEmpty()
                ? null
                : UserMemento.ofNameAndRoleNames(name);
    }
}
