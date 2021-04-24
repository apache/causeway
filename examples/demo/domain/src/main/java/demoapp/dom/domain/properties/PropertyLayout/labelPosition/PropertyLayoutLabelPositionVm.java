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
package demoapp.dom.domain.properties.PropertyLayout.labelPosition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.LabelPosition;
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
        objectType = "demo.PropertyLayoutLabelPositionVm",
        editing = Editing.ENABLED
)
public class PropertyLayoutLabelPositionVm implements HasAsciiDocDescription {

    public String title() {
        return "PropertyLayout#labelPosition";
    }

//tag::annotation[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
        labelPosition = LabelPosition.TOP                   // <.>
        , describedAs =
            "@PropertyLayout(labelPosition = TOP)",
        fieldSetId = "annotation", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyUsingAnnotation;
//end::annotation[]

//tag::layout-file[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(                                        // <.>
        describedAs =
            "<cpt:property id=\"...\" labelPosition=\"TOP\"/>",
        fieldSetId = "layout-file", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyUsingLayout;
//end::layout-file[]

//tag::meta-annotated[]
    @Property(optionality = Optionality.OPTIONAL)
    @LabelPositionTopMetaAnnotation                         // <.>
    @PropertyLayout(
        describedAs = "@LabelPositionTopMetaAnnotation",
        fieldSetId = "meta-annotated", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyUsingMetaAnnotation;
//end::meta-annotated[]

//tag::meta-annotated-overridden[]
    @LabelPositionTopMetaAnnotation                         // <.>
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
        labelPosition = LabelPosition.LEFT                  // <.>
        , describedAs =
            "@LabelPositionTopMetaAnnotation @PropertyLayout(...)",
        fieldSetId = "meta-annotated-overridden", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyUsingMetaAnnotationButOverridden;
//end::meta-annotated-overridden[]

//tag::variants-top[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
        labelPosition = LabelPosition.TOP                   // <.>
        , describedAs =
        "@PropertyLayout(labelPosition = TOP)",
        fieldSetId = "variants", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyLabelPositionTop;
//end::variants-top[]

//tag::variants-left[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
        labelPosition = LabelPosition.LEFT                  // <.>
        , describedAs =
        "@PropertyLayout(labelPosition = LEFT)",
        fieldSetId = "variants", sequence = "2")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyLabelPositionLeft;
//end::variants-left[]

//tag::variants-right[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
        labelPosition = LabelPosition.RIGHT                 // <.>
        , describedAs =
        "@PropertyLayout(labelPosition = RIGHT)",
        fieldSetId = "variants", sequence = "3")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyLabelPositionRight;
//end::variants-right[]

//tag::variants-right-boolean[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
        labelPosition = LabelPosition.RIGHT                 // <.>
        , describedAs =
        "@PropertyLayout(labelPosition = RIGHT)",
        fieldSetId = "variants", sequence = "3.1")
    @XmlElement(required = false)
    @Getter @Setter
    private Boolean propertyBooleanLabelPositionRight;
//end::variants-right-boolean[]

//tag::variants-none[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
        labelPosition = LabelPosition.NONE                  // <.>
        , describedAs =
        "@PropertyLayout(labelPosition = NONE)",
        fieldSetId = "variants", sequence = "4")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyLabelPositionNone;
//end::variants-none[]

//tag::variants-none-multiline[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
            labelPosition = LabelPosition.NONE              // <.>
            , multiLine = 10
            , describedAs =
            "@PropertyLayout(labelPosition = NONE, multiLine = 10)",
            fieldSetId = "variants", sequence = "4.1")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyLabelPositionNoneMultiline;
//end::variants-none-multiline[]

//tag::variants-not-specified[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
        labelPosition = LabelPosition.NOT_SPECIFIED         // <.>
        , describedAs =
        "@PropertyLayout(labelPosition = NONE)",
        fieldSetId = "variants", sequence = "5")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyLabelPositionNotSpecified;
//end::variants-not-specified[]


}
//end::class[]
