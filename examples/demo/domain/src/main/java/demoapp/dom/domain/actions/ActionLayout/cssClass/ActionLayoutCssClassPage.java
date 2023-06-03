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
package demoapp.dom.domain.actions.ActionLayout.cssClass;

import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.SemanticsOf;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

//tag::class[]
@Named("demo.ActionLayoutCssClassVm")
@DomainObject(nature=Nature.VIEW_MODEL)
@DomainObjectLayout(cssClassFa="fa-pen-nib")
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
public class ActionLayoutCssClassPage implements HasAsciiDocDescription {

    @ObjectSupport public String title() {
        return "@ActionLayout#cssClass";
    }

    @Property
    @XmlElement
    @Getter
    @Setter
    private String name;

//tag::below-btn-info[]
    @Action
    @ActionLayout(
            cssClass = "btn-info",                  // <.>
            associateWith = "name",
            sequence = "1",
            position = ActionLayout.Position.BELOW  // <.>
    )
    public Object updateNameBtnInfo(final String newName) {
        setName(newName);
        return this;
    }
//end::below-btn-info[]

//tag::below-btn-warning[]
    @Action
    @ActionLayout(
            cssClass = "btn-warning",               // <.>
            associateWith = "name",
            sequence = "2",                         // <.>
            position = ActionLayout.Position.BELOW
    )
    public Object updateNameBtnWarning(final String newName) {
        setName(newName);
        return this;
    }
//end::below-btn-warning[]

//tag::panel-btn-primary[]
    @Action
    @ActionLayout(
            cssClass = "btn-primary",               // <.>
            associateWith = "name",
            sequence = "1",
            position = ActionLayout.Position.PANEL  // <.>
    )
    public Object updateNameFromPanelBtnPrimary(final String newName) {
        setName(newName);
        return this;
    }
//end::panel-btn-primary[]

//tag::panel-btn-secondary[]
    @Action
    @ActionLayout(
            cssClass = "btn-secondary",             // <.>
            associateWith = "name",
            sequence = "2",
            position = ActionLayout.Position.PANEL
    )
    public Object updateNameFromPanelBtnSecondary(final String newName) {
        setName(newName);
        return this;
    }
//end::panel-btn-secondary[]

//tag::panel-dropdown-btn-light[]
    @Action
    @ActionLayout(
            cssClass = "btn-light",                             // <.>
            associateWith = "name",
            sequence = "1",
            position = ActionLayout.Position.PANEL_DROPDOWN     // <.>
    )
    public Object updateNameFromPanelDropdownBtnLight(final String newName) {
        setName(newName);
        return this;
    }
//end::panel-dropdown-btn-light[]

//tag::panel-dropdown-btn-dark[]
    @Action
    @ActionLayout(
            cssClass = "btn-dark",       // <.>
            associateWith = "name",
            sequence = "2",
            position = ActionLayout.Position.PANEL_DROPDOWN
    )
    public Object updateNameFromPanelDropdownBtnDark(final String newName) {
        setName(newName);
        return this;
    }
//end::panel-dropdown-btn-dark[]

//tag::delete[]
    @Action(semantics = SemanticsOf.IDEMPOTENT_ARE_YOU_SURE)
    @ActionLayout()     // <.>
    public Object delete() {
        return this;
    }
//end::delete[]

}
//end::class[]
