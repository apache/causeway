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
package org.apache.isis.extensions.secman.applib.user.dom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.ObjectSupport;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.mixins.security.HasUsername;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.user.RoleMemento;
import org.apache.isis.applib.services.user.UserMemento;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.extensions.secman.applib.IsisModuleExtSecmanApplib;
import org.apache.isis.extensions.secman.applib.SecmanConfiguration;
import org.apache.isis.extensions.secman.applib.permission.dom.ApplicationPermission;
import org.apache.isis.extensions.secman.applib.permission.dom.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.applib.permission.dom.ApplicationPermissionRepository;
import org.apache.isis.extensions.secman.applib.permission.dom.ApplicationPermissionValueSet;
import org.apache.isis.extensions.secman.applib.permission.spi.PermissionsEvaluationService;
import org.apache.isis.extensions.secman.applib.role.dom.ApplicationRole;
import org.apache.isis.extensions.secman.applib.tenancy.dom.HasAtPath;

import lombok.val;

/**
 * @since 2.0 {@index}
 */
@DomainObject(
        logicalTypeName = ApplicationUser.LOGICAL_TYPE_NAME
)
public abstract class ApplicationUser
        implements HasUsername, HasAtPath, Comparable<ApplicationUser> {

    public static final String LOGICAL_TYPE_NAME = IsisModuleExtSecmanApplib.NAMESPACE + ".ApplicationUser";

    @Inject private transient ApplicationUserRepository applicationUserRepository;
    @Inject private transient ApplicationPermissionRepository applicationPermissionRepository;
    @Inject private transient UserService userService;
    @Inject private transient PermissionsEvaluationService permissionsEvaluationService;
    @Inject private transient SecmanConfiguration configBean;

    protected ApplicationUserRepository getApplicationUserRepository() {
        return applicationUserRepository;
    }

    protected ApplicationPermissionRepository getApplicationPermissionRepository() {
        return applicationPermissionRepository;
    }

    protected UserService getUserService() {
        return userService;
    }

    /**
     * Optional service, if configured then is used to evaluate permissions within
     * {@link ApplicationPermissionValueSet#evaluate(ApplicationFeatureId, ApplicationPermissionMode)}
     * else will fallback to a default implementation.
     */
    protected PermissionsEvaluationService getPermissionsEvaluationService() {
        return permissionsEvaluationService;
    }

    protected SecmanConfiguration getConfigBean() {
        return configBean;
    }

    // -- CONSTANTS

    public static final String NAMED_QUERY_FIND_BY_USERNAME = "ApplicationUser.findByUsername";
    public static final String NAMED_QUERY_FIND_BY_EMAIL_ADDRESS = "ApplicationUser.findByEmailAddress";
    public static final String NAMED_QUERY_FIND = "ApplicationUser.find";
    public static final String NAMED_QUERY_FIND_BY_ATPATH = "ApplicationUser.findByAtPath";

    // -- DOMAIN EVENTS

    public static abstract class PropertyDomainEvent<T> extends IsisModuleExtSecmanApplib.PropertyDomainEvent<ApplicationUser, T> {}
    public static abstract class CollectionDomainEvent<T> extends IsisModuleExtSecmanApplib.CollectionDomainEvent<ApplicationUser, T> {}



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
            domainEvent = Username.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = Username.MAX_LENGTH
    )
    @PropertyLayout(
            fieldSetId="identity",
            hidden = Where.PARENTED_TABLES,
            sequence = "1"
    )
    @Parameter(
            maxLength = Username.MAX_LENGTH
    )
    @ParameterLayout(
            named = "Username"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Username {
        int MAX_LENGTH = 120;

        class DomainEvent extends PropertyDomainEvent<String> {}
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
        int MAX_LENGTH = 120;

        class DomainEvent extends PropertyDomainEvent<String> {}
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
        int MAX_LENGTH = 120;

        class DomainEvent extends PropertyDomainEvent<String> {}
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
        int MAX_LENGTH = 120;

        class KnownAsDomainEvent extends PropertyDomainEvent<String> {}
    }

    @KnownAs
    public abstract String getKnownAs();
    public abstract void setKnownAs(String knownAs);


    // -- EMAIL ADDRESS

    @Property(
            domainEvent = EmailAddress.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = EmailAddress.MAX_LENGTH
    )
    @PropertyLayout(
            fieldSetName = "Contact Details",
            sequence = "3.1"
    )
    @Parameter(
            maxLength = EmailAddress.MAX_LENGTH
    )
    @ParameterLayout(
            named = "Email"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface EmailAddress {
        int MAX_LENGTH = 120;

        class DomainEvent extends PropertyDomainEvent<String> {}
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
        int MAX_LENGTH = 120;

        class DomainEvent extends PropertyDomainEvent<String> {}
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
        int MAX_LENGTH = 120;

        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @FaxNumber
    public abstract String getFaxNumber();
    public abstract void setFaxNumber(String faxNumber);

    // -- LOCALE

    @Property(
            domainEvent = UserLocale.DomainEvent.class,
            editing = Editing.DISABLED //  edit via update button
    )
    @PropertyLayout(
            fieldSetId = "regional"
    )
    @Parameter(
            optionality = Optionality.OPTIONAL
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface UserLocale {
        int MAX_LENGTH = 120;
        class DomainEvent extends PropertyDomainEvent<Locale> {}
    }

    @UserLocale
    public abstract Locale getLanguage();
    public abstract void setLanguage(Locale locale);

    @UserLocale
    public abstract Locale getNumberFormat();
    public abstract void setNumberFormat(Locale locale);

    @UserLocale
    public abstract Locale getTimeFormat();
    public abstract void setTimeFormat(Locale locale);

    // -- AT PATH

    @Property(
            domainEvent = AtPath.DomainEvent.class,
            editing = Editing.DISABLED,
            optionality = Optionality.OPTIONAL
    )
    @PropertyLayout(
            fieldSetId = "access",
            sequence = "4"
    )
    @Parameter(
            optionality = Optionality.OPTIONAL
    )
    @ParameterLayout(
            named = "AtPath"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface AtPath {
        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @Override
    @AtPath
    public abstract String getAtPath();
    public abstract void setAtPath(String atPath);


    // -- ACCOUNT TYPE

    @Property(
            domainEvent = AccountType.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            fieldSetId = "access",
            sequence = "2"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface AccountType {
        class DomainEvent extends PropertyDomainEvent<AccountType> {}
    }

    @AccountType
    public abstract org.apache.isis.extensions.secman.applib.user.dom.AccountType getAccountType();
    public abstract void setAccountType(org.apache.isis.extensions.secman.applib.user.dom.AccountType accountType);


    // -- STATUS

    @Property(
            domainEvent = Status.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            fieldSetId = "access",
            sequence = "1"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Status {
        class DomainEvent extends PropertyDomainEvent<ApplicationUserStatus> {}
    }

    @Status
    public abstract ApplicationUserStatus getStatus();
    public abstract void setStatus(ApplicationUserStatus disabled);


    // -- ENCRYPTED PASSWORD

    @PropertyLayout(
            hidden = Where.EVERYWHERE
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface EncryptedPassword {
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
    }

    @Roles
    public abstract Set<ApplicationRole> getRoles();


    // -- PERMISSION SET

    // short-term caching
    private transient ApplicationPermissionValueSet cachedPermissionSet;

    @Programmatic
    public ApplicationPermissionValueSet getPermissionSet() {
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

    @Programmatic
    public boolean isForSelfOrRunAsAdministrator() {
        val currentUser = currentUser();
        val currentUserName = currentUser.getName();
        // is for self?
        val forSelf = Objects.equals(getUsername(), currentUserName);
        if(forSelf) {
            return true;
        }

        // is runAsAdministrator?

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


    // -- HELPERS

    @Programmatic
    public boolean isLocalAccount() {
        return getAccountType() == org.apache.isis.extensions.secman.applib.user.dom.AccountType.LOCAL;
    }

    @Programmatic
    private String getAdminRoleName() {
        return getConfigBean().getAdminRoleName();
    }

    @Programmatic
    private UserMemento currentUser() {
        return getUserService().currentUserElseFail();
    }


    // -- equals, hashCode, compareTo, toString
    private static final String propertyNames = "username";

    private static final ObjectContracts.ObjectContract<ApplicationUser> contract =
            ObjectContracts.parse(ApplicationUser.class, propertyNames);


    @Override
    public int compareTo(final org.apache.isis.extensions.secman.applib.user.dom.ApplicationUser other) {
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
