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
package org.apache.causeway.extensions.secman.jdo.tenancy.dom;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Named;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
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
import org.apache.causeway.extensions.secman.applib.tenancy.dom.ApplicationTenancy.Nq;

import lombok.Getter;
import lombok.Setter;


@PersistenceCapable(
        identityType = IdentityType.APPLICATION,
        schema = ApplicationTenancy.SCHEMA,
        table = ApplicationTenancy.TABLE)
@Uniques({
    @Unique(name = "ApplicationTenancy__name__UNQ", members = { "name" })
})
@Queries( {
    @Query(
            name = Nq.FIND_BY_PATH,
            value = "SELECT "
                  + "  FROM " + ApplicationTenancy.FQCN
                  + " WHERE path == :path"),
    @Query(
            name = Nq.FIND_BY_NAME,
            value = "SELECT "
                  + "  FROM " + ApplicationTenancy.FQCN
                  + " WHERE name == :name"),
    @Query(
            name = Nq.FIND_BY_NAME_OR_PATH_MATCHING,
            value = "SELECT "
                  + "  FROM " + ApplicationTenancy.FQCN
                  + " WHERE name.matches(:regex) "
                  + "    || path.matches(:regex) ")
})
@Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
@DatastoreIdentity(strategy = IdGeneratorStrategy.NATIVE, column = "id")
@Version(strategy = VersionStrategy.VERSION_NUMBER, column = "version")
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
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

    protected final static String FQCN = "org.apache.causeway.extensions.secman.jdo.tenancy.dom.ApplicationTenancy";


    @Column(allowsNull = Name.ALLOWS_NULL, length = Name.MAX_LENGTH)
    @Name
    @Getter @Setter
    private String name;


    @PrimaryKey
    @Column(allowsNull = Path.ALLOWS_NULL, length = Path.MAX_LENGTH)
    @Path
    @Getter @Setter
    private String path;


    @Column(name = Parent.NAME, allowsNull = Parent.ALLOWS_NULL)
    @Parent
    @Getter
    private ApplicationTenancy parent;
    @Override
    public void setParent(org.apache.causeway.extensions.secman.applib.tenancy.dom.ApplicationTenancy parent) {
        this.parent = _Casts.uncheckedCast(parent);
    }


    @Persistent(mappedBy = Children.MAPPED_BY)
    @Children
    private SortedSet<ApplicationTenancy> children = new TreeSet<>();
    @Override
    public SortedSet<org.apache.causeway.extensions.secman.applib.tenancy.dom.ApplicationTenancy> getChildren() {
        return _Casts.uncheckedCast(children);
    }
    public void setChildren(final SortedSet<org.apache.causeway.extensions.secman.applib.tenancy.dom.ApplicationTenancy> children) {
        this.children = _Casts.uncheckedCast(children);
    }
    // necessary for integration tests
    public void removeFromChildren(final org.apache.causeway.extensions.secman.applib.tenancy.dom.ApplicationTenancy applicationTenancy) {
        getChildren().remove(applicationTenancy);
    }


}
