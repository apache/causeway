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
package org.apache.isis.applib.services.user;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

/**
 * Immutable serializable value holding details about a user and its roles.
 *
 * @since 1.x revised for 2.0 {@index}
 */
@DomainObject(objectType = "isis.applib.UserMemento")
public final class UserMemento implements Serializable {

    private static final long serialVersionUID = 7190090455587885367L;
    private static final UserMemento SYSTEM_USER = UserMemento.ofName("__system");

    // -- FACTORIES

    /**
     * The framework's internal user with unrestricted privileges.
     */
    public static UserMemento system() {
        return SYSTEM_USER;
    }


    /**
     * Creates a new user with the specified name and no roles.
     */
    public static UserMemento ofName(
            final @NonNull String name) {
        return new UserMemento(name, Stream.empty());
    }

    /**
     * Creates a new user with the specified name and assigned roles.
     */
    public static UserMemento ofNameAndRoles(
            final @NonNull String name,
            final RoleMemento... roles) {
        return new UserMemento(name, Stream.of(roles));
    }

    /**
     * Creates a new user with the specified name and assigned role names.
     */
    public static UserMemento ofNameAndRoleNames(
            final @NonNull String name,
            final String... roleNames) {
        return new UserMemento(name, Stream.of(roleNames).map(RoleMemento::new));
    }

    /**
     * Creates a new user with the specified name and assigned role names.
     */
    public static UserMemento ofNameAndRoleNames(
            final @NonNull String name,
            final @NonNull Stream<String> roleNames) {
        return new UserMemento(name, roleNames.map(RoleMemento::new));
    }

    // -- CONSTRUCTOR

    /**
     * Creates a new user with the specified name and assigned roles.
     */
    public UserMemento(final String name, final @NonNull Stream<RoleMemento> roles) {
        if (_Strings.isEmpty(name)) {
            throw new IllegalArgumentException("Name not specified");
        }
        this.name = name;
        this.roles = roles.collect(_Lists.toUnmodifiable());
    }

    public String title() {
        return name;
    }

    /**
     * The user's login name.
     */
    @PropertyLayout(sequence = "1.1")
    @Getter
    private final String name;

    /**
     * The roles associated with this user.
     */
    @CollectionLayout(sequence = "1.2")
    private final List<RoleMemento> roles;
    public List<RoleMemento> getRoles() {
        return roles;
    }

    /**
     * Determine if the specified name is this user.
     *
     * <p>
     *
     * @return true if the names match (is case sensitive).
     */
    public boolean isCurrentUser(final @Nullable String userName) {
        return name.equals(userName);
    }

    public Stream<String> streamRoleNames() {
        return roles.stream()
                .map(RoleMemento::getName);
    }

    public boolean hasRoleName(final @Nullable String roleName) {
        return streamRoleNames().anyMatch(myRoleName->myRoleName.equals(roleName));
    }

    // -- TO STRING, EQUALS, HASHCODE

    @Override
    public String toString() {
        val rolesStringified = roles.stream()
        .map(RoleMemento::getName)
        .collect(Collectors.joining(", "));
        return "User [name=" + getName() + ", roles=" + rolesStringified + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        return isEqualsTo((UserMemento) obj);
    }

    private boolean isEqualsTo(final UserMemento other) {
        if(!Objects.equals(this.getName(), other.getName())) {
            return false;
        }
        return Objects.equals(this.getRoles(), other.getRoles());
    }

    @Override
    public int hashCode() {
        return getName().hashCode(); // its good enough to hash on just the user's name
    }


}

