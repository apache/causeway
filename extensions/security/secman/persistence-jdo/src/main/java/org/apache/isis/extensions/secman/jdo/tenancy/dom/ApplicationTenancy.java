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
package org.apache.isis.extensions.secman.jdo.tenancy.dom;

import java.util.SortedSet;
import java.util.TreeSet;

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

import org.apache.isis.applib.annotations.BookmarkPolicy;
import org.apache.isis.applib.annotations.DomainObject;
import org.apache.isis.applib.annotations.DomainObjectLayout;
import org.apache.isis.commons.internal.base._Casts;


@PersistenceCapable(
        identityType = IdentityType.APPLICATION,
        schema = "isisExtensionsSecman",
        table = "ApplicationTenancy")
@Inheritance(
        strategy = InheritanceStrategy.NEW_TABLE)
@DatastoreIdentity(
        strategy = IdGeneratorStrategy.NATIVE, column = "id")
@Version(
        strategy = VersionStrategy.VERSION_NUMBER,
        column = "version")
@Uniques({
    @Unique(
            name = "ApplicationTenancy_name_UNQ", members = { "name" })
})
@Queries( {
    @Query(
            name = org.apache.isis.extensions.secman.applib.tenancy.dom.ApplicationTenancy.NAMED_QUERY_FIND_BY_PATH,
            value = "SELECT "
                    + "FROM " + ApplicationTenancy.FQCN
                    + " WHERE path == :path"),
    @Query(
            name = org.apache.isis.extensions.secman.applib.tenancy.dom.ApplicationTenancy.NAMED_QUERY_FIND_BY_NAME,
            value = "SELECT "
                    + "FROM " + ApplicationTenancy.FQCN
                    + " WHERE name == :name"),
    @Query(
            name = org.apache.isis.extensions.secman.applib.tenancy.dom.ApplicationTenancy.NAMED_QUERY_FIND_BY_NAME_OR_PATH_MATCHING,
            value = "SELECT "
                    + "FROM " + ApplicationTenancy.FQCN
                    + " WHERE name.matches(:regex) || path.matches(:regex) ")})
@DomainObject(
        logicalTypeName = ApplicationTenancy.LOGICAL_TYPE_NAME,
        autoCompleteRepository = ApplicationTenancyRepository.class,
        autoCompleteMethod = "findMatching"
)
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_ROOT
        )
public class ApplicationTenancy
    extends org.apache.isis.extensions.secman.applib.tenancy.dom.ApplicationTenancy {

    protected final static String FQCN = "org.apache.isis.extensions.secman.jdo.tenancy.dom.ApplicationTenancy";


    // -- NAME

    @Column(allowsNull  ="false", length = Name.MAX_LENGTH)
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

    @PrimaryKey
    @Column(allowsNull = "false", length = Path.MAX_LENGTH)
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


    @Column(name = "parentPath", allowsNull = "true")
    private ApplicationTenancy parent;

    @Parent
    @Override
    public org.apache.isis.extensions.secman.applib.tenancy.dom.ApplicationTenancy getParent() {
        return parent;
    }
    @Override
    public void setParent(org.apache.isis.extensions.secman.applib.tenancy.dom.ApplicationTenancy parent) {
        this.parent = _Casts.uncheckedCast(parent);
    }


    // -- CHILDREN

    @Persistent(mappedBy = "parent")
    private SortedSet<ApplicationTenancy> children = new TreeSet<>();

    @Children
    @Override
    public SortedSet<org.apache.isis.extensions.secman.applib.tenancy.dom.ApplicationTenancy> getChildren() {
        return _Casts.uncheckedCast(children);
    }
    public void setChildren(SortedSet<org.apache.isis.extensions.secman.applib.tenancy.dom.ApplicationTenancy> children) {
        this.children = _Casts.uncheckedCast(children);
    }
    // necessary for integration tests
    public void removeFromChildren(final org.apache.isis.extensions.secman.applib.tenancy.dom.ApplicationTenancy applicationTenancy) {
        getChildren().remove(applicationTenancy);
    }


}
