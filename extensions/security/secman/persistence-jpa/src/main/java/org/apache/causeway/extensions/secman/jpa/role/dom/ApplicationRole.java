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
package org.apache.causeway.extensions.secman.jpa.role.dom;

import java.util.Set;
import java.util.TreeSet;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.causeway.applib.annotation.BookmarkPolicy;
import org.apache.causeway.applib.annotation.Bounding;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.jaxb.PersistentEntityAdapter;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionRepository;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole.Nq;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRoleRepository;
import org.apache.causeway.extensions.secman.jpa.user.dom.ApplicationUser;
import org.apache.causeway.persistence.jpa.applib.integration.CausewayEntityListener;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(
        schema = ApplicationRole.SCHEMA,
        name = ApplicationRole.TABLE,
        uniqueConstraints =
            @UniqueConstraint(name = "ApplicationRole__name__UNQ", columnNames= { "name" })
)
@NamedQueries({
    @NamedQuery(
            name = Nq.FIND_BY_NAME,
            query = "SELECT r "
                  + "  FROM ApplicationRole r "
                  + " WHERE r.name = :name"),
    @NamedQuery(
            name = Nq.FIND_BY_NAME_CONTAINING,
            query = "SELECT r "
                  + "  FROM ApplicationRole r "
                  + " WHERE r.name LIKE :regex"),
})
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
@EntityListeners(CausewayEntityListener.class)
@Named(ApplicationRole.LOGICAL_TYPE_NAME)
@DomainObject(
        bounding = Bounding.BOUNDED,
        autoCompleteRepository = ApplicationRoleRepository.class,
        autoCompleteMethod = "findMatching")
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_ROOT,
        titleUiEvent = ApplicationRole.TitleUiEvent.class,
        iconUiEvent = ApplicationRole.IconUiEvent.class,
        cssClassUiEvent = ApplicationRole.CssClassUiEvent.class,
        layoutUiEvent = ApplicationRole.LayoutUiEvent.class)
public class ApplicationRole
    implements org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole {

    @Inject
    @Programmatic
    @Getter(onMethod_ = {@Override}) @Accessors(fluent = true)
    transient private ApplicationPermissionRepository applicationPermissionRepository;

    @Id
    @GeneratedValue
    private Long id;

    @Version
    private Long version;

    @Column(nullable = Name.NULLABLE, length = Name.MAX_LENGTH)
    @Name
    @Getter @Setter
    private String name;

    @Column(nullable = Description.NULLABLE, length = Description.MAX_LENGTH)
    @Description
    @Getter @Setter
    private String description;

    @ManyToMany(mappedBy = Users.MAPPED_BY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Users
    private Set<ApplicationUser> users = new TreeSet<>();

    @Override
    public Set<org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser> getUsers() {
        return _Casts.uncheckedCast(users);
    }

    // necessary for integration tests
    public void addToUsers(final ApplicationUser applicationUser) {
        getUsers().add(applicationUser);
    }

    @Override
    public boolean equals(final Object obj) {
        return EQUALITY.equals(this, obj);
    }

    @Override
    public int hashCode() {
        return HASHING.hashCode(this);
    }

    @Override
    public String toString() {
        return TOSTRING.toString(this);
    }

}
