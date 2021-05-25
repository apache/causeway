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
package demoapp.dom.domain.properties.PropertyLayout.named;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import lombok.Getter;
import lombok.Setter;

//tag::class[]
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
        nature=Nature.VIEW_MODEL,
        logicalTypeName = "demo.PropertyLayoutNamedVm",
        editing = Editing.ENABLED
)
public class PropertyLayoutNamedVm implements HasAsciiDocDescription {

    public String title() {
        return "PropertyLayout#named";
    }

//tag::annotation[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
        named = "Named using @PropertyLayout"                // <.>
        , describedAs =
            "@PropertyLayout(named= \"...\")",
        fieldSetId = "annotation", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyUsingAnnotation;
//end::annotation[]

//tag::layout-file[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(                                        // <.>
        describedAs =
            "<cpt:property id=\"...\">" +
                "<cpt:named>...</cpt:named>" +
            "</cpt:property>",
        fieldSetId = "layout-file", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyUsingLayout;
//end::layout-file[]

//tag::meta-annotated[]
    @Property(optionality = Optionality.OPTIONAL)
    @NamedMetaAnnotation                            // <.>
    @PropertyLayout(
        describedAs = "@NamedMetaAnnotation",
        fieldSetId = "meta-annotated", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyUsingMetaAnnotation;
//end::meta-annotated[]

//tag::meta-annotated-overridden[]
    @NamedMetaAnnotation                            // <.>
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
        named = "@PropertyLayout name " +
                "overrides meta-annotation"         // <.>
        , describedAs =
            "@NamedMetaAnnotation @PropertyLayout(...)",
        fieldSetId = "meta-annotated-overridden", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyUsingMetaAnnotationButOverridden;
//end::meta-annotated-overridden[]

    //tag::markup[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
        named = "Named <b>uses</b> <i>markup</i>",          // <.>
        namedEscaped = false                                // <.>
        , describedAs =
            "@PropertyLayout(named= \"...\", namedEscaped=false)",
        fieldSetId = "markup", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyUsingMarkup;
//end::markup[]

    //tag::markup-escaped[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
        named = "Named <b>but</b> <i>escaped</i>",          // <.>
        namedEscaped = true                                 // <.>
        , describedAs =
            "@PropertyLayout(" +
            "named = \"...\", namedEscaped=true)",
        fieldSetId = "markup", sequence = "2")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyUsingEscapedMarkup;
//end::markup-escaped[]


}
//end::class[]
