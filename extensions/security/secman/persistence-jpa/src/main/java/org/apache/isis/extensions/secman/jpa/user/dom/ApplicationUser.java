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
package org.apache.isis.extensions.secman.jpa.user.dom;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.user.UserMemento;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ObjectContracts.ObjectContract;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.extensions.secman.api.SecmanConfiguration;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermission;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermissionValueSet;
import org.apache.isis.extensions.secman.api.permission.spi.PermissionsEvaluationService;
import org.apache.isis.extensions.secman.api.role.dom.ApplicationRole;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUserStatus;
import org.apache.isis.extensions.secman.jpa.permission.dom.ApplicationPermissionRepository;
import org.apache.isis.persistence.jpa.applib.integration.JpaEntityInjectionPointResolver;

import lombok.val;

@Entity
@Table(
        schema = "isisExtensionsSecman",
        name = "ApplicationUser",
        uniqueConstraints =
            @UniqueConstraint(
                    name = "ApplicationUser_username_UNQ",
                    columnNames={"username"})
)
@NamedQueries({
    @NamedQuery(
            name = org.apache.isis.extensions.secman.api.user.dom.ApplicationUser.NAMED_QUERY_FIND_BY_USERNAME,
            query = "SELECT u "
                  + "  FROM ApplicationUser u "
                  + " WHERE u.username = :username"),
    @NamedQuery(
            name = org.apache.isis.extensions.secman.api.user.dom.ApplicationUser.NAMED_QUERY_FIND_BY_EMAIL_ADDRESS,
            query = "SELECT u "
                  + "  FROM ApplicationUser u "
                  + " WHERE u.emailAddress = :emailAddress"),
    @NamedQuery(
            name = org.apache.isis.extensions.secman.api.user.dom.ApplicationUser.NAMED_QUERY_FIND_BY_ATPATH,
            query = "SELECT u "
                  + "  FROM ApplicationUser u "
                  + " WHERE u.atPath = :atPath"),
    @NamedQuery(
            name = org.apache.isis.extensions.secman.api.user.dom.ApplicationUser.NAMED_QUERY_FIND,
            query = "SELECT u "
                  + "  FROM ApplicationUser u "
                  + " WHERE u.username LIKE :regex"
                  + "    OR u.familyName LIKE :regex"
                  + "    OR u.givenName LIKE :regex"
                  + "    OR u.knownAs LIKE :regex"
                  + "    OR u.emailAddress LIKE :regex")
})
@EntityListeners(JpaEntityInjectionPointResolver.class)
@DomainObject(
        objectType = "isis.ext.secman.ApplicationUser",
        autoCompleteRepository = ApplicationUserRepository.class,
        autoCompleteAction = "findMatching"
        )
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_ROOT
        )
