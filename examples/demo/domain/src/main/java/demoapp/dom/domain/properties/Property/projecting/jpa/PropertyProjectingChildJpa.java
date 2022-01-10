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
package demoapp.dom.domain.properties.Property.projecting.jpa;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.springframework.context.annotation.Profile;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.jaxb.PersistentEntityAdapter;
import org.apache.isis.persistence.jpa.applib.integration.IsisEntityListener;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import demoapp.dom.domain.properties.Property.projecting.persistence.PropertyProjectingChildEntity;

@Profile("demo-jpa")
//tag::class[]
@Entity
@Table(
    schema = "demo",
    name = "PropertyProjectingChildJpa"
)
@EntityListeners(IsisEntityListener.class)
@DomainObject(
    logicalTypeName = "demo.PropertyProjectingChildEntity"
    , editing = Editing.DISABLED
)
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
@NoArgsConstructor
public class PropertyProjectingChildJpa
        extends PropertyProjectingChildEntity {
    // ...
//end::class[]

    public PropertyProjectingChildJpa(final String initialValue) {
        this.name = initialValue;
    }

    @Id
    @GeneratedValue
    private Long id;

//tag::class[]
    @Title(prepend = "Entity: ")
    @PropertyLayout(fieldSetId = "properties", sequence = "1")
    @Getter @Setter
    private String name;

}
//end::class[]
