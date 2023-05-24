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
package demoapp.dom.domain.properties.Property.projecting;

import jakarta.inject.Named;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Projecting;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Title;
import org.apache.causeway.applib.annotation.Where;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

//tag::class[]
@XmlRootElement(name = "child")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@Named("demo.PropertyProjectingChildVm")
@DomainObject(nature=Nature.VIEW_MODEL)
@NoArgsConstructor
public class PropertyProjectingChildVm implements HasAsciiDocDescription {
    // ...
//end::class[]
    public PropertyProjectingChildVm(final PropertyProjectingChildEntity backingEntity) {
        setBackingEntity(backingEntity);
    }

//tag::class[]
    @Title
    @PropertyLayout(fieldSetId = "properties", sequence = "1")
    public String getProperty() {
        return getBackingEntity().getName();
    }

//tag::projecting[]
    @Property(
        projecting = Projecting.PROJECTED       // <.>
    )
    @PropertyLayout(hidden = Where.EVERYWHERE)  // <.>
    @XmlElement(required = true)
    @Getter @Setter
    private PropertyProjectingChildEntity backingEntity;
//end::projecting[]
}
//end::class[]
