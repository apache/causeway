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
package demoapp.dom.domain.objects.DomainObject.editing.jdo;

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

import demoapp.dom.domain.objects.DomainObject.editing.DomainObjectEditingEntity;

@Profile("demo-jdo")
@PersistenceCapable(
    identityType = IdentityType.DATASTORE,
    schema = "demo",
    table = "DomainObjectEditingEntity"
)
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@Named("demo.DomainObjectEditingEntity")
//tag::class[]
// ...
@DomainObject(
    nature = Nature.ENTITY,
    editing = Editing.ENABLED                                       // <.>
)
public class DomainObjectEditingEntityImpl
                extends DomainObjectEditingEntity {
    // ...
//end::class[]

    public DomainObjectEditingEntityImpl(String value) {
        setName(value);
        setOriginalName(value);
    }

//tag::class[]
    @Getter @Setter
    private String name;                                            // <.>

    @Property(
        editing = Editing.DISABLED,                                 // <.>
        editingDisabledReason = "This property may not be edited"
    )
    @Getter @Setter
    private String originalName;

    public Character getInitialCharacter() {                        // <.>
        return getName().charAt(0);
    }
//end::class[]


//tag::class[]
}
//end::class[]
