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
package org.apache.causeway.extensions.secman.jdo.role.dom;

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
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.Unique;
import javax.jdo.annotations.Uniques;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.causeway.applib.annotation.BookmarkPolicy;
import org.apache.causeway.applib.annotation.Bounding;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.jaxb.PersistentEntityAdapter;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole.Nq;

import lombok.Getter;
import lombok.Setter;


@PersistenceCapable(
        identityType = IdentityType.DATASTORE,
        schema = ApplicationRole.SCHEMA,
        table = ApplicationRole.TABLE)
@Uniques({
    @Unique(name = "ApplicationRole__name__UNQ", members = { "name" })
})
@Queries({
    @Query(
            name = Nq.FIND_BY_NAME,
            value = "SELECT "
                  + "  FROM " + ApplicationRole.FQCN
                  + " WHERE name == :name"),
    @Query(
            name = Nq.FIND_BY_NAME_CONTAINING,
            value = "SELECT "
                  + "  FROM " + ApplicationRole.FQCN
                  + " WHERE name.matches(:regex) ")
})
@Inheritance(
        strategy = InheritanceStrategy.NEW_TABLE)
@DatastoreIdentity(strategy = IdGeneratorStrategy.NATIVE, column = "id")
@Version(strategy = VersionStrategy.VERSION_NUMBER, column = "version")
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
@Named(ApplicationRole.LOGICAL_TYPE_NAME)
@DomainObject(
        bounding = Bounding.BOUNDED,
        autoCompleteRepository = ApplicationRoleRepository.class,
        autoCompleteMethod = "findMatching"
        )
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_ROOT
        )
public class ApplicationRole
    extends org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole {

    protected final static String FQCN = "org.apache.causeway.extensions.secman.jdo.role.dom.ApplicationRole";


    @Column(allowsNull = Name.ALLOWS_NULL, length = Name.MAX_LENGTH)
    @Name
    @Getter @Setter
    private String name;


    @Column(allowsNull = Description.ALLOWS_NULL, length = Description.MAX_LENGTH)
    @Description
    @Getter @Setter
    private String description;


    @Persistent(mappedBy = Users.MAPPED_BY)
    @Users
    private SortedSet<org.apache.causeway.extensions.secman.jdo.user.dom.ApplicationUser> users = new TreeSet<>();
    @Override
    public SortedSet<org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser> getUsers() {
        return _Casts.uncheckedCast(users);
    }
    // necessary for integration tests
    public void addToUsers(final org.apache.causeway.extensions.secman.jdo.user.dom.ApplicationUser applicationUser) {
        getUsers().add(applicationUser);
    }


}
