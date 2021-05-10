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
package org.apache.isis.extensions.secman.jpa.tenancy.dom;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.util.Equality;
import org.apache.isis.applib.util.Hashing;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ToString;
import org.apache.isis.commons.internal.base._Casts;


@Entity
@Table(
        schema = "isisExtensionsSecman",
        name = "ApplicationTenancy",
        uniqueConstraints =
            @UniqueConstraint(
                    name = "ApplicationTenancy_name_UNQ",
                    columnNames={"name"})
)
@NamedQueries({
    @NamedQuery(
            name = org.apache.isis.extensions.secman.api.tenancy.dom.ApplicationTenancy.NAMED_QUERY_FIND_BY_PATH,
            query = "SELECT t "
                  + "  FROM ApplicationTenancy t "
                  + " WHERE t.path = :path"),
    @NamedQuery(
            name = org.apache.isis.extensions.secman.api.tenancy.dom.ApplicationTenancy.NAMED_QUERY_FIND_BY_NAME,
            query = "SELECT t "
                  + "  FROM ApplicationTenancy t "
                  + " WHERE t.name = :name"),
    @NamedQuery(
            name = org.apache.isis.extensions.secman.api.tenancy.dom.ApplicationTenancy.NAMED_QUERY_FIND_BY_NAME_OR_PATH_MATCHING,
            query = "SELECT t "
                  + "  FROM ApplicationTenancy t "
                  + " WHERE t.name LIKE :regex "
                  + "    OR t.path LIKE :regex"),
})
@DomainObject(
        objectType = "isis.ext.secman.ApplicationTenancy",
        autoCompleteRepository = ApplicationTenancyRepository.class,
        autoCompleteAction = "findMatching"
        )
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_ROOT
        )
public class ApplicationTenancy
    implements org.apache.isis.extensions.secman.api.tenancy.dom.ApplicationTenancy {


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


    // -- PATH

    @Id
    @Column(nullable = false, length = Path.MAX_LENGTH)
    private String path;

    @Path
    @Override
    public String getPath() {
        return path;
    }
    @Override
    public void setPath(String path) {
        this.path = path;
    }


    // -- PARENT


    @ManyToOne
    @JoinColumn(name="parentPath", nullable = true)
    private ApplicationTenancy parent;

    @Parent
    @Override
    public org.apache.isis.extensions.secman.api.tenancy.dom.ApplicationTenancy getParent() {
        return parent;
    }
    @Override
    public void setParent(org.apache.isis.extensions.secman.api.tenancy.dom.ApplicationTenancy parent) {
        this.parent = _Casts.uncheckedCast(parent);
    }


    // -- CHILDREN

    @OneToMany(mappedBy = "parent")
    private SortedSet<ApplicationTenancy> children = new TreeSet<>();

    @Children
    @Override
    public SortedSet<org.apache.isis.extensions.secman.api.tenancy.dom.ApplicationTenancy> getChildren() {
        return _Casts.uncheckedCast(children);
    }
    public void setChildren(SortedSet<org.apache.isis.extensions.secman.api.tenancy.dom.ApplicationTenancy> children) {
        this.children = _Casts.uncheckedCast(children);
    }
    // necessary for integration tests
    public void removeFromChildren(final org.apache.isis.extensions.secman.api.tenancy.dom.ApplicationTenancy applicationTenancy) {
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
    public int compareTo(final org.apache.isis.extensions.secman.api.tenancy.dom.ApplicationTenancy other) {
        return comparator.compare(this, (ApplicationTenancy)other);
    }

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

}
