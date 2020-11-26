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
package demoapp.dom.annotDomain.Property.mementoSerialization;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.MementoSerialization;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
    nature=Nature.VIEW_MODEL,
    objectType = "demo.PropertyMementoSerializationVm",
    editing = Editing.ENABLED
)
@NoArgsConstructor
public class PropertyMementoSerializationVm implements HasAsciiDocDescription {

    public PropertyMementoSerializationVm(String text) {
        this.text = text;
        this.excludedProperty = text;
        this.includedProperty = text;
        this.notSpecifiedProperty = text;
        this.metaAnnotatedProperty = text;
        this.metaAnnotatedPropertyOverridden = text;
    }

    public String title() {
        return "PropertyMementoSerializationVm";
    }

    @Property()
    @MemberOrder(name = "no-annotations", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private String text;

    @Property(
        mementoSerialization = MementoSerialization.NOT_SPECIFIED
    )
    @PropertyLayout(
        describedAs = "@Property(mementoSerialization = NOT_SPECIFIED)"
    )
    @MemberOrder(name = "annotations", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private String notSpecifiedProperty;

    @Property(
        mementoSerialization = MementoSerialization.EXCLUDED
    )
    @PropertyLayout(
        describedAs = "@Property(mementoSerialization = EXCLUDED)"
    )
    @MemberOrder(name = "annotations", sequence = "2")
    @XmlElement(required = true)
    @Getter @Setter
    private String excludedProperty;

    @Property(
        mementoSerialization = MementoSerialization.INCLUDED
    )
    @PropertyLayout(
        describedAs = "@Property(mementoSerialization = INCLUDED)"
    )
    @MemberOrder(name = "annotations", sequence = "2")
    @XmlElement(required = true)
    @Getter @Setter
    private String includedProperty;

    @MementoSerializationExcludedMetaAnnotation     // <.>
    @Property()
    @PropertyLayout(
        describedAs = "@MementoSerializationExcludedMetaAnnotation "
    )
    @MemberOrder(name = "meta-annotations", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private String metaAnnotatedProperty;

    @MementoSerializationIncludedMetaAnnotation                 // <.>
    @Property(
        mementoSerialization = MementoSerialization.EXCLUDED    // <.>
    )
    @PropertyLayout(
        describedAs =
            "@MementoSerializationIncludedMetaAnnotation "
            + "@Property(mementoSerialization = EXCLUDED)"
    )
    @MemberOrder(name = "meta-annotations-overridden", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private String metaAnnotatedPropertyOverridden;

}
