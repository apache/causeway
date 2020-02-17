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
package org.apache.isis.extensions.secman.jdo.dom.user;

import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.VersionStrategy;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.security.RoleMemento;
import org.apache.isis.applib.security.UserMemento;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ObjectContracts.ObjectContract;
import org.apache.isis.applib.value.Password;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeatureId;
import org.apache.isis.extensions.secman.api.SecurityModuleConfig;
import org.apache.isis.extensions.secman.api.encryption.PasswordEncryptionService;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionValueSet;
import org.apache.isis.extensions.secman.api.permission.PermissionsEvaluationService;
import org.apache.isis.extensions.secman.api.user.AccountType;
import org.apache.isis.extensions.secman.api.user.ApplicationUserStatus;
import org.apache.isis.extensions.secman.jdo.dom.permission.ApplicationPermission;
import org.apache.isis.extensions.secman.jdo.dom.permission.ApplicationPermissionRepository;
import org.apache.isis.extensions.secman.jdo.dom.role.ApplicationRole;
import org.apache.isis.extensions.secman.jdo.dom.role.ApplicationRoleRepository;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

@javax.jdo.annotations.PersistenceCapable(
        identityType = IdentityType.DATASTORE,
        schema = "isissecurity",
        table = "ApplicationUser")
@javax.jdo.annotations.Inheritance(
        strategy = InheritanceStrategy.NEW_TABLE)
@javax.jdo.annotations.DatastoreIdentity(
        strategy = IdGeneratorStrategy.NATIVE, column = "id")
@javax.jdo.annotations.Version(
        strategy = VersionStrategy.VERSION_NUMBER,
        column = "version")
@javax.jdo.annotations.Uniques({
    @javax.jdo.annotations.Unique(
            name = "ApplicationUser_username_UNQ", members = { "username" })
})
@javax.jdo.annotations.Queries( {
    @javax.jdo.annotations.Query(
            name = "findByUsername", language = "JDOQL",
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.secman.jdo.dom.user.ApplicationUser "
                    + "WHERE username == :username"),
    @javax.jdo.annotations.Query(
            name = "findByEmailAddress", language = "JDOQL",
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.secman.jdo.dom.user.ApplicationUser "
                    + "WHERE emailAddress == :emailAddress"),
    @javax.jdo.annotations.Query(
            name = "findByAtPath", language = "JDOQL",
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.secman.jdo.dom.user.ApplicationUser "
                    + "WHERE atPath == :atPath"),
    @javax.jdo.annotations.Query(
            name = "findByName", language = "JDOQL",
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.secman.jdo.dom.user.ApplicationUser "
                    + "WHERE username.matches(:nameRegex)"
                    + "   || familyName.matches(:nameRegex)"
                    + "   || givenName.matches(:nameRegex)"
                    + "   || knownAs.matches(:nameRegex)"),
    @javax.jdo.annotations.Query(
            name = "find", language = "JDOQL",
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.secman.jdo.dom.user.ApplicationUser "
                    + "WHERE username.matches(:regex)"
                    + " || familyName.matches(:regex)"
                    + " || givenName.matches(:regex)"
                    + " || knownAs.matches(:regex)"
                    + " || emailAddress.matches(:regex)")
})
@DomainObject(
        objectType = "isissecurity.ApplicationUser",
        autoCompleteRepository = ApplicationUserRepository.class,
        autoCompleteAction = "findMatching"
        )
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_ROOT
        )
