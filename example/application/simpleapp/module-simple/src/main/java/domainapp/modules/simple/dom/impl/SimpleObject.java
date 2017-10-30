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
package domainapp.modules.simple.dom.impl;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.VersionStrategy;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.applib.util.ObjectContracts;

import domainapp.modules.simple.dom.types.Name;
import lombok.AccessLevel;

@javax.jdo.annotations.PersistenceCapable(
        identityType=IdentityType.DATASTORE,
        schema = "simple"
)
@javax.jdo.annotations.DatastoreIdentity(
        strategy=javax.jdo.annotations.IdGeneratorStrategy.IDENTITY,
        column="id")
@javax.jdo.annotations.Version(
        strategy= VersionStrategy.DATE_TIME,
        column="version")
@javax.jdo.annotations.Queries({
        @javax.jdo.annotations.Query(
                name = "findByName",
                value = "SELECT "
                        + "FROM domainapp.modules.simple.dom.impl.SimpleObject "
                        + "WHERE name.indexOf(:name) >= 0 ")
})
@javax.jdo.annotations.Unique(name="SimpleObject_name_UNQ", members = {"name"})
@DomainObject() // objectType inferred from @PersistenceCapable#schema
@DomainObjectLayout() // trigger events etc.
@lombok.Getter @lombok.Setter
@lombok.RequiredArgsConstructor(staticName = "create")
public class SimpleObject implements Comparable<SimpleObject> {


    @javax.jdo.annotations.Column(allowsNull = "false", length = Name.MAX_LEN)
    @lombok.NonNull
    @Title(prepend = "Object: ")
    @Name private String name;

    @javax.jdo.annotations.Column(allowsNull = "true", length = 4000)
    @Property(editing = Editing.ENABLED)
    private String notes;


    //region > updateName (action)
    @Action(semantics = SemanticsOf.IDEMPOTENT)
    public SimpleObject updateName(
            @Name final String name) {
        setName(name);
        return this;
    }

    public String default0UpdateName() {
        return getName();
    }

    //endregion

    //region > delete (action)
    @Action(semantics = SemanticsOf.NON_IDEMPOTENT_ARE_YOU_SURE)
    public void delete() {
        final String title = titleService.titleOf(this);
        messageService.informUser(String.format("'%s' deleted", title));
        repositoryService.remove(this);
    }
    //endregion


    //region > toString, compareTo
    @Override
    public String toString() {
        return ObjectContracts.toString(this, "name");
    }

    @Override
    public int compareTo(final SimpleObject other) {
        return ObjectContracts.compare(this, other, "name");
    }
    //endregion

    //region > injected services
    @javax.inject.Inject
    @lombok.Getter(AccessLevel.NONE) @lombok.Setter(AccessLevel.NONE)
    RepositoryService repositoryService;

    @javax.inject.Inject
    @lombok.Getter(AccessLevel.NONE) @lombok.Setter(AccessLevel.NONE)
    TitleService titleService;

    @javax.inject.Inject
    @lombok.Getter(AccessLevel.NONE) @lombok.Setter(AccessLevel.NONE)
    MessageService messageService;
    //endregion

}