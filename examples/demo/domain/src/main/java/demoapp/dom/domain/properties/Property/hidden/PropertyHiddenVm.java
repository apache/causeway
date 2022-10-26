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
package demoapp.dom.domain.properties.Property.hidden;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Where;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.domain.properties.Property.hidden.child.PropertyHiddenChildVm;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//tag::class[]
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@Named("demo.PropertyHiddenVm")
@DomainObject(
        nature=Nature.VIEW_MODEL,
        editing = Editing.ENABLED)
@NoArgsConstructor
public class PropertyHiddenVm implements HasAsciiDocDescription {

    @ObjectSupport public String title() {
        return "Property#hidden";
    }

//tag::annotation[]
    @Property(
        hidden = Where.NOWHERE                           // <.>
    )
    @PropertyLayout(
        describedAs =
            "@Property(hidden = Where.NOWHERE)",
        fieldSetId = "annotation", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyHiddenNowhereUsingAnnotation;
//end::annotation[]

//tag::variants-everywhere[]
    @Property(
        hidden = Where.EVERYWHERE                       // <.>
    )
    @PropertyLayout(
        describedAs =
            "@Property(hidden = Where.EVERYWHERE)",
        fieldSetId = "variants", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private String propertyHiddenEverywhere;
//end::variants-everywhere[]

//tag::variants-anywhere[]
    @Property(
        hidden = Where.ANYWHERE                       // <.>
    )
    @PropertyLayout(
        describedAs =
            "@Property(hidden = Where.ANYWHERE)",
        fieldSetId = "variants", sequence = "2")
    @XmlElement(required = true)
    @Getter @Setter
    private String propertyHiddenAnywhere;
//end::variants-anywhere[]

//tag::children[]
    @Getter @Setter
    @Collection
    @XmlElementWrapper(name = "children")
    @XmlElement(name = "child")
    private List<PropertyHiddenChildVm> children = new ArrayList<>();
//end::children[]

//tag::meta-annotated[]
    @HiddenEverywhereMetaAnnotation                        // <.>
    @Property()
    @PropertyLayout(
        describedAs = "@HiddenEverywhereMetaAnnotation",
        fieldSetId = "meta-annotated", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private String propertyUsingMetaAnnotation;
//end::meta-annotated[]

//tag::meta-annotated-overridden[]
    @HiddenEverywhereMetaAnnotation                     // <.>
    @Property(
        hidden = Where.NOWHERE                          // <.>
    )
    @PropertyLayout(
        describedAs =
            "@HiddenEverywhereMetaAnnotation " +
            "@Property(hidden = Where.NOWHERE)",
        fieldSetId = "meta-annotated-overridden", sequence = "2")
    @XmlElement(required = true)
    @Getter @Setter
    private String propertyUsingMetaAnnotationButOverridden;
//end::meta-annotated-overridden[]

}
//end::class[]
