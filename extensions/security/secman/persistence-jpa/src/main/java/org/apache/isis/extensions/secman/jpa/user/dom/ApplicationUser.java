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

import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

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
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.extensions.secman.applib.role.dom.ApplicationRole;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUserStatus;
import org.apache.isis.persistence.jpa.applib.integration.IsisEntityListener;

import lombok.Getter;
import lombok.Setter;

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
            name = org.apache.isis.extensions.secman.applib.user.dom.ApplicationUser.NAMED_QUERY_FIND_BY_USERNAME,
            query = "SELECT u "
                  + "  FROM ApplicationUser u "
                  + " WHERE u.username = :username"),
    @NamedQuery(
            name = org.apache.isis.extensions.secman.applib.user.dom.ApplicationUser.NAMED_QUERY_FIND_BY_EMAIL_ADDRESS,
            query = "SELECT u "
                  + "  FROM ApplicationUser u "
                  + " WHERE u.emailAddress = :emailAddress"),
    @NamedQuery(
            name = org.apache.isis.extensions.secman.applib.user.dom.ApplicationUser.NAMED_QUERY_FIND_BY_ATPATH,
            query = "SELECT u "
                  + "  FROM ApplicationUser u "
                  + " WHERE u.atPath = :atPath"),
    @NamedQuery(
            name = org.apache.isis.extensions.secman.applib.user.dom.ApplicationUser.NAMED_QUERY_FIND,
            query = "SELECT u "
                  + "  FROM ApplicationUser u "
                  + " WHERE u.username LIKE :regex"
                  + "    OR u.familyName LIKE :regex"
                  + "    OR u.givenName LIKE :regex"
                  + "    OR u.knownAs LIKE :regex"
                  + "    OR u.emailAddress LIKE :regex")
})
@EntityListeners(IsisEntityListener.class)
@DomainObject(
        logicalTypeName = ApplicationUser.LOGICAL_TYPE_NAME,
        autoCompleteRepository = ApplicationUserRepository.class,
        autoCompleteMethod = "findMatching"
        )
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_ROOT
        )
public class ApplicationUser
    extends org.apache.isis.extensions.secman.applib.user.dom.ApplicationUser {


    @Id
    @GeneratedValue
    private Long id;

    @Version
    private Long version;


    // -- USERNAME

    @Column(nullable = false, length = Username.MAX_LENGTH)
    private String username;

    @Username
    @Override
    public String getUsername() {
        return username;
    }
    @Override
    public void setUsername(final String username) { this.username = username; }


    // -- FAMILY NAME

    @Column(nullable = true, length = FamilyName.MAX_LENGTH)
    private String familyName;

    @FamilyName
    @Override
    public String getFamilyName() {
        return familyName;
    }
    @Override
    public void setFamilyName(final String familyName) {
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
    public void setGivenName(final String givenName) {
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
    public void setKnownAs(final String knownAs) {
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
    public void setEmailAddress(final String emailAddress) {
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
    public void setPhoneNumber(final String phoneNumber) {
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
    public void setFaxNumber(final String faxNumber) {
        this.faxNumber = faxNumber;
    }

    // -- REGIONAL SETTINGS

    @UserLocale
    @Column(nullable = true)
    @Getter @Setter
    private Locale language;

    @UserLocale
    @Column(nullable = true)
    @Getter @Setter
    private Locale numberFormat;

    @UserLocale
    @Column(nullable = true)
    @Getter @Setter
    private Locale timeFormat;

    // -- AT PATH

    @Column(nullable = true)
    private String atPath;

    @AtPath
    @Override
    public String getAtPath() {
        return atPath;
    }
    @Override
    public void setAtPath(final String atPath) {
        this.atPath = atPath;
    }


    // -- ACCOUNT TYPE

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private org.apache.isis.extensions.secman.applib.user.dom.AccountType accountType;

    @AccountType
    @Override
    public org.apache.isis.extensions.secman.applib.user.dom.AccountType getAccountType() {
        return accountType;
    }
    @Override
    public void setAccountType(final org.apache.isis.extensions.secman.applib.user.dom.AccountType accountType) {
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
    public void setStatus(final ApplicationUserStatus status) {
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
    public void setEncryptedPassword(final String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }


    // ROLES

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(
            schema = "isisExtensionsSecman",
            name = "ApplicationUserRoles",
            joinColumns = {@JoinColumn(name = "userId")},
            inverseJoinColumns = {@JoinColumn(name = "roleId")})
    private Set<org.apache.isis.extensions.secman.jpa.role.dom.ApplicationRole> roles = new TreeSet<>();

    @Roles
    @Override
    public Set<ApplicationRole> getRoles() {
        return _Casts.uncheckedCast(roles);
    }


}
