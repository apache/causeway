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
package org.apache.causeway.applib.services.user;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

import jakarta.inject.Named;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.locale.UserLocale;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.sudo.SudoService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;

import lombok.Builder;

/**
 * Immutable serializable value holding details about a user and its roles.
 *
 * @since 1.x revised for 2.0 {@index}
 */
@Named(UserMemento.LOGICAL_TYPE_NAME)
@DomainObject(nature = Nature.VIEW_MODEL)
@DomainObjectLayout(titleUiEvent = UserMemento.TitleUiEvent.class)
@Builder(builderMethodName = "builderInternal")
public record UserMemento(
    /**
     * The user's login name.
     */
    @Property
    @PropertyLayout(fieldSetId = "identity", sequence = "1", describedAs = "user's login name")
    @NonNull String name,
    
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(fieldSetId = "details", sequence = "1")
    @Nullable String realName,
    
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(fieldSetId = "details", sequence = "2")
    @Nullable URL avatarUrl,

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(fieldSetId = "regional", sequence = "1")
    @Nullable Locale languageLocale,

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(fieldSetId = "regional", sequence = "2")
    @Nullable Locale numberFormatLocale,

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(fieldSetId = "regional", sequence = "3")
    @Nullable Locale timeFormatLocale,

    /**
     * To support external security mechanisms such as keycloak,
     * where the validity of the session is defined by headers in the request.
     */
    @Property
    @PropertyLayout(fieldSetId = "security", sequence = "1")
    @NonNull AuthenticationSource authenticationSource,
    
    @Property
    @PropertyLayout(fieldSetId = "security", sequence = "3", named = "impersonating")
    boolean isImpersonating,

    /**
     * Indicates which tenancy (or tenancies) this user has access to.
     * <p>
     * The interpretation of this token is implementation-specific.
     */
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(fieldSetId = "security", sequence = "2", 
        describedAs = "tenancy (or tenancies) this user has access to")
    @Nullable String multiTenancyToken,
    
    /**
     * A unique code given to this user during authentication.
     * <p>
     * This can be used to confirm that the user has been authenticated.
     * It should return an empty string {@literal ""}
     * if this is an anonymous (unauthenticated) user.
     */
    @Property
    @PropertyLayout(hidden = Where.EVERYWHERE)
    @NonNull String authenticationCode,
    
    /**
     * The roles associated with this user.
     */
    @Collection
    @CollectionLayout(sequence = "1", describedAs = "roles associated with this user")
    @NonNull Can<RoleMemento> roles
    
    ) implements Serializable {

    public enum AuthenticationSource {
        DEFAULT,
        /**
         * Instructs the <code>AuthenticationManager</code>
         * to <i>not</i> cache this session in its internal map of sessions by validation code,
         * and therefore to ignore this aspect when considering if an {@link InteractionContext} is valid or not.
         */
        EXTERNAL;
        public boolean isExternal() { return this == EXTERNAL; }
    }
    
    public static class TitleUiEvent extends CausewayModuleApplib.TitleUiEvent<UserMemento> {}
    public static final String LOGICAL_TYPE_NAME = CausewayModuleApplib.NAMESPACE + ".UserMemento";

    /** Also used by the wicket-viewer and its AuthorizeInstantiation(...) annotations;
     *  the actual value is arbitrary; however, we use namespace style to clarify the origin*/
    public static final String AUTHORIZED_USER_ROLE = "org.apache.causeway.security.AUTHORIZED_USER_ROLE";

    private static final UserMemento SYSTEM_USER = UserMemento.ofName("__system");
    private static final String DEFAULT_AUTH_VALID_CODE = "";

    // -- FACTORIES

    /**
     * The framework's internal user with unrestricted privileges.
     */
    public static UserMemento system() { return SYSTEM_USER; }

    /**
     * Creates a new user with the specified name and no roles.
     */
    public static UserMemento ofName(final @NonNull String name) {
        return builder(name)
                .roles(Can.empty())
                .build();
    }

    /**
     * Creates a new user with the specified name and assigned roles.
     */
    public static UserMemento ofNameAndRoles(
            final @NonNull String name,
            final RoleMemento... roles) {
        return builder(name)
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
        return builder(name)
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
            var userMemento = Objects.requireNonNull(ev.getSource());
            ev.setTitle(userMemento.nameFormatted());
        }
    }

    // -- PROPERTIES

    @Programmatic
    public String nameFormatted() {
        return isImpersonating()
                ? String.format("%s 👻", name)
                : name;
    }

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
    @Programmatic
    public boolean isCurrentUser(final @Nullable String userName) {
        return name.equals(userName);
    }

    @Programmatic
    public Stream<String> streamRoleNames() {
        return roles.stream()
            .map(RoleMemento::name);
    }

    @Programmatic
    public boolean hasRoleName(final @Nullable String roleName) {
        return streamRoleNames().anyMatch(myRoleName->myRoleName.equals(roleName));
    }

    /**
     * Whether this {@link UserMemento}'s {@link UserMemento#roles() roles} contains the {@link SudoService}'s
     * {@link SudoService#ACCESS_ALL_ROLE ACCESS_ALL_ROLE} role (meaning that security checks are disabled).
     */
    @Programmatic
    public boolean hasSudoAccessAllRole() {
        return roles.contains(SudoService.ACCESS_ALL_ROLE);
    }

    // -- UTILITY

    @Programmatic
    public UserMementoBuilder asBuilder() {
        //XXX update whenever new fields are added
        return UserMemento.builderInternal()
                .name(name)
                .authenticationCode(authenticationCode)
                .authenticationSource(authenticationSource)
                .avatarUrl(avatarUrl)
                .languageLocale(languageLocale)
                .numberFormatLocale(numberFormatLocale)
                .timeFormatLocale(timeFormatLocale)
                .isImpersonating(isImpersonating)
                .realName(realName)
                .multiTenancyToken(multiTenancyToken)
                .roles(roles);
    }

    @Programmatic
    public UserLocale asUserLocale() {
        var main = languageLocale!=null
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
    
    public static UserMementoBuilder builder(final String name) {
        if (_Strings.isEmpty(name)) {
            throw new IllegalArgumentException("Name not specified");
        }
        return UserMemento.builderInternal()
                .name(name)
                .authenticationSource(AuthenticationSource.DEFAULT)
                .authenticationCode(DEFAULT_AUTH_VALID_CODE)
                .roles(Can.empty());
    }

    // -- WITHERS
    
    @Programmatic public UserMemento withRealName(String realName) {
        return asBuilder().realName(realName).build();
    }
    @Programmatic public UserMemento withAvatarUrl(URL avatarUrl) {
        return asBuilder().avatarUrl(avatarUrl).build();
    }
    @Programmatic public UserMemento withImpersonating(boolean impersonating) {
        return asBuilder().isImpersonating(impersonating).build();
    }
    @Programmatic public UserMemento withAuthenticationCode(String authenticationCode) {
        return asBuilder().authenticationCode(authenticationCode).build();
    }
    @Programmatic public UserMemento withAuthenticationSource(AuthenticationSource authenticationSource) {
        return asBuilder().authenticationSource(authenticationSource).build();
    }
    @Programmatic public UserMemento withMultiTenancyToken(String multiTenancyToken) {
        return asBuilder().multiTenancyToken(multiTenancyToken).build();
    }
    @Programmatic public UserMemento withLanguageLocale(Locale languageLocale) {
        return asBuilder().languageLocale(languageLocale).build();
    }
    @Programmatic public UserMemento withNumberFormatLocale(Locale numberFormatLocale) {
        return asBuilder().numberFormatLocale(numberFormatLocale).build();
    }
    @Programmatic public UserMemento withTimeFormatLocale(Locale timeFormatLocale) {
        return asBuilder().timeFormatLocale(timeFormatLocale).build();
    }
    
    // -- OBJECT CONTRACT
    
    @Override
    public final boolean equals(Object obj) {
        return (obj instanceof UserMemento other)
            ? isImpersonating == other.isImpersonating
                && Objects.equals(name, other.name)
                && Objects.equals(authenticationSource, other.authenticationSource)
                && Objects.equals(multiTenancyToken, other.multiTenancyToken)
                && Objects.equals(authenticationCode, other.authenticationCode)
                && Objects.equals(roles, other.roles)
            : false;
    }
    
    @Override
    public final int hashCode() {
        return Objects.hash(isImpersonating, name, authenticationSource, multiTenancyToken, authenticationCode, roles);
    }
    
    // -- HELPER
    
    private void readObject(final ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
    }
    
    // -- DEPRECATIONS
    
    /** @deprecated use {@link #name()} instead */
    @Programmatic @Deprecated public String getName() { return name(); }
    /** @deprecated use {@link #authenticationCode()} instead */
    @Programmatic @Deprecated public String getAuthenticationCode() { return authenticationCode(); }
    /** @deprecated use {@link #authenticationSource()} instead */
    @Programmatic @Deprecated public AuthenticationSource getAuthenticationSource() { return authenticationSource(); }
    /** @deprecated use {@link #avatarUrl()} instead */
    @Programmatic @Deprecated public URL getAvatarUrl() { return avatarUrl(); }
    /** @deprecated use {@link #realName()} instead */
    @Programmatic @Deprecated public String getRealName() { return realName(); }
    /** @deprecated use {@link #languageLocale()} instead */
    @Programmatic @Deprecated public Locale getLanguageLocale() { return languageLocale(); }
    /** @deprecated use {@link #numberFormatLocale()} instead */
    @Programmatic @Deprecated public Locale getnumberFormatLocale() { return numberFormatLocale(); }
    /** @deprecated use {@link #timeFormatLocale()} instead */
    @Programmatic @Deprecated public Locale getTimeFormatLocale() { return timeFormatLocale(); }
    /** @deprecated use {@link #multiTenancyToken()} instead */
    @Programmatic @Deprecated public String getMultiTenancyToken() { return multiTenancyToken(); }
    /** @deprecated use {@link #roles()} instead */
    @Programmatic @Deprecated public List<RoleMemento> getRoles() { return roles().toList(); }

}