public class ApplicationUser
    implements org.apache.isis.extensions.secman.api.user.dom.ApplicationUser {

    @Inject private transient ApplicationUserRepository applicationUserRepository;
    @Inject private transient ApplicationPermissionRepository applicationPermissionRepository;
    @Inject private transient UserService userService;
    /**
     * Optional service, if configured then is used to evaluate permissions within
     * {@link ApplicationPermissionValueSet#evaluate(ApplicationFeatureId, ApplicationPermissionMode)}
     * else will fallback to a default implementation.
     */
    @Inject private transient PermissionsEvaluationService permissionsEvaluationService;
    @Inject private transient SecmanConfiguration configBean;


    @Id
    @GeneratedValue
    private Long id;


    // -- NAME

    @Name
    @Transient
    @Override
    public String getName() {
        return org.apache.isis.extensions.secman.api.user.dom.ApplicationUser.super.getName();
    }


    // -- USERNAME

    @Column(nullable = false, length = Username.MAX_LENGTH)
    private String username;

    @Username
    @Override
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) { this.username = username; }


    // -- FAMILY NAME


    @Column(nullable = true, length = FamilyName.MAX_LENGTH)
    private String familyName;

    @FamilyName
    @Override
    public String getFamilyName() {
        return familyName;
    }
    @Override
    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }


    // -- GIVEN NAME

    @Column(nullable = true, length = GivenName.MAX_LENGTH)
    private String givenName;

    @GivenName
    @Override
    public String getGivenName() {
        return givenName;
    }
    @Override
    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }


    // -- KNOWN AS

    @Column(nullable = true, length = KnownAs.MAX_LENGTH)
    private String knownAs;

    @KnownAs
    @Override
    public String getKnownAs() {
        return knownAs;
    }
    @Override
    public void setKnownAs(String knownAs) {
        this.knownAs = knownAs;
    }


    // -- EMAIL ADDRESS

    @Column(nullable = true, length = EmailAddress.MAX_LENGTH)
    private String emailAddress;

    @EmailAddress
    @Override
    public String getEmailAddress() {
        return emailAddress;
    }
    @Override
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }


    // -- PHONE NUMBER

    @Column(nullable = true, length = PhoneNumber.MAX_LENGTH)
    private String phoneNumber;

    @PhoneNumber
    @Override
    public String getPhoneNumber() {
        return phoneNumber;
    }
    @Override
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }


    // -- FAX NUMBER

    @Column(nullable = true, length= FaxNumber.MAX_LENGTH)
    private String faxNumber;

    @FaxNumber
    @Override
    public String getFaxNumber() {
        return faxNumber;
    }
    @Override
    public void setFaxNumber(String faxNumber) {
        this.faxNumber = faxNumber;
    }


    // -- AT PATH

    @Column(nullable = true)
    private String atPath;

    @AtPath
    @Override
    public String getAtPath() {
        return atPath;
    }
    @Override
    public void setAtPath(String atPath) {
        this.atPath = atPath;
    }


    // -- ACCOUNT TYPE

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private org.apache.isis.extensions.secman.api.user.dom.AccountType accountType;

    @AccountType
    @Override
    public org.apache.isis.extensions.secman.api.user.dom.AccountType getAccountType() {
        return accountType;
    }
    @Override
    public void setAccountType(org.apache.isis.extensions.secman.api.user.dom.AccountType accountType) {
        this.accountType = accountType;
    }


    // -- STATUS

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ApplicationUserStatus status;

    @Status
    @Override
    public ApplicationUserStatus getStatus() {
        return status;
    }
    @Override
    public void setStatus(ApplicationUserStatus status) {
        this.status = status;
    }


    // -- ENCRYPTED PASSWORD

    @Column(nullable = true)
    private String encryptedPassword;

    @EncryptedPassword
    @Override
    public String getEncryptedPassword() {
        return encryptedPassword;
    }
    @Override
    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public boolean hideEncryptedPassword() {
        return !applicationUserRepository.isPasswordFeatureEnabled(this);
    }


    // -- HAS PASSWORD

    @HasPassword
    @Override
    public boolean isHasPassword() {
        return org.apache.isis.extensions.secman.api.user.dom.ApplicationUser.super.isHasPassword();
    }

    @Override
    public boolean hideHasPassword() {
        return !applicationUserRepository.isPasswordFeatureEnabled(this);
    }



    // ROLES

    @ManyToMany(mappedBy = "users", cascade = CascadeType.ALL)
    @JoinTable(
            name = "ApplicationUserRoles",
            joinColumns = {@JoinColumn(name = "userId")},
            inverseJoinColumns = {@JoinColumn(name = "roleId")})
    private SortedSet<org.apache.isis.extensions.secman.jpa.role.dom.ApplicationRole> roles = new TreeSet<>();

    @Roles
    @Override
    public SortedSet<ApplicationRole> getRoles() {
        return _Casts.uncheckedCast(roles);
    }


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


    // -- HELPERS


    @Programmatic
    @Override
    public String getAdminRoleName() {
        return configBean.getAdminRoleName();
    }

    @Programmatic
    @Override
    public UserMemento currentUser() {
        return userService.currentUserElseFail();
    }


    // -- equals, hashCode, compareTo, toString
    private static final String propertyNames = "username";

    private static final ObjectContract<ApplicationUser> contract =
            ObjectContracts.parse(ApplicationUser.class, propertyNames);


    @Override
    public int compareTo(final org.apache.isis.extensions.secman.api.user.dom.ApplicationUser other) {

        return contract.compare(this, (ApplicationUser)other);
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
