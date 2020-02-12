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
package org.apache.isis.extensions.secman.jdo.dom.tenancy;

import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.VersionStrategy;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.util.Equality;
import org.apache.isis.applib.util.Hashing;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ToString;
import org.apache.isis.extensions.secman.jdo.dom.user.ApplicationUser;
import org.apache.isis.extensions.secman.jdo.dom.user.ApplicationUserRepository;

import lombok.Getter;
import lombok.Setter;

@javax.jdo.annotations.PersistenceCapable(
        identityType = IdentityType.APPLICATION,
        schema = "isissecurity",
        table = "ApplicationTenancy")
@javax.jdo.annotations.Inheritance(
        strategy = InheritanceStrategy.NEW_TABLE)
@javax.jdo.annotations.DatastoreIdentity(
        strategy = IdGeneratorStrategy.NATIVE, column = "id")
@javax.jdo.annotations.Version(
        strategy = VersionStrategy.VERSION_NUMBER,
        column = "version")
@javax.jdo.annotations.Uniques({
    @javax.jdo.annotations.Unique(
            name = "ApplicationTenancy_name_UNQ", members = { "name" })
})
@javax.jdo.annotations.Queries( {
    @javax.jdo.annotations.Query(
            name = "findByPath", language = "JDOQL",
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.secman.jdo.dom.tenancy.ApplicationTenancy "
                    + "WHERE path == :path"),
    @javax.jdo.annotations.Query(
            name = "findByName", language = "JDOQL",
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.secman.jdo.dom.tenancy.ApplicationTenancy "
                    + "WHERE name == :name"),
    @javax.jdo.annotations.Query(
            name = "findByNameOrPathMatching", language = "JDOQL",
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.secman.jdo.dom.tenancy.ApplicationTenancy "
                    + "WHERE name.matches(:regex) || path.matches(:regex) ")})
@DomainObject(
        objectType = "isissecurity.ApplicationTenancy",
        autoCompleteRepository = ApplicationTenancyRepository.class,
        autoCompleteAction = "findMatching"
        )
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_ROOT
        )
