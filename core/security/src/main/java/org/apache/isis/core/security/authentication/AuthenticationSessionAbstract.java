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
import java.util.stream.Stream;

import org.apache.isis.applib.clock.VirtualClock;
import org.apache.isis.applib.services.user.RoleMemento;
import org.apache.isis.applib.services.user.UserMemento;
import org.apache.isis.applib.util.ToString;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public abstract class AuthenticationSessionAbstract 
implements AuthenticationSession, Serializable {

    private static final long serialVersionUID = 1L;

    // -- Constructor, fields

    @Getter(onMethod_ = {@Override}) @NonNull 
    private final VirtualClock clock;
    
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
            @NonNull final String name, 
            @NonNull final String validationCode) {
        this(VirtualClock.system(), name, Stream.empty(), validationCode);
    }

    public AuthenticationSessionAbstract(
            @NonNull final VirtualClock clock,
            @NonNull final String userName, 
            @NonNull final Stream<String> roles, 
            @NonNull final String validationCode) {

        this.clock = clock;
        this.userName = userName;
        this.roles = Can.ofStream(roles
                .filter(_Strings::isNotEmpty)
                .distinct());

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

    // -- UserMemento

    private transient UserMemento user; 
    
    @Override
    public UserMemento getUser() {
        if(user==null) {
            val roleMementos = _Lists.<RoleMemento>newArrayList(roles.size());
            for (final String roleName : roles) {
                roleMementos.add(new RoleMemento(roleName));
            }
            user = new UserMemento(getUserName(), roleMementos);
        }
        return user;
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
