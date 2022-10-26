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
package org.apache.causeway.extensions.secman.applib.user.dom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.mixins.security.HasUsername;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureId;
import org.apache.causeway.applib.services.user.RoleMemento;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.applib.services.user.UserService;
import org.apache.causeway.applib.util.ObjectContracts;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.CausewayConfiguration.Extensions.Secman;
import org.apache.causeway.extensions.secman.applib.CausewayModuleExtSecmanApplib;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermission;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionMode;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionRepository;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionValueSet;
import org.apache.causeway.extensions.secman.applib.permission.spi.PermissionsEvaluationService;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole;
import org.apache.causeway.extensions.secman.applib.tenancy.dom.HasAtPath;

import lombok.val;
import lombok.experimental.UtilityClass;

/**
 * @since 2.0 {@index}
 */
@Named(ApplicationUser.LOGICAL_TYPE_NAME)
@DomainObject(
        autoCompleteRepository = ApplicationUserRepository.class,
        autoCompleteMethod = "findMatching"
        )
@DomainObjectLayout(
        titleUiEvent = ApplicationUser.TitleUiEvent.class,
        iconUiEvent = ApplicationUser.IconUiEvent.class,
        cssClassUiEvent = ApplicationUser.CssClassUiEvent.class,
        layoutUiEvent = ApplicationUser.LayoutUiEvent.class
)
public abstract class ApplicationUser
        implements HasUsername, HasAtPath, Comparable<ApplicationUser> {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleExtSecmanApplib.NAMESPACE + ".ApplicationUser";
    public static final String SCHEMA = CausewayModuleExtSecmanApplib.SCHEMA;
    public static final String TABLE = "ApplicationUser";

    @UtilityClass
    public static class Nq {
        public static final String FIND_BY_USERNAME = LOGICAL_TYPE_NAME + ".findByUsername";
        public static final String FIND_BY_EMAIL_ADDRESS = LOGICAL_TYPE_NAME + ".findByEmailAddress";
        public static final String FIND = LOGICAL_TYPE_NAME + ".find";
        public static final String FIND_BY_ATPATH = LOGICAL_TYPE_NAME + ".findByAtPath";
    }

    // -- UI & DOMAIN EVENTS

    public static class TitleUiEvent extends CausewayModuleExtSecmanApplib.TitleUiEvent<ApplicationUser> { }
    public static class IconUiEvent extends CausewayModuleExtSecmanApplib.IconUiEvent<ApplicationUser> { }
    public static class CssClassUiEvent extends CausewayModuleExtSecmanApplib.CssClassUiEvent<ApplicationUser> { }
    public static class LayoutUiEvent extends CausewayModuleExtSecmanApplib.LayoutUiEvent<ApplicationUser> { }

    public static abstract class PropertyDomainEvent<T> extends CausewayModuleExtSecmanApplib.PropertyDomainEvent<ApplicationUser, T> {}
    public static abstract class CollectionDomainEvent<T> extends CausewayModuleExtSecmanApplib.CollectionDomainEvent<ApplicationUser, T> {}

    @Inject private transient ApplicationUserRepository applicationUserRepository;
    @Inject private transient ApplicationPermissionRepository applicationPermissionRepository;
    @Inject private transient UserService userService;
    @Inject private transient PermissionsEvaluationService permissionsEvaluationService;
    @Inject private transient CausewayConfiguration config;

    @Programmatic protected ApplicationUserRepository getApplicationUserRepository() {
        return applicationUserRepository;
    }

    @Programmatic protected ApplicationPermissionRepository getApplicationPermissionRepository() {
        return applicationPermissionRepository;
    }

    @Programmatic protected UserService getUserService() {
        return userService;
    }

    /**
     * Optional service, if configured then is used to evaluate permissions within
     * {@link ApplicationPermissionValueSet#evaluate(ApplicationFeatureId, ApplicationPermissionMode)}
     * else will fallback to a default implementation.
     */
    @Programmatic protected PermissionsEvaluationService getPermissionsEvaluationService() {
        return permissionsEvaluationService;
    }

    @Programmatic protected Secman getSecmanConfig() {
        return config.getExtensions().getSecman();
    }





    @ObjectSupport public String title() {
        return getName();
    }
    @ObjectSupport public String iconName() {
        return getStatus().isUnlocked() ? "unlocked" : "locked";
    }


    // -- NAME

    @Property(
            domainEvent = Name.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            hidden= Where.OBJECT_FORMS,
            fieldSetId = "identity",
            sequence = "1"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Name {
        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @Name
    public String getName() {
        final StringBuilder buf = new StringBuilder();
        if(getFamilyName() != null) {
            if(getKnownAs() != null) {
                buf.append(getKnownAs());
            } else {
                buf.append(getGivenName());
            }
            buf.append(' ')
                    .append(getFamilyName())
                    .append(" (").append(getUsername()).append(')');
        } else {
            buf.append(getUsername());
        }
        return buf.toString();
    }


    // -- USERNAME

    @Property(
            domainEvent = Username.DomainEvent.class
    )
    @PropertyLayout(
            fieldSetId="identity",
            hidden = Where.PARENTED_TABLES,
            sequence = "1"
    )
    @HasUsername.Username
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Username {
        class DomainEvent extends PropertyDomainEvent<String> {}
        int MAX_LENGTH = HasUsername.Username.MAX_LENGTH;
        boolean NULLABLE = HasUsername.Username.NULLABLE;
        String ALLOWS_NULL = HasUsername.Username.ALLOWS_NULL;
    }
    @Override
    @Username
    public abstract String getUsername();
    public abstract void setUsername(String username);


    // -- FAMILY NAME

    @Property(
            domainEvent = FamilyName.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = FamilyName.MAX_LENGTH,
            optionality = Optionality.OPTIONAL
    )
    @PropertyLayout(
            fieldSetId = "name",
            hidden = Where.ALL_TABLES,
            sequence = "2.1"
    )
    @Parameter(
            maxLength = FamilyName.MAX_LENGTH,
            optionality = Optionality.OPTIONAL
    )
    @ParameterLayout(
            named="Family Name"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface FamilyName {
        class DomainEvent extends PropertyDomainEvent<String> {}
        int MAX_LENGTH = 120;
        boolean NULLABLE = true;
        String ALLOWS_NULL = "true";
    }
    @FamilyName
    public abstract String getFamilyName();
    public abstract void setFamilyName(String familyName);


    // -- GIVEN NAME

    @Property(
            domainEvent = GivenName.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = GivenName.MAX_LENGTH,
            optionality = Optionality.OPTIONAL
    )
    @PropertyLayout(
            fieldSetId = "name",
            hidden = Where.ALL_TABLES,
            sequence = "2.2"
    )
    @Parameter(
            maxLength = GivenName.MAX_LENGTH,
            optionality = Optionality.OPTIONAL
    )
    @ParameterLayout(
            named="Given Name"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface GivenName {
        class DomainEvent extends PropertyDomainEvent<String> {}
        int MAX_LENGTH = 120;
        boolean NULLABLE = true;
        String ALLOWS_NULL = "true";
    }
    @GivenName
    public abstract String getGivenName();
    public abstract void setGivenName(String givenName);


    // -- KNOWN AS

    @Property(
            domainEvent = KnownAs.KnownAsDomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = KnownAs.MAX_LENGTH,
            optionality = Optionality.OPTIONAL
    )
    @PropertyLayout(
            fieldSetId = "name",
            hidden = Where.ALL_TABLES,
            sequence = "2.3"
    )
    @Parameter(
            maxLength = KnownAs.MAX_LENGTH,
            optionality = Optionality.OPTIONAL
    )
    @ParameterLayout(
            named="Known As"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface KnownAs {
        class KnownAsDomainEvent extends PropertyDomainEvent<String> {}
        int MAX_LENGTH = 120;
        boolean NULLABLE = true;
        String ALLOWS_NULL = "true";
    }
    @KnownAs
    public abstract String getKnownAs();
    public abstract void setKnownAs(String knownAs);


    // -- EMAIL ADDRESS

    @Property(
            domainEvent = EmailAddress.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = EmailAddress.MAX_LENGTH,
            optionality = Optionality.OPTIONAL
    )
    @PropertyLayout(
            fieldSetName = "Contact Details",
            sequence = "3.1"
    )
    @Parameter(
            maxLength = EmailAddress.MAX_LENGTH,
            optionality = Optionality.OPTIONAL
    )
    @ParameterLayout(
            named = "Email"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface EmailAddress {
        class DomainEvent extends PropertyDomainEvent<String> {}
        int MAX_LENGTH = 120;
        boolean NULLABLE = true;
        String ALLOWS_NULL = "true";
    }
    @EmailAddress
    public abstract String getEmailAddress();
    public abstract void setEmailAddress(String emailAddress);


    // -- PHONE NUMBER

    @Property(
            domainEvent = PhoneNumber.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = PhoneNumber.MAX_LENGTH
    )
    @PropertyLayout(
            fieldSetName = "Contact Details",
            sequence = "3.2"
    )
    @Parameter(
            maxLength = PhoneNumber.MAX_LENGTH,
            optionality = Optionality.OPTIONAL
    )
    @ParameterLayout(
            named = "Phone"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface PhoneNumber {
        class DomainEvent extends PropertyDomainEvent<String> {}
        int MAX_LENGTH = 120;
        boolean NULLABLE = true;
        String ALLOWS_NULL = "true";
    }
    @PhoneNumber
    public abstract String getPhoneNumber();
    public abstract void setPhoneNumber(String phoneNumber);


    // -- FAX NUMBER

    @Property(
            domainEvent = FaxNumber.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = FaxNumber.MAX_LENGTH,
            optionality = Optionality.OPTIONAL
    )
    @PropertyLayout(
            fieldSetName = "Contact Details",
            hidden = Where.PARENTED_TABLES,
            sequence = "3.3"
    )
    @Parameter(
            maxLength = FaxNumber.MAX_LENGTH,
            optionality = Optionality.OPTIONAL
    )
    @ParameterLayout(
            named = "Fax"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface FaxNumber {
        class DomainEvent extends PropertyDomainEvent<String> {}
        int MAX_LENGTH = 120;
        boolean NULLABLE = true;
        String ALLOWS_NULL = "true";
    }
    @FaxNumber
    public abstract String getFaxNumber();
    public abstract void setFaxNumber(String faxNumber);


    // -- LOCALEs

    @Property(
            domainEvent = Locale.DomainEvent.class,
            editing = Editing.DISABLED, //  edit via update button
            optionality = Optionality.OPTIONAL
    )
    @PropertyLayout(
            fieldSetId = "regional"
    )
    @Parameter(
            optionality = Optionality.OPTIONAL
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Locale {
        class DomainEvent extends PropertyDomainEvent<java.util.Locale> {}
        boolean NULLABLE = true;
        String ALLOWS_NULL = "true";
    }


    @Property(
            domainEvent = Locale.DomainEvent.class
    )
    @Locale
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Language {
        class DomainEvent extends Locale.DomainEvent {}
        boolean NULLABLE = Locale.NULLABLE;
        String ALLOWS_NULL = Locale.ALLOWS_NULL;
    }
    @Language
    public abstract java.util.Locale getLanguage();
    public abstract void setLanguage(java.util.Locale locale);


    @Property(
            domainEvent = NumberFormat.DomainEvent.class
    )
    @Locale
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface NumberFormat {
        class DomainEvent extends Locale.DomainEvent {}
        boolean NULLABLE = Locale.NULLABLE;
        String ALLOWS_NULL = Locale.ALLOWS_NULL;
    }
    @NumberFormat
    public abstract java.util.Locale getNumberFormat();
    public abstract void setNumberFormat(java.util.Locale locale);


    @Property(
            domainEvent = TimeFormat.DomainEvent.class
    )
    @Locale
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TimeFormat {
        class DomainEvent extends Locale.DomainEvent {}
        boolean NULLABLE = Locale.NULLABLE;
        String ALLOWS_NULL = Locale.ALLOWS_NULL;
    }
    @TimeFormat
    public abstract java.util.Locale getTimeFormat();
    public abstract void setTimeFormat(java.util.Locale locale);



    @Property(
            domainEvent = AtPath.DomainEvent.class
    )
    @PropertyLayout(
            fieldSetId = "access",
            sequence = "4"
    )
    @HasAtPath.AtPath
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface AtPath {
        class DomainEvent extends PropertyDomainEvent<String> {}
        int MAX_LENGTH = HasAtPath.AtPath.MAX_LENGTH;
        boolean NULLABLE = HasAtPath.AtPath.NULLABLE;
        String ALLOWS_NULL = HasAtPath.AtPath.ALLOWS_NULL;
    }
    @Override
    @AtPath
    public abstract String getAtPath();
    public abstract void setAtPath(String atPath);



    // -- ACCOUNT TYPE

    @Property(
            domainEvent = AccountType.DomainEvent.class,
            editing = Editing.DISABLED,
            optionality = Optionality.MANDATORY
    )
    @PropertyLayout(
            fieldSetId = "access",
            sequence = "2"
    )
    @Parameter(
            optionality = Optionality.MANDATORY
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface AccountType {
        class DomainEvent extends PropertyDomainEvent<AccountType> {}
        boolean NULLABLE = false;
        String ALLOWS_NULL = "false";
    }
    @AccountType
    public abstract org.apache.causeway.extensions.secman.applib.user.dom.AccountType getAccountType();
    public abstract void setAccountType(org.apache.causeway.extensions.secman.applib.user.dom.AccountType accountType);


    // -- STATUS

    @Property(
            domainEvent = Status.DomainEvent.class,
            editing = Editing.DISABLED,
            optionality = Optionality.MANDATORY
    )
    @PropertyLayout(
            fieldSetId = "access",
            sequence = "1"
    )
    @Parameter(
            optionality = Optionality.MANDATORY
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Status {
        class DomainEvent extends PropertyDomainEvent<ApplicationUserStatus> {}
        boolean NULLABLE = false;
        String ALLOWS_NULL = "false";
    }
    @Status
    public abstract ApplicationUserStatus getStatus();
    public abstract void setStatus(ApplicationUserStatus disabled);


    // -- ENCRYPTED PASSWORD

    @Property(
            hidden = Where.EVERYWHERE
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface EncryptedPassword {
        int MAX_LENGTH = 255;
        boolean NULLABLE = true;
        String ALLOWS_NULL = "true";
    }
    @EncryptedPassword
    public abstract String getEncryptedPassword();
    public abstract void setEncryptedPassword(String encryptedPassword);
    @MemberSupport public boolean hideEncryptedPassword() {
        return !getApplicationUserRepository().isPasswordFeatureEnabled(this);
    }


    // -- HAS PASSWORD

    @Property(
            domainEvent = HasPassword.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            fieldSetId = "access",
            named = "Has password?",
            sequence = "3"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface HasPassword {
        class DomainEvent extends PropertyDomainEvent<Boolean> {}
    }

    @HasPassword
    public boolean isHasPassword() {
        return _Strings.isNotEmpty(getEncryptedPassword());
    }
    @MemberSupport public boolean hideHasPassword() {
        return !getApplicationUserRepository().isPasswordFeatureEnabled(this);
    }


    // ROLES

    @Collection(
            domainEvent = Roles.RolesDomainEvent.class
    )
    @CollectionLayout(
            defaultView="table",
            sequence = "1"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Roles {
        class RolesDomainEvent extends CollectionDomainEvent<ApplicationRole> {}

        @UtilityClass
        class Persistence {
            public static final String TABLE = "ApplicationUserRoles";
            public static final String JOIN_COLUMN = "userId";
            public static final String INVERSE_JOIN_COLUMN = "roleId";
        }
    }

    @Roles
    public abstract Set<ApplicationRole> getRoles();


    // -- PERMISSION SET

    // short-term caching
    private transient ApplicationPermissionValueSet cachedPermissionSet;

    @Programmatic public ApplicationPermissionValueSet getPermissionSet() {
        if(cachedPermissionSet != null) {
            return cachedPermissionSet;
        }
        List<ApplicationPermission> permissions;
        if(userService.isImpersonating()) {
            permissions = getApplicationPermissionRepository().findByUserMemento(userService.getUser());
        } else {
            permissions = getApplicationPermissionRepository().findByUser(this);
        }
        return cachedPermissionSet =
                new ApplicationPermissionValueSet(
                        _Lists.map(_Casts.uncheckedCast(permissions), ApplicationPermission.Functions.AS_VALUE),
                        getPermissionsEvaluationService());
    }



    // -- IS FOR SELF OR RUN AS ADMINISTRATOR

    @Programmatic public boolean isForSelf() {
        val currentUser = currentUser();
        val currentUserName = currentUser.getName();
        val forSelf = Objects.equals(getUsername(), currentUserName);
        return forSelf;
    }

    @Programmatic public boolean isRunAsAdministrator() {
        val currentUser = currentUser();
        val adminRoleSuffix = ":" + getAdminRoleName();
        for (final RoleMemento role : currentUser.getRoles()) {
            final String roleName = role.getName();
            // format is realmName:roleName.
            // since we don't know what the realm's name is (depends on its configuration in shiro.ini),
            // simply check that the last part matches the role name.
            if(roleName.endsWith(adminRoleSuffix)) {
                return true;
            }
        }
        return false;
    }

    @Programmatic public boolean isForSelfOrRunAsAdministrator() {
        return isForSelf()
                || isRunAsAdministrator();
    }

    // -- HELPERS

    @Programmatic public boolean isLocalAccount() {
        return getAccountType() == org.apache.causeway.extensions.secman.applib.user.dom.AccountType.LOCAL;
    }

    @Programmatic private String getAdminRoleName() {
        return getSecmanConfig().getSeed().getAdmin().getRoleName();
    }

    @Programmatic private UserMemento currentUser() {
        return getUserService().currentUserElseFail();
    }


    // -- equals, hashCode, compareTo, toString
    private static final String propertyNames = "username";

    private static final ObjectContracts.ObjectContract<ApplicationUser> contract =
            ObjectContracts.parse(ApplicationUser.class, propertyNames);


    @Override
    public int compareTo(final org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser other) {
        return contract.compare(this, other);
    }

    @Override
    public boolean equals(final Object obj) {
        return contract.equals(this, obj);
    }

    @Override
    public int hashCode() {
        return contract.hashCode(this);
    }

    @Override
    public String toString() {
        return contract.toString(this);
    }

}
