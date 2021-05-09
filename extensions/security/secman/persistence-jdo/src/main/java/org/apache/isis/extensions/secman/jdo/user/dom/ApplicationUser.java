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
package org.apache.isis.extensions.secman.jdo.user.dom;

import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.VersionStrategy;

import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.user.RoleMemento;
import org.apache.isis.applib.services.user.UserMemento;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ObjectContracts.ObjectContract;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.extensions.secman.api.SecmanConfiguration;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermission;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermissionValueSet;
import org.apache.isis.extensions.secman.api.permission.spi.PermissionsEvaluationService;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUserStatus;
import org.apache.isis.extensions.secman.jdo.permission.dom.ApplicationPermissionRepository;
import org.apache.isis.extensions.secman.jdo.role.dom.ApplicationRole;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

@javax.jdo.annotations.PersistenceCapable(
        identityType = IdentityType.DATASTORE,
        schema = "isisExtensionsSecman",
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
            name = "ApplicationUser_username_UNQ",
            members = { "username" })
})
@javax.jdo.annotations.Queries( {
    @javax.jdo.annotations.Query(
            name = org.apache.isis.extensions.secman.api.user.dom.ApplicationUser.NAMED_QUERY_FIND_BY_USERNAME,
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.secman.jdo.user.dom.ApplicationUser "
                    + "WHERE username == :username"),
    @javax.jdo.annotations.Query(
            name = org.apache.isis.extensions.secman.api.user.dom.ApplicationUser.NAMED_QUERY_FIND_BY_EMAIL_ADDRESS,
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.secman.jdo.user.dom.ApplicationUser "
                    + "WHERE emailAddress == :emailAddress"),
    @javax.jdo.annotations.Query(
            name = org.apache.isis.extensions.secman.api.user.dom.ApplicationUser.NAMED_QUERY_FIND_BY_ATPATH,
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.secman.jdo.user.dom.ApplicationUser "
                    + "WHERE atPath == :atPath"),
    @javax.jdo.annotations.Query(
            name = org.apache.isis.extensions.secman.api.user.dom.ApplicationUser.NAMED_QUERY_FIND,
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.secman.jdo.user.dom.ApplicationUser "
                    + "WHERE username.matches(:regex)"
                    + " || familyName.matches(:regex)"
                    + " || givenName.matches(:regex)"
                    + " || knownAs.matches(:regex)"
                    + " || emailAddress.matches(:regex)")
})
@DomainObject(
        objectType = "isis.ext.secman.ApplicationUser",
        autoCompleteRepository = ApplicationUserRepository.class,
        autoCompleteAction = "findMatching"
        )
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_ROOT
        )
