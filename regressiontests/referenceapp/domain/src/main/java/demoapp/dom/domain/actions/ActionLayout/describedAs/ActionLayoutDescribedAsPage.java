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
package demoapp.dom.domain.actions.ActionLayout.describedAs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.inject.Named;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.SemanticsOf;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.domain.actions.ActionLayout.describedAs.child.ActionLayoutDescribedAsChildVm;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Named("demo.ActionLayoutDescribedAsPage")
@DomainObject(nature=Nature.VIEW_MODEL)
@DomainObjectLayout(cssClassFa="fa-comment")
@XmlRootElement(name = "root")
@XmlType()
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
//tag::class[]
//...
public class ActionLayoutDescribedAsPage
//end::class[]
        implements HasAsciiDocDescription
//tag::class[]
{
    @Property
    @XmlElement
    @Getter @Setter
    private String name;

    @Collection()
    @XmlElement
    @Getter
    private List<ActionLayoutDescribedAsChildVm> children = new ArrayList<>();

    // ...
//end::class[]

    @ObjectSupport public String title() {
        return "@ActionLayout#describedAs";
    }

//tag::below[]
    @Action
    @ActionLayout(
        associateWith = "name",
        describedAs = "Updates the name property.  The new name must be different from the old name" // <.>
    )
    public Object updateName(final String newName) {
        // ...
//end::below[]
        setName(newName);
        return this;
    }
    public String default0UpdateName() {
        return getName();
    }
    public String validate0UpdateName(final String proposedName) {
        return Objects.equals(name, proposedName) ? "New name must be different from current name" : null;
//tag::below[]
    }
//end::below[]

//tag::panel[]
    @Action
    @ActionLayout(
        associateWith = "name",
        describedAs = "Updates the name property.  This action has no validation",  // <.>
        position = ActionLayout.Position.PANEL
    )
    public Object updateNameFromPanel(final String newName) {
        // ...
//end::panel[]
        setName(newName);
        return this;
//tag::panel[]
    }
//end::panel[]

//tag::panel-dropdown[]
    @Action
    @ActionLayout(
        associateWith = "name",
        describedAs = "Updates the name property.  This action also has no validation",  // <.>
        position = ActionLayout.Position.PANEL_DROPDOWN
    )
    public Object updateNameFromPanelDropdown(final String newName) {
        // ...
//end::panel-dropdown[]
        setName(newName);
        return this;
//tag::panel-dropdown[]
    }
//end::panel-dropdown[]

//tag::collection[]
    @Action
    @ActionLayout(
        associateWith = "children",
        describedAs = "Adds a child to the collection",         // <.>
        sequence = "1"
    )
    public Object addChild(final String newValue) {
        // ...
//end::collection[]
        getChildren().add(new ActionLayoutDescribedAsChildVm(newValue));
        return this;
//tag::collection[]
    }
//end::collection[]

    @Action
    // no @ActionLayout, instead uses .layout.xml
    public Object removeChild(final ActionLayoutDescribedAsChildVm child) {
        getChildren().removeIf(x -> x.getValue().equals(child.getValue()));
        return this;
    }
    public List<ActionLayoutDescribedAsChildVm> choices0RemoveChild() {
        return getChildren();
    }

//tag::delete[]
    @Action(semantics = SemanticsOf.IDEMPOTENT_ARE_YOU_SURE)
    @ActionLayout(
        describedAs = "Despite its name, this action is a no-op"  // <.>
    )
    public Object delete() {
        // ...
//end::delete[]
        return this;
//tag::delete[]
    }
//end::delete[]

//tag::class[]
}
//end::class[]
