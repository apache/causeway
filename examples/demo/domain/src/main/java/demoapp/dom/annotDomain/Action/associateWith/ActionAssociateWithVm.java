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
package demoapp.dom.annotDomain.Action.associateWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.events.domain.ActionDomainEvent;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.val;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.annotDomain.Action.associateWith.child.ActionAssociateWithChildVm;
import demoapp.dom.types.Samples;

@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
    nature=Nature.VIEW_MODEL,
    objectType = "demo.ActionAssociateWithVm"
)
@NoArgsConstructor
//tag::class[]
public class ActionAssociateWithVm implements HasAsciiDocDescription {
    // ...
//end::class[]

    public ActionAssociateWithVm(String text) {
        this.text = text;
    }

    public String title() {
        return "Action#associateWith";
    }

//tag::text[]
    @Property()
    @MemberOrder(name = "annotation", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private String text;

    @Property()
    @MemberOrder(name = "annotation", sequence = "2")
    @XmlElement(required = true)
    @Getter @Setter
    private String anotherProperty;
//end::text[]

//tag:children[]
    @Collection()
    @CollectionLayout()
    @Getter
    private List<ActionAssociateWithChildVm> children = new ArrayList<>();
//end:children[]

//tag:children-favorites[]
    @Collection()
    @CollectionLayout()
    @Getter
    private List<ActionAssociateWithChildVm> favorites = new ArrayList<>();
//end:children-favorites[]

//tag::action-associateWith-property[]
    @Action(
            semantics = SemanticsOf.IDEMPOTENT
            , associateWith = "text", associateWithSequence = "1"   // <.>
    )
    @ActionLayout(
        describedAs =
            "@Action(associateWith = \"text\", associateWithSequence = \"1\")"
    )
    public ActionAssociateWithVm updateText(final String text) {
        setText(text);
        return this;
    }
    public String default0UpdateText() {
        return getText();
    }
//end::action-associateWith-property[]

//tag::action-associateWith-collection[]
    @Action(
            semantics = SemanticsOf.IDEMPOTENT
            , associateWith = "children", associateWithSequence = "1"   // <.>
    )
    @ActionLayout(
        describedAs =
            "@Action(associateWith = \"children\"" +
                     ", associateWithSequence = \"1\")"
    )
    public ActionAssociateWithVm addChild(final String value) {
        val childVm = new ActionAssociateWithChildVm(value);
        getChildren().add(childVm);
        return this;
    }

//end::action-associateWith-collection[]

//tag::action-associateWith-collection[]
    @Action(
            semantics = SemanticsOf.IDEMPOTENT
            , associateWith = "children", associateWithSequence = "2"   // <.>
    )
    @ActionLayout(
        describedAs =
            "@Action(associateWith = \"children\"" +
                    ", associateWithSequence = \"2\")"
    )
    public ActionAssociateWithVm removeChild(final ActionAssociateWithChildVm child) {
        getChildren().removeIf(x -> Objects.equals(x.getValue(), child.getValue()));
        return this;
    }
//end::action-associateWith-collection[]

//tag::action-associateWith-collection[]
    @Action(
            semantics = SemanticsOf.IDEMPOTENT
            , associateWith = "children", associateWithSequence = "3"   // <.>
    )
    @ActionLayout(
        describedAs =
            "@Action(associateWith = \"children\"" +
                    ", associateWithSequence = \"3\")"
    )
    public ActionAssociateWithVm removeChildren(final List<ActionAssociateWithChildVm> children) {
        for (ActionAssociateWithChildVm child : children) {
            removeChild(child);
        }
        return this;
    }
//end::action-associateWith-collection[]
//tag::class[]

}
//end::class[]
