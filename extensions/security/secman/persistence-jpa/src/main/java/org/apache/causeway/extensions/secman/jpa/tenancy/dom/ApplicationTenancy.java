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
package org.apache.causeway.extensions.secman.jpa.tenancy.dom;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Named;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.causeway.applib.annotation.BookmarkPolicy;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.jaxb.PersistentEntityAdapter;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.extensions.secman.applib.tenancy.dom.ApplicationTenancy.Nq;
import org.apache.causeway.persistence.jpa.applib.integration.CausewayEntityListener;

import lombok.Getter;
import lombok.Setter;


@Entity
@Table(
        schema = ApplicationTenancy.SCHEMA,
        name = ApplicationTenancy.TABLE,
        uniqueConstraints =
            @UniqueConstraint(name = "ApplicationTenancy__name__UNQ", columnNames = { "name" })
)
@NamedQueries({
    @NamedQuery(
            name = Nq.FIND_BY_PATH,
            query = "SELECT t "
                  + "  FROM ApplicationTenancy t "
                  + " WHERE t.path = :path"),
    @NamedQuery(
            name = Nq.FIND_BY_NAME,
            query = "SELECT t "
                  + "  FROM ApplicationTenancy t "
                  + " WHERE t.name = :name"),
    @NamedQuery(
            name = Nq.FIND_BY_NAME_OR_PATH_MATCHING,
            query = "SELECT t "
                  + "  FROM ApplicationTenancy t "
                  + " WHERE t.name LIKE :regex "
                  + "    OR t.path LIKE :regex"),
})
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
@EntityListeners(CausewayEntityListener.class)
@Named(ApplicationTenancy.LOGICAL_TYPE_NAME)
@DomainObject(
        autoCompleteRepository = ApplicationTenancyRepository.class,
        autoCompleteMethod = "findMatching"
        )
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_ROOT
        )
public class ApplicationTenancy
    extends org.apache.causeway.extensions.secman.applib.tenancy.dom.ApplicationTenancy {

    @Version
    private Long version;


    @Column(nullable = Name.NULLABLE, length = Name.MAX_LENGTH)
    @Name
    @Getter @Setter
    private String name;


    @Id
    @Column(nullable = Path.NULLABLE, length = Path.MAX_LENGTH)
    @Path
    @Getter @Setter
    private String path;


    @ManyToOne
    @JoinColumn(name=Parent.NAME, nullable = Parent.NULLABLE)
    @Parent
    @Getter
    private ApplicationTenancy parent;
    @Override
    public void setParent(final org.apache.causeway.extensions.secman.applib.tenancy.dom.ApplicationTenancy parent) {
        final ApplicationTenancy newParent = _Casts.uncheckedCast(parent);
        if(Objects.equals(this.parent, newParent)) {
            this.parent = newParent;
            if(newParent != null) {
                newParent.children.add(this);
            }
            return;
        }
        final ApplicationTenancy previousParent = this.parent;
        this.parent = newParent;
        if(previousParent != null) {
            previousParent.children.remove(this);
        }
        if(newParent != null) {
            newParent.children.add(this);
        }
    }


    @OneToMany(mappedBy = Children.MAPPED_BY)
    @Children
    private Set<ApplicationTenancy> children = new TreeSet<>();

    @Override
    public Set<org.apache.causeway.extensions.secman.applib.tenancy.dom.ApplicationTenancy> getChildren() {
        return _Casts.uncheckedCast(children);
    }
    public void setChildren(final Set<org.apache.causeway.extensions.secman.applib.tenancy.dom.ApplicationTenancy> children) {
        final Set<ApplicationTenancy> replacement = _Casts.uncheckedCast(children);
        new TreeSet<>(this.children).stream()
                .filter(child -> !replacement.contains(child))
                .forEach(child -> child.setParent(null));
        new TreeSet<>(replacement)
                .forEach(child -> child.setParent(this));
    }
    // necessary for integration tests
    public void removeFromChildren(final org.apache.causeway.extensions.secman.applib.tenancy.dom.ApplicationTenancy applicationTenancy) {
        final ApplicationTenancy child = _Casts.uncheckedCast(applicationTenancy);
        if(Objects.equals(child.getParent(), this)) {
            child.setParent(null);
        } else {
            children.remove(child);
        }
    }


}
