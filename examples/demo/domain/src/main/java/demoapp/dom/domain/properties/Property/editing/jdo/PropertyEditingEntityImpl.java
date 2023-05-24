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
package demoapp.dom.domain.properties.Property.editing.jdo;

import jakarta.inject.Named;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;

import org.springframework.context.annotation.Profile;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Property;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom.domain.properties.Property.editing.PropertyEditingEntity;

@Profile("demo-jdo")
@PersistenceCapable(
    identityType = IdentityType.DATASTORE,
    schema = "demo",
    table = "PropertyEditingEntity"
)
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@Named("demo.PropertyEditingEntity")
//tag::class[]
// ...
@DomainObject(nature = Nature.ENTITY)
public class PropertyEditingEntityImpl
                extends PropertyEditingEntity {
    // ...
//end::class[]

    public PropertyEditingEntityImpl(String value) {
        setName(value);
        setOriginalName(value);
    }
//tag::class[]

//tag::name[]
    @Property(
            editing = Editing.ENABLED           // <.>
    )
    @Getter @Setter
    private String name;
//end::name[]

//tag::original-name[]
    @Property(
            editingDisabledReason =             // <.>
                    "This property cannot be edited; " +
                    "it stores the original value of the name"
    )
    @Getter @Setter
    private String originalName;
//end::original-name[]

//tag::initial-character[]
    public Character getInitialCharacter() {    // <.>
        return getName().charAt(0);
    }
//end::initial-character[]
//end::class[]


//tag::class[]
}
//end::class[]
