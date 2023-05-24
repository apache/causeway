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
package demoapp.dom.domain.objects.DomainObject.introspection.encapsulated.jdo;

import jakarta.inject.Named;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;

import org.springframework.context.annotation.Profile;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Introspection;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.SemanticsOf;

import demoapp.dom.domain.objects.DomainObject.introspection.encapsulated.DomainObjectIntrospectionEncapsulatedEntity;

@Profile("demo-jdo")
@PersistenceCapable(
    identityType = IdentityType.DATASTORE,
    schema = "demo",
    table = "DomainObjectIntrospectionEncapsulatedEntity"
)
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@Named("demo.DomainObjectIntrospectionEncapsulatedEntity")
//tag::class[]
// ...
@DomainObject(
        introspection = Introspection.ENCAPSULATION_ENABLED
)
public class DomainObjectIntrospectionEncapsulatedEntityImpl
        extends DomainObjectIntrospectionEncapsulatedEntity {
    // ...
//end::class[]

    public DomainObjectIntrospectionEncapsulatedEntityImpl(String value) {
        setName(value);
    }

    @Override
    public String value() {
        return getName();
    }
//tag::class[]

    private String name;
    @Property                                                                       // <.>
    private String getName() {                                                      // <1>
        return name;
    }
    private void setName(String name) {
        this.name = name;
    }

    @Action(semantics = SemanticsOf.IDEMPOTENT)                                     // <.>
    private DomainObjectIntrospectionEncapsulatedEntity updateName(final String name) {   // <2>
        setName(name);
        return this;
    }
    @MemberSupport                                                                  // <.>
    private String default0UpdateName() {                                           // <3>
        return getName();
    }
}
//end::class[]