//@MemberGroupLayout(columnSpans = {4,4,4,12},
//    left = {"Id", "Name", "Metadata"},
//    middle= {"Contact Details"},
//    right= {"Status", "AtPath"}
//)
public class ApplicationUser implements Comparable<ApplicationUser>, 
org.apache.isis.extensions.secman.api.user.ApplicationUser {

    // -- identification

    /**
     * having a title() method (rather than using @Title annotation) is necessary as a workaround to be able to use
     * wrapperFactory#unwrap(...) method, which is otherwise broken in Isis 1.6.0
     */
    public String title() {
        return getName();
    }

    public String iconName() {
        return getStatus().isEnabled() ? "enabled" : "disabled"; 
    }

    // -- name (derived property)

    public static class NameDomainEvent extends PropertyDomainEvent<String> {}

    @Override
    @javax.jdo.annotations.NotPersistent
    @Property(
            domainEvent = NameDomainEvent.class,
            editing = Editing.DISABLED
            )
    @PropertyLayout(
            hidden=Where.OBJECT_FORMS
            )
    @MemberOrder(name="Id", sequence = "1")
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


    // -- username (property)

    public static class UsernameDomainEvent extends PropertyDomainEvent<String> {}


    @javax.jdo.annotations.Column(allowsNull="false", length = MAX_LENGTH_USERNAME)
    @Property(
            domainEvent = UsernameDomainEvent.class,
            editing = Editing.DISABLED
            )
    @PropertyLayout(
            hidden=Where.PARENTED_TABLES
            )
    @MemberOrder(name="Id", sequence = "1")
    @Getter @Setter
    private String username;



    // -- updateUsername (action)

    public static class UpdateUsernameDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = UpdateUsernameDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
            )
    @MemberOrder(name="username", sequence = "1")
    public ApplicationUser updateUsername(
            @Parameter(maxLength = MAX_LENGTH_USERNAME)
            @ParameterLayout(named="Username")
            final String username) {
        setUsername(username);
        return this;
    }

    public String default0UpdateUsername() {
        return getUsername();
    }


    // -- familyName (property)

    public static class FamilyNameDomainEvent extends PropertyDomainEvent<String> {}


    @javax.jdo.annotations.Column(allowsNull="true", length = MAX_LENGTH_FAMILY_NAME)
    @Property(
            domainEvent = FamilyNameDomainEvent.class,
            editing = Editing.DISABLED
            )
    @PropertyLayout(
            hidden=Where.ALL_TABLES
            )
    @MemberOrder(name="Name",sequence = "2.1")
    @Getter @Setter
    private String familyName;


    // -- givenName (property)

    public static class GivenNameDomainEvent extends PropertyDomainEvent<String> {}


    @javax.jdo.annotations.Column(allowsNull="true", length = MAX_LENGTH_GIVEN_NAME)
    @Property(
            domainEvent = GivenNameDomainEvent.class,
            editing = Editing.DISABLED
            )
    @PropertyLayout(
            hidden=Where.ALL_TABLES
            )
    @MemberOrder(name="Name", sequence = "2.2")
    @Getter @Setter
    private String givenName;



    // -- knownAs (property)

    public static class KnownAsDomainEvent extends PropertyDomainEvent<String> {}


    @javax.jdo.annotations.Column(allowsNull="true", length = MAX_LENGTH_KNOWN_AS)
    @Property(
            domainEvent = KnownAsDomainEvent.class,
            editing = Editing.DISABLED
            )
    @PropertyLayout(
            hidden=Where.ALL_TABLES
            )
    @MemberOrder(name="Name",sequence = "2.3")
    @Getter @Setter
    private String knownAs;


    // -- updateName (action)

    public static class UpdateNameDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = UpdateNameDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
            )
    @MemberOrder(name="knownAs", sequence = "1")
    public ApplicationUser updateName(
            @Parameter(maxLength = MAX_LENGTH_FAMILY_NAME, optionality = Optionality.OPTIONAL)
            @ParameterLayout(named="Family Name")
            final String familyName,
            @Parameter(maxLength = MAX_LENGTH_GIVEN_NAME, optionality = Optionality.OPTIONAL)
            @ParameterLayout(named="Given Name")
            final String givenName,
            @Parameter(maxLength = MAX_LENGTH_KNOWN_AS, optionality = Optionality.OPTIONAL)
            @ParameterLayout(named="Known As")
            final String knownAs
            ) {
        setFamilyName(familyName);
        setGivenName(givenName);
        setKnownAs(knownAs);
        return this;
    }

    public String default0UpdateName() {
        return getFamilyName();
    }

    public String default1UpdateName() {
        return getGivenName();
    }

    public String default2UpdateName() {
        return getKnownAs();
    }

    public String disableUpdateName() {
        return isForSelfOrRunAsAdministrator()? null: "Can only update your own user record.";
    }

    public String validateUpdateName(final String familyName, final String givenName, final String knownAs) {
        if(familyName != null && givenName == null) {
            return "Must provide given name if family name has been provided.";
        }
        if(familyName == null && (givenName != null | knownAs != null)) {
            return "Must provide family name if given name or 'known as' name has been provided.";
        }
        return null;
    }


    // -- emailAddress (property)

    public static class EmailAddressDomainEvent extends PropertyDomainEvent<String> {}



    @javax.jdo.annotations.Column(allowsNull="true", length = MAX_LENGTH_EMAIL_ADDRESS)
    @Property(
            domainEvent = EmailAddressDomainEvent.class,
            editing = Editing.DISABLED
            )
    @MemberOrder(name="Contact Details", sequence = "3.1")
    @Getter @Setter
    private String emailAddress;



    // -- updateEmailAddress (action)

    public static class UpdateEmailAddressDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = UpdateEmailAddressDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
            )
    @MemberOrder(name="emailAddress", sequence = "1")
    public ApplicationUser updateEmailAddress(
            @Parameter(maxLength = MAX_LENGTH_EMAIL_ADDRESS)
            @ParameterLayout(named="Email")
            final String emailAddress) {
        setEmailAddress(emailAddress);
        return this;
    }

    public String default0UpdateEmailAddress() {
        return getEmailAddress();
    }

    public String disableUpdateEmailAddress() {
        return isForSelfOrRunAsAdministrator()? null: "Can only update your own user record.";
    }


    // -- phoneNumber (property)

    public static class PhoneNumberDomainEvent extends PropertyDomainEvent<String> {}


    @javax.jdo.annotations.Column(allowsNull="true", length = MAX_LENGTH_PHONE_NUMBER)
    @Property(
            domainEvent = PhoneNumberDomainEvent.class,
            editing = Editing.DISABLED
            )
    @MemberOrder(name="Contact Details", sequence = "3.2")
    @Getter @Setter
    private String phoneNumber;



    // -- phoneNumber (property)

    public static class UpdatePhoneNumberDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = UpdatePhoneNumberDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
            )
    @MemberOrder(name="phoneNumber", sequence = "1")
    public ApplicationUser updatePhoneNumber(
            @ParameterLayout(named="Phone")
            @Parameter(maxLength = MAX_LENGTH_PHONE_NUMBER, optionality = Optionality.OPTIONAL)
            final String phoneNumber) {
        setPhoneNumber(phoneNumber);
        return this;
    }

    public String disableUpdatePhoneNumber() {
        return isForSelfOrRunAsAdministrator()? null: "Can only update your own user record.";
    }
    public String default0UpdatePhoneNumber() {
        return getPhoneNumber();
    }



    // -- faxNumber (property)

    public static class FaxNumberDomainEvent extends PropertyDomainEvent<String> {}



    @javax.jdo.annotations.Column(allowsNull="true", length = MAX_LENGTH_PHONE_NUMBER)
    @Property(
            domainEvent = FaxNumberDomainEvent.class,
            editing = Editing.DISABLED
            )
    @PropertyLayout(
            hidden=Where.PARENTED_TABLES
            )
    @MemberOrder(name="Contact Details", sequence = "3.3")
    @Getter @Setter
    private String faxNumber;



    // -- updateFaxNumber (action)

    public static class UpdateFaxNumberDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = UpdateFaxNumberDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
            )
    @MemberOrder(name="faxNumber", sequence = "1")
    public ApplicationUser updateFaxNumber(
            @Parameter(maxLength = MAX_LENGTH_PHONE_NUMBER, optionality = Optionality.OPTIONAL)
            @ParameterLayout(named="Fax")
            final String faxNumber) {
        setFaxNumber(faxNumber);
        return this;
    }

    public String default0UpdateFaxNumber() {
        return getFaxNumber();
    }

    public String disableUpdateFaxNumber() {
        return isForSelfOrRunAsAdministrator()? null: "Can only update your own user record.";
    }



    // -- atPath (property)

    public static class AtPathDomainEvent extends PropertyDomainEvent<String> {}


    @javax.jdo.annotations.Column(name = "atPath", allowsNull="true")
    @Property(
            domainEvent = AtPathDomainEvent.class,
            editing = Editing.DISABLED
            )
    @MemberOrder(name="atPath", sequence = "3.4")
    @Getter @Setter
    private String atPath;



    // -- updateAtPath (action)

    public static class UpdateAtPathDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = UpdateAtPathDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
            )
    @MemberOrder(name="atPath", sequence = "1")
    public ApplicationUser updateAtPath(
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(named = "AtPath")
            final String atPath) {
        setAtPath(atPath);
        return this;
    }

    public String default0UpdateAtPath() {
        return getAtPath();
    }


    // -- accountType (property)

    public static class AccountTypeDomainEvent extends PropertyDomainEvent<AccountType> {}


    @javax.jdo.annotations.Column(allowsNull="false")
    @Property(
            domainEvent = AccountTypeDomainEvent.class,
            editing = Editing.DISABLED
            )
    @MemberOrder(name="Status", sequence = "3")
    @Getter @Setter
    private AccountType accountType;



    // -- updateAccountType (action)

    public static class UpdateAccountTypeDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = UpdateAccountTypeDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
            )
    @MemberOrder(name = "Account Type", sequence = "1")
    public ApplicationUser updateAccountType(
            final AccountType accountType) {
        setAccountType(accountType);
        return this;
    }
    public String disableUpdateAccountType() {
        return applicationUserRepository.isAdminUser(this)
                ? "Cannot change account type for admin user"
                        : null;
    }
    public AccountType default0UpdateAccountType() {
        return getAccountType();
    }

    private boolean isDelegateAccountOrPasswordEncryptionNotAvailable() {
        return !isLocalAccountWithPasswordEncryptionAvailable();
    }

    private boolean isLocalAccountWithPasswordEncryptionAvailable() {
        return getAccountType() == AccountType.LOCAL && passwordEncryptionService != null;
    }



    // -- status (property), visible (action), usable (action)

    public static class StatusDomainEvent extends PropertyDomainEvent<ApplicationUserStatus> {}


    @javax.jdo.annotations.Column(allowsNull="false")
    @Property(
            domainEvent = StatusDomainEvent.class,
            editing = Editing.DISABLED
            )
    @MemberOrder(name="Status", sequence = "4")
    @Getter @Setter
    private ApplicationUserStatus status;



    // -- unlock (action)

    public static class UnlockDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = UnlockDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
            )
    @ActionLayout(named="Enable") // symmetry with lock (disable)
    @MemberOrder(name = "Status", sequence = "1")
    public ApplicationUser unlock() {
        setStatus(ApplicationUserStatus.ENABLED);
        return this;
    }
    public String disableUnlock() {
        return getStatus() == ApplicationUserStatus.ENABLED ? "Status is already set to ENABLE": null;
    }



    // -- lock (action)

    public static class LockDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = LockDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
            )
    @ActionLayout(named="Disable") // method cannot be called 'disable' as that would clash with Isis' naming conventions
    @MemberOrder(name = "Status", sequence = "2")
    public ApplicationUser lock() {
        setStatus(ApplicationUserStatus.DISABLED);
        return this;
    }
    public String disableLock() {
        if(applicationUserRepository.isAdminUser(this)) {
            return "Cannot disable the '" + configBean.getAdminUserName() + "' user.";
        }
        return getStatus() == ApplicationUserStatus.DISABLED ? "Status is already set to DISABLE": null;
    }



    // -- encryptedPassword (hidden property)


    @javax.jdo.annotations.Column(allowsNull="true")
    @PropertyLayout(hidden=Where.EVERYWHERE)
    @Getter @Setter
    private String encryptedPassword;

    public boolean hideEncryptedPassword() {
        return isDelegateAccountOrPasswordEncryptionNotAvailable();
    }


    // -- hasPassword (derived property)

    public static class HasPasswordDomainEvent extends PropertyDomainEvent<Boolean> {}

    @Property(
            domainEvent = HasPasswordDomainEvent.class,
            editing = Editing.DISABLED
            )
    @MemberOrder(name="Status", sequence = "4")
    public boolean isHasPassword() {
        return !_Strings.isNullOrEmpty(getEncryptedPassword());
    }

    public boolean hideHasPassword() {
        return isDelegateAccountOrPasswordEncryptionNotAvailable();
    }



    // -- updatePassword (action)

    public static class UpdatePasswordDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = UpdatePasswordDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
            )
    @MemberOrder(name="hasPassword", sequence = "10")
    public ApplicationUser updatePassword(
            @ParameterLayout(named="Existing password")
            final Password existingPassword,
            @ParameterLayout(named="New password")
            final Password newPassword,
            @ParameterLayout(named="Re-enter password")
            final Password newPasswordRepeat) {
        updatePassword(newPassword.getPassword());
        return this;
    }

    public boolean hideUpdatePassword() {
        return isDelegateAccountOrPasswordEncryptionNotAvailable();
    }

    public String disableUpdatePassword() {

        if(!isForSelfOrRunAsAdministrator()) {
            return "Can only update password for your own user account.";
        }
        if (!isHasPassword()) {
            return "Password must be reset by administrator.";
        }
        return null;
    }


    public String validateUpdatePassword(
            final Password existingPassword,
            final Password newPassword,
            final Password newPasswordRepeat) {
        if(isDelegateAccountOrPasswordEncryptionNotAvailable()) {
            return null;
        }

        if(getEncryptedPassword() != null) {
            if (!passwordEncryptionService.matches(existingPassword.getPassword(), getEncryptedPassword())) {
                return "Existing password is incorrect";
            }
        }

        if (!match(newPassword, newPasswordRepeat)) {
            return "Passwords do not match";
        }

        return null;
    }

    @Programmatic
    public void updatePassword(final String password) {
        // in case called programmatically
        if(isDelegateAccountOrPasswordEncryptionNotAvailable()) {
            return;
        }
        final String encryptedPassword = passwordEncryptionService.encrypt(password);
        setEncryptedPassword(encryptedPassword);
    }



    // -- resetPassword (action)

    public static class ResetPasswordDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent =ResetPasswordDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
            )
    @MemberOrder(name="hasPassword", sequence = "20")
    public ApplicationUser resetPassword(
            @ParameterLayout(named="New password")
            final Password newPassword,
            @ParameterLayout(named="Repeat password")
            final Password newPasswordRepeat) {
        updatePassword(newPassword.getPassword());
        return this;
    }

    public boolean hideResetPassword() {
        return isDelegateAccountOrPasswordEncryptionNotAvailable();
    }

    public String validateResetPassword(
            final Password newPassword,
            final Password newPasswordRepeat) {
        if(isDelegateAccountOrPasswordEncryptionNotAvailable()) {
            return null;
        }
        if (!match(newPassword, newPasswordRepeat)) {
            return "Passwords do not match";
        }

        return null;
    }

    boolean match(final Password newPassword, final Password newPasswordRepeat) {
        if (newPassword == null && newPasswordRepeat == null) {
            return true;
        }
        if (newPassword == null || newPasswordRepeat == null) {
            return false;
        }
        return Objects.equals(newPassword.getPassword(), newPasswordRepeat.getPassword());
    }



    // -- roles (collection)
    public static class RolesDomainEvent extends CollectionDomainEvent<ApplicationRole> {}

    @javax.jdo.annotations.Persistent(table="ApplicationUserRoles")
    @javax.jdo.annotations.Join(column="userId")
    @javax.jdo.annotations.Element(column="roleId")
    @Collection(
            domainEvent = RolesDomainEvent.class,
            editing = Editing.DISABLED
            )
    @CollectionLayout(
            defaultView="table"
            )
    @MemberOrder(sequence = "20")
    @Getter @Setter
    private SortedSet<ApplicationRole> roles = new TreeSet<>();


    // necessary only because otherwise call to getRoles() through wrapped object
    // (in integration tests) is ambiguous.
    public void addToRoles(final ApplicationRole applicationRole) {
        getRoles().add(applicationRole);
    }
    // necessary only because otherwise call to getRoles() through wrapped object
    // (in integration tests) is ambiguous.
    public void removeFromRoles(final ApplicationRole applicationRole) {
        getRoles().remove(applicationRole);
    }


    // -- removeRole (action)

    public static class RemoveRoleDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = RemoveRoleDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
            )
    @ActionLayout(
            named="Remove"
            )
    @MemberOrder(name="roles", sequence = "2")
    public ApplicationUser removeRole(final ApplicationRole role) {
        removeFromRoles(role);
        return this;
    }

    public String disableRemoveRole() {
        return getRoles().isEmpty()? "No roles to remove": null;
    }

    public SortedSet<ApplicationRole> choices0RemoveRole() {
        return getRoles();
    }

    // duplicated in ApplicationRole_removeUser mixin
    public String validateRemoveRole(
            final ApplicationRole applicationRole) {
        if(applicationUserRepository.isAdminUser(this) && applicationRoleRepository.isAdminRole(applicationRole)) {
            return "Cannot remove admin user from the admin role.";
        }
        return null;
    }



    // -- delete (action)

    public static class DeleteDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = DeleteDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT_ARE_YOU_SURE
            )
    @MemberOrder(sequence = "1")
    public java.util.Collection<org.apache.isis.extensions.secman.api.user.ApplicationUser> delete() {
        repository.removeAndFlush(this);
        return applicationUserRepository.allUsers();
    }

    public String disableDelete() {
        return applicationUserRepository.isAdminUser(this)? "Cannot delete the admin user": null;
    }


    // -- PermissionSet (programmatic)

    // short-term caching
    private transient ApplicationPermissionValueSet cachedPermissionSet;
    @Override
    @Programmatic
    public ApplicationPermissionValueSet getPermissionSet() {
        if(cachedPermissionSet != null) {
            return cachedPermissionSet;
        }
        val permissions = applicationPermissionRepository.findByUser(this);
        return cachedPermissionSet =
                new ApplicationPermissionValueSet(
                        _Lists.map(permissions, ApplicationPermission.Functions.AS_VALUE),
                        permissionsEvaluationService);
    }


    // -- helpers
    boolean isForSelfOrRunAsAdministrator() {
        return isForSelf() || isRunAsAdministrator();
    }

    boolean isForSelf() {
        final String currentUserName = userService.getUser().getName();
        return Objects.equals(getUsername(), currentUserName);
    }
    boolean isRunAsAdministrator() {
        final UserMemento currentUser = userService.getUser();
        final List<RoleMemento> roles = currentUser.getRoles();

        val adminRoleSuffix = ":" + configBean.getAdminRoleName();

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


    // -- equals, hashCode, compareTo, toString
    private static final String propertyNames = "username";

    private static final ObjectContract<ApplicationUser> contract = 
            ObjectContracts.parse(ApplicationUser.class, propertyNames);


    @Override
    public int compareTo(final ApplicationUser o) {
        return contract.compare(this, o);
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

    @Inject ApplicationRoleRepository applicationRoleRepository;
    @Inject ApplicationUserRepository applicationUserRepository;
    @Inject ApplicationPermissionRepository applicationPermissionRepository;
    @Inject PasswordEncryptionService passwordEncryptionService;
    @Inject RepositoryService repository;
    @Inject UserService userService;
    /**
     * Optional service, if configured then is used to evaluate permissions within
     * {@link org.apache.isis.extensions.secman.api.permission.ApplicationPermissionValueSet#evaluate(ApplicationFeatureId, ApplicationPermissionMode)}
     * else will fallback to a {@link org.apache.isis.extensions.secman.api.permission.PermissionsEvaluationService#DEFAULT default}
     * implementation.
     */
    @Inject PermissionsEvaluationService permissionsEvaluationService;
    @Inject private SecurityModuleConfig configBean;

}
