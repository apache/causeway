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
package org.apache.causeway.extensions.secman.jdo.user.dom;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Named;
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
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.causeway.applib.annotation.BookmarkPolicy;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.jaxb.PersistentEntityAdapter;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser.Nq;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserStatus;

import lombok.Getter;
import lombok.Setter;

@PersistenceCapable(
        identityType = IdentityType.DATASTORE,
        schema = ApplicationUser.SCHEMA,
        table = ApplicationUser.TABLE)
@Uniques({
    @Unique(name = "ApplicationUser__username__UNQ", members = { "username" })})
@Queries( {
    @Query(
            name = Nq.FIND_BY_USERNAME,
            value = "SELECT "
                  + "  FROM " + ApplicationUser.FQCN
                  + " WHERE username == :username"),
    @Query(
            name = Nq.FIND_BY_EMAIL_ADDRESS,
            value = "SELECT "
                  + "  FROM " + ApplicationUser.FQCN
                  + " WHERE emailAddress == :emailAddress"),
    @Query(
            name = Nq.FIND_BY_ATPATH,
            value = "SELECT "
                  + "  FROM " + ApplicationUser.FQCN
                  + " WHERE atPath == :atPath"),
    @Query(
            name = Nq.FIND,
            value = "SELECT "
                  + "  FROM " + ApplicationUser.FQCN
                  + " WHERE username.matches(:regex)"
                  + "    || familyName.matches(:regex)"
                  + "    || givenName.matches(:regex)"
                  + "    || knownAs.matches(:regex)"
                  + "    || emailAddress.matches(:regex)")
})
@Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
@DatastoreIdentity(strategy = IdGeneratorStrategy.NATIVE, column = "id")
@Version(strategy = VersionStrategy.VERSION_NUMBER, column = "version")
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
@Named(ApplicationUser.LOGICAL_TYPE_NAME)
@DomainObject(
        autoCompleteRepository = ApplicationUserRepository.class,
        autoCompleteMethod = "findMatching"
        )
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_ROOT
        )
public class ApplicationUser
    extends org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser {

    protected final static String FQCN = "org.apache.causeway.extensions.secman.jdo.user.dom.ApplicationUser";


    @Column(allowsNull = Username.ALLOWS_NULL, length = Username.MAX_LENGTH)
    @Username
    @Getter @Setter
    private String username;


    @Column(allowsNull = FamilyName.ALLOWS_NULL, length = FamilyName.MAX_LENGTH)
    @FamilyName
    @Getter @Setter
    private String familyName;


    @Column(allowsNull = GivenName.ALLOWS_NULL, length = GivenName.MAX_LENGTH)
    @GivenName
    @Getter @Setter
    private String givenName;


    @Column(allowsNull = KnownAs.ALLOWS_NULL, length = KnownAs.MAX_LENGTH)
    @KnownAs
    @Getter @Setter
    private String knownAs;


    @Column(allowsNull = EmailAddress.ALLOWS_NULL, length = EmailAddress.MAX_LENGTH)
    @EmailAddress
    @Getter @Setter
    private String emailAddress;


    @Column(allowsNull = PhoneNumber.ALLOWS_NULL, length = PhoneNumber.MAX_LENGTH)
    @PhoneNumber
    @Getter @Setter
    private String phoneNumber;


    @Column(allowsNull = FaxNumber.ALLOWS_NULL, length = FaxNumber.MAX_LENGTH)
    @FaxNumber
    @Getter @Setter
    private String faxNumber;


    @Column(allowsNull = Language.ALLOWS_NULL)
    @Language
    @Getter @Setter
    private java.util.Locale language;


    @Column(allowsNull = NumberFormat.ALLOWS_NULL)
    @NumberFormat
    @Getter @Setter
    private java.util.Locale numberFormat;


    @Column(allowsNull = TimeFormat.ALLOWS_NULL)
    @TimeFormat
    @Getter @Setter
    private java.util.Locale timeFormat;


    @Column(allowsNull = AtPath.ALLOWS_NULL, length = AtPath.MAX_LENGTH)
    @AtPath
    @Getter @Setter
    private String atPath;


    @Column(allowsNull = AccountType.ALLOWS_NULL)
    @AccountType
    @Getter
    private org.apache.causeway.extensions.secman.applib.user.dom.AccountType accountType;
    @Override
    public void setAccountType(final org.apache.causeway.extensions.secman.applib.user.dom.AccountType accountType) {
        this.accountType = accountType;
    }


    @Column(allowsNull = Status.ALLOWS_NULL)
    @Status
    @Getter @Setter
    private ApplicationUserStatus status;


    @Column(allowsNull = EncryptedPassword.ALLOWS_NULL, length = EncryptedPassword.MAX_LENGTH)
    @EncryptedPassword
    @Getter @Setter
    private String encryptedPassword;



    // ROLES

    @Persistent(table = Roles.Persistence.TABLE)
    @Join(column = Roles.Persistence.JOIN_COLUMN)
    @Element(column = Roles.Persistence.INVERSE_JOIN_COLUMN)
    @Roles
    private SortedSet<org.apache.causeway.extensions.secman.jdo.role.dom.ApplicationRole> roles = new TreeSet<>();
    @Override
    public SortedSet<org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole> getRoles() {
        return _Casts.uncheckedCast(roles);
    }
}
