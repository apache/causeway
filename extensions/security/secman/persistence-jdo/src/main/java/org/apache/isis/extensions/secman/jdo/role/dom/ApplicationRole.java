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
package org.apache.isis.extensions.secman.jdo.role.dom;

import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.Unique;
import javax.jdo.annotations.Uniques;

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
import org.apache.isis.extensions.secman.jdo.permission.dom.ApplicationPermissionRepository;


@PersistenceCapable(
        identityType = IdentityType.DATASTORE,
        schema = "isisExtensionsSecman",
        table = "ApplicationRole")
@Inheritance(
        strategy = InheritanceStrategy.NEW_TABLE)
@DatastoreIdentity(
        strategy = IdGeneratorStrategy.NATIVE, column = "id")
@Uniques({
    @Unique(
            name = "ApplicationRole_name_UNQ", members = { "name" })
})
@Queries({
    @Query(
            name = org.apache.isis.extensions.secman.api.role.dom.ApplicationRole.NAMED_QUERY_FIND_BY_NAME,
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.secman.jdo.role.dom.ApplicationRole "
                    + "WHERE name == :name"),
    @Query(
            name = org.apache.isis.extensions.secman.api.role.dom.ApplicationRole.NAMED_QUERY_FIND_BY_NAME_CONTAINING,
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.secman.jdo.role.dom.ApplicationRole "
                    + "WHERE name.matches(:regex) ")
})
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

    @Inject private ApplicationPermissionRepository applicationPermissionRepository;


    // -- NAME

    @Column(allowsNull = "false", length = Name.MAX_LENGTH)
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

    @Column(allowsNull = "true", length = Description.MAX_LENGTH)
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

    @Persistent(mappedBy = "roles")
    private SortedSet<org.apache.isis.extensions.secman.jdo.user.dom.ApplicationUser> users = new TreeSet<>();

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
