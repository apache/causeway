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

import javax.inject.Named;
import javax.xml.bind.annotation.*;
import org.apache.causeway.applib.annotation.*;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.domain.actions.ActionLayout.associateWith.child.ActionLayoutAssociateWithChildVm;
import demoapp.dom.domain.actions.ActionLayout.describedAs.child.ActionLayoutDescribedAsChildVm;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Named("demo.ActionLayoutDescribedAsPage")
@DomainObject(
        nature=Nature.VIEW_MODEL)
@XmlRootElement(name = "root")
@XmlType()
@XmlAccessorType(XmlAccessType.FIELD)
//tag::class[]
//...
public class ActionLayoutDescribedAsPage
//end::class[]
        implements HasAsciiDocDescription
//tag::class[]
{
    @Property
    @XmlElement
    @Getter
    @Setter
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
        setName(newName);
        return this;
    }
    public String default0UpdateName() {
        return getName();
    }
    public String validate0UpdateName(final String proposedName) {
        return Objects.equals(name, proposedName) ? "New name must be different from current name" : null;
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
        setName(newName);
        return this;
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
        setName(newName);
        return this;
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
        getChildren().add(new ActionLayoutDescribedAsChildVm(newValue));
        return this;
    }

    @Action
    @ActionLayout(
            associateWith = "children",
            describedAs = "Removes a child from the collection",    // <1>
            sequence = "2"
    )
    public Object removeChild(final ActionLayoutDescribedAsChildVm child) {
        getChildren().removeIf(x -> x.getValue().equals(child.getValue()));
        return this;
    }
    public List<ActionLayoutDescribedAsChildVm> choices0RemoveChild() {
        return getChildren();
    }
//end::collection[]


//tag::delete[]
    @Action(semantics = SemanticsOf.IDEMPOTENT_ARE_YOU_SURE)
    @ActionLayout(
        describedAs = "Despite its name, this action is a no-op"  // <.>
    )
    public Object delete() {
        return this;
    }
//end::delete[]

//tag::class[]
}
//end::class[]
