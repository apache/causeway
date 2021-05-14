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
package org.apache.isis.extensions.secman.api.user.dom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.mixins.security.HasUsername;
import org.apache.isis.applib.services.user.RoleMemento;
import org.apache.isis.applib.services.user.UserMemento;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermissionValueSet;
import org.apache.isis.extensions.secman.api.role.dom.ApplicationRole;
import org.apache.isis.extensions.secman.api.tenancy.dom.HasAtPath;

import lombok.val;

/**
 * @since 2.0 {@index}
 */
@DomainObject(
        objectType = IsisModuleExtSecmanApi.NAMESPACE + ".api.ApplicationUser"
)
public interface ApplicationUser
        extends HasUsername, HasAtPath, Comparable<ApplicationUser> {

    String OBJECT_TYPE = IsisModuleExtSecmanApi.NAMESPACE + ".ApplicationUser";

    // -- CONSTANTS

    String NAMED_QUERY_FIND_BY_USERNAME = "ApplicationUser.findByUsername";
    String NAMED_QUERY_FIND_BY_EMAIL_ADDRESS = "ApplicationUser.findByEmailAddress";
    String NAMED_QUERY_FIND = "ApplicationUser.find";
    String NAMED_QUERY_FIND_BY_ATPATH = "ApplicationUser.findByAtPath";

    // -- DOMAIN EVENTS

    abstract class PropertyDomainEvent<T> extends IsisModuleExtSecmanApi.PropertyDomainEvent<ApplicationUser, T> {}
    abstract class CollectionDomainEvent<T> extends IsisModuleExtSecmanApi.CollectionDomainEvent<ApplicationUser, T> {}

    // -- MODEL

    /**
     * having a title() method (rather than using @Title annotation) is necessary as a workaround to be able to use
     * wrapperFactory#unwrap(...) method, which is otherwise broken in Isis 1.6.0
     */
    default String title() {
        return getName();
    }

    default String iconName() {
        return getStatus().isEnabled() ? "enabled" : "disabled";
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
    @interface Name {
        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @Name
    default String getName() {
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
    @interface Username {
        int MAX_LENGTH = 120;

        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @Override
    @Username
    String getUsername();
    void setUsername(String username);


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
    @interface FamilyName {
        int MAX_LENGTH = 120;

        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @FamilyName
    String getFamilyName();
    void setFamilyName(String familyName);


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
    @interface GivenName {
        int MAX_LENGTH = 120;

        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @GivenName
    String getGivenName();
    void setGivenName(String givenName);


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
    @interface KnownAs {
        int MAX_LENGTH = 120;

        class KnownAsDomainEvent extends PropertyDomainEvent<String> {}
    }

    @KnownAs
    String getKnownAs();
    void setKnownAs(String knownAs);


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
    @interface EmailAddress {
        int MAX_LENGTH = 120;

        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @EmailAddress
    String getEmailAddress();
    void setEmailAddress(String emailAddress);


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
    @interface PhoneNumber {
        int MAX_LENGTH = 120;

        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @PhoneNumber
    String getPhoneNumber();
    void setPhoneNumber(String phoneNumber);


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
    @interface FaxNumber {
        int MAX_LENGTH = 120;

        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @FaxNumber
    String getFaxNumber();
    void setFaxNumber(String faxNumber);


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
    @interface AtPath {
        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @Override
    @AtPath
    String getAtPath();
    void setAtPath(String atPath);


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
    @interface AccountType {
        class DomainEvent extends PropertyDomainEvent<AccountType> {}
    }

    @AccountType
    org.apache.isis.extensions.secman.api.user.dom.AccountType getAccountType();
    void setAccountType(org.apache.isis.extensions.secman.api.user.dom.AccountType accountType);


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
    @interface Status {
        class DomainEvent extends PropertyDomainEvent<ApplicationUserStatus> {}
    }

    @Status
    ApplicationUserStatus getStatus();
    void setStatus(ApplicationUserStatus disabled);


    // -- ENCRYPTED PASSWORD

    @PropertyLayout(
            hidden = Where.EVERYWHERE
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface EncryptedPassword {
    }

    @EncryptedPassword
    String getEncryptedPassword();
    void setEncryptedPassword(String encryptedPassword);


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
    @interface HasPassword {
        class DomainEvent extends PropertyDomainEvent<Boolean> {}
    }

    @HasPassword
    default boolean isHasPassword() {
        return _Strings.isNotEmpty(getEncryptedPassword());
    }

    boolean hideHasPassword();


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
    @interface Roles {
        class RolesDomainEvent extends CollectionDomainEvent<ApplicationRole> {}
    }

    @Roles
    SortedSet<ApplicationRole> getRoles();


    // -- PERMISSION SET

    @Programmatic
    ApplicationPermissionValueSet getPermissionSet();


    // -- IS FOR SELF OR RUN AS ADMINISTRATOR

    @Programmatic
    default boolean isForSelfOrRunAsAdministrator() {
        val currentUser = currentUser();
        val currentUserName = currentUser.getName();
        // is for self?
        val forSelf = Objects.equals(getUsername(), currentUserName);
        if(forSelf) {
            return true;
        }

        // is runAsAdministrator?
        final List<RoleMemento> roles = currentUser.getRoles();

        val adminRoleSuffix = ":" + getAdminRoleName();

        for (final RoleMemento role : roles) {
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

    @Programmatic
    String getAdminRoleName();

    @Programmatic
    UserMemento currentUser();


    // -- HELPERS

    @Programmatic
    default boolean isLocalAccount() {
        return getAccountType() == org.apache.isis.extensions.secman.api.user.dom.AccountType.LOCAL;
    }

}
