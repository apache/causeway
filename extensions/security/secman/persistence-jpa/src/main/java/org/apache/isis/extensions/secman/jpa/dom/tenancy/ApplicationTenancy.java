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
package org.apache.isis.extensions.secman.jpa.dom.tenancy;

import java.util.Comparator;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.util.Equality;
import org.apache.isis.applib.util.Hashing;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ToString;
import org.apache.isis.extensions.secman.jpa.dom.constants.NamedQueryNames;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "ApplicationTenancy", 
        uniqueConstraints =
            @UniqueConstraint(
                    name = "ApplicationTenancy_name_UNQ", 
                    columnNames={"name"})
)
@NamedQueries({
    @NamedQuery(
            name = NamedQueryNames.TENANCY_BY_PATH, 
            query = "SELECT t "
                  + "FROM org.apache.isis.extensions.secman.jpa.dom.tenancy.ApplicationTenancy t "
                  + "WHERE t.path = :path"),
    @NamedQuery(
            name = NamedQueryNames.TENANCY_BY_NAME, 
            query = "SELECT t "
                  + "FROM org.apache.isis.extensions.secman.jpa.dom.tenancy.ApplicationTenancy t "
                  + "WHERE t.name = :name"),
    @NamedQuery(
            name = NamedQueryNames.TENANCY_BY_NAME_OR_PATH_MATCHING, 
            query = "SELECT t "
                  + "FROM org.apache.isis.extensions.secman.jpa.dom.tenancy.ApplicationTenancy t "
                  + "WHERE t.name LIKE '%:regex%' "
                  + "    OR t.path LIKE '%:regex%'"),
})
@DomainObject(
        objectType = "isissecurity.ApplicationTenancy",
        autoCompleteRepository = ApplicationTenancyRepository.class,
        autoCompleteAction = "findMatching"
        )
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_ROOT
        )
public class ApplicationTenancy 
implements 
    Comparable<ApplicationTenancy>,
    org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancy {

    // -- name (property, title)

    public static class NameDomainEvent extends PropertyDomainEvent<String> {}

    @Column(nullable=false, length=MAX_LENGTH_NAME)
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


    // -- path

    public static class PathDomainEvent extends PropertyDomainEvent<String> {}


    @Id
    @Column(nullable=false, length=MAX_LENGTH_PATH)
    @Property(
            domainEvent = PathDomainEvent.class,
            editing = Editing.DISABLED
            )
    @Getter @Setter
    private String path;


    // -- parent (property)

    public static class ParentDomainEvent extends PropertyDomainEvent<ApplicationTenancy> {}


    @JoinColumn(name="parentPath", nullable=true)
    @Property(
            domainEvent = ParentDomainEvent.class,
            editing = Editing.DISABLED
            )
    @PropertyLayout(
            hidden = Where.PARENTED_TABLES
            )
    @Getter @Setter
    private ApplicationTenancy parent;


    // -- children

    public static class ChildrenDomainEvent extends CollectionDomainEvent<ApplicationTenancy> {}

    @OneToMany(mappedBy="parent")
    @Collection(
            domainEvent = ChildrenDomainEvent.class,
            editing = Editing.DISABLED
            )
    @CollectionLayout(
            defaultView="table"
            )
    @Getter @Setter
    private TreeSet<ApplicationTenancy> children = new TreeSet<>();


    // necessary for integration tests
    public void addToChildren(final ApplicationTenancy applicationTenancy) {
        getChildren().add(applicationTenancy);
    }
    // necessary for integration tests
    public void removeFromChildren(final ApplicationTenancy applicationTenancy) {
        getChildren().remove(applicationTenancy);
    }


    // -- CONTRACT

    private static final Equality<ApplicationTenancy> equality =
            ObjectContracts.checkEquals(ApplicationTenancy::getPath);

    private static final Hashing<ApplicationTenancy> hashing =
            ObjectContracts.hashing(ApplicationTenancy::getPath);

    private static final ToString<ApplicationTenancy> toString =
            ObjectContracts.toString("path", ApplicationTenancy::getPath)
            .thenToString("name", ApplicationTenancy::getName);

    private static final Comparator<ApplicationTenancy> comparator =
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

}
