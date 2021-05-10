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
package org.apache.isis.extensions.secman.jpa.role.dom;

import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.Bounding;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.util.Equality;
import org.apache.isis.applib.util.Hashing;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ToString;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUser;
import org.apache.isis.extensions.secman.jpa.permission.dom.ApplicationPermissionRepository;
import org.apache.isis.persistence.jpa.applib.integration.JpaEntityInjectionPointResolver;


@SuppressWarnings("JpaQlInspection")
@Entity
@Table(
        schema = "isisExtensionsSecman",
        name = "ApplicationRole",
        uniqueConstraints =
            @UniqueConstraint(
                    name = "ApplicationRole_name_UNQ",
                    columnNames={"name"})
)
@NamedQueries({
    @NamedQuery(
            name = org.apache.isis.extensions.secman.api.role.dom.ApplicationRole.NAMED_QUERY_FIND_BY_NAME,
            query = "SELECT r "
                  + "FROM org.apache.isis.extensions.secman.jpa.role.dom.ApplicationRole r "
                  + "WHERE r.name = :name"),
    @NamedQuery(
            name = org.apache.isis.extensions.secman.api.role.dom.ApplicationRole.NAMED_QUERY_FIND_BY_NAME_CONTAINING,
            query = "SELECT r "
                  + "FROM org.apache.isis.extensions.secman.jpa.role.dom.ApplicationRole r "
                  + "WHERE r.name LIKE :regex"),
})
@EntityListeners(JpaEntityInjectionPointResolver.class)
@DomainObject(
        bounding = Bounding.BOUNDED,
        //		bounded = true,
        objectType = "isis.ext.secman.ApplicationRole",
        autoCompleteRepository = ApplicationRoleRepository.class,
        autoCompleteAction = "findMatching"
        )
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_ROOT
        )
public class ApplicationRole
    implements org.apache.isis.extensions.secman.api.role.dom.ApplicationRole {

    @Inject private transient ApplicationPermissionRepository applicationPermissionRepository;


    @Id
    @GeneratedValue
    private Long id;


    // -- NAME

    @Column(nullable = false, length = Name.MAX_LENGTH)
    private String name;

    @Name
    @Override
    public String getName() {
        return name;
    }
    @Override
    public void setName(String name) {
        this.name = name;
    }
    // -- DESCRIPTION

    @Column(nullable = true, length = Description.MAX_LENGTH)
    private String description;

    @Description
    @Override
    public String getDescription() {
        return description;
    }
    @Override
    public void setDescription(String description) {
        this.description = description;
    }


    // -- USERS

    @Users
    @ManyToMany
    private SortedSet<org.apache.isis.extensions.secman.jpa.user.dom.ApplicationUser> users = new TreeSet<>();

    @Users
    @Override
    public SortedSet<ApplicationUser> getUsers() {
        return _Casts.uncheckedCast(users);
    }
    // necessary for integration tests
    public void addToUsers(final ApplicationUser applicationUser) {
        getUsers().add(applicationUser);
    }


    // -- PERMISSIONS

    @Permissions
    public List<org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermission> getPermissions() {
        return applicationPermissionRepository.findByRole(this);
    }


    // -- equals, hashCode, compareTo, toString

    private static final Comparator<ApplicationRole> comparator =
            Comparator.comparing(ApplicationRole::getName);

    private static final Equality<ApplicationRole> equality =
            ObjectContracts.checkEquals(ApplicationRole::getName);

    private static final Hashing<ApplicationRole> hashing =
            ObjectContracts.hashing(ApplicationRole::getName);

    private static final ToString<ApplicationRole> toString =
            ObjectContracts.toString("name", ApplicationRole::getName);


    @Override
    public int compareTo(final org.apache.isis.extensions.secman.api.role.dom.ApplicationRole other) {
        return comparator.compare(this, (ApplicationRole)other);
    }

    @Override
    public boolean equals(final Object obj) {
        return equality.equals(this, obj);
    }

    @Override
    public int hashCode() {
        return hashing.hashCode(this);
    }

    @Override
    public String toString() {
        return toString.toString(this);
    }

}
