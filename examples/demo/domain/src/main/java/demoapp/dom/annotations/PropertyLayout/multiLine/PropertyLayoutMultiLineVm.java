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
package demoapp.dom.annotations.PropertyLayout.multiLine;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

//tag::class[]
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
        nature=Nature.VIEW_MODEL,
        objectType = "demo.PropertyLayoutMultiLineVm",
        editing = Editing.ENABLED
)
public class PropertyLayoutMultiLineVm implements HasAsciiDocDescription {

    public String title() {
        return "PropertyLayout#multiLine";
    }

//tag::annotation[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
        multiLine = 5                           // <.>
        , describedAs =
            "@PropertyLayout(multiLine = 5)"
        , hidden = Where.ALL_TABLES
    )
    @MemberOrder(name = "properties", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyUsingAnnotation;
//end::annotation[]

//tag::annotation-readonly[]
    @Property(
            optionality = Optionality.OPTIONAL
            , editing = Editing.DISABLED                // <.>
    )
    @PropertyLayout(
        multiLine = 5
        , describedAs =
            "@PropertyLayout(multiLine = 5)"
        , hidden = Where.ALL_TABLES)
    @MemberOrder(name = "properties", sequence = "2")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyUsingAnnotationReadOnly;
//end::annotation-readonly[]

//tag::layout-file[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(                                        // <.>
        describedAs =
            "<cpt:property id=\"...\" multiLine=\"5\"/>"
        , hidden = Where.ALL_TABLES
    )
    @MemberOrder(name = "properties", sequence = "3")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyUsingLayout;
//end::layout-file[]

//tag::meta-annotated[]
    @Property(optionality = Optionality.OPTIONAL)
    @MultiLineMetaAnnotation                        // <.>
    @PropertyLayout(
        describedAs = "@MultiLineMetaAnnotation"
        , hidden = Where.ALL_TABLES
    )
    @MemberOrder(name = "meta-annotated", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyUsingMetaAnnotation;
//end::meta-annotated[]

//tag::meta-annotated-overridden[]
    @Property(optionality = Optionality.OPTIONAL)
    @MultiLineMetaAnnotation                            // <.>
    @PropertyLayout(
        multiLine = 3                                   // <.>
        , describedAs =
            "@MultiLineMetaAnnotation @PropertyLayout(...)"
        , hidden = Where.ALL_TABLES
    )
    @MemberOrder(name = "meta-annotated", sequence = "2")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyUsingMetaAnnotationButOverridden;
//end::meta-annotated-overridden[]

}
//end::class[]
