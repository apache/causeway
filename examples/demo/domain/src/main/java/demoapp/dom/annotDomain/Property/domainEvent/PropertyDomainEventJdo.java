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
package demoapp.dom.annotDomain.Property.domainEvent;

import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.events.domain.PropertyDomainEvent;
import org.apache.isis.applib.jaxb.PersistentEntityAdapter;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

//tag::class[]
@PersistenceCapable(identityType = IdentityType.DATASTORE, schema = "demo")
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@DomainObject(
        objectType = "demo.PropertyDomainEventJdo",
        editing = Editing.ENABLED
)
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
public class PropertyDomainEventJdo implements HasAsciiDocDescription {
    // ...
//end::class[]

    public PropertyDomainEventJdo(String text) {
        this.text = text;
    }

    public String title() {
        return "Property#domainEvent";
    }

    public static class TextDomainEvent                             // <.>
        extends PropertyDomainEvent<PropertyDomainEventJdo,String> {}
//tag::annotation[]
    @Property(
        domainEvent = TextDomainEvent.class                         // <.>
    )
    @PropertyLayout(
        describedAs =
            "@Property(domainEvent = TextDomainEvent.class)"
    )
    @MemberOrder(name = "annotation", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private String text;
//end::annotation[]

//tag::class[]
}
//end::class[]
