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
package org.apache.causeway.extensions.secman.jpa.user.dom;

import java.util.Set;
import java.util.TreeSet;

import javax.inject.Named;
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
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.causeway.applib.annotation.BookmarkPolicy;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.jaxb.PersistentEntityAdapter;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser.Nq;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserStatus;
import org.apache.causeway.extensions.secman.jpa.role.dom.ApplicationRole;
import org.apache.causeway.persistence.jpa.applib.integration.CausewayEntityListener;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        schema = ApplicationUser.SCHEMA,
        name = ApplicationUser.TABLE,
        uniqueConstraints =
            @UniqueConstraint(name = "ApplicationUser__username__UNQ", columnNames = { "username" })
)
@NamedQueries({
    @NamedQuery(
            name = Nq.FIND_BY_USERNAME,
            query = "SELECT u "
                  + "  FROM ApplicationUser u "
                  + " WHERE u.username = :username"),
    @NamedQuery(
            name = Nq.FIND_BY_EMAIL_ADDRESS,
            query = "SELECT u "
                  + "  FROM ApplicationUser u "
                  + " WHERE u.emailAddress = :emailAddress"),
    @NamedQuery(
            name = Nq.FIND_BY_ATPATH,
            query = "SELECT u "
                  + "  FROM ApplicationUser u "
                  + " WHERE u.atPath = :atPath"),
    @NamedQuery(
            name = Nq.FIND,
            query = "SELECT u "
                  + "  FROM ApplicationUser u "
                  + " WHERE u.username LIKE :regex"
                  + "    OR u.familyName LIKE :regex"
                  + "    OR u.givenName LIKE :regex"
                  + "    OR u.knownAs LIKE :regex"
                  + "    OR u.emailAddress LIKE :regex")
})
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
@EntityListeners(CausewayEntityListener.class)
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

    @Id
    @GeneratedValue
    private Long id;


    @Version
    private Long version;


    @Column(nullable = Username.NULLABLE, length = Username.MAX_LENGTH)
    @Username
    @Getter @Setter
    private String username;


    @Column(nullable = FamilyName.NULLABLE, length = FamilyName.MAX_LENGTH)
    @FamilyName
    @Getter @Setter
    private String familyName;


    @Column(nullable = GivenName.NULLABLE, length = GivenName.MAX_LENGTH)
    @GivenName
    @Getter @Setter
    private String givenName;


    @Column(nullable = KnownAs.NULLABLE, length = KnownAs.MAX_LENGTH)
    @KnownAs
    @Getter @Setter
    private String knownAs;


    @Column(nullable = EmailAddress.NULLABLE, length = EmailAddress.MAX_LENGTH)
    @EmailAddress
    @Getter @Setter
    private String emailAddress;


    @Column(nullable = PhoneNumber.NULLABLE, length = PhoneNumber.MAX_LENGTH)
    @PhoneNumber
    @Getter @Setter
    private String phoneNumber;


    @Column(nullable = FaxNumber.NULLABLE, length= FaxNumber.MAX_LENGTH)
    @FaxNumber
    @Getter @Setter
    private String faxNumber;


    @Column(nullable = Language.NULLABLE)
    @Language
    @Getter @Setter
    private java.util.Locale language;

    @Column(nullable = NumberFormat.NULLABLE)
    @NumberFormat
    @Getter @Setter
    private java.util.Locale numberFormat;


    @Column(nullable = TimeFormat.NULLABLE)
    @TimeFormat
    @Getter @Setter
    private java.util.Locale timeFormat;


    @Column(nullable = AtPath.NULLABLE, length = AtPath.MAX_LENGTH)
    @AtPath
    @Getter @Setter
    private String atPath;


    @Column(nullable = AccountType.NULLABLE) @Enumerated(EnumType.STRING)
    @AccountType
    @Getter
    private org.apache.causeway.extensions.secman.applib.user.dom.AccountType accountType;
    @Override
    public void setAccountType(final org.apache.causeway.extensions.secman.applib.user.dom.AccountType accountType) {
        this.accountType = accountType;
    }


    @Column(nullable = Status.NULLABLE) @Enumerated(EnumType.STRING)
    @Status
    @Getter @Setter
    private ApplicationUserStatus status;


    @Column(nullable = EncryptedPassword.NULLABLE, length = EncryptedPassword.MAX_LENGTH)
    @EncryptedPassword
    @Getter @Setter
    private String encryptedPassword;



    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(
            schema = ApplicationUser.SCHEMA,
            name = Roles.Persistence.TABLE,
            joinColumns = {@JoinColumn(name = Roles.Persistence.JOIN_COLUMN)},
            inverseJoinColumns = {@JoinColumn(name = Roles.Persistence.INVERSE_JOIN_COLUMN)})
    @Roles
    private Set<ApplicationRole> roles = new TreeSet<>();

    @Override
    public Set<org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole> getRoles() {
        return _Casts.uncheckedCast(roles);
    }
}
