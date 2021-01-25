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
package demoapp.dom.domain.properties.Property.optionality;

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

import lombok.Getter;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

//tag::class[]
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
        nature=Nature.VIEW_MODEL,
        objectType = "demo.PropertyOptionalityVm",
        editing = Editing.ENABLED
)
public class PropertyOptionalityVm implements HasAsciiDocDescription {
    // ...
//end::class[]

    public String title() {
        return "Property#optionality";
    }

//tag::annotation[]
    @Property(
        optionality = Optionality.OPTIONAL              // <.>
    )
    @PropertyLayout(
        describedAs =
            "@Property(optionality = OPTIONAL)"
    )
    @MemberOrder(name = "annotation", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyUsingAnnotation;
//end::annotation[]

//tag::annotation-mandatory[]
    @Property(
        optionality = Optionality.MANDATORY             // <.>
    )
    @PropertyLayout(
        describedAs =
            "@Property(optionality = MANDATORY)"
    )
    @MemberOrder(name = "annotation", sequence = "2")
    @XmlElement(required = true)
    @Getter @Setter
    private String mandatoryPropertyUsingAnnotation;
//end::annotation-mandatory[]

//tag::meta-annotated[]
    @OptionalityOptionalMetaAnnotation                  // <.>
    @Property()
    @PropertyLayout(
        describedAs = "@OptionalityOptionalMetaAnnotation"
    )
    @MemberOrder(name = "meta-annotated", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyUsingMetaAnnotation;
//end::meta-annotated[]

//tag::meta-annotated-overridden[]
    @OptionalityOptionalMetaAnnotation                  // <.>
    @Property(
        optionality = Optionality.MANDATORY             // <.>
    )
    @PropertyLayout(
        describedAs =
            "@OptionalityOptionalMetaAnnotation " +
            "@PropertyLayout(optionality = MANDATORY)"
    )
    @MemberOrder(name = "meta-annotated-overridden", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private String propertyUsingMetaAnnotationButOverridden;
//end::meta-annotated-overridden[]

//tag::class[]
}
//end::class[]
