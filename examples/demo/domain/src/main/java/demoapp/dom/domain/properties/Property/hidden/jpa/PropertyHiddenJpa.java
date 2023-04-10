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
package demoapp.dom.domain.properties.Property.hidden.jpa;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom._infra.values.ValueHolder;
import demoapp.dom.domain.properties.Property.hidden.PropertyHidden;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.inject.Named;
import javax.persistence.*;

import org.apache.causeway.applib.annotation.*;
import org.apache.causeway.persistence.jpa.applib.integration.CausewayEntityListener;
import org.springframework.context.annotation.Profile;

@Profile("demo-jpa")
@Entity
@Table(
    schema = "demo",
    name = "PropertyHiddenJpa"
)
@EntityListeners(CausewayEntityListener.class)
@Named("demo.PropertyHiddenJpa")
@NoArgsConstructor
//tag::class[]
// ...
@DomainObject(nature = Nature.ENTITY)
public class PropertyHiddenJpa extends PropertyHidden {
    // ...
//end::class[]

    public PropertyHiddenJpa(String value) {
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

    @Property(hidden = Where.ALL_TABLES)
    @Getter @Setter
    private String nameHiddenAllTables;

    @Property(hidden = Where.EVERYWHERE)
    @Getter @Setter
    private String nameHiddenEverywhere;

    @Property(hidden = Where.OBJECT_FORMS)
    @Getter @Setter
    private String nameHiddenObjectForms;


//tag::class[]
}
//end::class[]
