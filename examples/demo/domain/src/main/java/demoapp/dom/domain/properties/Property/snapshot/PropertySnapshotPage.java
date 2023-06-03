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

import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.Snapshot;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

@Named("demo.PropertySnapshotPage")
@DomainObject(
    nature=Nature.VIEW_MODEL,
    editing = Editing.ENABLED
)
@DomainObjectLayout(cssClassFa="fa-camera")
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
public class PropertySnapshotPage implements HasAsciiDocDescription {

    public PropertySnapshotPage(
            final String givenName,
            final String familyName,
            final String middleInitial,
            final String notes) {
        this.familyName = familyName;
        this.givenName = givenName;
        this.middleInitial = middleInitial;
        this.notes = notes;
    }

    @ObjectSupport public String title() {
        return "PropertySnapshotPage";
    }

//tag::annotated-not_specified[]
    @Property(
        snapshot = Snapshot.NOT_SPECIFIED   // <.>
    )
    @XmlElement(required = true)
    @Getter @Setter
    private String givenName;
//end::annotated-not_specified[]

//tag::annotated-included[]
    @Property(
        snapshot = Snapshot.INCLUDED        // <.>
    )
    @XmlElement(required = true)
    @Getter @Setter
    private String familyName;
//end::annotated-included[]

//tag::annotated-excluded[]
    @Property(
        snapshot = Snapshot.EXCLUDED        // <.>
    )
    @XmlElement(required = true)
    @Getter @Setter
    private String middleInitial;
//end::annotated-excluded[]

//tag::no-annotation[]
    @Property()                             // <.>
    @XmlElement(required = true)
    @Getter @Setter
    private String notes;
//end::no-annotation[]


}
