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
package demoapp.dom.domain.actions.Action.domainEvent;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.ActionLayout;
import org.apache.isis.applib.annotations.DomainObject;
import org.apache.isis.applib.annotations.MemberSupport;
import org.apache.isis.applib.annotations.Nature;
import org.apache.isis.applib.annotations.ObjectSupport;
import org.apache.isis.applib.annotations.Property;
import org.apache.isis.applib.annotations.PropertyLayout;
import org.apache.isis.applib.annotations.SemanticsOf;
import org.apache.isis.applib.events.domain.ActionDomainEvent;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
    nature=Nature.VIEW_MODEL,
    logicalTypeName = "demo.ActionDomainEventVm"
)
@NoArgsConstructor
//tag::class[]
public class ActionDomainEventVm implements HasAsciiDocDescription {
    // ...
//end::class[]

    public ActionDomainEventVm(final String text) {
        this.text = text;
    }

    @ObjectSupport public String title() {
        return "Action#domainEvent";
    }

    @Property()
    @PropertyLayout(fieldSetId = "annotation", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private String text;
//tag::class[]

    public static class UpdateTextDomainEvent                       // <.>
            extends ActionDomainEvent<ActionDomainEventVm> {}

    @Action(
            semantics = SemanticsOf.IDEMPOTENT
            , domainEvent = UpdateTextDomainEvent.class             // <.>
    )
    @ActionLayout(
        describedAs =
            "@Action(domainEvent = UpdateTextDomainEvent.class)"
        , associateWith = "text"
        , sequence = "1"
    )
    public ActionDomainEventVm updateText(final String text) {
        setText(text);
        return this;
    }
    @MemberSupport public String default0UpdateText() {
        return getText();
    }
}
//end::class[]
