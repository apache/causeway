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
package demoapp.dom.domain.properties.Property.snapshot;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.ObjectSupport;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Snapshot;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
    nature=Nature.VIEW_MODEL,
    logicalTypeName = "demo.PropertySnapshotVm",
    editing = Editing.ENABLED
)
@NoArgsConstructor
public class PropertySnapshotVm implements HasAsciiDocDescription {

    public PropertySnapshotVm(final String text) {
        this.text = text;
        this.excludedProperty = text;
        this.includedProperty = text;
        this.notSpecifiedProperty = text;
        this.metaAnnotatedProperty = text;
        this.metaAnnotatedPropertyOverridden = text;
    }

    @ObjectSupport public String title() {
        return "PropertySnapshotVm";
    }

//tag::no-annotation[]
    @Property()
    @PropertyLayout(fieldSetId = "no-annotations", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private String text;
//end::no-annotation[]

//tag::annotated-not_specified[]
    @Property(
        snapshot = Snapshot.NOT_SPECIFIED
    )
    @PropertyLayout(
        describedAs = "@Property(snapshot = NOT_SPECIFIED)",
        fieldSetId = "annotations", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private String notSpecifiedProperty;
//end::annotated-not_specified[]

//tag::annotated-excluded[]
    @Property(
        snapshot = Snapshot.EXCLUDED
    )
    @PropertyLayout(
        describedAs = "@Property(snapshot = EXCLUDED)",
        fieldSetId = "annotations", sequence = "2")
    @XmlElement(required = true)
    @Getter @Setter
    private String excludedProperty;
//end::annotated-excluded[]

//tag::annotated-included[]
    @Property(
        snapshot = Snapshot.INCLUDED
    )
    @PropertyLayout(
        describedAs = "@Property(snapshot = INCLUDED)",
        fieldSetId = "annotations", sequence = "2")
    @XmlElement(required = true)
    @Getter @Setter
    private String includedProperty;
//end::annotated-included[]

//tag::meta-annotated-excluded[]
    @SnapshotExcludedMetaAnnotation
    @Property()
    @PropertyLayout(
        describedAs = "@SnapshotExcludedMetaAnnotation ",
        fieldSetId = "meta-annotations", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private String metaAnnotatedProperty;
//end::meta-annotated-excluded[]

//tag::meta-annotated-included[]
    @SnapshotIncludedMetaAnnotation
    @Property(
        snapshot = Snapshot.EXCLUDED
    )
    @PropertyLayout(
        describedAs =
            "@SnapshotIncludedMetaAnnotation "
            + "@Property(snapshot = EXCLUDED)",
        fieldSetId = "meta-annotations-overridden", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private String metaAnnotatedPropertyOverridden;
//end::meta-annotated-included[]

}
