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
package demoapp.dom.domain.actions.Action.hidden;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.annotation.Where;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
    nature=Nature.VIEW_MODEL,
    logicalTypeName = "demo.ActionHiddenVm"
)
@NoArgsConstructor
//tag::class[]
public class ActionHiddenVm implements HasAsciiDocDescription {
    // ...
//end::class[]

    public ActionHiddenVm(final String value) {
        this.text = value;
        this.otherText = value;
    }

    @Title
    public String title() {
        return "Action#hidden";
    }

    @Property()
    @PropertyLayout(fieldSetId = "properties", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private String text;

    @Property()
    @PropertyLayout(fieldSetId = "properties", sequence = "2")
    @XmlElement(required = true)
    @Getter @Setter
    private String otherText;

//tag::action-no-annotation[]
    @Action(
            semantics = SemanticsOf.IDEMPOTENT
            // no hidden attribute              // <.>
    )
    @ActionLayout(
        describedAs =
            "@Action()"
        , associateWith = "text"
        , sequence = "1"
    )
    public ActionHiddenVm updateTextNoAnnotation(final String text) {
        setText(text);
        return this;
    }
    @MemberSupport public String default0UpdateTextNoAnnotation() {
        return getText();
    }
//end::action-no-annotation[]

//tag::action-hidden-nowhere[]
    @Action(
        semantics = SemanticsOf.IDEMPOTENT
        , hidden = Where.NOWHERE               // <.>
    )
    @ActionLayout(
        describedAs =
            "@Action(hidden = Where.NOWHERE)"
        , associateWith = "text"
        , sequence = "2"
    )
    public ActionHiddenVm updateTextAndHiddenNowhere(final String text) {
        setText(text);
        return this;
    }
    @MemberSupport public String default0UpdateTextAndHiddenNowhere() {
        return getText();
    }
//end::action-hidden-nowhere[]

//tag::action-but-hidden-on-forms[]
    @Action(
        semantics = SemanticsOf.IDEMPOTENT
        , hidden = Where.OBJECT_FORMS       // <.>
    )
    @ActionLayout(
        describedAs =
            "@Action(hidden = Where.OBJECT_FORMS)"
        , associateWith = "text"
        , sequence = "3"
    )
    public ActionHiddenVm updateTextButHiddenOnForms(final String text) {
        setText(text);
        return this;
    }
    @MemberSupport public String default0UpdateTextButHiddenOnForms() {
        return getText();
    }
//end::action-but-hidden-on-forms[]

//tag::action-but-hidden-everywhere[]
    @Action(
        semantics = SemanticsOf.IDEMPOTENT

        , hidden = Where.EVERYWHERE         // <.>
    )
    @ActionLayout(
        describedAs =
            "@Action(hidden = Where.EVERYWHERE)"
        , associateWith = "text"
        , sequence = "4"
    )
    public ActionHiddenVm updateTextButHiddenEverywhere(final String text) {
        setText(text);
        return this;
    }
    @MemberSupport public String default0UpdateTextButHiddenEverywhere() {
        return getText();
    }
//end::action-but-hidden-everywhere[]

//tag::class[]
}
//end::class[]
