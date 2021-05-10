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
import java.util.Set;
import java.util.SortedSet;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.mixins.security.HasUsername;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermissionValueSet;
import org.apache.isis.extensions.secman.api.role.dom.ApplicationRole;
import org.apache.isis.extensions.secman.api.tenancy.dom.HasAtPath;

/**
 * @since 2.0 {@index}
 */
public interface ApplicationUser
        extends HasUsername, HasAtPath, Comparable<ApplicationUser> {

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

    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Name {
    }

    @Name
    String getName();


    // -- USERNAME

    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Username {
        int MAX_LENGTH = 120;
    }

    @Username
    String getUsername();
    void setUsername(String username);


    // -- FAMILY NAME

    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface FamilyName {
        int MAX_LENGTH = 120;
    }

    @FamilyName
    String getFamilyName();
    void setFamilyName(String familyName);


    // -- GIVEN NAME

    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface GivenName {
        int MAX_LENGTH = 120;
    }

    @GivenName
    String getGivenName();
    void setGivenName(String givenName);


    // -- KNOWN AS

    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface KnownAs {
        int MAX_LENGTH = 120;
    }

    @KnownAs
    String getKnownAs();
    void setKnownAs(String knownAs);


    // -- EMAIL ADDRESS

    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface EmailAddress {
        int MAX_LENGTH = 120;
    }

    @EmailAddress
    String getEmailAddress();
    void setEmailAddress(String emailAddress);


    // -- PHONE NUMBER

    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface PhoneNumber {
        int MAX_LENGTH = 120;
    }

    @PhoneNumber
    String getPhoneNumber();
    void setPhoneNumber(String phoneNumber);


    // -- FAX NUMBER

    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface FaxNumber {
        int MAX_LENGTH = 120;
    }

    @FaxNumber
    String getFaxNumber();
    void setFaxNumber(String faxNumber);


    // -- AT PATH

    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface AtPath {
    }

    @AtPath
    String getAtPath();
    void setAtPath(String atPath);


    // -- ACCOUNT TYPE

    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface AccountType {
    }

    @AccountType
    org.apache.isis.extensions.secman.api.user.dom.AccountType getAccountType();
    void setAccountType(org.apache.isis.extensions.secman.api.user.dom.AccountType accountType);


    // -- STATUS

    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Status {
    }

    @Status
    ApplicationUserStatus getStatus();
    void setStatus(ApplicationUserStatus disabled);


    // -- ENCRYPTED PASSWORD

    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface EncryptedPassword {
    }

    @EncryptedPassword
    String getEncryptedPassword();
    void setEncryptedPassword(String encryptedPassword);


    // -- HAS PASSWORD

    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface HasPassword {
    }

    @HasPassword
    boolean isHasPassword();


    // ROLES

    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Roles {
    }

    @Roles
    SortedSet<ApplicationRole> getRoles();


    // -- PERMISSION SET

    @Programmatic
    ApplicationPermissionValueSet getPermissionSet();


    // -- IS FOR SELF OR RUN AS ADMINISTRATOR

    @Programmatic
    boolean isForSelfOrRunAsAdministrator();


    // -- HELPERS

    @Programmatic
    default boolean isLocalAccount() {
        return getAccountType() == org.apache.isis.extensions.secman.api.user.dom.AccountType.LOCAL;
    }

}
