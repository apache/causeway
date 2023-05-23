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
package demoapp.dom.domain.objects.DomainObject.introspection.annotReqd.jpa;

import javax.inject.Named;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.context.annotation.Profile;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Introspection;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.persistence.jpa.applib.integration.CausewayEntityListener;

import lombok.NoArgsConstructor;

import demoapp.dom.domain.objects.DomainObject.introspection.annotReqd.DomainObjectIntrospectionAnnotReqdEntity;

@Profile("demo-jpa")
@Entity
@Table(
    schema = "demo",
    name = "DomainObjectIntrospectionAnnotReqdEntity"
)
@EntityListeners(CausewayEntityListener.class)
@Named("demo.DomainObjectIntrospectionAnnotReqdEntity")
@NoArgsConstructor
//tag::class[]
// ...
@DomainObject(
        introspection = Introspection.ANNOTATION_REQUIRED
)
public class DomainObjectIntrospectionAnnotReqdEntityImpl
        extends DomainObjectIntrospectionAnnotReqdEntity {
    // ...
//end::class[]

    public DomainObjectIntrospectionAnnotReqdEntityImpl(String value) {
        setName(value);
    }

    @Id
    @GeneratedValue
    private Long id;
//tag::class[]

    private String name;
    @Property                                                                   // <.>
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Action(semantics = SemanticsOf.IDEMPOTENT)                                 // <.>
    public DomainObjectIntrospectionAnnotReqdEntityImpl updateName(final String name) {
        setName(name);
        return this;
    }
    public String default0UpdateName() {                                        // <.>
        return getName();
    }
}
//end::class[]
