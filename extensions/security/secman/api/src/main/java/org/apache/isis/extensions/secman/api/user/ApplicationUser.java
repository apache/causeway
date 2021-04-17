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
package org.apache.isis.extensions.secman.api.user;

import java.util.Set;

import org.apache.isis.applib.mixins.security.HasUsername;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionValueSet;
import org.apache.isis.extensions.secman.api.role.ApplicationRole;
import org.apache.isis.extensions.secman.api.tenancy.HasAtPath;

/**
 * @since 2.0 {@index}
 */
public interface ApplicationUser extends HasUsername, HasAtPath {

    // -- CONSTANTS

    public static final int MAX_LENGTH_USERNAME = 30;
    public static final int MAX_LENGTH_FAMILY_NAME = 50;
    public static final int MAX_LENGTH_GIVEN_NAME = 50;
    public static final int MAX_LENGTH_KNOWN_AS = 20;
    public static final int MAX_LENGTH_EMAIL_ADDRESS = 50;
    public static final int MAX_LENGTH_PHONE_NUMBER = 25;

    // -- DOMAIN EVENTS

    public static abstract class PropertyDomainEvent<T> extends IsisModuleExtSecmanApi.PropertyDomainEvent<ApplicationUser, T> {}
    public static abstract class CollectionDomainEvent<T> extends IsisModuleExtSecmanApi.CollectionDomainEvent<ApplicationUser, T> {}
    public static abstract class ActionDomainEvent extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationUser> {}

    public static class AddRoleDomainEvent extends ActionDomainEvent {}
    public static class UpdateAtPathDomainEvent extends ActionDomainEvent {}
    public static class UpdateUsernameDomainEvent extends ActionDomainEvent {}
    public static class UpdateNameDomainEvent extends ActionDomainEvent {}
    public static class UpdateEmailAddressDomainEvent extends ActionDomainEvent {}
    public static class UpdatePhoneNumberDomainEvent extends ActionDomainEvent {}
    public static class UpdateFaxNumberDomainEvent extends ActionDomainEvent {}
    public static class UpdateAccountTypeDomainEvent extends ActionDomainEvent {}
    public static class UnlockDomainEvent extends ActionDomainEvent {}
    public static class LockDomainEvent extends ActionDomainEvent {}
    public static class UpdatePasswordDomainEvent extends ActionDomainEvent {}
    public static class ResetPasswordDomainEvent extends ActionDomainEvent {}
    public static class RemoveRoleDomainEvent extends ActionDomainEvent {}
    public static class DeleteDomainEvent extends ActionDomainEvent {}
    public static class NewDelegateUserDomainEvent extends ActionDomainEvent {}
    public static class NewLocalUserDomainEvent extends ActionDomainEvent {}
    public static class UserDuplicateDomainEvent extends ActionDomainEvent {}

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

    String getName();

    /**
     * Only used for {@link #getAccountType() local} users, stores the
     * password in encrypted form.
     *
     * <p>
     *     The password will have been encrypted by the configured
     *     {@link org.apache.isis.extensions.secman.api.encryption.PasswordEncryptionService}.
     * </p>
     *
     * @see org.apache.isis.extensions.secman.api.encryption.PasswordEncryptionService
     */
    String getEncryptedPassword();

    /**
     * Determines how to authenticate this user.
     *
     * <p>
     *     {@link AccountType#LOCAL local} users are authenticated directly
     *     against the {@link #getEncryptedPassword() password}.
     * </p>
     *
     * <p>
     *     {@link AccountType#DELEGATED delegated} users are authenticated by
     *     some other authentication mechanism.  For example, Secman's
     *     Shiro realm allows a delegate realm to be configured, and will
     *     call that delegate realm for delegated users rather than
     *     authenticating locally.
     * </p>
     */
    AccountType getAccountType();
    void setAccountType(AccountType accountType);

    ApplicationPermissionValueSet getPermissionSet();

    Set<? extends ApplicationRole> getRoles();

    ApplicationUserStatus getStatus();
    void setStatus(ApplicationUserStatus disabled);

    void setAtPath(String atPath);

    String getEmailAddress();
    void setEmailAddress(String emailAddress);

    String getFaxNumber();
    void setFaxNumber(String faxNumber);

    String getFamilyName();
    void setFamilyName(String familyName);

    String getGivenName();
    void setGivenName(String givenName);

    String getKnownAs();
    void setKnownAs(String knownAs);

    String getPhoneNumber();
    void setPhoneNumber(String phoneNumber);

    void setUsername(String username);

    void setEncryptedPassword(String encryptedPassword);

    boolean isForSelfOrRunAsAdministrator();

    boolean isHasPassword();

    default boolean isLocalAccount() {
        return getAccountType() == AccountType.LOCAL;
    }

}