public class ApplicationTenancy implements Comparable<ApplicationTenancy>,
org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancy {

    // -- name (property, title)

    public static class NameDomainEvent extends PropertyDomainEvent<String> {}

    @javax.jdo.annotations.Column(allowsNull="false", length = MAX_LENGTH_NAME)
    @Title
    @Property(
            domainEvent = NameDomainEvent.class,
            editing = Editing.DISABLED
            )
    @PropertyLayout(
            typicalLength=TYPICAL_LENGTH_NAME
            )
    @MemberOrder(sequence = "1")
    @Getter @Setter
    private String name;

    // -- updateName (action)

    public static class UpdateNameDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent =UpdateNameDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
            )
    @MemberOrder(name="name", sequence = "1")
    public ApplicationTenancy updateName(
            @Parameter(maxLength = MAX_LENGTH_NAME)
            @ParameterLayout(named="Name", typicalLength=TYPICAL_LENGTH_NAME)
            final String name) {
        setName(name);
        return this;
    }

    public String default0UpdateName() {
        return getName();
    }


    // -- path

    public static class PathDomainEvent extends PropertyDomainEvent<String> {}


    @javax.jdo.annotations.PrimaryKey
    @javax.jdo.annotations.Column(length = MAX_LENGTH_PATH, allowsNull = "false")
    @Property(
            domainEvent = PathDomainEvent.class,
            editing = Editing.DISABLED
            )
    @Getter @Setter
    private String path;



    // -- users (collection)

    public static class UsersDomainEvent extends CollectionDomainEvent<ApplicationUser> {}

    @Collection(
            domainEvent = UsersDomainEvent.class,
            editing = Editing.DISABLED
            )
    @CollectionLayout(
            defaultView="table"
            )
    @MemberOrder(sequence = "10")
    public List<ApplicationUser> getUsers() {
        return applicationUserRepository.findByAtPath(getPath());
    }

    // necessary for integration tests
    public void addToUsers(final ApplicationUser applicationUser) {
        applicationUser.setAtPath(getPath());
    }
    // necessary for integration tests
    public void removeFromUsers(final ApplicationUser applicationUser) {
        applicationUser.setAtPath(null);
    }




    // -- parent (property)

    public static class ParentDomainEvent extends PropertyDomainEvent<ApplicationTenancy> {}


    @javax.jdo.annotations.Column(name = "parentPath", allowsNull = "true")
    @Property(
            domainEvent = ParentDomainEvent.class,
            editing = Editing.DISABLED
            )
    @PropertyLayout(
            hidden = Where.PARENTED_TABLES
            )
    @Getter @Setter
    private ApplicationTenancy parent;



    // -- updateParent (action)

    public static class UpdateParentDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = UpdateParentDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
            )
    @MemberOrder(name="parent", sequence = "1")
    public ApplicationTenancy updateParent(
            @Parameter(optionality = Optionality.OPTIONAL)
            final ApplicationTenancy tenancy
            ) {
        // no need to add to children set, since will be done by JDO/DN.
        setParent(tenancy);
        return this;
    }

    public ApplicationTenancy default0UpdateParent() {
        return getParent();
    }



    // -- children

    public static class ChildrenDomainEvent extends CollectionDomainEvent<ApplicationTenancy> {}

    @javax.jdo.annotations.Persistent(mappedBy = "parent")
    @Collection(
            domainEvent = ChildrenDomainEvent.class,
            editing = Editing.DISABLED
            )
    @CollectionLayout(
            defaultView="table"
            )
    @Getter @Setter
    private SortedSet<ApplicationTenancy> children = new TreeSet<>();


    // necessary for integration tests
    public void addToChildren(final ApplicationTenancy applicationTenancy) {
        getChildren().add(applicationTenancy);
    }
    // necessary for integration tests
    public void removeFromChildren(final ApplicationTenancy applicationTenancy) {
        getChildren().remove(applicationTenancy);
    }


    // -- addChild (action)

    public static class AddChildDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = AddChildDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
            )
    @ActionLayout(
            named="Add"
            )
    @MemberOrder(name="Children", sequence = "1")
    public ApplicationTenancy addChild(final ApplicationTenancy applicationTenancy) {
        applicationTenancy.setParent(this);
        // no need to add to children set, since will be done by JDO/DN.
        return this;
    }


    // -- removeChild (action)

    public static class RemoveChildDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = RemoveChildDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
            )
    @MemberOrder(name="Children", sequence = "2")
    public ApplicationTenancy removeChild(final ApplicationTenancy applicationTenancy) {
        applicationTenancy.setParent(null);
        // no need to remove from children set, since will be done by JDO/DN.
        return this;
    }
    public java.util.Collection<ApplicationTenancy> choices0RemoveChild() {
        return getChildren();
    }
    public String disableRemoveChild() {
        return choices0RemoveChild().isEmpty()? "No children to remove": null;
    }


    // -- delete (action)
    public static class DeleteDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = DeleteDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT_ARE_YOU_SURE
            )
    @MemberOrder(sequence = "1")
    public java.util.Collection<org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancy> delete() {
        for (final ApplicationUser user : getUsers()) {
            user.updateAtPath(null);
        }
        repository.removeAndFlush(this);
        return applicationTenancyRepository.allTenancies();
    }


    // -- CONTRACT

    private final static Equality<ApplicationTenancy> equality =
            ObjectContracts.checkEquals(ApplicationTenancy::getPath);

    private final static Hashing<ApplicationTenancy> hashing =
            ObjectContracts.hashing(ApplicationTenancy::getPath);

    private final static ToString<ApplicationTenancy> toString =
            ObjectContracts.toString("path", ApplicationTenancy::getPath)
            .thenToString("name", ApplicationTenancy::getName);

    private final static Comparator<ApplicationTenancy> comparator =
            Comparator.comparing(ApplicationTenancy::getPath);


    @Override
    public boolean equals(final Object other) {
        return equality.equals(this, other);
    }

    @Override
    public int hashCode() {
        return hashing.hashCode(this);
    }

    @Override
    public String toString() {
        return toString.toString(this);
    }

    @Override
    public int compareTo(final ApplicationTenancy o) {
        return comparator.compare(this, o);
    }

    @Inject ApplicationUserRepository applicationUserRepository;
    @Inject ApplicationTenancyRepository applicationTenancyRepository;
    @Inject FactoryService factoryService;
    @Inject RepositoryService repository;

}
