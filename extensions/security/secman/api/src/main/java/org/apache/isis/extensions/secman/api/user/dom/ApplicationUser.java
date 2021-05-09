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

import java.util.Set;
import java.util.SortedSet;

import org.apache.isis.applib.mixins.security.HasUsername;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermissionValueSet;
import org.apache.isis.extensions.secman.api.role.dom.ApplicationRole;
import org.apache.isis.extensions.secman.api.tenancy.dom.HasAtPath;

/**
 * @since 2.0 {@index}
 */
public interface ApplicationUser extends HasUsername, HasAtPath {

    // -- CONSTANTS

    public static final int MAX_LENGTH_USERNAME = 120;
    public static final int MAX_LENGTH_FAMILY_NAME = 120;
    public static final int MAX_LENGTH_GIVEN_NAME = 120;
    public static final int MAX_LENGTH_KNOWN_AS = 120;
    public static final int MAX_LENGTH_EMAIL_ADDRESS = 120;
    public static final int MAX_LENGTH_PHONE_NUMBER = 120;

    String NAMED_QUERY_FIND_BY_USERNAME = "ApplicationUser.findByUsername";
    String NAMED_QUERY_FIND_BY_EMAIL_ADDRESS = "ApplicationUser.findByEmailAddress";
    String NAMED_QUERY_FIND = "ApplicationUser.find";
    String NAMED_QUERY_FIND_BY_ATPATH = "ApplicationUser.findByAtPath";

    // -- DOMAIN EVENTS

    public static abstract class PropertyDomainEvent<T> extends IsisModuleExtSecmanApi.PropertyDomainEvent<ApplicationUser, T> {}
    public static abstract class CollectionDomainEvent<T> extends IsisModuleExtSecmanApi.CollectionDomainEvent<ApplicationUser, T> {}

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

    String getName();

    // -- USERNAME

    void setUsername(String username);

    // -- FAMILY NAME

    String getFamilyName();
    void setFamilyName(String familyName);

    // -- GIVEN NAME

    String getGivenName();
    void setGivenName(String givenName);

    // -- KNOWN AS

    String getKnownAs();
    void setKnownAs(String knownAs);


    // -- EMAIL ADDRESS

    String getEmailAddress();
    void setEmailAddress(String emailAddress);

    // -- PHONE NUMBER

    String getPhoneNumber();
    void setPhoneNumber(String phoneNumber);


    // -- FAX NUMBER

    String getFaxNumber();
    void setFaxNumber(String faxNumber);


    // -- AT PATH

    void setAtPath(String atPath);


    // -- ACCOUNT TYPE

    AccountType getAccountType();
    void setAccountType(AccountType accountType);


    // -- STATUS

    ApplicationUserStatus getStatus();
    void setStatus(ApplicationUserStatus disabled);


    // -- ENCRYPTED PASSWORD

    String getEncryptedPassword();
    void setEncryptedPassword(String encryptedPassword);


    // -- HAS PASSWORD

    boolean isHasPassword();


    // ROLES

    SortedSet<ApplicationRole> getRoles();


    // -- PERMISSION SET

    ApplicationPermissionValueSet getPermissionSet();


    // -- IS FOR SELF OR RUN AS ADMINISTRATOR

    boolean isForSelfOrRunAsAdministrator();


    // -- HELPERS

    default boolean isLocalAccount() {
        return getAccountType() == AccountType.LOCAL;
    }

}
