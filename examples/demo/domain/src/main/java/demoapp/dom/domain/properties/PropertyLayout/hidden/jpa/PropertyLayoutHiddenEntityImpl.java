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
package demoapp.dom.domain.properties.PropertyLayout.hidden.jpa;

import javax.inject.Named;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.context.annotation.Profile;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.persistence.jpa.applib.integration.CausewayEntityListener;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import demoapp.dom.domain.properties.PropertyLayout.hidden.PropertyLayoutHidden;

@Profile("demo-jpa")
@Entity
@Table(
    schema = "demo",
    name = "PropertyLayoutHiddenEntity"
)
@EntityListeners(CausewayEntityListener.class)
@Named("demo.PropertyLayoutHiddenEntity")
@NoArgsConstructor
//tag::class[]
// ...
@DomainObject(nature = Nature.ENTITY)
public class PropertyLayoutHiddenEntityImpl extends PropertyLayoutHidden {
    // ...
//end::class[]

    public PropertyLayoutHiddenEntityImpl(final String value) {
        setName(value);
        setNameHiddenAllTables(value);
        setNameHiddenEverywhere(value);
        setNameHiddenObjectForms(value);
    }

    @Id
    @GeneratedValue
    private Long id;

    @Property()
    @Getter @Setter
    private String name;

    @Property
    @PropertyLayout(hidden = Where.ALL_TABLES)
    @Getter @Setter
    private String nameHiddenAllTables;

    @Property
    @PropertyLayout(hidden = Where.EVERYWHERE)
    @Getter @Setter
    private String nameHiddenEverywhere;

    @Property
    @PropertyLayout(hidden = Where.OBJECT_FORMS)
    @Getter @Setter
    private String nameHiddenObjectForms;


//tag::class[]
}
//end::class[]
