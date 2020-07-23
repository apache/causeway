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
package demoapp.dom.annotDomain.Property.mustSatisfy;

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
        objectType = "demo.PropertyLayoutMustSatisfyVm",
        editing = Editing.ENABLED
)
public class PropertyMustSatisfyVm implements HasAsciiDocDescription {

    public String title() {
        return "PropertyLayout#mustSatisfy";
    }

//tag::annotation[]
    @Property(
        mustSatisfy = OfWorkingAgeSpecification.class       // <.>
    )
    @PropertyLayout(
        describedAs =
            "mustSatisfy = OfWorkingAgeSpecification.class"
    )
    @MemberOrder(name = "annotation", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private Integer customerAgePropertyUsingAnnotation;
//end::annotation[]

//tag::meta-annotated[]
    @MustSatisfyOfWorkingAgeMetaAnnotation                     // <.>
    @Property()
    @PropertyLayout(
        describedAs = "@MustSatisfyOfWorkingAgeMetaAnnotation"
    )
    @MemberOrder(name = "meta-annotated", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private Integer customerAgePropertyUsingMetaAnnotation;
//end::meta-annotated[]

//tag::meta-annotated-overridden[]
    @MustSatisfyOfWorkingAgeMetaAnnotation                     // <.>
    @Property(
        mustSatisfy = OfRetirementAgeSpecification.class       // <.>
    )
    @PropertyLayout(
        describedAs =
            "@MustSatisfyOfWorkingAgeMetaAnnotation " +
            "@PropertyLayout(mustSatisfy = OfRetirementAgeSpecification.class)"
    )
    @MemberOrder(name = "meta-annotated-overridden", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private Integer customerAgePropertyUsingMetaAnnotationButOverridden;
//end::meta-annotated-overridden[]

}
//end::class[]
