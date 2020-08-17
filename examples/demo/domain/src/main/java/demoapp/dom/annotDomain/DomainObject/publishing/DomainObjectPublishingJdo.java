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
package demoapp.dom.annotDomain.DomainObject.publishing;

import java.net.URI;

import javax.inject.Inject;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.xml.bind.annotation.XmlElement;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.annotation.Redirect;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.linking.DeepLinkService;
import org.apache.isis.applib.services.message.MessageService;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

//tag::class[]
@PersistenceCapable(identityType = IdentityType.DATASTORE, schema = "demo")
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@DomainObject(
        nature=Nature.JDO_ENTITY
        , objectType = "demo.DomainObjectPublishingJdo"
        , editing = Editing.ENABLED
        , publishing = Publishing.ENABLED          // <.>
)
@DomainObjectLayout(
        describedAs =
                "@DomainObject(publishing=ENABLED)"
)
public class DomainObjectPublishingJdo implements HasAsciiDocDescription {
    // ...
//end::class[]

    public DomainObjectPublishingJdo(String initialValue) {
        this.property = initialValue;
    }

    public String title() {
        return "DomainObject#publishing";
    }

//tag::property[]
    @Property()
    @MemberOrder(name = "general", sequence = "1")
    @Getter @Setter
    private String property;
//end::property[]

//tag::action[]
    @Action(
        semantics = SemanticsOf.IDEMPOTENT
        , associateWith = "property"
    )
    @MemberOrder(name = "annotation", sequence = "1")
    public DomainObjectPublishingJdo updateProperty(final String value) {
        setProperty(value);
        return this;
    }
    public String default0UpdateProperty() {
        return getProperty();
    }
//end::action[]

//tag::class[]
}
//end::class[]
