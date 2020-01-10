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

package org.apache.isis.applib.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.isis.applib.annotation.MemberOrder;

/**
 * Details, obtained from the container, about the user and his roles.
 * Read-only.
 */
public final class UserMemento {

    // -- constructors
    /**
     * Creates a new user with the specified name and no roles.
     */
    public UserMemento(final String name) {
        this(name, new RoleMemento[0]);
    }

    /**
     * Creates a new user with the specified name and assigned roles.
     */
    public UserMemento(final String name, final RoleMemento... roles) {
        this(name, Arrays.asList(roles));
    }

    /**
     * Creates a new user with the specified name and assigned roles.
     */
    public UserMemento(final String name, final List<RoleMemento> roles) {
        if (name == null) {
            throw new IllegalArgumentException("Name not specified");
        }
        this.name = name;
        this.roles.addAll(roles);
    }


    // -- title
    public String title() {
        return name;
    }



    // -- name, NameType

    // {{ (User) Name, isCurrentUser
    private final String name;

    /**
     * The user's login name.
     */
    @MemberOrder(sequence = "1.1")
    public String getName() {
        return name;
    }

    public static class NameType {

        private NameType() {}

        public static class Meta {

            public static final int MAX_LEN = 50;

            private Meta() {}

        }

    }



    // -- isCurrentUser
    /**
     * Determine if the specified name is this user.
     *
     * <p>
     *
     * @return true if the names match (is case sensitive).
     */
    public boolean isCurrentUser(final String userName) {
        if (userName == null) {
            throw new IllegalArgumentException("no user name provided");
        }
        return name.equals(userName);
    }



    // -- roles
    private final List<RoleMemento> roles = new ArrayList<RoleMemento>();

    /**
     * The roles associated with this user.
     */
    @MemberOrder(sequence = "1.1")
    public List<RoleMemento> getRoles() {
        return roles;
    }

    /**
     * Determines if the user fulfills the specified role.
     *
     * @param role  the role to search for, regular expressions are allowed
     */
    public boolean hasRole(final RoleMemento role) {
        return hasRole(role.getName());
    }

    /**
     * Determines if the user fulfills the specified role. Roles are compared
     * lexically by role name.
     */
    public boolean hasRole(final String roleName) {
        for (final RoleMemento role : roles) {
            if (role.getName().matches(roleName)) {
                return true;
            }
        }
        return false;
    }



    // -- toString
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        for (final RoleMemento role : roles) {
            buf.append(role.getName()).append(" ");
        }
        return "User [name=" + getName() + ",roles=" + buf.toString() + "]";
    }



}
