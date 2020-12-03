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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.isis.applib.clock.VirtualClock;
import org.apache.isis.applib.services.user.RoleMemento;
import org.apache.isis.applib.services.user.UserMemento;
import org.apache.isis.applib.util.ToString;
import org.apache.isis.commons.collections.Can;

import lombok.Getter;
import lombok.NonNull;

public abstract class AuthenticationSessionAbstract 
implements AuthenticationSession, Serializable {

    private static final long serialVersionUID = 1L;

    protected static final String DEFAULT_AUTH_VALID_CODE = "";
    
    // -- Constructor, fields

    @Getter(onMethod_ = {@Override}) @NonNull 
    private final VirtualClock clock;
    
    @Getter(onMethod_ = {@Override}) @NonNull 
    private final UserMemento user;
    
    @Getter(onMethod_ = {@Override}) @NonNull 
    private final String userName;
    
    @Getter(onMethod_ = {@Override}) @NonNull
    private final Can<String> roles;
    
    @Getter(onMethod_ = {@Override}) @NonNull
    private final String validationCode;

    @Getter(onMethod_ = {@Override}) @NonNull
    private final MessageBroker messageBroker;
    
    private final Map<String, Object> attributeByName = new HashMap<String, Object>();

    public AuthenticationSessionAbstract(
            @NonNull final UserMemento user) {
        this(VirtualClock.system(), user, DEFAULT_AUTH_VALID_CODE);
    }

    public AuthenticationSessionAbstract(
            @NonNull final VirtualClock clock,
            @NonNull final UserMemento user,
            @NonNull final String validationCode) {

        this.clock = clock;
        this.user = user;
        this.userName = user.getName();
        this.roles = Can.ofCollection(user.getRoles())
                .map(RoleMemento::getName);

        this.validationCode = validationCode;
        this.messageBroker = new MessageBroker();
        // nothing to do
    }

    // -- User Name

    @Override
    public boolean hasUserNameOf(final String userName) {
        return Objects.equals(userName, getUserName());
    }

    // -- Roles

    @Override
    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    // -- Attributes

    @Override
    public Object getAttribute(final String attributeName) {
        return attributeByName.get(attributeName);
    }

    @Override
    public void setAttribute(final String attributeName, final Object attribute) {
        attributeByName.put(attributeName, attribute);
    }


    // -- toString

    private static final ToString<AuthenticationSessionAbstract> toString = ToString
            .toString("name", AuthenticationSessionAbstract::getUserName)
            .thenToString("code", AuthenticationSessionAbstract::getValidationCode);

    @Override
    public String toString() {
        return toString.toString(this);
    }

}
