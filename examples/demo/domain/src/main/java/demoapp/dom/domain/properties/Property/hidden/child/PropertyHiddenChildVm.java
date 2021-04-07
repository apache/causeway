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
package demoapp.dom.domain.properties.Property.hidden.child;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.domain.properties.Property.hidden.PropertyHiddenVm;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//tag::class[]
@XmlRootElement(name = "child")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
        nature=Nature.VIEW_MODEL,
        objectType = "demo.PropertyHiddenChildVm"
)
@NoArgsConstructor
public class PropertyHiddenChildVm implements HasAsciiDocDescription {

    public PropertyHiddenChildVm(String value, PropertyHiddenVm parent) {
        setPropertyHiddenNowhere(value);
        setPropertyHiddenEverywhere(value);
        setPropertyHiddenAnywhere(value);

        setPropertyHiddenAllTables(value);
        setPropertyHiddenObjectForms(value);

        setPropertyHiddenStandaloneTables(value);
        setPropertyHiddenAllExceptStandaloneTables(value);
        setPropertyHiddenParentedTables(value);

        setPropertyHiddenReferencesParent(parent);
    }

    public String title() {
        return "Property#hidden (child object)";
    }

//tag::variants-nowhere[]
    @Property(
        hidden = Where.NOWHERE                           // <.>
    )
    @PropertyLayout(
        describedAs =
            "@Property(hidden = Where.NOWHERE)",
        group = "variants", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyHiddenNowhere;
//end::variants-nowhere[]

//tag::variants-everywhere[]
    @Property(
        hidden = Where.EVERYWHERE                        // <.>
    )
    @PropertyLayout(
        describedAs =
            "@Property(hidden = Where.EVERYWHERE)",
        group = "variants", sequence = "2")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyHiddenEverywhere;
//end::variants-everywhere[]

//tag::variants-anywhere[]
    @Property(
        hidden = Where.ANYWHERE                        // <.>
    )
    @PropertyLayout(
        describedAs =
            "@Property(hidden = Where.ANYWHERE)",
        group = "variants", sequence = "2.1")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyHiddenAnywhere;
//end::variants-everywhere[]

//tag::variants-all_tables[]
    @Property(
        hidden = Where.ALL_TABLES                       // <.>
    )
    @PropertyLayout(
        describedAs =
            "@Property(hidden = Where.ALL_TABLES)",
        group = "variants", sequence = "3")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyHiddenAllTables;
//end::variants-all_tables[]

//tag::variants-object_forms[]
    @Property(
        hidden = Where.OBJECT_FORMS                     // <.>
    )
    @PropertyLayout(
        describedAs =
            "@Property(hidden = Where.OBJECT_FORMS)",
        group = "variants", sequence = "4")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyHiddenObjectForms;
//end::variants-all_tables[]

    //tag::variants-standalone_tables[]
    @Property(
        hidden = Where.STANDALONE_TABLES            // <.>
    )
    @PropertyLayout(
        describedAs =
            "@Property(hidden = Where.STANDALONE_TABLES)",
        group = "variants", sequence = "5")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyHiddenStandaloneTables;
//end::variants-standalone_tables[]

    //tag::variants-all_except_standalone_tables[]
    @Property(
        hidden = Where.ALL_EXCEPT_STANDALONE_TABLES            // <.>
    )
    @PropertyLayout(
        describedAs =
            "@Property(hidden = Where.ALL_EXCEPT_STANDALONE_TABLES)",
        group = "variants", sequence = "6")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyHiddenAllExceptStandaloneTables;
//end::variants-all_except_standalone_tables[]


    //tag::variants-parented_tables[]
    @Property(
        hidden = Where.PARENTED_TABLES            // <.>
    )
    @PropertyLayout(
        describedAs =
            "@Property(hidden = Where.PARENTED_TABLES)",
        group = "variants", sequence = "7")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyHiddenParentedTables;
//end::variants-parented_tables[]

//tag::variants-references_parent[]
    @Property(
        hidden = Where.REFERENCES_PARENT            // <.>
    )
    @PropertyLayout(
        describedAs =
            "@Property(hidden = Where.REFERENCES_PARENT)",
        group = "variants", sequence = "8")
    @XmlTransient   // to avoid cycles
    @Getter @Setter
    private PropertyHiddenVm propertyHiddenReferencesParent;
//tag::variants-references_parent[]

}
//end::class[]
