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
package org.apache.isis.extensions.secman.jpa.dom.role;

import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;

import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.Bounding;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.isis.applib.types.DescriptionType;
import org.apache.isis.applib.util.Equality;
import org.apache.isis.applib.util.Hashing;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ToString;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionRule;
import org.apache.isis.extensions.secman.jpa.dom.permission.ApplicationPermission;
import org.apache.isis.extensions.secman.jpa.dom.permission.ApplicationPermissionRepository;
import org.apache.isis.extensions.secman.jpa.dom.user.ApplicationUser;

import lombok.Getter;
import lombok.Setter;

@javax.jdo.annotations.PersistenceCapable(
        identityType = IdentityType.DATASTORE,
        schema = "isisExtensionsSecman",
        table = "ApplicationRole")
@javax.jdo.annotations.Inheritance(
        strategy = InheritanceStrategy.NEW_TABLE)
@javax.jdo.annotations.DatastoreIdentity(
        strategy = IdGeneratorStrategy.NATIVE, column = "id")
@javax.jdo.annotations.Uniques({
    @javax.jdo.annotations.Unique(
            name = "ApplicationRole_name_UNQ", members = { "name" })
})
@javax.jdo.annotations.Queries({
    @javax.jdo.annotations.Query(
            name = "findByName", language = "JDOQL",
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.secman.jdo.dom.role.ApplicationRole "
                    + "WHERE name == :name"),
    @javax.jdo.annotations.Query(
            name = "findByNameContaining", language = "JDOQL",
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.secman.jdo.dom.role.ApplicationRole "
                    + "WHERE name.matches(:nameRegex) ")
})
@DomainObject(
        bounding = Bounding.BOUNDED,
        //		bounded = true,
        objectType = "isissecurity.ApplicationRole",
        autoCompleteRepository = ApplicationRoleRepository.class,
        autoCompleteAction = "findMatching"
        )
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_ROOT
        )
public class ApplicationRole 
implements org.apache.isis.extensions.secman.api.role.ApplicationRole, Comparable<ApplicationRole> {

    @Inject private ApplicationFeatureRepository applicationFeatureRepository;
    @Inject private ApplicationPermissionRepository applicationPermissionRepository;
    
    // -- name (property)

    public static class NameDomainEvent extends PropertyDomainEvent<String> {}


    @javax.jdo.annotations.Column(allowsNull="false", length = MAX_LENGTH_NAME)
    @Property(
            domainEvent = NameDomainEvent.class,
            editing = Editing.DISABLED
            )
    @PropertyLayout(typicalLength=TYPICAL_LENGTH_NAME)
    @MemberOrder(sequence = "1")
    @Getter @Setter
    private String name;


    // -- description (property)

    public static class DescriptionDomainEvent extends PropertyDomainEvent<String> {}


    @javax.jdo.annotations.Column(allowsNull="true", length = DescriptionType.Meta.MAX_LEN)
    @Property(
            domainEvent = DescriptionDomainEvent.class,
            editing = Editing.DISABLED
            )
    @PropertyLayout(
            typicalLength=TYPICAL_LENGTH_DESCRIPTION
            )
    @MemberOrder(sequence = "2")
    @Getter @Setter
    private String description;

    // -- permissions (derived collection)
    public static class PermissionsCollectionDomainEvent extends CollectionDomainEvent<ApplicationPermission> {}

    @Collection(
            domainEvent = PermissionsCollectionDomainEvent.class
            )
    @CollectionLayout(
            defaultView="table",
            sortedBy = ApplicationPermission.DefaultComparator.class
            )
    @MemberOrder(sequence = "10")
    public List<ApplicationPermission> getPermissions() {
        return applicationPermissionRepository.findByRole(this);
    }




    /**
     * Package names that have classes in them.
     */
    public java.util.Collection<String> choices2AddClass() {
        return applicationFeatureRepository.packageNamesContainingClasses(null);
    }

    /**
     * Class names for selected package.
     */
    public java.util.Collection<String> choices3AddClass(
            final ApplicationPermissionRule rule,
            final ApplicationPermissionMode mode,
            final String packageFqn) {
        return applicationFeatureRepository.classNamesContainedIn(packageFqn, null);
    }

    // -- users (collection)

    public static class UsersDomainEvent extends CollectionDomainEvent<ApplicationUser> {}

    @javax.jdo.annotations.Persistent(mappedBy = "roles")
    @Collection(
            domainEvent = UsersDomainEvent.class,
            editing = Editing.DISABLED
            )
    @CollectionLayout(
            defaultView="table"
            )
    @MemberOrder(sequence = "20")
    @Getter @Setter
    private SortedSet<ApplicationUser> users = new TreeSet<>();


    // necessary for integration tests
    public void addToUsers(final ApplicationUser applicationUser) {
        getUsers().add(applicationUser);
    }
    // necessary for integration tests
    public void removeFromUsers(final ApplicationUser applicationUser) {
        getUsers().remove(applicationUser);
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
    public int compareTo(final ApplicationRole o) {
        return comparator.compare(this, o);
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
