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
package demoapp.dom.annotations.PropertyLayout.hidden;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

//tag::class[]
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
        nature=Nature.VIEW_MODEL,
        objectType = "demo.PropertyLayoutHiddenVm",
        editing = Editing.ENABLED
)
@NoArgsConstructor
public class PropertyLayoutHiddenVm implements HasAsciiDocDescription {

    public PropertyLayoutHiddenVm(String value) {
        setPropertyHiddenNowhereUsingAnnotation(value);
        setPropertyHiddenNowhereUsingLayout(value);

        setPropertyUsingMetaAnnotation(value);
        setPropertyUsingMetaAnnotationButOverridden(value);

        setPropertyHiddenAnywhere(value);
        setPropertyHiddenEverywhere(value);
    }

    public String title() {
        return "PropertyLayout#hidden";
    }

//tag::annotation[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
        hidden = Where.NOWHERE                           // <.>
        , describedAs =
            "@PropertyLayout(hidden = Where.NOWHERE)"
    )
    @MemberOrder(name = "annotation", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyHiddenNowhereUsingAnnotation;
//end::annotation[]

//tag::layout-file[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(                                        // <.>
        describedAs =
            "<cpt:property id=\"...\" hidden=\"NOWHERE\"/>"
        , hidden = Where.ALL_TABLES
    )
    @MemberOrder(name = "layout-file", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyHiddenNowhereUsingLayout;
//end::layout-file[]

//tag::variants-everywhere[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
            hidden = Where.EVERYWHERE                       // <.>
            , describedAs =
            "@PropertyLayout(hidden = Where.EVERYWHERE)"
    )
    @MemberOrder(name = "variants", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyHiddenEverywhere;
//end::variants-annotation[]

//tag::variants-anywhere[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
            hidden = Where.ANYWHERE                       // <.>
            , describedAs =
            "@PropertyLayout(hidden = Where.ANYWHERE)"
    )
    @MemberOrder(name = "variants", sequence = "2")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyHiddenAnywhere;
//end::variants-anywhere[]

//tag::children[]
    @Getter @Setter
    @Collection
    @XmlElementWrapper(name = "children")
    @XmlElement(name = "child")
    private List<PropertyLayoutHiddenChildVm> children = new ArrayList<>();
//end::children[]

//tag::meta-annotated[]
    @Property(optionality = Optionality.OPTIONAL)
    @HiddenEverywhereMetaAnnotation                        // <.>
    @PropertyLayout(
        describedAs = "@HiddenAllTablesMetaAnnotation"
    )
    @MemberOrder(name = "meta-annotated", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyUsingMetaAnnotation;
//end::meta-annotated[]

//tag::meta-annotated-overridden[]
    @Property(optionality = Optionality.OPTIONAL)
    @HiddenEverywhereMetaAnnotation                     // <.>
    @PropertyLayout(
        hidden = Where.NOWHERE                         // <.>
        , describedAs =
            "@HiddenAllTablesMetaAnnotation " +
            "@PropertyLayout(hidden = Where.NOWHERE)"
    )
    @MemberOrder(name = "meta-annotated", sequence = "2")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyUsingMetaAnnotationButOverridden;
//end::meta-annotated-overridden[]

}
//end::class[]
