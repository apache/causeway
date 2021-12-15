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

import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.Element;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.Unique;
import javax.jdo.annotations.Uniques;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;

import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.extensions.secman.applib.role.dom.ApplicationRole;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUserStatus;

import lombok.Getter;
import lombok.Setter;

@PersistenceCapable(
        identityType = IdentityType.DATASTORE,
        schema = "isisExtensionsSecman",
        table = "ApplicationUser")
@Inheritance(
        strategy = InheritanceStrategy.NEW_TABLE)
@DatastoreIdentity(
        strategy = IdGeneratorStrategy.NATIVE, column = "id")
@Version(
        strategy = VersionStrategy.VERSION_NUMBER,
        column = "version")
@Uniques({
    @Unique(
            name = "ApplicationUser_username_UNQ",
            members = { "username" })
})
@Queries( {
    @Query(
            name = org.apache.isis.extensions.secman.applib.user.dom.ApplicationUser.NAMED_QUERY_FIND_BY_USERNAME,
            value = "SELECT "
                    + "FROM " + ApplicationUser.FQCN
                    + " WHERE username == :username"),
    @Query(
            name = org.apache.isis.extensions.secman.applib.user.dom.ApplicationUser.NAMED_QUERY_FIND_BY_EMAIL_ADDRESS,
            value = "SELECT "
                    + "FROM " + ApplicationUser.FQCN
                    + " WHERE emailAddress == :emailAddress"),
    @Query(
            name = org.apache.isis.extensions.secman.applib.user.dom.ApplicationUser.NAMED_QUERY_FIND_BY_ATPATH,
            value = "SELECT "
                    + "FROM " + ApplicationUser.FQCN
                    + " WHERE atPath == :atPath"),
    @Query(
            name = org.apache.isis.extensions.secman.applib.user.dom.ApplicationUser.NAMED_QUERY_FIND,
            value = "SELECT "
                    + "FROM " + ApplicationUser.FQCN
                    + " WHERE username.matches(:regex)"
                    + " || familyName.matches(:regex)"
                    + " || givenName.matches(:regex)"
                    + " || knownAs.matches(:regex)"
                    + " || emailAddress.matches(:regex)")
})
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

    protected final static String FQCN = "org.apache.isis.extensions.secman.jdo.user.dom.ApplicationUser";


    // -- USERNAME

    @Column(allowsNull = "false", length = Username.MAX_LENGTH)
    private String username;

    @Username
    @Override
    public String getUsername() {
        return username;
    }
    @Override
    public void setUsername(final String username) { this.username = username; }


    // -- FAMILY NAME

    @Column(allowsNull = "true", length = FamilyName.MAX_LENGTH)
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

    @Column(allowsNull = "true", length = GivenName.MAX_LENGTH)
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

    @Column(allowsNull = "true", length = KnownAs.MAX_LENGTH)
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

    @Column(allowsNull="true", length = EmailAddress.MAX_LENGTH)
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

    @Column(allowsNull = "true", length = PhoneNumber.MAX_LENGTH)
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

    @Column(allowsNull = "true", length = FaxNumber.MAX_LENGTH)
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

    // -- LOCALE

    @UserLocale
    @Column(allowsNull="true")
    @Getter @Setter
    private Locale language;

    @UserLocale
    @Column(allowsNull="true")
    @Getter @Setter
    private Locale numberFormat;

    @UserLocale
    @Column(allowsNull="true")
    @Getter @Setter
    private Locale timeFormat;

    // -- AT PATH

    @Column(allowsNull="true")
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

    @Column(allowsNull = "false")
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

    @Column(allowsNull = "false")
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

    @Column(allowsNull = "true")
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

    @Persistent(table="ApplicationUserRoles")
    @Join(column="userId")
    @Element(column="roleId")
    private SortedSet<org.apache.isis.extensions.secman.jdo.role.dom.ApplicationRole> roles = new TreeSet<>();

    @Roles
    @Override
    public SortedSet<ApplicationRole> getRoles() {
        return _Casts.uncheckedCast(roles);
    }


}
