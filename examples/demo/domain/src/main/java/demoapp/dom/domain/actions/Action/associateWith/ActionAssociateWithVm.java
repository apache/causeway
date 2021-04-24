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
package demoapp.dom.domain.actions.Action.associateWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.SemanticsOf;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.domain.actions.Action.associateWith.child.ActionAssociateWithChildVm;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.val;

@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
    nature=Nature.VIEW_MODEL,
    objectType = "demo.ActionAssociateWithVm"
)
@NoArgsConstructor
//tag::class-properties[]
//tag::class-collections-children[]
//tag::class-collections-favorites[]
public class ActionAssociateWithVm implements HasAsciiDocDescription {

//end::class-properties[]
//end::class-collections-children[]
//end::class-collections-favorites[]
    public ActionAssociateWithVm(String text) {
        this.text = text;
    }

    public String title() {
        return "Action#associateWith";
    }

//tag::class-properties[]
    @Property()
    @PropertyLayout(fieldSetId = "annotation", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private String text;

    @Property()
    @PropertyLayout(fieldSetId = "annotation", sequence = "2")
    @XmlElement(required = true)
    @Getter @Setter
    private String otherProperty;

    // ...
//end::class-properties[]

//tag::class-collections-children[]
    @Collection()
    @CollectionLayout()
    @Getter
    private List<ActionAssociateWithChildVm> children = new ArrayList<>();

    // ...
//end::class-collections-children[]

//tag::class-collections-favorites[]
    @Collection()
    @CollectionLayout()
    @Getter
    private List<ActionAssociateWithChildVm> favorites = new ArrayList<>();

    // ...
//end::class-collections-favorites[]

//tag::action-associateWith-property[]
    @Action(
        semantics = SemanticsOf.IDEMPOTENT
        , associateWith = "text"                // <.>
    )
    @ActionLayout(
        describedAs =
            "@Action(associateWith = \"text\") " +
            "@ActionLayout(sequence = \"1\")"
        , sequence = "1"           // <.>
    )
    public ActionAssociateWithVm updateText(final String text) {
        setText(text);
        return this;
    }
    public String default0UpdateText() {
        return getText();
    }
//end::action-associateWith-property[]

//tag::action-associateWith-children-1[]
    @Action(
        semantics = SemanticsOf.IDEMPOTENT
        , associateWith = "children"        // <.>
    )
    @ActionLayout(
        describedAs =
            "@Action(associateWith = \"children\") " +
            "@ActionLayout(sequence = \"1\")"
        , sequence = "1"                    // <.>
    )
    public ActionAssociateWithVm addChild(final String value) {
        val childVm = new ActionAssociateWithChildVm(value);
        getChildren().add(childVm);
        return this;
    }

//end::action-associateWith-children-1[]

//tag::action-associateWith-children-2[]
    @Action(
        semantics = SemanticsOf.IDEMPOTENT
        , associateWith = "children"        // <.>
    )
    @ActionLayout(
        describedAs =
            "@Action(associateWith = \"children\") " +
            "@ActionLayout(sequence = \"2\")"
        , sequence = "2"                   // <.>
    )
    public ActionAssociateWithVm removeChild(final ActionAssociateWithChildVm child) {
        getChildren().removeIf(x -> Objects.equals(x.getValue(), child.getValue()));
        return this;
    }
    // no choices or autoComplete required      // <.>
//end::action-associateWith-children-2[]

//tag::action-associateWith-children-3[]
    @Action(
            semantics = SemanticsOf.IDEMPOTENT
            , associateWith = "children"        // <.>
    )
    @ActionLayout(
        describedAs =
            "@Action(associateWith = \"children\") " +
            "@ActionLayout(sequence = \"3\")"
        , sequence = "3"                        // <.>
    )
    public ActionAssociateWithVm removeChildren(final List<ActionAssociateWithChildVm> children) {
        for (ActionAssociateWithChildVm child : children) {
            removeChild(child);
        }
        return this;
    }
    // no choices or autoComplete required      // <.>
//end::action-associateWith-children-3[]
//tag::class-properties[]
//tag::class-collections-children[]
//tag::class-collections-favorites[]

}
//end::class-properties[]
//end::class-collections-children[]
//end::class-collections-favorites[]
