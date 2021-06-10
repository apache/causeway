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
import java.net.URL;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties.Authentication;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.With;

/**
 * Immutable serializable value holding details about a user and its roles.
 *
 * @since 1.x revised for 2.0 {@index}
 */
@DomainObject(logicalTypeName = UserMemento.LOGICAL_TYPE_NAME)
@lombok.Value @lombok.Builder
public final class UserMemento implements Serializable {

    public static final String LOGICAL_TYPE_NAME = IsisModuleApplib.NAMESPACE + ".UserMemento";

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

        return builderWithDefaults(name)
                .roles(Can.empty())
                .build();
    }

    /**
     * Creates a new user with the specified name and assigned roles.
     */
    public static UserMemento ofNameAndRoles(
            final @NonNull String name,
            final RoleMemento... roles) {

        return builderWithDefaults(name)
                .roles(Can.ofArray(roles))
                .build();
    }

    /**
     * Creates a new user with the specified name and assigned role names.
     */
    public static UserMemento ofNameAndRoleNames(
            final @NonNull String name,
            final String... roleNames) {
        return ofNameAndRoleNames(name, Stream.of(roleNames));
    }

    /**
     * Creates a new user with the specified name and assigned role names.
     */
    public static UserMemento ofNameAndRoleNames(
            final @NonNull String name,
            final @NonNull List<String> roleNames) {
        return ofNameAndRoleNames(name, roleNames.stream());
    }

    /**
     * Creates a new user with the specified name and assigned role names.
     */
    public static UserMemento ofNameAndRoleNames(
            final @NonNull String name,
            final @NonNull Stream<String> roleNames) {

        return builderWithDefaults(name)
                .roles(roleNames
                        .map(RoleMemento::new)
                        .collect(Can.toCan()))
                .build();
    }

    // -- UI TITLE

    public String title() {
        return name;
    }

    // -- PROPERTIES

    /**
     * The user's login name.
     */
    @Property
    @PropertyLayout(sequence = "1.1")
    @Getter
    private final @NonNull String name;

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(sequence = "1.2")
    @Getter @With(onMethod_ = {@Programmatic})
    private final @Nullable String realName;

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(sequence = "1.3")
    @Getter @With(onMethod_ = {@Programmatic})
    private final @Nullable URL avatarUrl;

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(sequence = "1.4")
    @Getter @Builder.Default @With(onMethod_ = {@Programmatic})
    private final boolean impersonating = false;

    /**
     * The roles associated with this user.
     */
    @Collection
    @CollectionLayout(sequence = "1.4")
    @Getter @Builder.Default
    private final Can<RoleMemento> roles = Can.empty();


    @Programmatic
    public UserMemento withRoleAdded(String role) {
        return asBuilder()
        .roles(roles.add(new RoleMemento(role)))
        .build();
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

    @Programmatic
    public Stream<String> streamRoleNames() {
        return roles.stream()
                .map(RoleMemento::getName);
    }

    @Programmatic
    public boolean hasRoleName(final @Nullable String roleName) {
        return streamRoleNames().anyMatch(myRoleName->myRoleName.equals(roleName));
    }

    // -- AUTHENTICATION

    /**
     * To support external security mechanisms such as keycloak,
     * where the validity of the session is defined by headers in the request.
     */
    @Property
    @PropertyLayout(sequence = "2.0")
    @Getter @Builder.Default @With(onMethod_ = {@Programmatic})
    private final @NonNull AuthenticationSource authenticationSource = AuthenticationSource.DEFAULT;

    public enum AuthenticationSource {
        DEFAULT,
        /**
         * Instructs the {@link org.apache.isis.core.security.authentication.manager.AuthenticationManager} to not cache this session in its internal map of
         * sessions by validation code, and therefore to ignore this aspect when considering if an
         * {@link Authentication} is
         * {@link org.apache.isis.core.security.authentication.manager.AuthenticationManager#isSessionValid(Authentication) valid} or not.
         */
        EXTERNAL;

        public boolean isExternal() {
            return this == EXTERNAL;
        }
    }

    /**
     * A unique code given to this user during authentication.
     * <p>
     * This can be used to confirm that the user has been authenticated.
     * It should return an empty string {@literal ""}
     * if this is an anonymous (unauthenticated) user.
     */
    @Property
    @PropertyLayout(sequence = "2.1")
    @Getter @Builder.Default @With(onMethod_ = {@Programmatic})
    private final @NonNull String authenticationCode = "";


    // -- UTILITY

    @Programmatic
    public UserMementoBuilder asBuilder() {
        //XXX update whenever new fields are added
        return UserMemento.builder()
                .name(name)
                .authenticationCode(authenticationCode)
                .authenticationSource(authenticationSource)
                .avatarUrl(avatarUrl)
                .impersonating(impersonating)
                .realName(realName)
                .roles(roles);
    }

    // -- HELPER

    private static UserMementoBuilder builderWithDefaults(final String name) {
        if (_Strings.isEmpty(name)) {
            throw new IllegalArgumentException("Name not specified");
        }
        // actually using @Builder.Default on the fields to set builder defaults
        return UserMemento.builder()
                .name(name);
    }


}

