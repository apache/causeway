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

import jakarta.inject.Named;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.LabelPosition;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//tag::class[]
@Named("demo.PropertyLayoutLabelPositionVm")
@DomainObject(
        nature=Nature.VIEW_MODEL,
        editing = Editing.ENABLED
)
@DomainObjectLayout(cssClassFa="fa-tag")
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
public class PropertyLayoutLabelPositionPage implements HasAsciiDocDescription {

    @ObjectSupport public String title() {
        return "@PropertyLayout#labelPosition";
    }

//tag::annotated-not-provided[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout()
    @XmlElement(required = false)
    @Getter @Setter
    private String property;
//end::annotated-not-provided[]

//tag::annotated-left[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
            labelPosition = LabelPosition.LEFT
    )
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyLabelPositionLeft;
//end::annotated-left[]

//tag::annotated-top[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
            labelPosition = LabelPosition.TOP
    )
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyLabelPositionTop;
//end::annotated-top[]

//tag::annotated-none[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
            labelPosition = LabelPosition.NONE,
            multiLine = 3
    )
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyLabelPositionNone;
//end::annotated-none[]

//tag::annotated-not-specified[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
            labelPosition = LabelPosition.NOT_SPECIFIED
    )
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyLabelPositionNotSpecified;
//end::annotated-not-specified[]

//tag::annotated-right-boolean[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout()
    @XmlElement(required = false)
    @Getter @Setter
    private Boolean propertyBoolean;
//end::annotated-right-boolean[]

//tag::annotated-right-boolean[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
            labelPosition = LabelPosition.RIGHT
    )
    @XmlElement(required = false)
    @Getter @Setter
    private Boolean propertyBooleanLabelPositionRight;
//end::annotated-right-boolean[]

//tag::annotated-right[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
            labelPosition = LabelPosition.RIGHT
    )
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyLabelPositionRight;
//end::annotated-right[]

//tag::layout-left[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout()
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyLayoutLabelPositionLeft;
//end::layout-left[]

//tag::layout-top[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout()
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyLayoutLabelPositionTop;
//end::layout-top[]

//tag::layout-none[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout()
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyLayoutLabelPositionNone;
//end::layout-none[]

//tag::layout-right-boolean[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout()
    @XmlElement(required = false)
    @Getter @Setter
    private Boolean propertyLayoutBooleanLabelPositionRight;
//end::layout-right-boolean[]

}
//end::class[]