public class ApplicationUser implements Comparable<ApplicationUser>,
        org.apache.isis.extensions.secman.api.user.dom.ApplicationUser {

    @Inject private ApplicationUserRepository applicationUserRepository;
    @Inject private ApplicationPermissionRepository applicationPermissionRepository;
    @Inject private UserService userService;
    /**
     * Optional service, if configured then is used to evaluate permissions within
     * {@link org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermissionValueSet#evaluate(ApplicationFeatureId, ApplicationPermissionMode)}
     * else will fallback to a default implementation.
     */
    @Inject private PermissionsEvaluationService permissionsEvaluationService;
    @Inject private SecmanConfiguration configBean;


    // -- NAME
    // (derived property)

    public static class NameDomainEvent extends PropertyDomainEvent<String> {}

    @Override
    @javax.jdo.annotations.NotPersistent
    @Property(
            domainEvent = NameDomainEvent.class,
            editing = Editing.DISABLED
            )
    @PropertyLayout(
            hidden=Where.OBJECT_FORMS,
            fieldSetId="Id",
            sequence = "1")
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

    public static class UsernameDomainEvent extends PropertyDomainEvent<String> {}

    @javax.jdo.annotations.Column(allowsNull="false", length = Username.MAX_LENGTH)
    @Property(
            domainEvent = UsernameDomainEvent.class,
            editing = Editing.DISABLED
            )
    @PropertyLayout(
            hidden=Where.PARENTED_TABLES,
            fieldSetId="Id",
            sequence = "1")
    @Getter @Setter
    private String username;


    // -- FAMILY NAME

    public static class FamilyNameDomainEvent extends PropertyDomainEvent<String> {}


    @javax.jdo.annotations.Column(allowsNull="true", length = FamilyName.MAX_LENGTH)
    @Property(
            domainEvent = FamilyNameDomainEvent.class,
            editing = Editing.DISABLED
            )
    @PropertyLayout(
            hidden=Where.ALL_TABLES,
            fieldSetId="Name",
            sequence = "2.1")
    @Getter @Setter
    private String familyName;


    // -- GIVEN NAME

    public static class GivenNameDomainEvent extends PropertyDomainEvent<String> {}


    @javax.jdo.annotations.Column(allowsNull="true", length = GivenName.MAX_LENGTH)
    @Property(
            domainEvent = GivenNameDomainEvent.class,
            editing = Editing.DISABLED
            )
    @PropertyLayout(
            hidden=Where.ALL_TABLES,
            fieldSetId="Name",
            sequence = "2.2")
    @Getter @Setter
    private String givenName;


    // -- KNOWN AS

    public static class KnownAsDomainEvent extends PropertyDomainEvent<String> {}


    @javax.jdo.annotations.Column(allowsNull="true", length = KnownAs.MAX_LENGTH)
    @Property(
            domainEvent = KnownAsDomainEvent.class,
            editing = Editing.DISABLED
            )
    @PropertyLayout(
            hidden=Where.ALL_TABLES,
            fieldSetId="Name",
            sequence = "2.3")
    @Getter @Setter
    private String knownAs;


    // -- EMAIL ADDRESS

    public static class EmailAddressDomainEvent extends PropertyDomainEvent<String> {}

    @javax.jdo.annotations.Column(allowsNull="true", length = EmailAddress.MAX_LENGTH)
    @Property(
            domainEvent = EmailAddressDomainEvent.class,
            editing = Editing.DISABLED
            )
    @PropertyLayout(fieldSetId="Contact Details", sequence = "3.1")
    @Getter @Setter
    private String emailAddress;


    // -- PHONE NUMBER

    public static class PhoneNumberDomainEvent extends PropertyDomainEvent<String> {}


    @javax.jdo.annotations.Column(allowsNull="true", length = PhoneNumber.MAX_LENGTH)
    @Property(
            domainEvent = PhoneNumberDomainEvent.class,
            editing = Editing.DISABLED
            )
    @PropertyLayout(fieldSetId="Contact Details", sequence = "3.2")
    @Getter @Setter
    private String phoneNumber;


    // -- FAX NUMBER

    public static class FaxNumberDomainEvent extends PropertyDomainEvent<String> {}

    @javax.jdo.annotations.Column(allowsNull="true", length = FaxNumber.MAX_LENGTH)
    @Property(
            domainEvent = FaxNumberDomainEvent.class,
            editing = Editing.DISABLED
            )
    @PropertyLayout(
            hidden=Where.PARENTED_TABLES,
            fieldSetId="Contact Details",
            sequence = "3.3")
    @Getter @Setter
    private String faxNumber;


    // -- AT PATH

    public static class AtPathDomainEvent extends PropertyDomainEvent<String> {}


    @javax.jdo.annotations.Column(name = "atPath", allowsNull="true")
    @Property(
            domainEvent = AtPathDomainEvent.class,
            editing = Editing.DISABLED
            )
    @PropertyLayout(fieldSetId="atPath", sequence = "3.4")
    @Getter @Setter
    private String atPath;


    // -- ACCOUNT TYPE

    public static class AccountTypeDomainEvent extends PropertyDomainEvent<AccountType> {}


    @javax.jdo.annotations.Column(allowsNull="false")
    @Property(
            domainEvent = AccountTypeDomainEvent.class,
            editing = Editing.DISABLED
            )
    @PropertyLayout(fieldSetId="Status", sequence = "3")
    @Getter @Setter
    private AccountType accountType;


    // -- STATUS

    public static class StatusDomainEvent extends PropertyDomainEvent<ApplicationUserStatus> {}


    @javax.jdo.annotations.Column(allowsNull="false")
    @Property(
            domainEvent = StatusDomainEvent.class,
            editing = Editing.DISABLED
            )
    @PropertyLayout(fieldSetId="Status", sequence = "4")
    @Getter @Setter
    private ApplicationUserStatus status;


    // -- ENCRYPTED PASSWORD


    @javax.jdo.annotations.Column(allowsNull="true")
    @PropertyLayout(hidden=Where.EVERYWHERE)
    @Getter @Setter
    private String encryptedPassword;

    public boolean hideEncryptedPassword() {
        return !applicationUserRepository.isPasswordFeatureEnabled(this);
    }


    // -- HAS PASSWORD
    // (derived property)

    public static class HasPasswordDomainEvent extends PropertyDomainEvent<Boolean> {}

    @Property(
            domainEvent = HasPasswordDomainEvent.class,
            editing = Editing.DISABLED
            )
    @PropertyLayout(fieldSetId="Status", sequence = "4")
    @Override
    public boolean isHasPassword() {
        return _Strings.isNotEmpty(getEncryptedPassword());
    }

    public boolean hideHasPassword() {
        return !applicationUserRepository.isPasswordFeatureEnabled(this);
    }



    // ROLES

    public static class RolesDomainEvent extends CollectionDomainEvent<ApplicationRole> {}

    @javax.jdo.annotations.Persistent(table="ApplicationUserRoles")
    @javax.jdo.annotations.Join(column="userId")
    @javax.jdo.annotations.Element(column="roleId")
    @Collection(
            domainEvent = RolesDomainEvent.class
            )
    @CollectionLayout(
            defaultView="table",
            sequence = "20")
    @Getter @Setter
    private SortedSet<ApplicationRole> roles = new TreeSet<>();


    // -- PERMISSION SET

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
                        _Lists.map(_Casts.uncheckedCast(permissions), ApplicationPermission.Functions.AS_VALUE),
                        permissionsEvaluationService);
    }


    // -- IS FOR SELF OR RUN AS ADMINISTRATOR

    @Override
    public boolean isForSelfOrRunAsAdministrator() {
        return isForSelf() || isRunAsAdministrator();
    }

    // -- HELPERS

    boolean isForSelf() {
        final String currentUserName = userService.currentUserElseFail().getName();
        return Objects.equals(getUsername(), currentUserName);
    }
    boolean isRunAsAdministrator() {
        final UserMemento currentUser = userService.currentUserElseFail();
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



}
