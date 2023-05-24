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
package demoapp.dom.domain.objects.DomainObject.bounded.jpa;

import jakarta.inject.Named;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.springframework.context.annotation.Profile;

import org.apache.causeway.applib.annotation.Bounding;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.persistence.jpa.applib.integration.CausewayEntityListener;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import demoapp.dom.domain.objects.DomainObject.bounded.DomainObjectBoundingEntity;

@Profile("demo-jpa")
@Entity
@Table(
    schema = "demo",
    name = "DomainObjectBoundingEntity"
)
@EntityListeners(CausewayEntityListener.class)
@Named("demo.DomainObjectBoundingEntity")
@NoArgsConstructor
//tag::class[]
// ...
@DomainObject(
    nature = Nature.ENTITY,
    bounding = Bounding.BOUNDED         // <.>
)
public class DomainObjectBoundingEntityImpl extends DomainObjectBoundingEntity {
    // ...
//end::class[]

    public DomainObjectBoundingEntityImpl(String value) {
        setName(value);
    }

    @Id
    @GeneratedValue
    private Long id;

    @Getter @Setter
    private String name;


//tag::class[]
}
//end::class[]
