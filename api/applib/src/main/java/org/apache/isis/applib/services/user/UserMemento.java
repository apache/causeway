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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.locale.UserLocale;
import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.With;
import lombok.val;

/**
 * Immutable serializable value holding details about a user and its roles.
 *
 * @since 1.x revised for 2.0 {@index}
 */
@DomainObject(
        nature = Nature.VIEW_MODEL,
        logicalTypeName = UserMemento.LOGICAL_TYPE_NAME
)
@DomainObjectLayout(
        titleUiEvent = UserMemento.TitleUiEvent.class
)
@lombok.Value @lombok.Builder
public class UserMemento implements Serializable {

    public static class TitleUiEvent extends IsisModuleApplib.TitleUiEvent<UserMemento> {}

    private void readObject(final ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
    }

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

    public static class UiSubscriber {
        @Order(PriorityPrecedence.LATE)
        @EventListener(UserMemento.TitleUiEvent.class)
        public void on(final UserMemento.TitleUiEvent ev) {
            val userMemento = ev.getSource();
            assert userMemento != null;
            val title = String.format("%s %s", userMemento.getName(), userMemento.isImpersonating() ? " (impersonating)" : "");
            ev.setTitle(title);
        }
    }

    // -- PROPERTIES

    /**
     * The user's login name.
     */
    @Property
    @PropertyLayout(fieldSetId = "identity", sequence = "1")
    @Getter
    @NonNull
    String name;

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(fieldSetId = "details", sequence = "1")
    @Getter @With(onMethod_ = {@Programmatic})
    @Nullable
    String realName;

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(fieldSetId = "details", sequence = "2")
    @Getter @With(onMethod_ = {@Programmatic})
    @Nullable
    URL avatarUrl;

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(fieldSetId = "regional", sequence = "1")
    @Getter @With(onMethod_ = {@Programmatic})
    @Nullable
    Locale languageLocale;

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(fieldSetId = "details", sequence = "2")
    @Getter @With(onMethod_ = {@Programmatic})
    @Nullable
    Locale numberFormatLocale;

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(fieldSetId = "details", sequence = "3")
    @Getter @With(onMethod_ = {@Programmatic})
    @Nullable
    Locale timeFormatLocale;

    /**
     * To support external security mechanisms such as keycloak,
     * where the validity of the session is defined by headers in the request.
     */
    @Property
    @PropertyLayout(fieldSetId = "security", sequence = "1")
    @Getter @Builder.Default @With(onMethod_ = {@Programmatic})
    @NonNull
    AuthenticationSource authenticationSource = AuthenticationSource.DEFAULT;


    public enum AuthenticationSource {
        DEFAULT,
        /**
         * Instructs the <code>AuthenticationManager</code>
         * to <i>not</i> cache this session in its internal map of sessions by validation code,
         * and therefore to ignore this aspect when considering if an {@link InteractionContext} is valid or not.
         */
        EXTERNAL;

        public boolean isExternal() {
            return this == EXTERNAL;
        }
    }


    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(fieldSetId = "security", sequence = "2")
    @Getter @Builder.Default @With(onMethod_ = {@Programmatic})
    boolean impersonating = false;


    /**
     * Indicates which tenancy (or tenancies) this user has access to.
     *
     * <p>
     * The interpretation of this token is implementation-specific.
     * </p>
     */
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(fieldSetId = "security", sequence = "3")
    @Getter @With(onMethod_ = {@Programmatic})
    @Nullable
    String multiTenancyToken;


    private static final String DEFAULT_AUTH_VALID_CODE = "";

    /**
     * A unique code given to this user during authentication.
     * <p>
     * This can be used to confirm that the user has been authenticated.
     * It should return an empty string {@literal ""}
     * if this is an anonymous (unauthenticated) user.
     */
    @Property(hidden = Where.EVERYWHERE)
    @Getter @Builder.Default @With(onMethod_ = {@Programmatic})
    @NonNull
    String authenticationCode = DEFAULT_AUTH_VALID_CODE;

    /**
     * The roles associated with this user.
     */
    @Collection
    @CollectionLayout(sequence = "1")
    public List<RoleMemento> getRoles() {
        return roles.toList();
    }

    @Programmatic
    @Builder.Default
    Can<RoleMemento> roles = Can.empty();


    @Programmatic
    public UserMemento withRoleAdded(final String role) {
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




    // -- UTILITY

    @Programmatic
    public UserMementoBuilder asBuilder() {
        //XXX update whenever new fields are added
        return UserMemento.builder()
                .name(name)
                .authenticationCode(authenticationCode)
                .authenticationSource(authenticationSource)
                .avatarUrl(avatarUrl)
                .languageLocale(languageLocale)
                .numberFormatLocale(numberFormatLocale)
                .timeFormatLocale(timeFormatLocale)
                .impersonating(impersonating)
                .realName(realName)
                .multiTenancyToken(multiTenancyToken)
                .roles(roles);
    }

    @Programmatic
    public UserLocale asUserLocale() {
        val main = languageLocale!=null
                ? languageLocale
                : Locale.getDefault();
        return UserLocale.builder()
                .languageLocale(main)
                .numberFormatLocale(numberFormatLocale!=null
                        ? numberFormatLocale
                        : main)
                .timeFormatLocale(timeFormatLocale!=null
                        ? timeFormatLocale
                        : main)
                .build();
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
