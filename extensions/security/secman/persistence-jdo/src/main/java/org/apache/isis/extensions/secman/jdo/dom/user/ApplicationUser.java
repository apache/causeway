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
import java.util.Set;
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
import org.apache.isis.applib.annotation.MemberOrder;
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
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.extensions.secman.api.SecmanConfiguration;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionValueSet;
import org.apache.isis.extensions.secman.api.permission.PermissionsEvaluationService;
import org.apache.isis.extensions.secman.api.user.AccountType;
import org.apache.isis.extensions.secman.api.user.ApplicationUserStatus;
import org.apache.isis.extensions.secman.jdo.dom.permission.ApplicationPermission;
import org.apache.isis.extensions.secman.jdo.dom.permission.ApplicationPermissionRepository;
import org.apache.isis.extensions.secman.jdo.dom.role.ApplicationRole;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

@javax.jdo.annotations.PersistenceCapable(
        identityType = IdentityType.DATASTORE,
        schema = "isisSecman",
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
        objectType = "isis.ext.secman.ApplicationUser",
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

    @Inject private ApplicationUserRepository applicationUserRepository;
    @Inject private ApplicationPermissionRepository applicationPermissionRepository;
    @Inject private UserService userService;
    /**
     * Optional service, if configured then is used to evaluate permissions within
     * {@link org.apache.isis.extensions.secman.api.permission.ApplicationPermissionValueSet#evaluate(ApplicationFeatureId, ApplicationPermissionMode)}
     * else will fallback to a {@link org.apache.isis.extensions.secman.api.permission.PermissionsEvaluationService#DEFAULT default}
     * implementation.
     */
    @Inject private PermissionsEvaluationService permissionsEvaluationService;
    @Inject private SecmanConfiguration configBean;

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


    // -- encryptedPassword (hidden property)


    @javax.jdo.annotations.Column(allowsNull="true")
    @PropertyLayout(hidden=Where.EVERYWHERE)
    @Getter @Setter
    private String encryptedPassword;

    public boolean hideEncryptedPassword() {
        return !applicationUserRepository.isPasswordFeatureEnabled(this);
    }


    // -- hasPassword (derived property)

    public static class HasPasswordDomainEvent extends PropertyDomainEvent<Boolean> {}

    @Property(
            domainEvent = HasPasswordDomainEvent.class,
            editing = Editing.DISABLED
            )
    @MemberOrder(name="Status", sequence = "4")
    @Override
    public boolean isHasPassword() {
        return _Strings.isNotEmpty(getEncryptedPassword());
    }

    public boolean hideHasPassword() {
        return !applicationUserRepository.isPasswordFeatureEnabled(this);
    }

    // -- roles (collection)
    public static class RolesDomainEvent extends CollectionDomainEvent<ApplicationRole> {}

    @javax.jdo.annotations.Persistent(table="ApplicationUserRoles")
    @javax.jdo.annotations.Join(column="userId")
    @javax.jdo.annotations.Element(column="roleId")
    @Collection(
            domainEvent = RolesDomainEvent.class
            )
    @CollectionLayout(
            defaultView="table"
            )
    @MemberOrder(sequence = "20")
    @Getter @Setter
    private Set<ApplicationRole> roles = new TreeSet<>();


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

    @Override
    public boolean isForSelfOrRunAsAdministrator() {
        return isForSelf() || isRunAsAdministrator();
    }

    // -- helpers
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
