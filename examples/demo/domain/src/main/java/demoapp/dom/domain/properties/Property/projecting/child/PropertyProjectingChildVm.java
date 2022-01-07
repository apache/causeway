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
package demoapp.dom.domain.properties.Property.projecting.child;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotations.DomainObject;
import org.apache.isis.applib.annotations.Nature;
import org.apache.isis.applib.annotations.Projecting;
import org.apache.isis.applib.annotations.Property;
import org.apache.isis.applib.annotations.PropertyLayout;
import org.apache.isis.applib.annotations.Title;
import org.apache.isis.applib.annotations.Where;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.domain.properties.Property.projecting.persistence.PropertyProjectingChildEntity;

//tag::class[]
@XmlRootElement(name = "child")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
        nature=Nature.VIEW_MODEL,
        logicalTypeName = "demo.PropertyProjectingChildVm"
)
@NoArgsConstructor
public class PropertyProjectingChildVm implements HasAsciiDocDescription {

//end::class[]
    public PropertyProjectingChildVm(final PropertyProjectingChildEntity backingEntity) {
        this.backingEntity = backingEntity;
    }

//tag::class[]
    @Title
    @PropertyLayout(fieldSetId = "properties", sequence = "1")
    public String getProperty() {
        return getBackingEntity().getName();
    }

//tag::projecting[]
    @Property(
        projecting = Projecting.PROJECTED   // <.>
        , hidden = Where.EVERYWHERE         // <.>
    )
    @XmlElement(required = true)
    @Getter @Setter
    private PropertyProjectingChildEntity backingEntity;
//end::projecting[]

}
//end::class[]
